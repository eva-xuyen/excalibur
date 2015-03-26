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

package org.apache.avalon.fortress;

/**
 * Holds the events subscribers and allows the container to 
 * raise the events.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Team</a>
 */
public interface EventManager
{
    /**
     * Adds an event subscriber
     * 
     * @param listener ContainerListener implementation
     */
	void addListener( ContainerListener listener );
    
    /**
     * Removes an event subscriber
     * 
     * @param listener ContainerListener implementation
     */
    void removeListener( ContainerListener listener );

    /**
     * Raises the ComponentCreated event to susbcribers
     * 
     * @param entry Collection of information about the component
     * @param newInstance instance that has been created.
     * @return the instance itself or a wrapped one
     */
    Object fireComponentCreated( MetaInfoEntry entry, Object newInstance );

    /**
     * Raises the ComponentDestroyed event to susbcribers
     * 
     * @param entry Collection of information about the component
     * @param newInstance instance that has been released.
     */
    void fireComponentDestroyed( MetaInfoEntry entry, Object newInstance );
}
