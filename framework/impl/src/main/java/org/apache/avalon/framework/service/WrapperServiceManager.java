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
package org.apache.avalon.framework.service;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * This is a {@link ServiceManager} implementation that can wrap around a legacy
 * {@link ComponentManager} object effectively adapting a {@link ComponentManager}
 * interface to a {@link ServiceManager} interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: WrapperServiceManager.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public class WrapperServiceManager
    implements ServiceManager
{
    /**
     * The component manager thaty this class wraps.
     */
    private final ComponentManager m_componentManager;

    /**
     * This constructor is a constructor for a WrapperServiceManager.
     *
     * @param componentManager the ComponentManager instance that is being wrapped
     */
    public WrapperServiceManager( final ComponentManager componentManager )
    {
        if( null == componentManager )
        {
            throw new NullPointerException( "componentManager" );
        }

        m_componentManager = componentManager;
    }

    /**
     * Retrieve a service using specified key.
     *
     * @param key the key to use to lookup component
     * @return the matching service
     * @throws ServiceException if unable to provide the service
     * @see ServiceManager#lookup
     */
    public Object lookup( final String key )
        throws ServiceException
    {
        try
        {
            final Object service = m_componentManager.lookup( key );
            if( service instanceof ComponentSelector )
            {
                return new WrapperServiceSelector( key, (ComponentSelector)service );
            }
            else
            {
                return service;
            }
        }
        catch( final ComponentException ce )
        {
            throw new ServiceException( key, ce.getMessage(), ce );
        }
    }

    /**
     * Return true if the component is available in ServiceManager.
     *
     * @param key the lookup
     * @return true if the component is available in ServiceManager
     */
    public boolean hasService( final String key )
    {
        return m_componentManager.hasComponent( key );
    }

    /**
     * Release the service back to the ServiceManager.
     *
     * @param service the service
     */
    public void release( final Object service )
    {
        if ( service instanceof WrapperServiceSelector )
        {
            m_componentManager.
                release( ((WrapperServiceSelector)service).getWrappedSelector() );
        }
        else
        {
            m_componentManager.release( (Component)service );
        }
    }
}
