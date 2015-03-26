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
package org.apache.avalon.framework.logger;

/**
 * The default LogKit wrapper class for Logger.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LogKitLogger.java 506231 2007-02-12 02:36:54Z crossley $
 */
public final class LogKitLogger
    implements Logger
{
    //underlying implementation to delegate to
    private final org.apache.log.Logger m_logger;

    /**
     * Create a logger that delegates to specified logger.
     *
     * @param logImpl the LogKit logger to delegate to
     */
    public LogKitLogger( org.apache.log.Logger logImpl )
    {
        m_logger = logImpl;
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     */
    public final void debug( final String message )
    {
        m_logger.debug( message );
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void debug( final String message, final Throwable throwable )
    {
        m_logger.debug( message, throwable );
    }

    /**
     * Determine if messages of priority "debug" will be logged.
     *
     * @return true if "debug" messages will be logged
     */
    public final boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }

    /**
     * Log a info message.
     *
     * @param message the message
     */
    public final void info( final String message )
    {
        m_logger.info( message );
    }

    /**
     * Log a info message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void info( final String message, final Throwable throwable )
    {
        m_logger.info( message, throwable );
    }

    /**
     * Determine if messages of priority "info" will be logged.
     *
     * @return true if "info" messages will be logged
     */
    public final boolean isInfoEnabled()
    {
        return m_logger.isInfoEnabled();
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     */
    public final void warn( final String message )
    {
        m_logger.warn( message );
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void warn( final String message, final Throwable throwable )
    {
        m_logger.warn( message, throwable );
    }

    /**
     * Determine if messages of priority "warn" will be logged.
     *
     * @return true if "warn" messages will be logged
     */
    public final boolean isWarnEnabled()
    {
        return m_logger.isWarnEnabled();
    }

    /**
     * Log a error message.
     *
     * @param message the message
     */
    public final void error( final String message )
    {
        m_logger.error( message );
    }

    /**
     * Log a error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void error( final String message, final Throwable throwable )
    {
        m_logger.error( message, throwable );
    }

    /**
     * Determine if messages of priority "error" will be logged.
     *
     * @return true if "error" messages will be logged
     */
    public final boolean isErrorEnabled()
    {
        return m_logger.isErrorEnabled();
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     */
    public final void fatalError( final String message )
    {
        m_logger.fatalError( message );
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void fatalError( final String message, final Throwable throwable )
    {
        m_logger.fatalError( message, throwable );
    }

    /**
     * Determine if messages of priority "fatalError" will be logged.
     *
     * @return true if "fatalError" messages will be logged
     */
    public final boolean isFatalErrorEnabled()
    {
        return m_logger.isFatalErrorEnabled();
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
        return new LogKitLogger( m_logger.getChildLogger( name ) );
    }
}
