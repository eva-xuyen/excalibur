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

import java.util.Map;

import org.apache.avalon.excalibur.thread.impl.DefaultThreadPool;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Default implementation of ThreadManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 * @avalon.component name="thread-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.threads.ThreadManager"
 */
public class DefaultThreadManager
    extends AbstractThreadManager
{
    protected void configureThreadPool( final Map threadPools,
                                        final Configuration configuration )
        throws ConfigurationException
    {
        final String name = configuration.getChild( "name" ).getValue();
        // NEVER USED!!
        //final int priority = configuration.getChild( "priority" ).getValueAsInteger( 5 );
        final boolean isDaemon = configuration.getChild( "is-daemon" ).getValueAsBoolean( false );

        final int minThreads = configuration.getChild( "min-threads" ).getValueAsInteger( 5 );
        final int maxThreads = configuration.getChild( "max-threads" ).getValueAsInteger( 10 );

        // NEVER USED!!
        //final int minSpareThreads = configuration.getChild( "min-spare-threads" ).
        //    getValueAsInteger( maxThreads - minThreads );

        try
        {
            final DefaultThreadPool threadPool =
                new DefaultThreadPool( name, minThreads, maxThreads );
            threadPool.setDaemon( isDaemon );
            threadPool.enableLogging( getLogger() );
            threadPools.put( name, threadPool );
        }
        catch( final Exception e )
        {
            final String message = "Error creating ThreadPool named " + name;
            throw new ConfigurationException( message, e );
        }
    }
}
