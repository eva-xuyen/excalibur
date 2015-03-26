/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.logger.decorator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.excalibur.logger.LoggerManager;
import java.util.Map;
import java.util.HashMap;

/**
 * This class implements LoggerManager interface by
 * passing all the job to a wrapped LoggerManager,
 * but the returened Loggers are cached.
 *
 * All operations of this class are synchronized via
 * a single lock. As the <code>LoggerManager</code> is
 * not expected to be a performance bottleneck probably
 * this design will be good enough.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class CachingDecorator extends LoggerManagerDecorator
{
    /**
     * Logger-s cache. 
     * All access synchronized( m_loggers ).
     */
    private final Map m_loggers = new HashMap();
    /**
     * This variable caches the result of 
     * getDefaultLogger(). This class will
     * treat getDefaultLogger() and getLoggerForCategory("")
     * on our wrapped LoggerManager as being potentially
     * different, although all of the existing adapters
     * probably return the same Logger for both.
     *
     * Access synchronized( this );
     */
    private Logger m_defaultLogger = null;

    /**
     * Creates a <code>CachingDecorator</code> instance.
     */
    public CachingDecorator( final LoggerManager loggerManager )
    {
        super( loggerManager );
    }

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        synchronized( m_loggers )
        {
            Logger logger = (Logger) m_loggers.get( categoryName );

            if ( logger == null )
            {
                logger = m_loggerManager.getLoggerForCategory( categoryName );

                if ( logger == null ) 
                {
                    final String message = "getLoggerForCategory('" +
                            categoryName + "')";
                    throw new NullPointerException( message );
                }

                m_loggers.put( categoryName, logger );
            }

            return logger;
        }
    }

    /**
     * Return the default Logger.  Although it is expected
     * that the wrapped loggerManager will return the same
     * as getLoggerForCategory("") we cache the value separtely.
     */
    public Logger getDefaultLogger()
    {
        synchronized( this )
        {
            if ( m_defaultLogger == null )
            {
                m_defaultLogger = m_loggerManager.getDefaultLogger();

                if ( m_defaultLogger == null )
                {
                    throw new NullPointerException ( "getDefaultLogger()" );
                }
            }
            return m_defaultLogger;
        }
    }
}
