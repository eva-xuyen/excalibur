/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.fortress.ContainerListener;
import org.apache.avalon.fortress.EventManager;
import org.apache.avalon.fortress.MetaInfoEntry;

/**
 * Default implementation of EventManager
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class DefaultEventManager implements EventManager
{
    /**
     * Holds the subscribers 
     */
    private Set m_listeners = Collections.synchronizedSet( new HashSet() );
    
    /**
     * Adds an event subscriber
     * 
     * @param listener ContainerListener implementation
     */
    public void addListener(ContainerListener listener)
    {
        m_listeners.add( listener );
    }

    /**
     * Removes an event subscriber
     * 
     * @param listener ContainerListener implementation
     */
    public void removeListener(ContainerListener listener)
    {
        m_listeners.remove( listener );
    }

    /**
     * Raises the ComponentCreated event to susbcribers
     * 
     * @param entry Collection of information about the component
     * @param newInstance instance that has been created.
     * @return the instance itself or a wrapped one
     */
    public Object fireComponentCreated(MetaInfoEntry entry, Object newInstance)
    {
        Object wrapperInstance = newInstance;
        
        for (Iterator iter = m_listeners.iterator(); iter.hasNext();)
        {
            ContainerListener listener = (ContainerListener) iter.next();
            wrapperInstance = listener.componentCreated( entry, wrapperInstance );
        }
        
        return wrapperInstance;
    }

    /**
     * Raises the ComponentDestroyed event to susbcribers
     * 
     * @param entry Collection of information about the component
     * @param newInstance instance that has been released.
     */
    public void fireComponentDestroyed(MetaInfoEntry entry, Object newInstance)
    {
        for (Iterator iter = m_listeners.iterator(); iter.hasNext();)
        {
            ContainerListener listener = (ContainerListener) iter.next();
            listener.componentDestroyed( entry, newInstance );
        }
    }
}
