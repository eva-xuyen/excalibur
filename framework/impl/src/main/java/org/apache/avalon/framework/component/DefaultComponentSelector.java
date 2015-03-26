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
import java.util.Map;

/**
 * This is the default implementation of the ComponentSelector.
 *
 * <p>
 *  <span style="color: red">Deprecated: </span><i>
 *    Use {@link org.apache.avalon.framework.service.DefaultServiceSelector} instead.
 *  </i>
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultComponentSelector.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class DefaultComponentSelector
    implements ComponentSelector
{
    private final HashMap m_components = new HashMap();
    private boolean m_readOnly;

    /**
     * Select the desired component.  It does not cascade, neither
     * should it.
     *
     * @param hint the hint to retrieve Component
     * @return the Component
     * @throws ComponentException if an error occurs
     */
    public Component select( Object hint )
        throws ComponentException
    {
        final Component component = (Component)m_components.get( hint );

        if( null != component )
        {
            return component;
        }
        else
        {
            throw new ComponentException( hint.toString(), "Unable to provide implementation." );
        }
    }

    /**
     * Returns whether a Component exists or not
     * @param hint the hint to retrieve Component
     * @return <code>true</code> if the Component exists
     */
    public boolean hasComponent( final Object hint )
    {
        boolean componentExists = false;

        try
        {
            this.release( this.select( hint ) );
            componentExists = true;
        }
        catch( Throwable t )
        {
            // Ignore all throwables--we want a yes or no answer.
        }

        return componentExists;
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
     * Populate the ComponentSelector.
     * @param hint the hint to retrieve Component
     * @param component the component to add
     */
    public void put( final Object hint, final Component component )
    {
        checkWriteable();
        m_components.put( hint, component );
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
     * Make this component selector read-only.
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
            throw new IllegalStateException
                ( "ComponentSelector is read only and can not be modified" );
        }
    }
}
