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

import java.util.Iterator;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.collections.StaticBucketMap;
import org.apache.excalibur.event.EventHandler;
import org.apache.excalibur.event.Source;

import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 * Abstract base class for a ThreadManager that has a single ThreadPool for
 * all pipelines
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractThreadManager extends AbstractLogEnabled
    implements Runnable, ThreadManager, Initializable, Disposable
{
    /** The pipelines we are managing */
    private final StaticBucketMap m_pipelines = new StaticBucketMap();

    /** The ThreadPool we are using */
    private Executor m_executor;

    /** Whether we are done or not */
    private volatile boolean m_done = false;

    /** The number of milliseconds to sleep before runngin again: 1000 (1 sec.) */
    private long m_sleepTime = 1000L;

    /** Whether this class has been initialized or not */
    private volatile boolean m_initialized = false;

    /** Return whether the thread manager has been initialized or not */
    protected boolean isInitialized()
    {
        return m_initialized;
    }

    /**
     * Set the amount of time to sleep between checks on the queue
     *
     * @param sleepTime  Number of milliseconds
     */
    protected void setSleepTime( long sleepTime )
    {
        m_sleepTime = sleepTime;
    }

    /**
     * Get the current amount of sleep time.
     */
    protected long getSleepTime()
    {
        return m_sleepTime;
    }

    /**
     * Set the executor we are using
     *
     * @param executor to use
     */
    protected void setExecutor( Executor executor )
    {
        if( null == m_executor )
        {
            m_executor = executor;
        }
        else
        {
            throw new IllegalStateException( "Can only set the executor once" );
        }
    }

    /**
     * Set up the ThreadManager.  All required parameters must have already been set.
     *
     * @throws Exception if there is any problem setting up the ThreadManager
     */
    public void initialize() throws Exception
    {
        if( null == m_executor )
        {
            throw new IllegalStateException( "No thread pool set" );
        }

        m_executor.execute( this );
        this.m_initialized = true;
    }

    /**
     * Register an EventPipeline with the ThreadManager.
     *
     * @param pipeline  The pipeline we are registering
     */
    public void register( EventPipeline pipeline )
    {
        if( !isInitialized() )
        {
            throw new IllegalStateException( "ThreadManager must be initialized before "
                                             + "registering a pipeline" );
        }

        try
        {
            PipelineRunner runner = new PipelineRunner( pipeline );
            runner.enableLogging( getLogger() );
            m_pipelines.put( pipeline, runner );

            if( m_done )
            {
                m_executor.execute( this );
            }
        }
        catch( InterruptedException ie )
        {
            getLogger().warn("Caught InterruptedException in register", ie);
            // ignore for now
        }
    }

    /**
     * Deregister an EventPipeline with the ThreadManager
     *
     * @param pipeline  The pipeline we are de-registering
     */
    public void deregister( EventPipeline pipeline )
    {
        if( !isInitialized() )
        {
            throw new IllegalStateException( "ThreadManager must be initialized before "
                                             + "deregistering a pipeline" );
        }

        m_pipelines.remove( pipeline );

        if( m_pipelines.isEmpty() )
        {
            m_done = true;
        }
    }

    /**
     * Deregisters all EventPipelines from this ThreadManager
     */
    public void deregisterAll()
    {
        if( !isInitialized() )
        {
            throw new IllegalStateException( "ThreadManager must be initialized "
                                             + "before deregistering pipelines" );
        }

        Iterator it = m_pipelines.keySet().iterator();
        while ( it.hasNext() )
        {
            deregister( (EventPipeline) it.next() );
        }

        m_done = true;

        if ( ! m_pipelines.isEmpty() )
        {
            throw new IllegalStateException("We still have pipelines, but no runners are available!");
        }
    }


    /**
     * Get rid of the ThreadManager.
     */
    public void dispose()
    {
        m_done = true;
        deregisterAll();

        doDispose();
    }

    protected void doDispose() {} // default impl to work with released code

    /**
     * The code that is run in the background to manage the ThreadPool and the
     * EventPipelines
     */
    public void run()
    {
        while( !m_done )
        {
            Iterator i = m_pipelines.values().iterator();

            while( i.hasNext() )
            {
                PipelineRunner nextRunner = ( PipelineRunner ) i.next();

                try
                {
                    m_executor.execute( nextRunner );
                }
                catch( Exception e )
                {
                    if( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "Caught exception in ThreadManager management thread", e );
                    }
                }
            }

            if ( !m_done )
            {
                try
                {
                    Thread.sleep( m_sleepTime );
                }
                catch( InterruptedException e )
                {
                    Thread.interrupted();
                }
            }
        }
    }

    /**
     * The PipelineRunner class pulls all the events from the Source, and puts them in the EventHandler.
     * Both of those objects are part of the EventPipeline.
     */
    public static final class PipelineRunner
        extends AbstractLogEnabled
        implements Runnable
    {
        /** The pipeline we are managing */
        private final EventPipeline m_pipeline;

        /**
         * Create a PipelineRunner.
         *
         * @param pipeline  The EventPipeline we are running
         */
        protected PipelineRunner( EventPipeline pipeline )
        {
            m_pipeline = pipeline;
        }

        /**
         * The code that actually pulls the events from the Sources and sends them to the event handler
         */
        public void run()
        {
            Source[] sources = m_pipeline.getSources();
            EventHandler handler = m_pipeline.getEventHandler();

            for( int i = 0; i < sources.length; i++ )
            {
                try
                {
                    handler.handleEvents( sources[i].dequeueAll() );
                }
                catch( Exception e )
                {
                    // We want to catch this, because this is the only
                    // place where exceptions happening in this thread
                    // can be logged

                    if( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "Exception processing EventPipeline [msg: "
                                           + e.getMessage() + "]", e );
                    }
                }
            }
        }
    }
}
