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
package org.apache.avalon.excalibur.component;

import org.apache.avalon.excalibur.logger.LogKitManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.LogKit2AvalonLoggerAdapter;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;


/**
 * An adapter between LogkitManager and LoggerManager.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:14 $
 */
class Logger2LogKitManager
    implements LogKitManager
{
    private final Hierarchy m_hierarchy = new Hierarchy();
    private final LoggerManager m_loggerManager;
    
    public Logger2LogKitManager( final LoggerManager loggerManager )
    {
        m_loggerManager = loggerManager;
        final LogKit2AvalonLoggerAdapter target =
            new LogKit2AvalonLoggerAdapter( loggerManager.getDefaultLogger() );
        m_hierarchy.setDefaultLogTarget( target );
    }
    
    public org.apache.log.Logger getLogger( final String categoryName )
    {
        final Logger logger =
            m_loggerManager.getLoggerForCategory( categoryName );
        final org.apache.log.Logger logkitLogger =
            getHierarchy().getLoggerFor( categoryName );
        final LogKit2AvalonLoggerAdapter target =
            new LogKit2AvalonLoggerAdapter( logger );
        logkitLogger.setLogTargets( new LogTarget[ ] { target } );
        
        if ( logger.isDebugEnabled() )
        {
            logkitLogger.setPriority( Priority.DEBUG );
        }
        else if ( logger.isInfoEnabled() )
        {
            logkitLogger.setPriority( Priority.INFO );
        }
        else if ( logger.isWarnEnabled() )
        {
            logkitLogger.setPriority( Priority.WARN );
        }
        else if ( logger.isErrorEnabled() )
        {
            logkitLogger.setPriority( Priority.ERROR );
        }
        else if ( logger.isFatalErrorEnabled() )
        {
            logkitLogger.setPriority( Priority.FATAL_ERROR );
        }
        
        return logkitLogger;
    }
    
    public Hierarchy getHierarchy()
    {
        return m_hierarchy;
    }
}
