/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.monitor;

import org.apache.avalon.framework.component.Component;

/**
 * The Monitor is used to actively check a set of resources to see if they have
 * changed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Monitor.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public interface Monitor
    extends Component
{
    String ROLE = Monitor.class.getName();

    /**
     * Add a resource to monitor.  The resource key referenced in the other
     * interfaces is derived from the resource object.
     */
    void addResource( Resource resource );

    /**
     * Find a monitored resource.  If no resource is available, return null
     */
    Resource getResource( String key );

    /**
     * Remove a monitored resource by key.
     */
    void removeResource( String key );

    /**
     * Remove a monitored resource by reference.
     */
    void removeResource( Resource resource );
}
