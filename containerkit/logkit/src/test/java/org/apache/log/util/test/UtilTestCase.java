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
package org.apache.log.util.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import junit.framework.TestCase;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.util.LoggerOutputStream;

/**
 * Test suite for utility features of Logger.
 *
 * @author Peter Donald
 */
public final class UtilTestCase
    extends TestCase
{
    private static final String EOL = System.getProperty( "line.separator", "\n" );
    private static final RawFormatter FORMATTER = new RawFormatter();

    private static final String MSG = "No soup for you!";
    private static final String RMSG = MSG;

    public UtilTestCase( final String name )
    {
        super( name );
    }

    private String getResult( final ByteArrayOutputStream output )
    {
        final String result = output.toString();
        output.reset();
        return result;
    }

    public void testStackIntrospector()
        throws Exception
    {
        /*
        final String METHOD_RESULT = UtilTestCase.class.getName() + ".";
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final StreamTarget target = new StreamTarget( output, METHOD_FORMATTER );
        final Hierarchy hierarchy = new Hierarchy();
        hierarchy.setDefaultLogTarget( target );

        final Logger logger = hierarchy.getLoggerFor( "myLogger" );

        logger.debug( MSG );
        final String result = getResult( output );
        final String expected = METHOD_RESULT + "testStackIntrospector()";
        assert( "StackIntrospector", result.startsWith( expected ) );
        //result of StackIntrospector.getCallerMethod( Logger.class );
        */
    }

    public void testLoggerOutputStream()
        throws Exception
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final StreamTarget target = new StreamTarget( output, FORMATTER );

        final Hierarchy hierarchy = new Hierarchy();
        hierarchy.setDefaultLogTarget( target );

        final Logger logger = hierarchy.getLoggerFor( "myLogger" );
        final LoggerOutputStream outputStream = new LoggerOutputStream( logger, Priority.DEBUG );
        final PrintStream printer = new PrintStream( outputStream, true );

        printer.println( MSG );
        assertEquals( "LoggerOutputStream", RMSG + EOL, getResult( output ) );

        //unbuffered output
        printer.print( MSG );
        printer.flush();
        assertEquals( "LoggerOutputStream", RMSG, getResult( output ) );

        printer.close();
    }
}
