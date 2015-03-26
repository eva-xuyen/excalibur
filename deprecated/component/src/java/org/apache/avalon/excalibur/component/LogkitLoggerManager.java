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
package org.apache.avalon.excalibur.component;

import org.apache.avalon.excalibur.logger.LogKitManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * An adapter class to help with backwards comaptability.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:14 $
 */
public class LogkitLoggerManager
    implements LoggerManager
{
    private LoggerManager m_loggerManager;
    private LogKitManager m_logKitManager;

    public LogkitLoggerManager( final LoggerManager loggerManager,
                                final LogKitManager logKitManager )
    {
        m_loggerManager = loggerManager;
        m_logKitManager = logKitManager;
    }

    public org.apache.log.Logger getLogKitLoggerForCategory( final String categoryName )
    {
        return getLogKitManager().getLogger( categoryName );
    }

    public Logger getLoggerForCategory( String categoryName )
    {
        if( null != m_loggerManager )
        {
            return m_loggerManager.getLoggerForCategory( categoryName );
        }
        else
        {
            return new LogKitLogger( getLogKitLoggerForCategory( categoryName ) );
        }
    }

    public Logger getDefaultLogger()
    {
        return getLoggerForCategory( "" );
    }

    LogKitManager getLogKitManager()
    {
        if( null == m_logKitManager )
        {
            m_logKitManager = new Logger2LogKitManager( m_loggerManager );
        }
        return m_logKitManager;
    }
}
