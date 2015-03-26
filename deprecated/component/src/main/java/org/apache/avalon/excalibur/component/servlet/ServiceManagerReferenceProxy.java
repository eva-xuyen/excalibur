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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Reference Proxy to a ServiceManager
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
final class ServiceManagerReferenceProxy
    extends AbstractReferenceProxy
    implements ServiceManager
{
    private ServiceManager m_serviceManager;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new proxy.
     *
     * @param serviceManager ServiceManager being proxied.
     * @param latch Latch wich will be notified when this proxy is finalized.
     * @param name Name of the proxy.
     */
    ServiceManagerReferenceProxy( ServiceManager serviceManager,
                                  AbstractReferenceProxyLatch latch,
                                  String name )
    {
        super( latch, name );
        m_serviceManager = serviceManager;
    }

    /*---------------------------------------------------------------
     * ServiceManager Methods
     *-------------------------------------------------------------*/
    /**
     * Get the <code>Object</code> associated with the given role.  For
     * instance, If the <code>ServiceManager</code> had a
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
     * @param role The role name of the <code>Object</code> to retrieve.
     * @return an <code>Object</code> value
     * @throws ServiceException if an error occurs
     */
    public Object lookup( String role )
        throws ServiceException
    {
        return m_serviceManager.lookup( role );
    }

    /**
     * Check to see if a <code>Object</code> exists for a role.
     *
     * @param role  a string identifying the role to check.
     * @return True if the object exists, False if it does not.
     */
    public boolean hasService( String role )
    {
        return m_serviceManager.hasService( role );
    }

    /**
     * Return the <code>Object</code> when you are finished with it.  This
     * allows the <code>ServiceManager</code> to handle the End-Of-Life Lifecycle
     * events associated with the <code>Object</code>.  Please note, that no
     * Exception should be thrown at this point.  This is to allow easy use of the
     * ServiceManager system without having to trap Exceptions on a release.
     *
     * @param object The <code>Object</code> we are releasing.
     */
    public void release( Object service )
    {
        m_serviceManager.release( service );
    }
}
