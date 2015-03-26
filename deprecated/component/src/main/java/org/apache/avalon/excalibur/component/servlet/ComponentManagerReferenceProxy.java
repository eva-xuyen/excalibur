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
package org.apache.avalon.excalibur.component.servlet;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;

/**
 * Reference Proxy to a ComponentManager
 *
 * @deprecated The ComponentManager interface has been deprecated in favor
 *             of the ServiceManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
final class ComponentManagerReferenceProxy
    extends AbstractReferenceProxy
    implements ComponentManager
{
    private ComponentManager m_componentManager;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new proxy.
     *
     * @param componentManager ComponentManager being proxied.
     * @param latch Latch wich will be notified when this proxy is finalized.
     * @param name Name of the proxy.
     */
    ComponentManagerReferenceProxy( ComponentManager componentManager,
                                    AbstractReferenceProxyLatch latch,
                                    String name )
    {
        super( latch, name );
        m_componentManager = componentManager;
    }
    
    /*---------------------------------------------------------------
     * ComponentManager Methods
     *-------------------------------------------------------------*/
    /**
     * Get the <code>Component</code> associated with the given role.  For
     * instance, If the <code>ComponentManager</code> had a
     * <code>LoggerComponent</code> stored and referenced by role, I would use
     * the following call:
     * <pre>
     * try
     * {
     *     MyComponent log;
     *     myComponent = (MyComponent) manager.lookup(MyComponent.ROLE);
     * }
     * catch (...)
     * {
     *     ...
     * }
     * </pre>
     *
     * @param role The role name of the <code>Component</code> to retrieve.
     * @return the desired component
     * @throws ComponentException if an error occurs
     */
    public Component lookup( String role )
        throws ComponentException
    {
        return m_componentManager.lookup( role );
    }

    /**
     * Check to see if a <code>Component</code> exists for a role.
     *
     * @param role  a string identifying the role to check.
     * @return True if the component exists, False if it does not.
     */
    public boolean hasComponent( String role )
    {
        return m_componentManager.hasComponent( role );
    }

    /**
     * Return the <code>Component</code> when you are finished with it.  This
     * allows the <code>ComponentManager</code> to handle the End-Of-Life Lifecycle
     * events associated with the Component.  Please note, that no Exceptions
     * should be thrown at this point.  This is to allow easy use of the
     * ComponentManager system without having to trap Exceptions on a release.
     *
     * @param component The Component we are releasing.
     */
    public void release( Component component )
    {
        m_componentManager.release( component );
    }
}
