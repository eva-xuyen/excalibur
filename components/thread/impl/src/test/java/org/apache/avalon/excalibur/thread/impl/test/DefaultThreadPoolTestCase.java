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
package org.apache.avalon.excalibur.thread.impl.test;

import junit.framework.TestCase;
import org.apache.avalon.excalibur.thread.impl.DefaultThreadPool;
import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 * TestCase for DefaultThreadPool.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultThreadPoolTestCase
    extends TestCase
{
    public DefaultThreadPoolTestCase( final String name )
    {
        super( name );
    }

    public void testWithThreadContext()
        throws Exception
    {
        final DefaultThreadPool pool = new DefaultThreadPool( "default", 10 );
        pool.setDaemon( false );
        pool.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_INFO ) );
        pool.execute( new DummyRunnable() );
    }

    public void testWithoutThreadContext()
        throws Exception
    {
        final DefaultThreadPool pool = new DefaultThreadPool( "default", 10 );
        pool.setDaemon( false );
        pool.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_INFO ) );
        pool.execute( new DummyRunnable() );
    }

    private static class DummyRunnable
        implements Runnable
    {
        public void run()
        {
        }
    }
}
