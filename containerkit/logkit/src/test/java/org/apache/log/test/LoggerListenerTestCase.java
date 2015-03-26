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
package org.apache.log.test;

import junit.framework.TestCase;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Test suite for logger listener features of Logger.
 *
 * @author Peter Donald
 */
public final class LoggerListenerTestCase
    extends TestCase
{
    public LoggerListenerTestCase( final String name )
    {
        super( name );
    }

    public void testUnicastLoggerListener()
    {
        final Hierarchy hierarchy = new Hierarchy();
        final RecordingLoggerListener listener = new RecordingLoggerListener();

        try
        {
            hierarchy.addLoggerListener( listener );
            hierarchy.addLoggerListener( listener );

            fail( "You should only be able to add one listener." );
        }
        catch (UnsupportedOperationException uoe)
        {
            // It passed, yay!
        }
    }

    public void testRemoveLoggerListener()
    {
        final Hierarchy hierarchy = new Hierarchy();
        final RecordingLoggerListener listener = new RecordingLoggerListener();

        hierarchy.addLoggerListener( listener );
        hierarchy.removeLoggerListener( listener );
        hierarchy.addLoggerListener( listener );

        // If no exceptions have been thrown, we are in business!
    }

    public void testPriorityInheritance()
        throws Exception
    {
        final RecordingLoggerListener listener = new RecordingLoggerListener();
        final Hierarchy hierarchy = new Hierarchy();
        hierarchy.addLoggerListener( listener );

        final Logger root = hierarchy.getRootLogger();
        final Logger l1 = root.getChildLogger( "logger1" );
        final Logger l2 = root.getChildLogger( "logger2" );
        final Logger l3 = root.getChildLogger( "logger1.logger3" );
        final Logger l4 = root.getChildLogger( "logger5.logger4" );
        final Logger l5 = root.getChildLogger( "logger5" );

        final Logger[] loggers = listener.getLoggers();
        assertEquals( "Logger Count", 5, loggers.length );
        assertEquals( "Logger[0]", l1, loggers[ 0 ] );
        assertEquals( "Logger[1]", l2, loggers[ 1 ] );
        assertEquals( "Logger[2]", l3, loggers[ 2 ] );
        assertEquals( "Logger[3]", l5, loggers[ 3 ] );
        assertEquals( "Logger[4]", l4, loggers[ 4 ] );
    }
}
