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

import org.apache.log.Hierarchy;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.Priority;

/**
 * A basic adapter that adapts an Avalon Logger to a Logkit Logger.
 * Useful when providing backwards compatability support for Loggable
 * components.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LogKit2AvalonLoggerAdapter.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public final class LogKit2AvalonLoggerAdapter
    implements LogTarget
{
    /**
     * The Avalon Logger that we re-route to.
     */
    private final Logger m_logger;
    
    /**
     * Create a Logkit {@link org.apache.log.Logger} instance that
     * redirects to an Avalon {@link org.apache.avalon.framework.logger.Logger} instance.
     *
     * @param logger the Avalon Logger
     * @return the LogKit Logger
     */
    public static org.apache.log.Logger createLogger( final Logger logger )
    {
        final Hierarchy hierarchy = new Hierarchy();
        final org.apache.log.Logger logKitLogger = hierarchy.getLoggerFor( "" );
        final LogKit2AvalonLoggerAdapter target =
            new LogKit2AvalonLoggerAdapter( logger );
        logKitLogger.setLogTargets( new LogTarget[ ] { target } );
        
        if ( logger.isDebugEnabled() )
        {
            logKitLogger.setPriority( Priority.DEBUG );
        }
        else if ( logger.isInfoEnabled() )
        {
            logKitLogger.setPriority( Priority.INFO );
        }
        else if ( logger.isWarnEnabled() )
        {
            logKitLogger.setPriority( Priority.WARN );
        }
        else if ( logger.isErrorEnabled() )
        {
            logKitLogger.setPriority( Priority.ERROR );
        }
        else if ( logger.isFatalErrorEnabled() )
        {
            logKitLogger.setPriority( Priority.FATAL_ERROR );
        }
        return logKitLogger;
    }
    
    /**
     * Constructor for an Adaptor. Adapts to
     * specified Avalon Logger.
     *
     * @param logger the avalon logger.
     */
    public LogKit2AvalonLoggerAdapter( final Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        m_logger = logger;
    }
    
    /**
     * Route a LogKit message to an avalon Logger.
     *
     * @param event the log message
     */
    public void processEvent( LogEvent event )
    {
        final String message = event.getMessage();
        final Throwable throwable = event.getThrowable();
        final Priority priority = event.getPriority();
        if( Priority.DEBUG == priority )
        {
            m_logger.debug( message, throwable );
        }
        else if( Priority.INFO == priority )
        {
            m_logger.info( message, throwable );
        }
        else if( Priority.WARN == priority )
        {
            m_logger.warn( message, throwable );
        }
        else if( Priority.ERROR == priority )
        {
            m_logger.error( message, throwable );
        }
        else
        {
            m_logger.fatalError( message, throwable );
        }
    }
}
