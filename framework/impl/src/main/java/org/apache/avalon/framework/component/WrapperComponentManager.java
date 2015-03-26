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
package org.apache.avalon.framework.component;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * This is a {@link ComponentManager} implementation that can wrap around a
 * {@link ServiceManager} object effectively adapting a {@link ServiceManager}
 * interface to a {@link ComponentManager} interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: WrapperComponentManager.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public class WrapperComponentManager
    implements ComponentManager
{
    /**
     * The service manager we are adapting.
     */
    private final ServiceManager m_manager;

   /**
    * Creation of a new wrapper component manger using a supplied
    * service manager as a source backing the wrapped.  This implementation
    * redirects lookup requests to the supplied service manager provided under
    * this constructor. No attempt is made to proxy object supplied by the
    * primary manager as Component instances - as such, it is the responsibility
    * of the application establishing the wrapper to ensure that objects
    * accessed via the primary manager implement the Component interface.
    *
    * @param manager the service manager backing the wrapper.
    */
    public WrapperComponentManager( final ServiceManager manager )
    {
        if( null == manager )
        {
            throw new NullPointerException( "manager" );
        }

        m_manager = manager;
    }

    /**
     * Retrieve a component via a key.
     *
     * @param key the key
     * @return the component
     * @throws ComponentException if unable to aquire component
     */
    public Component lookup( final String key )
        throws ComponentException
    {
        try
        {
            final Object object = m_manager.lookup( key );
            if( object instanceof ServiceSelector )
            {
                return new WrapperComponentSelector( key, (ServiceSelector)object );
            }
            else if( object instanceof Component )
            {
                return (Component)object;
            }
        }
        catch( final ServiceException se )
        {
            throw new ComponentException( se.getKey(), se.getMessage(), se.getCause() );
        }

        final String message = "Role does not implement the Component " +
            "interface and thus can not be accessed via ComponentManager";
        throw new ComponentException( key, message );
    }

    /**
     * Check to see if a <code>Component</code> exists for a key.
     *
     * @param key  a string identifying the key to check.
     * @return True if the component exists, False if it does not.
     */
    public boolean hasComponent( final String key )
    {
        return m_manager.hasService( key );
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
    public void release( final Component component )
    {
        if( component instanceof WrapperComponentSelector )
        {
            final WrapperComponentSelector selector = (WrapperComponentSelector)component;
            m_manager.release( selector.getWrappedSelector() );
        }
        else
        {
            m_manager.release( component );
        }
    }
}
