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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is a static implementation of a ComponentManager. Allow ineritance
 * and extension so you can generate a tree of ComponentManager each defining
 * Component scope.
 *
 * <p>
 *  <span style="color: red">Deprecated: </span><i>
 *    Use {@link org.apache.avalon.framework.service.DefaultServiceManager} instead.
 *  </i>
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultComponentManager.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class DefaultComponentManager
    implements ComponentManager
{
    private final HashMap m_components = new HashMap();
    private final ComponentManager m_parent;
    private boolean m_readOnly;

    /**
     * Construct ComponentManager with no parent.
     *
     */
    public DefaultComponentManager()
    {
        this( null );
    }

    /**
     * Construct ComponentManager with specified parent.
     *
     * @param parent the ComponentManagers parent
     */
    public DefaultComponentManager( final ComponentManager parent )
    {
        m_parent = parent;
    }

    /**
     * Retrieve Component by key from ComponentManager.
     *
     * @param key the key
     * @return the Component
     * @throws ComponentException if an error occurs
     */
    public Component lookup( final String key )
        throws ComponentException
    {
        final Component component = (Component)m_components.get( key );

        if( null != component )
        {
            return component;
        }
        else if( null != m_parent )
        {
            return m_parent.lookup( key );
        }
        else
        {
            throw new ComponentException( key, "Unable to provide implementation." );
        }
    }

    /**
     * Returns <code>true</code> if the component m_manager is managing a component
     * with the specified key, <code>false</code> otherwise.
     *
     * @param key key of the component you are lokking for
     * @return <code>true</code> if the component m_manager has a component with that key
     */
    public boolean hasComponent( final String key )
    {
        boolean componentExists = false;

        try
        {
            this.release( this.lookup( key ) );
            componentExists = true;
        }
        catch( Throwable t )
        {
            // Ignore all throwables--we want a yes or no answer.
        }

        return componentExists;
    }

    /**
     * Place Component into ComponentManager.
     *
     * @param key the components key
     * @param component the component
     */
    public void put( final String key, final Component component )
    {
        checkWriteable();
        m_components.put( key, component );
    }

    /**
     * Release component.
     *
     * @param component the component
     */
    public void release( final Component component )
    {
        // if the ComponentManager handled pooling, it would be
        // returned to the pool here.
    }

    /**
     * Build a human readable representation of ComponentManager.
     *
     * @return the description of ComponentManager
     */
    public String toString()
    {
        final StringBuffer buffer = new StringBuffer();
        final Iterator components = m_components.keySet().iterator();
        buffer.append( "Components:" );

        while( components.hasNext() )
        {
            buffer.append( "[" );
            buffer.append( components.next() );
            buffer.append( "]" );
        }

        return buffer.toString();
    }

    /**
     * Helper method for subclasses to retrieve parent.
     *
     * @return the parent ComponentManager
     */
    protected final ComponentManager getParent()
    {
        return m_parent;
    }

    /**
     * Helper method for subclasses to retrieve component map.
     *
     * @return the component map
     */
    protected final Map getComponentMap()
    {
        return m_components;
    }

    /**
     * Make this component m_manager read only.
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Check if this component m_manager is writeable.
     *
     * @throws IllegalStateException if this component m_manager is read-only
     */
    protected final void checkWriteable()
        throws IllegalStateException
    {
        if( m_readOnly )
        {
            final String message =
                "ComponentManager is read only and can not be modified";
            throw new IllegalStateException( message );
        }
    }
}
