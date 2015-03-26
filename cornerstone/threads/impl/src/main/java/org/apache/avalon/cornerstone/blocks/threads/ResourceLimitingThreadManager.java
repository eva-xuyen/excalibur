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
import org.apache.avalon.excalibur.thread.impl.ResourceLimitingThreadPool;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Implementation of ResourceLimitingThreadManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="limiting-thread-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.threads.ThreadManager"
 */
public class ResourceLimitingThreadManager
    extends AbstractThreadManager
{
    protected void configureThreadPool( final Map threadPools,
                                        final Configuration configuration )
        throws ConfigurationException
    {
        final String name = configuration.getChild( "name" ).getValue();
        final boolean isDaemon = configuration.getChild( "is-daemon" ).getValueAsBoolean( false );
        
        final int max = configuration.getChild( "max-threads" ).getValueAsInteger( 10 );
        final boolean maxStrict = configuration.getChild( "max-strict" ).getValueAsBoolean( true );
        final boolean blocking = configuration.getChild( "blocking" ).getValueAsBoolean( true );
        final long blockTimeout = configuration.getChild( "block-timeout" ).getValueAsLong( 0 );
        final long trimInterval = configuration.getChild( "trim-interval" ).getValueAsLong( 10000 );

        try
        {
            final ResourceLimitingThreadPool threadPool = new ResourceLimitingThreadPool(
                name, max, maxStrict, blocking, blockTimeout, trimInterval );
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
