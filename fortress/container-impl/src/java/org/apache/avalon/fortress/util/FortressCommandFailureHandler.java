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

package org.apache.avalon.fortress.util;

import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.PrepareHandlerCommand;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.d_haven.event.command.Command;
import org.d_haven.event.command.CommandFailureHandler;

/**
 * The default CommandFailureHandler used by Fortress to log any
 *  failures encountered while executing commands.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 15:16:26 $
 * @since 4.1
 */
public class FortressCommandFailureHandler
    extends AbstractLogEnabled
    implements CommandFailureHandler
{
    /**
     * Handle a command failure.  If a command throws an exception, it has failed.  The
     * CommandManager will call this method so that we can handle the problem effectively.
     *
     * @param command    The original Command object that failed
     * @param throwable  The throwable that caused the failure
     *
     * @return <code>true</code> if the CommandManager should cease to process commands.
     */
    public boolean handleCommandFailure( final Command command, final Throwable throwable )
    {
        if ( command instanceof PrepareHandlerCommand )
        {
            PrepareHandlerCommand phc = (PrepareHandlerCommand)command;
            ComponentHandler handler = phc.getHandler();

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not prepare ComponentHandler for: "
                    + handler.getComponentClass().getName(), throwable );
            }
        }
        else
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Command failed: " + command, throwable );
            }
        }

        // This handler never requests that commands cease to be processed.
        return false;
    }
}

