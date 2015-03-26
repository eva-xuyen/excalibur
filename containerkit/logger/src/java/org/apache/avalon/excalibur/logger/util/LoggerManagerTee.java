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
package org.apache.avalon.excalibur.logger.util;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;

/**
 * An AvalonTee object is not usefull by itself as it does not
 * implement any component interface. Its primary use is to
 * serve as a base class for objects proxing not only
 * lifecycle but also some "meaningfull" interfaces.
 * This object proxies LoggerManager interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class LoggerManagerTee extends AvalonTee implements LoggerManager
{
    /* The wrapped LoggerManager to delegate all the work to.*/
    private final LoggerManager m_loggerManager;

    /**
     * Creates a new instance of LoggerManagerTee. Adds the supplied
     * LoggerManager as the first tee. (It will receive the lifecycle
     * events first of all tees).
     */
    public LoggerManagerTee( final LoggerManager loggerManager )
    {
        addTee( loggerManager );
        m_loggerManager = loggerManager;
    }

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        return m_loggerManager.getLoggerForCategory( categoryName );
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger()
    {
        return m_loggerManager.getDefaultLogger();
    }
}
