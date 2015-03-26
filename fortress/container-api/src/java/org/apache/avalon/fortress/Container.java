/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.service.ServiceException;

/**
 * The Container is an interface used to mark the Containers in your system.
 *
 * <p>It is used to functionally identify Containers. It's primary use is to
 * assist Container developers to obtain the desired object from the Context.
 * Most applications will not, or barely, refer to implementations of this
 * class; rather they will interact wit a {@link ContainerManager ContainerManager}
 * implementation. All communication from the ContainerManager to the Container is
 * through the {@link Context Context} object.</p>
 *
 * <p>While plans exist to extend the Container interface to expose more of the
 * Container's internals, we currently feel that we have insufficient use case
 * information to determine the generic form of the container internals.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/02/28 15:16:24 $
 * @see ContainerConstants for the contract surrounding the Container context
 * @see <a href="http://avalon.apache.org/framework/guide-cop-in-avalon.html">COP In Avalon</a>
 */
public interface Container
{
    /**
     * Work interface identifier.
     *
     * @since 1.1-dev
     */
    String ROLE = Container.class.getName();

    /**
     * This is the method that the ContainerComponentManager and Selector use
     * to gain access to the ComponentHandlers and ComponentSelectors.  The
     * actual access of the ComponentHandler is delegated to the Container.
     *
     * @param  key  The role we intend to access a Component for.
     * @param  hint  The hint that we use as a qualifier
     *         (note: if null, the default implementation is returned).
     *
     * @return Object  a reference to the ComponentHandler or
     *                 ComponentSelector for the role/hint combo.
     *
     * @throws ServiceException if the container cannot get the component
     */
    Object get( String key, Object hint ) throws ServiceException;

    /**
     * This is the method that the ContainerComponentManager and Selector use
     * to gain access to the ComponentHandlers and ComponentSelectors.  The
     * actual access of the ComponentHandler is delegated to the Container.
     *
     * @param  key  The role we intend to access a Component for.
     * @param  hint  The hint that we use as a qualifier
     *         (note: if null, the default implementation is returned).
     *
     * @return true  if a reference to the role exists.
     */
    boolean has( String key, Object hint );
}

