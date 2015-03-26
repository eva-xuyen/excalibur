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
 * A Signal is a specific type of QueueElement that denotes a Control code
 * for the Queue system.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface RepeatedCommand extends DelayedCommand
{
    /**
     * If the value is less than 1 (0 or negative), the command repeats for
     * as long as the CommandManager is running.  If the value is above 0,
     * the Command repeats only for that specific amount of times before it
     * is removed from the system.
     *
     * @return the number of times the command repeats
     */
    int getNumberOfRepeats();

    /**
     * Gets the repeat interval so that the CommandQueue keeps it for the
     * specified amount of time before enqueuing it again.  This value must
     * not be negative.
     *
     * @return the number of milliseconds between each repeat
     */
    long getRepeatInterval();
}
