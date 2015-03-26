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
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * This is a {@link ServiceSelector} implementation that can wrap around a legacy
 * {@link ComponentSelector} object effectively adapting a {@link ComponentSelector}
 * interface to a {@link ServiceSelector} interface.
 * <p>
 * This class implements the {@link Component} interface because it is used in
 * environments which expect all components to implement Component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: WrapperServiceSelector.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public class WrapperServiceSelector
    implements ServiceSelector
{
    /**
     * The Selector we are wrapping.
     */
    private final ComponentSelector m_selector;

    /**
     * The role that this selector was aquired via.
     */
    private final String m_key;

    /**
     * This constructor is a constructor for a ComponentServiceManager
     *
     * @param key the key used to aquire this selector
     * @param selector the selector to wrap
     */
    public WrapperServiceSelector( final String key,
                                   final ComponentSelector selector )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == selector )
        {
            throw new NullPointerException( "selector" );
        }

        m_key = key + "/";
        m_selector = selector;
    }

    /**
     * Select a service based on a policy.
     *
     * @param policy the policy
     * @return the service
     * @throws ServiceException if unable to select service
     */
    public Object select( final Object policy )
        throws ServiceException
    {
        try
        {
            return m_selector.select( policy );
        }
        catch( final ComponentException ce )
        {
            throw new ServiceException( m_key + policy, ce.getMessage(), ce );
        }
    }

    /**
     * Check to see if a {@link Object} exists relative to the supplied policy.
     *
     * @param policy a {@link Object} containing the selection criteria
     * @return True if the component is available, False if it not.
     */
    public boolean isSelectable( final Object policy )
    {
        return m_selector.hasComponent( policy );
    }

    /**
     * Return the {@link Object} when you are finished with it.  This
     * allows the {@link ServiceSelector} to handle the End-Of-Life Lifecycle
     * events associated with the {@link Object}.  Please note, that no
     * Exception should be thrown at this point.  This is to allow easy use of the
     * ServiceSelector system without having to trap Exceptions on a release.
     *
     * @param object The {@link Object} we are releasing.
     */
    public void release( Object object )
    {
        m_selector.release( (Component)object );
    }

    /**
     * The {@link WrapperServiceManager} wraps ComponentSelectors in
     *  WrapperServiceSelectors when they are looked up.  This method
     *  makes it possible to release the original component selector.
     *
     * @return The {@link ComponentSelector} being wrapped.
     */
    ComponentSelector getWrappedSelector()
    {
        return m_selector;
    }
}
