/* 
 * Copyright 1999-2004 The Apache Software Foundation
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

package org.apache.excalibur.event.command;

/**
 * CommandFailureHandler is used by the CommandManager to handle any exceptions that might
 * be thrown by a Command.  That way the application using the CommandManager can properly
 * manage what happens when an exception is thrown.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS Revision: 1.1 $
 */
public interface CommandFailureHandler
{
    /**
     * Handle a command failure.  If a command throws an exception, it has failed.  The
     * CommandManager will call this method so that we can handle the problem effectively.
     *
     * @param command    The original Command object that failed
     * @param throwable  The throwable that caused the failure
     * @return <code>true</code> if the CommandManager should cease to process commands.
     */
    boolean handleCommandFailure(final Command command, final Throwable throwable);
}
