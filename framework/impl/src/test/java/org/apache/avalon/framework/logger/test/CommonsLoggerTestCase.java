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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.avalon.framework.logger.CommonsLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Test case for CommonsLogger class.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CommonsLoggerTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.3
 */
public class CommonsLoggerTestCase extends MockObjectTestCase
{

    private Mock mockLog;
    private Exception exception;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockLog = mock(Log.class);
        exception = new Exception("JUnit");
    }

    /**
     * Test debug level.
     */
    public void testDebug()
    {
        final Logger logger = new CommonsLogger((Log)mockLog.proxy(), "JUnit");

        mockLog.expects(once()).method("isDebugEnabled").will(returnValue(true));
        mockLog.expects(once()).method("debug").with(eq("JUnit"));
        mockLog.expects(once()).method("debug").with(eq("JUnit"), same(exception));

        if(logger.isDebugEnabled())
        {
            logger.debug("JUnit");
            logger.debug("JUnit", exception);
        }
    }

    /**
     * Test info level.
     */
    public void testInfo()
    {
        final Logger logger = new CommonsLogger((Log)mockLog.proxy(), "JUnit");

        mockLog.expects(once()).method("isInfoEnabled").will(returnValue(true));
        mockLog.expects(once()).method("info").with(eq("JUnit"));
        mockLog.expects(once()).method("info").with(eq("JUnit"), same(exception));

        if(logger.isInfoEnabled())
        {
            logger.info("JUnit");
            logger.info("JUnit", exception);
        }
    }

    /**
     * Test warn level.
     */
    public void testWarn()
    {
        final Logger logger = new CommonsLogger((Log)mockLog.proxy(), "JUnit");

        mockLog.expects(once()).method("isWarnEnabled").will(returnValue(true));
        mockLog.expects(once()).method("warn").with(eq("JUnit"));
        mockLog.expects(once()).method("warn").with(eq("JUnit"), same(exception));

        if(logger.isWarnEnabled())
        {
            logger.warn("JUnit");
            logger.warn("JUnit", exception);
        }
    }

    /**
     * Test error level.
     */
    public void testError()
    {
        final Logger logger = new CommonsLogger((Log)mockLog.proxy(), "JUnit");

        mockLog.expects(once()).method("isErrorEnabled").will(returnValue(true));
        mockLog.expects(once()).method("error").with(eq("JUnit"));
        mockLog.expects(once()).method("error").with(eq("JUnit"), same(exception));

        if(logger.isErrorEnabled())
        {
            logger.error("JUnit");
            logger.error("JUnit", exception);
        }
    }

    /**
     * Test fatal error level.
     */
    public void testFatalError()
    {
        final Logger logger = new CommonsLogger((Log)mockLog.proxy(), "JUnit");

        mockLog.expects(once()).method("isFatalEnabled").will(returnValue(true));
        mockLog.expects(once()).method("fatal").with(eq("JUnit"));
        mockLog.expects(once()).method("fatal").with(eq("JUnit"), same(exception));

        if(logger.isFatalErrorEnabled())
        {
            logger.fatalError("JUnit");
            logger.fatalError("JUnit", exception);
        }
    }

    /**
     * Test creation of a child logger. Nees <tt>simplelog.properties</tt> as resource.
     */
    public void testChildLogger()
    {
        final Properties systemProperties = System.getProperties();
        final PrintStream err = System.err;
        try
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final PrintStream stream = new PrintStream(buffer, true);
            System.setProperty(Log.class.getName(), SimpleLog.class.getName());
            LogFactory.releaseAll();
            System.setErr(stream);
            final Logger logger = new CommonsLogger(LogFactory.getLog("JUnit"), "JUnit");
            final Logger child = logger.getChildLogger("test");
            child.fatalError("foo");
            assertEquals("[FATAL] JUnit.test - foo", buffer.toString().trim());
        }
        finally
        {
            System.setProperties(systemProperties);
            System.setErr(err);
        }

    }
}

