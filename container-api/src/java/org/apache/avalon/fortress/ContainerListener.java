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
 * Listener to allow hooks into Container
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Team</a>
 */
public interface ContainerListener
{
    /**
     * Signalizes that a component instance has been created.
     * The original or a wrapped instance must be returned by the implementation
     * 
     * @param entry Collection of information about the component
     * @param instance The created instance
     * @return the old or new instance
     */
	Object componentCreated( MetaInfoEntry entry, Object instance );
	
    /**
     * Signalizes that a component instance has been destroyed.
     * 
     * @param entry Collection of information about the component
     * @param instance The instance being destroyed
     */
	void componentDestroyed( MetaInfoEntry entry, Object instance );
}
