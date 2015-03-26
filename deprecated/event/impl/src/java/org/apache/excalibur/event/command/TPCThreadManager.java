/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.event.command;

import org.apache.avalon.framework.logger.NullLogger;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.util.SystemUtil;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This is a ThreadManager that uses a certain number of threads per
 * processor.  The number of threads in the pool is a direct proportion to
 * the number of processors. The size of the thread pool is (processors
 * threads-per-processor) + 1
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class TPCThreadManager extends AbstractThreadManager implements Parameterizable
{
    private PooledExecutor m_threadPool;
    private int m_processors = -1;
    private int m_threadsPerProcessor = 1;
    private int m_keepAliveTime = 300000;
    private boolean m_hardShutdown = false;

    /**
     * The following parameters can be set for this class:
     *
     * <table>
     *   <tr>
     *     <th>Name</th> <th>Description</th> <th>Default Value</th>
     *   </tr>
     *   <tr>
     *     <td>processors</td>
     *     <td>Number of processors (autodetected if less than one)</td>
     *     <td>Results from SystemUtil.numProcessors()</td>
     *   </tr>
     *   <tr>
     *     <td>threads-per-processor</td>
     *     <td>Threads per processor to use (Rewritten to 1 if less than one)</td>
     *     <td>1</td>
     *   </tr>
     *   <tr>
     *     <td>sleep-time</td>
     *     <td>Time (in milliseconds) to wait between queue pipeline processing runs</td>
     *     <td>1000</td>
     *   </tr>
     *   <tr>
     *     <td>keep-alive-time</td>
     *     <td>Time (in milliseconds) that idle threads should remain in the threadpool</td>
     *     <td>300000</td>
     *   </tr>
     *   <tr>
     *     <td>force-shutdown</td>
     *     <td>At shutdown time, allow currently queued tasks to finish, or immediately quit</td>
     *     <td>false</td>
     *   </tr>
     * </table>
     *
     * @param parameters  The Parameters object
     *
     * @throws ParameterException if there is a problem with the parameters.
     */
    public void parameterize( Parameters parameters ) throws ParameterException
    {
        m_processors = Math.max(1, parameters.getParameterAsInteger( "processors", 1 ) );

        m_threadsPerProcessor =
            Math.max( parameters.getParameterAsInteger( "threads-per-processor", 1 ), 1 );

        m_keepAliveTime = parameters.getParameterAsInteger("keep-alive-time", 300000);

        setSleepTime( parameters.getParameterAsLong( "sleep-time", 1000L ) );

        m_hardShutdown = ( parameters.getParameterAsBoolean( "force-shutdown", false ) );
    }

    public void initialize() throws Exception
    {
        if( m_processors < 1 )
        {
            m_processors = Math.max( 1, SystemUtil.numProcessors() );
        }

        if( isInitialized() )
        {
            throw new IllegalStateException( "ThreadManager is already initailized" );
        }

        final int maxPoolSize = Math.max(( m_processors * m_threadsPerProcessor ) + 1, m_processors + 1);
        m_threadPool = new PooledExecutor( m_processors + 1 );
        m_threadPool.setMinimumPoolSize( 2 ); // at least two threads
        m_threadPool.setMaximumPoolSize( maxPoolSize );
        m_threadPool.waitWhenBlocked();
        m_threadPool.setThreadFactory( new ThreadFactory() {
            public Thread newThread(Runnable run) {
                Thread newThread = new Thread(run);

                newThread.setDaemon( true );
                newThread.setPriority( Thread.MIN_PRIORITY );

                return newThread;
            }
        });
        if( maxPoolSize == 2 )
        {
            // The PooledExecutor has an inherent race condition between releasing threads
            // and adding new tasks (when using the waitWhenBlocked policy):
            // it could be that a thread is being released while a new
            // task is being added. That task would then remain waiting to be picked up by
            // the next thread that becomes available, but meanwhile the threadpool is below its maximum capacity.
            // If the threadpool has a maximum size of 1, then this could leave the task waiting forever.
            // Here we check if maxPoolSize == 2 because one of the threads used by the threadpool will
            // be used continuously by the ThreadManager itself.
            // As a solution to this problem, the one available work-thread we have in this case
            // is set to never expire.
            m_threadPool.setKeepAliveTime( -1 );
        }
        else
        {
            m_threadPool.setKeepAliveTime( m_keepAliveTime );
        }

        if( null == getLogger() )
        {
            this.enableLogging( new NullLogger() );
        }

        setExecutor( m_threadPool );

        super.initialize();
    }

    protected final void doDispose()
    {
        if ( m_hardShutdown )
        {
            m_threadPool.shutdownNow();
        }
        else
        {
            m_threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        }

        m_threadPool.interruptAll();

        try
        {
            if ( !m_threadPool.awaitTerminationAfterShutdown( getSleepTime() ) )
            {
                getLogger().warn("Thread pool took longer than " + getSleepTime() +
                     " ms to shut down");
            }
        }
        catch (InterruptedException ie)
        {
            getLogger().warn("Thread pool was interrupted while waiting for shutdown to complete.", ie);
        }
    }
}
