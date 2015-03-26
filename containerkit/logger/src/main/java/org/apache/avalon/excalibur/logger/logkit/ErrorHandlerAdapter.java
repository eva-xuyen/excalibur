/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.log.ErrorHandler;
import org.apache.log.Priority;
import org.apache.log.LogEvent;

/**
 * This class adapts o.a.a.f.logger.Logger
 * to the LogKit ErrorHandler interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */

public class ErrorHandlerAdapter implements ErrorHandler
{
    private final Logger m_reliableLogger;

    public ErrorHandlerAdapter( final Logger reliableLogger )
    {
       if ( reliableLogger == null )
       {
           throw new NullPointerException( "reliableLogger" );
       }
       m_reliableLogger = reliableLogger;
    }

    public void error( final String message, final Throwable throwable, final LogEvent event )
    {
        // let them know we're not OK
        m_reliableLogger.fatalError( message, throwable );

        // transmit the original error
        final Priority p = event.getPriority();
        final String nestedMessage = "nested log event: " + event.getMessage();

        if ( p == Priority.DEBUG )
        {
            m_reliableLogger.debug( nestedMessage, event.getThrowable() );
        }
        else if ( p == Priority.INFO )
        {
            m_reliableLogger.info( nestedMessage, event.getThrowable() );
        }
        else if ( p == Priority.WARN )
        {
            m_reliableLogger.warn( nestedMessage, event.getThrowable() );
        }
        else if ( p == Priority.ERROR )
        {
            m_reliableLogger.error( nestedMessage, event.getThrowable() );
        }
        else if ( p == Priority.FATAL_ERROR)
        {
            m_reliableLogger.fatalError( nestedMessage, event.getThrowable() );
        }
        else
        {
            /** This just plainly can't happen :-)*/
            m_reliableLogger.error( "unrecognized priority " + nestedMessage, 
                event.getThrowable() );
        }
    }
}
