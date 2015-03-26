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

import org.apache.excalibur.event.EventHandler;
import org.apache.excalibur.event.Source;

/**
 * An EventPipeline is used by the ThreadManager to manage the event Queue
 * and EventHandler relationship.  The ThreadManager manages the automatic
 * forwarding of the Events from the queue to the Event Handler.
 *
 * <p>
 *   The interface design is heavily influenced by
 *   <a href="mailto:mdw@cs.berkeley.edu">Matt Welsh</a>'s SandStorm server,
 *   his demonstration of the SEDA architecture.  We have deviated where we
 *   felt the design differences where better.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface EventPipeline
{
    /**
     * There can be many different sources to merge into a pipeline.  For the
     * CommandManager, there is only one sink.
     *
     * @return the array of sources that feed the handler
     */
    Source[] getSources();

    /**
     * Returns the reference to the EventHandler that the events from all the
     * Sinks get merged into.
     *
     * @return the handler for the pipeline
     */
    EventHandler getEventHandler();
}
