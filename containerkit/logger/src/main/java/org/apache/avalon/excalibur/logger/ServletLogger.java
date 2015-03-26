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
package org.apache.avalon.excalibur.logger;

import org.apache.avalon.framework.logger.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Logger to bootstrap avalon application iside a servlet.
 * Intended to be used as a logger for Fortress
 * ContextManager/ContainerManager.
 * 
 * Adapted from ConsoleLogger.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/10 13:54:50 $
 */

public class ServletLogger implements Logger
{
    /** Typecode for debugging messages. */
    public static final int LEVEL_DEBUG = 0;

    /** Typecode for informational messages. */
    public static final int LEVEL_INFO = 1;

    /** Typecode for warning messages. */
    public static final int LEVEL_WARN = 2;

    /** Typecode for error messages. */
    public static final int LEVEL_ERROR = 3;

    /** Typecode for fatal error messages. */
    public static final int LEVEL_FATAL = 4;

    /** Typecode for disabled log levels. */
    public static final int LEVEL_DISABLED = 5;

    private final ServletContext m_servletContext;
    private final int m_logLevel;
    private final String m_prompt;

    /**
     * Creates a new ServletLogger with the priority set to DEBUG.
     */
    public ServletLogger( final ServletConfig servletConfig )
    {
        this( servletConfig, LEVEL_DEBUG );
    }

    /** Helper method to write the constructors. */
    private void checkState()
    {
        if ( m_servletContext == null )
        {
            throw new NullPointerException( "servletContext" );
        }
        if ( m_logLevel < LEVEL_DEBUG || m_logLevel > LEVEL_DISABLED )
        {
            throw new IllegalArgumentException( "Bad logLevel: " + m_logLevel );
        }
    }

    /**
     * Creates a new ServletLogger.
     * @param servletContext ServletContext to log messages to
     * @param prompt text to prepend to every message
     * @param logLevel log level typecode
     */
    public ServletLogger( final ServletContext servletContext, final String prompt,
            final int logLevel )
    {
        m_servletContext = servletContext;
        m_logLevel = logLevel;
        checkState();
        m_prompt = prompt;
    }

    /**
     * Creates a new ServletLogger.
     * @param servletConfig the servletConfig to extract ServletContext from;
     *        also the servlet name is extracted to be prepended to every message.
     * @param logLevel log level typecode
     */
    public ServletLogger( final ServletConfig servletConfig, final int logLevel )
    {
        m_servletContext = servletConfig.getServletContext();
        m_logLevel = logLevel;
        checkState();

        final String servletName = servletConfig.getServletName();

        if ( servletName == null || "".equals( servletName ) )
        {
            m_prompt = "unknown: ";
        }
        else
        {
            m_prompt = servletName + ": ";
        }
    }

    /**
     * Logs a debugging message.
     *
     * @param message a <code>String</code> value
     */
    public void debug( final String message )
    {
        debug( message, null );
    }

    /**
     * Logs a debugging message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void debug( final String message, final Throwable throwable )
    {
        if( m_logLevel <= LEVEL_DEBUG )
        {
            m_servletContext.log( m_prompt + "[DEBUG] " + message, throwable );
        }
    }

    /**
     * Returns <code>true</code> if debug-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if debug-level logging
     */
    public boolean isDebugEnabled()
    {
        return m_logLevel <= LEVEL_DEBUG;
    }

    /**
     * Logs an informational message.
     *
     * @param message a <code>String</code> value
     */
    public void info( final String message )
    {
        info( message, null );
    }

    /**
     * Logs an informational message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void info( final String message, final Throwable throwable )
    {
        if( m_logLevel <= LEVEL_INFO )
        {
            m_servletContext.log( m_prompt + "[INFO] " + message, throwable );
        }
    }

    /**
     * Returns <code>true</code> if info-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if info-level logging is enabled
     */
    public boolean isInfoEnabled()
    {
        return m_logLevel <= LEVEL_INFO;
    }

    /**
     * Logs a warning message.
     *
     * @param message a <code>String</code> value
     */
    public void warn( final String message )
    {
        warn( message, null );
    }

    /**
     * Logs a warning message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void warn( final String message, final Throwable throwable )
    {
        if( m_logLevel <= LEVEL_WARN )
        {
            m_servletContext.log( m_prompt + "[WARNING] " + message, throwable );
        }
    }

    /**
     * Returns <code>true</code> if warn-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if warn-level logging is enabled
     */
    public boolean isWarnEnabled()
    {
        return m_logLevel <= LEVEL_WARN;
    }

    /**
     * Logs an error message.
     *
     * @param message a <code>String</code> value
     */
    public void error( final String message )
    {
        error( message, null );
    }

    /**
     * Logs an error message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void error( final String message, final Throwable throwable )
    {
        if( m_logLevel <= LEVEL_ERROR )
        {
            m_servletContext.log( m_prompt + "[ERROR] " + message, throwable );
        }
    }

    /**
     * Returns <code>true</code> if error-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if error-level logging is enabled
     */
    public boolean isErrorEnabled()
    {
        return m_logLevel <= LEVEL_ERROR;
    }

    /**
     * Logs a fatal error message.
     *
     * @param message a <code>String</code> value
     */
    public void fatalError( final String message )
    {
        fatalError( message, null );
    }

    /**
     * Logs a fatal error message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void fatalError( final String message, final Throwable throwable )
    {
        if( m_logLevel <= LEVEL_FATAL )
        {
            m_servletContext.log( m_prompt + "[FATAL ERROR] " + message, throwable );
        }
    }

    /**
     * Returns <code>true</code> if fatal-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if fatal-level logging is enabled
     */
    public boolean isFatalErrorEnabled()
    {
        return m_logLevel <= LEVEL_FATAL;
    }

    /**
     * Just returns this logger (<code>ServletLogger</code> is not hierarchical).
     *
     * @param name ignored
     * @return this logger
     */
    public Logger getChildLogger( final String name )
    {
        return this;
    }
}
