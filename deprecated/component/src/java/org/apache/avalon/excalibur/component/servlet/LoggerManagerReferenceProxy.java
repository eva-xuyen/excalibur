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
package org.apache.avalon.excalibur.component.servlet;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;

/**
 * Reference Proxy to a LoggerManager
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
final class LoggerManagerReferenceProxy
    extends AbstractReferenceProxy
    implements LoggerManager
{
    private LoggerManager m_loggerManager;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new proxy.
     *
     * @param componentManager LoggerManager being proxied.
     * @param latch Latch wich will be notified when this proxy is finalized.
     * @param name Name of the proxy.
     */
    LoggerManagerReferenceProxy( LoggerManager loggerManager,
                                 AbstractReferenceProxyLatch latch,
                                 String name )
    {
        super( latch, name );
        m_loggerManager = loggerManager;
    }

    /*---------------------------------------------------------------
     * LoggerManager Methods
     *-------------------------------------------------------------*/
    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( String categoryName )
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
