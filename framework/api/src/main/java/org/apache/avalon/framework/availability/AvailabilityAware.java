/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.availability;


/** An Availability contract between the container and the component.
 *
 * <p>A component that implements AvailabilityAware, is capable to be notified
 * that components that it has previously looked up via the 
 * ServiceManager.lookup() method are made available and non-available, 
 * and can gracefully handle unavailability.</p>
 *
 * <p>Containers are only required to support this interface, if the
 * container supports dynamic loading/unloading of components, hot-deploys,
 * remote components and similar cases where components may disappear for
 * a shorter or longer period of time.</p>
 *
 * @since 4.5
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public interface AvailabilityAware
{
    /** Notification that component is available.
     * <p>The component that previously has been looked up in the 
     * ServiceManager, using <i>key</i> is now available. This method can 
     * either be called after the componentUnavailable() method signalled that 
     * a component is no longer available, OR if the ServiceManager.lookup() 
     * returned a null (optional lookup) indicating that the requested 
     * component didn't exist.</p>
     * <p>
     *   This method will NOT be called with a <i>key</i> argument that has
     *   not been used in a ServiceManager.lookup() previously.
     * </p>
     *
     * @param key The key that was used to lookup the component in the 
     *            ServiceManager earlier.
     */
    void componentAvailable( String key );
    
    /** Notification that component is no longer available.
     * <p>The component that previously has been looked up in the 
     * ServiceManager, using <i>key</i> is no longer available. The container 
     * does not place any restrictions on how long the component will be 
     * unavailble, and the AvailabilityAware component, must employ one or more
     * strategies on how to gracefully handle short and long absences of 
     * dependent components. </p>
     * <p>
     *   This method will NOT be called with a <i>key</i> argument that has
     *   not been used in a ServiceManager.lookup() previously.
     * </p>
     *
     * @param key The key that was used to lookup the component in the 
     *            ServiceManager earlier.
     */
    void componentUnavailable( String key );
}
