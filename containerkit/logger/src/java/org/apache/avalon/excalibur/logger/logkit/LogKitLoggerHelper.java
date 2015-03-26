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
package org.apache.avalon.excalibur.logger.logkit;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.log.Hierarchy;


/**
 * Tie this object to a LoggerManagerTee, give it the Hierachy
 * that LogKitAdapter operates upon and it will substitute
 * the ErrorHandler for the Hierarchy at the enableLogging() call.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class LogKitLoggerHelper implements LogEnabled
{
    /* The hierarchy to operate upon */
    private final Hierarchy m_hierarchy;

    /* Creates an instance of LogKitLoggerHelper. */
    public LogKitLoggerHelper( final Hierarchy hierarchy )
    {
        if ( hierarchy == null ) throw new NullPointerException( "hierarchy" );
        m_hierarchy = hierarchy;
    }

    /* The main work - creation of a custom ErrorHandler is done here. */
    public void enableLogging( final Logger logger )
    {
         if ( logger == null ) throw new NullPointerException( "logger" );
         final ErrorHandlerAdapter errorHandler = new ErrorHandlerAdapter( logger );
         m_hierarchy.setErrorHandler( errorHandler );
    }
}
