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

package org.apache.avalon.cornerstone.threads.tutorial;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.apache.excalibur.thread.ThreadControl;
import org.apache.excalibur.thread.ThreadPool;

import org.apache.avalon.cornerstone.services.threads.ThreadManager;

/**
 * ThreadConsumer is a example of a component that uses the ThreadManager
 * service to aquired a thread pool.
 *
 * @avalon.component name="consumer" lifestyle="singleton" version="1.0"
 * @author Stephen McConnell
 */
public class ThreadConsumer extends AbstractLogEnabled implements
Serviceable, Initializable, Disposable
{
   /**
    * The service manager from which serrvices are aquired and released.
    */
    private ServiceManager m_manager;

   /**
    * The cornerstone thread manager.
    */
    private ThreadManager m_threads;

   /**
    * A thread pool aquired from the thread manager.
    */
    private ThreadPool m_pool;

   /**
    * A thread control return from the launching of a new thread.
    */
    private ThreadControl m_control;


   /**
    * Servicing of the component by the container during which the 
    * component aquires the ThreadManager service.
    * 
    * @param manager the thread manager
    * @exception ServiceException if the thread manager service is 
    *   unresolvable
    * @avalon.dependency key="threads" 
    *   type="org.apache.avalon.cornerstone.services.threads.ThreadManager"
    */
    public void service( ServiceManager manager ) throws ServiceException
    {
        m_manager = manager;
        getLogger().info( "aquiring cornerstone threads service" );
        m_threads = (ThreadManager) m_manager.lookup( "threads" );
    }

   /**
    * Initialization of the component by the container during which we 
    * establish a child thread by passing a runnable object to the thread pool.
    * @exception Exception if an initialization stage error occurs
    */
    public void initialize() throws Exception
    {
        getLogger().info( "initialization" );

        //
        // get the default thread pool
        //

        m_pool = m_threads.getDefaultThreadPool();

        //
        // create a runnable object to run
        //
 
        Counter counter = new Counter();
        counter.enableLogging( getLogger().getChildLogger( "counter" ) );

        //
        // start a thread and get the thread control reference
        //

        m_control = m_pool.execute( counter );
    }

   /**
    * Disposal of the component during which he handle the closue of the 
    * child thread we have establshed during the initialization stage.
    */
    public void dispose()
    {

        getLogger().info( "disposal" );

        if( ( m_control != null ) && !m_control.isFinished() )
        {
            //
            // interrupt the child 
            //

            getLogger().info( "disposal invoked while child thread active" );
            m_control.interrupt();

            //
            // Using m_control.join() locks things up - why?  Using a 
            // wait for finished state instead.
            // 

            while( !m_control.isFinished() )
            {
                getLogger().info( "waiting for child" );
                try
                {
                    Thread.sleep( 1000 );
                }
                catch( InterruptedException ie )
                {
                    // ignore it
                }
            }
        }

        //
        // check for errors
        //

        if( ( m_control != null ) && ( m_control.getThrowable() != null ) )
        {
            getLogger().warn( 
              "thread terminated with exception condition", 
               m_control.getThrowable() );
        }

        if( m_pool != null )
        {
            if( m_pool instanceof Disposable )
            {
                ((Disposable)m_pool).dispose();
            }
            m_pool = null;
        }

        m_manager.release( m_threads );

        m_control = null;
        m_threads = null;
        m_manager = null;
    }
}

