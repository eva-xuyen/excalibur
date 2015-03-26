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

package org.apache.avalon.cornerstone.blocks.threads;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.cornerstone.services.threads.ThreadManager;

import org.apache.excalibur.thread.ThreadPool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Abstract implementation of ThreadManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractThreadManager
    extends AbstractLogEnabled
    implements ThreadManager, Configurable, Disposable
{
    ///Map of thread pools for application
    private HashMap m_threadPools = new HashMap();

    /**
     * Setup thread pools based on configuration data.
     *
     * @param configuration the configuration data
     * @exception ConfigurationException if an error occurs
     * @avalon.configuration schema="http://relaxng.org/ns/structure/1.0"
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration[] groups = configuration.getChildren( "thread-group" );
        for( int i = 0; i < groups.length; i++ )
        {
            configureThreadPool( m_threadPools, groups[ i ] );
        }
    }

    public void dispose()
    {
        final Iterator pools = m_threadPools.values().iterator();
        while( pools.hasNext() )
        {
            ContainerUtil.dispose( pools.next() );
        }
    }

    protected abstract void configureThreadPool( final Map threadPools,
                                                 final Configuration configuration )
        throws ConfigurationException;

    /**
     * Retrieve a thread pool by name.
     *
     * @param name the name of thread pool
     * @return the threadpool
     * @exception IllegalArgumentException if the name of thread pool is
     *            invalid or named pool does not exist
     */
    public ThreadPool getThreadPool( final String name )
        throws IllegalArgumentException
    {
        final ThreadPool threadPool = (ThreadPool)m_threadPools.get( name );

        if( null == threadPool )
        {
            final String message = "Unable to locate ThreadPool named " + name;
            throw new IllegalArgumentException( message );
        }

        return threadPool;
    }

    /**
     * Retrieve the default thread pool.
     *
     * @return the thread pool
     */
    public ThreadPool getDefaultThreadPool()
    {
        return getThreadPool( "default" );
    }
}
