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

import java.util.logging.Level;

/**
 * The default JDK 1.4 wrapper class for Logger.  Please note that there is
 * not an exact match to the priority levels that JDK 1.4 logging has and
 * what LogKit or Log4J has.  For that reason, the following priority level
 * matching was used:
 *
 * <ul>
 *   <li>SEVERE  = error, fatalError</li>
 *   <li>WARNING = warn</li>
 *   <li>INFO    = info</li>
 *   <li>FINE    = debug</li>
 * </ul>
 *
 * <p>
 *   JDK 1.4 does allow you to have other levels like: CONFIG, FINER, and
 *   FINEST.  Most projects don't separate out configuration logging from
 *   debugging information.  Also, we wanted to maintain backwards
 *   compatibility as much as possible.  Unfortunately, with all the "fineness"
 *   details, there is no equivalent to the "error" log level.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Jdk14Logger.java 506231 2007-02-12 02:36:54Z crossley $
 */
public final class Jdk14Logger
    implements Logger
{
    //The actual JDK1.4 logger implementation
    private final java.util.logging.Logger m_logger;

    /**
     * Construct a Logger with specified jdk1.4 logger instance as implementation.
     *
     * @param logImpl the jdk1.4 logger instance to delegate to
     */
    public Jdk14Logger( java.util.logging.Logger logImpl )
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
        m_logger.log( Level.FINE, message );
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void debug( final String message, final Throwable throwable )
    {
        m_logger.log( Level.FINE, message, throwable );
    }

    /**
     * Determine if messages of priority "debug" will be logged.
     *
     * @return true if "debug" messages will be logged
     */
    public final boolean isDebugEnabled()
    {
        return m_logger.isLoggable( Level.FINE );
    }

    /**
     * Log a info message.
     *
     * @param message the message
     */
    public final void info( final String message )
    {
        m_logger.log( Level.INFO, message );
    }

    /**
     * Log a info message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void info( final String message, final Throwable throwable )
    {
        m_logger.log( Level.INFO, message, throwable );
    }

    /**
     * Determine if messages of priority "info" will be logged.
     *
     * @return true if "info" messages will be logged
     */
    public final boolean isInfoEnabled()
    {
        return m_logger.isLoggable( Level.INFO );
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     */
    public final void warn( final String message )
    {
        m_logger.log( Level.WARNING, message );
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void warn( final String message, final Throwable throwable )
    {
        m_logger.log( Level.WARNING, message, throwable );
    }

    /**
     * Determine if messages of priority "warn" will be logged.
     *
     * @return true if "warn" messages will be logged
     */
    public final boolean isWarnEnabled()
    {
        return m_logger.isLoggable( Level.WARNING );
    }

    /**
     * Log a error message.
     *
     * @param message the message
     */
    public final void error( final String message )
    {
        m_logger.log( Level.SEVERE, message );
    }

    /**
     * Log a error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void error( final String message, final Throwable throwable )
    {
        m_logger.log( Level.SEVERE, message, throwable );
    }

    /**
     * Determine if messages of priority "error" will be logged.
     *
     * @return true if "error" messages will be logged
     */
    public final boolean isErrorEnabled()
    {
        return m_logger.isLoggable( Level.SEVERE );
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     */
    public final void fatalError( final String message )
    {
        m_logger.log( Level.SEVERE, message );
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public final void fatalError( final String message, final Throwable throwable )
    {
        m_logger.log( Level.SEVERE, message, throwable );
    }

    /**
     * Determine if messages of priority "fatalError" will be logged.
     *
     * @return true if "fatalError" messages will be logged
     */
    public final boolean isFatalErrorEnabled()
    {
        return m_logger.isLoggable( Level.SEVERE );
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
        return new Jdk14Logger( java.util.logging.Logger
                                .getLogger( m_logger.getName() + "." + name ) );
    }
}
