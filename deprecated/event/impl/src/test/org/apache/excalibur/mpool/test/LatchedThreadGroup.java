/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.excalibur.mpool.test;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * This class is useful for writing MultiThreaded test cases where you need to perform
 *  multithreaded load testing on a component.
 * <p>
 * An instance of will create a block of threads of the specified size.  Each thread will be
 *  assigned to run a specified Runnable instance.  The threads will then all wait at a latch
 *  until the go method is called.  The go method will not return until all of the
 *  Runnables have completed.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LatchedThreadGroup.java,v 1.1 2004/04/02 05:53:14 mcconnell Exp $
 */
public class LatchedThreadGroup
    extends AbstractLogEnabled
{
    private Thread[] m_threads;
    private Object m_semaphore = new Object();
    private int m_startedCount;
    private boolean m_latched;
    private int m_completedCount;
    private Throwable m_exception;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a LatchedThreadGroup with a thread for each Runnable in the runnables array.
     */
    public LatchedThreadGroup( Runnable[] runnables )
    {
        int threadCount = runnables.length;
        m_threads = new Thread[ threadCount ];
        for( int i = 0; i < threadCount; i++ )
        {
            m_threads[ i ] = new Runner( runnables[ i ], "Latched_Thread_" + i );
        }
    }

    /**
     * Creates a LatchedThreadGroup with threadCount threads each running runnable.
     */
    public LatchedThreadGroup( Runnable runnable, int threadCount )
    {
        m_threads = new Thread[ threadCount ];
        for( int i = 0; i < threadCount; i++ )
        {
            m_threads[ i ] = new Runner( runnable, "Latched_Thread_" + i );
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    protected void resetMemory()
    {
        System.gc();
        System.gc();

        // Let the system settle down.
        try
        {
            Thread.sleep( 50 );
        }
        catch( InterruptedException e )
        {
        }
        Runtime runtime = Runtime.getRuntime();
        getLogger().debug( "Memory: " + ( runtime.totalMemory() - runtime.freeMemory() ) );
    }

    /**
     * Causes all of the Runnables to start at the same instance.  This method will return
     *  once all of the Runnables have completed.
     *
     * @return time, in milliseconds, that it took for all of the Runnables to complete.
     */
    public long go()
        throws Exception
    {
        // Start each of the threads.  They will block until the latch is released.  This is
        //  necessary because it takes some time for the threads to each allocate their required
        //  system resources and actually be ready to run.
        int threadCount = m_threads.length;
        for( int i = 0; i < threadCount; i++ )
        {
            m_threads[ i ].start();
        }

        // Wait for all of the threads to start before starting to time the test
        synchronized( m_semaphore )
        {
            while( m_startedCount < threadCount )
            {
                m_semaphore.wait();
            }

            // Start clean
            resetMemory();

            // Release the threads.
            m_latched = true;
            getLogger().debug( "Main thread released the test thread latch." );
            m_semaphore.notifyAll();
        }
        // Start timing
        long startTime = System.currentTimeMillis();

        // Wait for all of the threads to complete
        synchronized( m_semaphore )
        {
            getLogger().debug( "Waiting for test threads to all complete." );
            while( m_completedCount < threadCount )
            {
                try
                {
                    m_semaphore.wait();
                }
                catch( InterruptedException e )
                {
                }
            }
        }
        final long duration = System.currentTimeMillis() - startTime;
        getLogger().debug( "All test threads completed." );

        if( m_exception != null )
        {
            throw new CascadingAssertionFailedError( "Exception in test thread.", m_exception );
        }
        return duration;
    }

    /**
     * Inner access method to getLogger() to work around a bug in the Javac compiler
     *  when getLogger() is called from the method of an inner class.  Jikes seems to
     *  handle it Ok. :-/
     */
    private Logger getInnerLogger()
    {
        return getLogger();
    }

    /*---------------------------------------------------------------
     * Inner Classes
     *-------------------------------------------------------------*/
    private class Runner extends Thread
    {
        private Runnable m_runnable;

        protected Runner( Runnable runnable, String name )
        {
            super( name );
            m_runnable = runnable;
        }

        public void run()
        {
            try
            {
                // Need all threads to wait until all the others are ready.
                synchronized( m_semaphore )
                {
                    m_startedCount++;
                    getInnerLogger().debug( "Started " + m_startedCount + " test threads." );
                    if( m_startedCount >= m_threads.length )
                    {
                        m_semaphore.notifyAll();
                    }
                    while( !m_latched )
                    {
                        try
                        {
                            m_semaphore.wait();
                        }
                        catch( InterruptedException e )
                        {
                        }
                    }
                }

                // Run the runnable
                try
                {
                    m_runnable.run();
                }
                catch( Throwable t )
                {
                    synchronized( m_semaphore )
                    {
                        getInnerLogger().error( "Error in " + Thread.currentThread().getName(), t );
                        if( m_exception != null )
                        {
                            m_exception = t;
                        }
                    }
                }
            }
            finally
            {
                // Say that we are done
                synchronized( m_semaphore )
                {
                    m_completedCount++;
                    getInnerLogger().debug( m_completedCount + " test threads completed." );
                    m_semaphore.notifyAll();
                }
            }
        }
    }
}
