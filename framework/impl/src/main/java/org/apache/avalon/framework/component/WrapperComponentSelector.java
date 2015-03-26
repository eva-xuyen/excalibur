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

import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.ServiceException;

/**
 * This is a {@link ServiceSelector} implementation that can wrap around a legacy
 * {@link ComponentSelector} object effectively adapting a {@link ComponentSelector}
 * interface to a {@link ServiceSelector} interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: WrapperComponentSelector.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public class WrapperComponentSelector
    implements ComponentSelector
{
    /**
     * The Selector we are wrapping.
     */
    private final ServiceSelector m_selector;

    /**
     * The role that this selector was aquired via.
     */
    private final String m_key;

    /**
     * This constructor is a constructor for a WrapperComponentSelector.
     *
     * @param key the key used to aquire this selector
     * @param selector the selector to wrap
     */
    public WrapperComponentSelector( final String key,
                                     final ServiceSelector selector )
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
     * Select a Component based on a policy.
     *
     * @param policy the policy
     * @return the Component
     * @throws ComponentException if unable to select service
     */
    public Component select( final Object policy )
        throws ComponentException
    {
        try
        {
            final Object object = m_selector.select( policy );
            if( object instanceof Component )
            {
                return (Component)object;
            }
        }
        catch( final ServiceException se )
        {
            throw new ComponentException( m_key + policy, se.getMessage(), se );
        }

        final String message = "Role does not implement the Component " 
           + "interface and thus can not be accessed via ComponentSelector";
        throw new ComponentException( m_key + policy, message );
    }

    /**
     * Check to see if a {@link Component} exists relative to the supplied policy.
     *
     * @param policy a {@link Object} containing the selection criteria
     * @return True if the component is available, False if it not.
     */
    public boolean hasComponent( final Object policy )
    {
        return m_selector.isSelectable( policy );
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
    public void release( final Component object )
    {
        m_selector.release( object );
    }

    /**
     * The {@link WrapperComponentManager} wraps ServiceSelectors in
     *  WrapperServiceSelectors when they are looked up.  This method
     *  makes it possible to release the original component selector.
     *
     * @return The {@link ServiceSelector} being wrapped.
     */
    ServiceSelector getWrappedSelector()
    {
        return m_selector;
    }
}
