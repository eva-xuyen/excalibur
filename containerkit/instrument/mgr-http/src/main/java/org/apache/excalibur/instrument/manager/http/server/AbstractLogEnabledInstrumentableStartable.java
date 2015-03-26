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

package org.apache.excalibur.instrument.manager.http.server;

import org.apache.avalon.framework.activity.Startable;

import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $
 */
abstract class AbstractLogEnabledInstrumentableStartable
    extends AbstractLogEnabledInstrumentable
    implements Startable, Runnable
{
    /** Reference to the worker thread. */
    private Thread m_runner;
    
    /** Flag set when the m_runner thread has been asked to stop. */
    private boolean m_runnerStop = false;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractLogEnabledInstrumentableStartable.
     */
    public AbstractLogEnabledInstrumentableStartable()
    {
        super();
    }
    
    /*---------------------------------------------------------------
     * Startable Methods
     *-------------------------------------------------------------*/
    /**
     * Starts the runner thread.
     *
     * @throws Exception If there are any problems.
     */
    public void start()
        throws Exception
    {
        getLogger().debug( "Starting..." );
        
        m_runner = new Thread( this, getInstrumentableName() + "_runner" );
        m_runner.start();
    }
    
    /**
     * Stops the runner thread, blocking until it has stopped.
     *
     * @throws Exception If there are any problems stopping the component.
     */
    public void stop()
        throws Exception
    {
        getLogger().debug( "Stopping." );
        
        Thread runner = m_runner;
        m_runnerStop = true;
        if ( runner != null )
        {
            runner.interrupt();
        }
        
        // Give the user code a change to stop cleanly.
        try
        {
            stopRunner();
        }
        catch ( Throwable t )
        {
            getLogger().error( "Encountered a problem while stopping the component.", t );
        }
        
        getLogger().debug( "Waiting for runner thread to stop." );
        synchronized ( this )
        {
            while ( m_runner != null )
            {
                try
                {
                    // Wait to be notified that the thread has exited.
                    this.wait();
                }
                catch ( InterruptedException e )
                {
                    // Ignore
                }
            }
        }
        getLogger().debug( "Stopped." );
    }
    
    /*---------------------------------------------------------------
     * Runable Methods
     *-------------------------------------------------------------*/
    /**
     * Run method which is responsible for launching the runner method and
     *  handling the shutdown cycle.
     */
    public void run()
    {
        if ( Thread.currentThread() != m_runner )
        {
            throw new IllegalStateException( "Private method." );
        }
        
        getLogger().debug( "Runner thread started." );
        
        try
        {
            try
            {
                runner();
            }
            catch ( Throwable t )
            {
                getLogger().warn(
                    "The runner method threw an uncaught exception, runner is terminating,", t );
            }
        }
        finally
        {
            synchronized ( this )
            {
                m_runner = null;
                
                // Wake up the stop method if it is waiting for the runner to stop.
                this.notify();
            }

            getLogger().debug( "Runner thread stopped." );
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Called when the component is being stopped, the isStopping method will
     *  always return true when this method is called.  This version of the
     *  method does nothing.
     *
     * @throws Exception If there are any problems
     */
    protected void stopRunner()
        throws Exception
    {
    }
    
    /**
     * Runner method that will be called when the component is started.
     *  The method must monitor the isStopping() method and make sure
     *  that it returns in a timely manner when the isStopping() method
     *  returns true.
     */
    protected abstract void runner();
    
    /**
     * Returns true when the component is in the process of being stopped.
     *
     * @return True when the component is in the process of being stopped.
     */
    public boolean isStopping()
    {
        return m_runnerStop;
    }
}
