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
package org.apache.avalon.excalibur.logger.log4j;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.excalibur.logger.Log4JLogger;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.log4j.spi.LoggerRepository;

/**
 * This class sits on top of an existing Log4J Hierarchy
 * and returns logger wrapping Log4J loggers.
 *
 * Attach PrefixDecorator and/or CachingDecorator if desired.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class Log4JAdapter extends AbstractLogEnabled implements LoggerManager
{
    protected final LoggerRepository m_hierarchy;

    public Log4JAdapter( final LoggerRepository hierarchy )
    {
        if ( hierarchy == null ) throw new NullPointerException( "hierarchy" );
        m_hierarchy = hierarchy;
    }

    /**
     * Return the Logger for the specified category.
     * Log4J probably won't like the "" category name 
     * so we shall better return its getRootLogger() instead.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        if ( null == categoryName || categoryName.length() == 0 )
        {
            return getDefaultLogger();
        }
        else
        {
            return new Log4JLogger( m_hierarchy.getLogger( categoryName ) );
        }
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger()
    {
        return new Log4JLogger( m_hierarchy.getRootLogger() );
    }
}
