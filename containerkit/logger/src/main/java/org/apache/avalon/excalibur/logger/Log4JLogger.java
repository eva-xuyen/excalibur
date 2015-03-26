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
import org.apache.log4j.Level;

/**
 * The default Log4J wrapper class for Logger. 
 * This implementation replaces the implementation from Avalon framework.
 * It "caches" the log levels for improved performance.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.21 $ $Date: 2004/05/04 13:08:00 $
 */
public class Log4JLogger 
    implements Logger
{
    
    /**
     * Constant for name of class to use when recording caller
     * of log method.
     */
    private static final String FQCN = Log4JLogger.class.getName();

    /** underlying implementation */
    private final org.apache.log4j.Logger m_logger;

    private final boolean m_isDebugEnabled;
    private final boolean m_isInfoEnabled;
    private final boolean m_isWarnEnabled;
    private final boolean m_isErrorEnabled;
    private final boolean m_isFatalErrorEnabled;

    /**
     * Create a logger that delegates to specified category.
     *
     * @param logImpl the category to delegate to
     */
    public Log4JLogger( final org.apache.log4j.Logger logImpl )
    {
        m_logger = logImpl;
        m_isDebugEnabled = logImpl.isDebugEnabled();
        m_isInfoEnabled = logImpl.isInfoEnabled();
        m_isWarnEnabled = logImpl.isEnabledFor( Level.WARN );
        m_isErrorEnabled = logImpl.isEnabledFor( Level.ERROR );
        m_isFatalErrorEnabled = logImpl.isEnabledFor( Level.FATAL );
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     */
    public final void debug( final String message )
    {
        if ( m_isDebugEnabled )
            m_logger.log( FQCN, Level.DEBUG, message, null );
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void debug( final String message, final Throwable throwable )
    {
        if ( m_isDebugEnabled )
            m_logger.log( FQCN, Level.DEBUG, message, throwable );
    }

    /**
     * Determine if messages of priority "debug" will be logged.
     *
     * @return true if "debug" messages will be logged
     */
    public final boolean isDebugEnabled()
    {
        return m_isDebugEnabled;
    }

    /**
     * Log a info message.
     *
     * @param message the message
     */
    public final void info( final String message )
    {
        if ( m_isInfoEnabled )
            m_logger.log( FQCN, Level.INFO, message, null );
    }

    /**
     * Log a info message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void info( final String message, final Throwable throwable )
    {
        if ( m_isInfoEnabled )
            m_logger.log( FQCN, Level.INFO, message, throwable );
    }

    /**
     * Determine if messages of priority "info" will be logged.
     *
     * @return true if "info" messages will be logged
     */
    public final boolean isInfoEnabled()
    {
        return m_isInfoEnabled;
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     */
    public final void warn( final String message )
    {
        if ( m_isWarnEnabled )
            m_logger.log( FQCN, Level.WARN, message, null );
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void warn( final String message, final Throwable throwable )
    {
        if ( m_isWarnEnabled )
            m_logger.log( FQCN, Level.WARN, message, throwable );
    }

    /**
     * Determine if messages of priority "warn" will be logged.
     *
     * @return true if "warn" messages will be logged
     */
    public final boolean isWarnEnabled()
    {
        return m_isWarnEnabled;
    }

    /**
     * Log a error message.
     *
     * @param message the message
     */
    public final void error( final String message )
    {
        if ( m_isErrorEnabled )
            m_logger.log( FQCN, Level.ERROR, message, null );
    }

    /**
     * Log a error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void error( final String message, final Throwable throwable )
    {
        if ( m_isErrorEnabled )
            m_logger.log( FQCN, Level.ERROR, message, throwable );
    }

    /**
     * Determine if messages of priority "error" will be logged.
     *
     * @return true if "error" messages will be logged
     */
    public final boolean isErrorEnabled()
    {
        return m_isErrorEnabled;
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     */
    public final void fatalError( final String message )
    {
        if ( m_isFatalErrorEnabled )
            m_logger.log( FQCN, Level.FATAL, message, null );
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void fatalError( final String message, final Throwable throwable )
    {
        if ( m_isFatalErrorEnabled )
            m_logger.log( FQCN, Level.ERROR, message, throwable );
    }

    /**
     * Determine if messages of priority "fatalError" will be logged.
     *
     * @return true if "fatalError" messages will be logged
     */
    public final boolean isFatalErrorEnabled()
    {
        return m_isFatalErrorEnabled;
    }

    /**
     * Create a new child logger.
     * The name of the child logger is [current-loggers-name].[passed-in-name]
     * Throws <code>IllegalArgumentException</code> if name has an empty element name
     *
     * @param name the subname of this logger
     * @return the new logger
     */
    public final Logger getChildLogger( final String name )
    {
        return new Log4JLogger( org.apache.log4j.Logger.getLogger( m_logger.getName() + "." + name ) );
    }
    
}
