/*
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl.handler;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.d_haven.event.command.Command;

/**
 * This is the command class to initialize a ComponentHandler
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.12 $ $Date: 2004/02/28 15:16:25 $
 */
public final class PrepareHandlerCommand implements Command
{
    private final ComponentHandler m_handler;
    private final Logger m_logger;

    /**
     * Creation of a new prepare handler command.
     * @param handler the compoent handler
     * @param logger the logging channel
     */
    public PrepareHandlerCommand( final ComponentHandler handler,
                                  final Logger logger )
    {
        m_handler = handler;
        m_logger = ( null == logger ) ? new NullLogger() : logger;
    }

    /**
     * Returns a reference to the ComponentHandler being prepared.
     *
     * @return The ComponentHandler.
     */
    public ComponentHandler getHandler()
    {
        return m_handler;
    }

    /**
     * Invoke execution of the handler
     * @exception java.lang.Exception if a handler execution exception occurs
     */
    public void execute()
        throws Exception
    {
        try
        {
            m_handler.prepareHandler();
        }
        catch ( final Exception e )
        {
            if ( m_logger.isErrorEnabled() )
            {
                //m_logger.error( "[REMOVE THIS] Could not prepare ComponentHandler for: " + m_handler.getComponentClass().getName(), e );
            }

            throw e;
        }
    }
}

