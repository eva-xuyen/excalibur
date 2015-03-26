/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.logger.test;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LoggerAwareOutputStream;

/**
 * Test case for LoggerAwareOutputStream class.
 * 
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since Dec 4, 2004 6:53:48 PM
 */
public class LoggerAwareOutputStreamTestCase extends TestCase
{
    /**
     * Tests the logger aware output stream by sending a test string to the
     * output stream, and checking whether the logger logged the same string. 
     * 
     * @throws Exception if an error occurs
     */
    public void testLoggerAwareOutputStream() throws Exception
    {
        final TestLogger logger = new TestLogger();
        final String testString = "a test string";
        
        final OutputStream os =
            new LoggerAwareOutputStream( logger ) {
                protected void logMessage( String message )
                {
                    m_logger.debug( message );
                }
            };

        final OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(testString, 0, testString.length());
        writer.close();
        final String message = logger.getDebugMessage();

        // check that string printed to the output stream is the same in the log
        assertEquals("Logged message doesn't equal source string", testString, message);

    }

    /**
     * Based on NullLogger. 
     */
    private class TestLogger implements Logger
    {
        /**
         * Creates a new <code>NullLogger</code>.
         */
        public TestLogger()
        {
        }

        /**
         * Stores debug message inside of this test logger.
         *
         * @param message ignored
         */
        public void debug( String message )
        {
            this.message = message;
        }
        
        private String message;
        
        /**
         * Get the saved message from the last call to debug
         * @return
         */
        public String getDebugMessage() 
        {
            return message;
        }

        /**
         * No-op.
         *
         * @param message ignored
         * @param throwable ignored
         */
        public void debug( String message, Throwable throwable )
        {
        }

        /**
         * Always returns true
         *
         * @return <code>true</code>
         */
        public boolean isDebugEnabled()
        {
            return true;
        }

        /**
         * No-op.
         *
         * @param message ignored
         */
        public void info( String message )
        {
        }

        /**
         * No-op.
         *
         * @param message ignored
         * @param throwable ignored
         */
        public void info( String message, Throwable throwable )
        {
        }

        /**
         * No-op.
         *
         * @return <code>false</code>
         */
        public boolean isInfoEnabled()
        {
            return false;
        }

        /**
         * No-op.
         *
         * @param message ignored
         */
        public void warn( String message )
        {
        }

        /**
         * No-op.
         *
         * @param message ignored
         * @param throwable ignored
         */
        public void warn( String message, Throwable throwable )
        {
        }

        /**
         * No-op.
         *
         * @return <code>false</code>
         */
        public boolean isWarnEnabled()
        {
            return false;
        }

        /**
         * No-op.
         *
         * @param message ignored
         */
        public void error( String message )
        {
        }

        /**
         * No-op.
         *
         * @param message ignored
         * @param throwable ignored
         */
        public void error( String message, Throwable throwable )
        {
        }

        /**
         * No-op.
         *
         * @return <code>false</code>
         */
        public boolean isErrorEnabled()
        {
            return false;
        }

        /**
         * No-op.
         *
         * @param message ignored
         */
        public void fatalError( String message )
        {
        }

        /**
         * No-op.
         *
         * @param message ignored
         * @param throwable ignored
         */
        public void fatalError( String message, Throwable throwable )
        {
        }

        /**
         * No-op.
         *
         * @return <code>false</code>
         */
        public boolean isFatalErrorEnabled()
        {
            return false;
        }

        /**
         * Returns this <code>NullLogger</code>.
         *
         * @param name ignored
         * @return this <code>NullLogger</code>
         */
        public Logger getChildLogger( String name )
        {
            return this;
        }
    }
}
