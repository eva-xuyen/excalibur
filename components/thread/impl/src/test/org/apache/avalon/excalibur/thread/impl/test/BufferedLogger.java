/* 
 * Copyright 2002-2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.logger.Logger;

/**
 * Simple Logger which logs all information to an internal StringBuffer.
 *  When logging is complete call toString() on the logger to obtain the
 *  logged output.  Useful for testing.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 17:22:49 $
 * @since 4.0
 */
public class BufferedLogger
    implements Logger
{
    private final StringBuffer m_sb = new StringBuffer();

    /**
     * Log a debug message.
     *
     * @param message the message
     */
    public void debug( final String message )
    {
        debug( message, null );
    }

    /**
     * Log a debug message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void debug( final String message, final Throwable throwable )
    {
        append( "DEBUG", message, throwable );
    }

    /**
     * Determine if messages of priority "debug" will be logged.
     *
     * @return true if "debug" messages will be logged
     */
    public boolean isDebugEnabled()
    {
        return true;
    }

    /**
     * Log a info message.
     *
     * @param message the message
     */
    public void info( final String message )
    {
        info( message, null );
    }

    /**
     * Log a info message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void info( final String message, final Throwable throwable )
    {
        append( "INFO", message, throwable );
    }

    /**
     * Determine if messages of priority "info" will be logged.
     *
     * @return true if "info" messages will be logged
     */
    public boolean isInfoEnabled()
    {
        return true;
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     */
    public void warn( final String message )
    {
        warn( message, null );
    }

    /**
     * Log a warn message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void warn( final String message, final Throwable throwable )
    {
        append( "WARN", message, throwable );
    }

    /**
     * Determine if messages of priority "warn" will be logged.
     *
     * @return true if "warn" messages will be logged
     */
    public boolean isWarnEnabled()
    {
        return true;
    }

    /**
     * Log a error message.
     *
     * @param message the message
     */
    public void error( final String message )
    {
        error( message, null );
    }

    /**
     * Log a error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void error( final String message, final Throwable throwable )
    {
        append( "ERROR", message, throwable );
    }

    /**
     * Determine if messages of priority "error" will be logged.
     *
     * @return true if "error" messages will be logged
     */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     */
    public void fatalError( final String message )
    {
        fatalError( message, null );
    }

    /**
     * Log a fatalError message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void fatalError( final String message, final Throwable throwable )
    {
        append( "FATAL ERROR", message, throwable );
    }

    /**
     * Determine if messages of priority "fatalError" will be logged.
     *
     * @return true if "fatalError" messages will be logged
     */
    public boolean isFatalErrorEnabled()
    {
        return true;
    }

    /**
     * Create a new child logger.
     * The name of the child logger is [current-loggers-name].[passed-in-name]
     *
     * @param name the subname of this logger
     * @return the new logger
     */
    public Logger getChildLogger( final String name )
    {
        return this;
    }

    /**
     * Returns the contents of the buffer.
     *
     * @return the buffer contents
     *
     */
    public String toString()
    {
        return m_sb.toString();
    }

    private void append( final String level,
                         final String message,
                         final Throwable throwable )
    {
        synchronized( m_sb )
        {
            m_sb.append( level );
            m_sb.append( " - " );
            m_sb.append( message );

            if( null != throwable )
            {
                final String stackTrace =
                    ExceptionUtil.printStackTrace( throwable );
                m_sb.append( " : " );
                m_sb.append( stackTrace );
            }
            m_sb.append( "\n" );
        }
    }
}
