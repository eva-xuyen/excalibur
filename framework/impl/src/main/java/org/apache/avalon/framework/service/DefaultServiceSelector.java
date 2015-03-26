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

import java.util.HashMap;
import java.util.Map;

/**
 * This is the default implementation of the ServiceSelector
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultServiceSelector.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class DefaultServiceSelector
    implements ServiceSelector
{
    private final HashMap m_objects = new HashMap();
    private boolean m_readOnly;
    private final String m_role;
    
    /**
     * Create a DefaultServiceSelector with a default empty role.
     */
    public DefaultServiceSelector()
    {
        this("");
    }
    
    /**
     * Create a DefaultServiceSelector with a role for debug purposes.
     * 
     * @param role  The role for this selector.
     * 
     * @throws NullPointerException if the role is null.
     */
    public DefaultServiceSelector(String role)
    {
        if ( null==role )
        {
            throw new NullPointerException(role);
        }
        
        m_role = role;
    }

    /**
     * Select the desired object.
     *
     * @param hint the hint to retrieve Object
     * @return the Object
     * @throws ServiceException if an error occurs
     */
    public Object select( Object hint )
        throws ServiceException
    {
        final Object object = m_objects.get( hint );

        if( null != object )
        {
            return object;
        }
        else
        {
            throw new ServiceException( m_role + "/" + hint.toString(), "Unable to provide implementation" );
        }
    }

    /**
     * Returns whether a Object exists or not
     * @param hint the hint to retrieve Object
     * @return <code>true</code> if the Object exists
     */
    public boolean isSelectable( final Object hint )
    {
        boolean objectExists = false;

        try
        {
            this.release( this.select( hint ) );
            objectExists = true;
        }
        catch( Throwable t )
        {
            // Ignore all throwables--we want a yes or no answer.
        }

        return objectExists;
    }

    /**
     * Release object.
     *
     * @param object the <code>Object</code> to release
     */
    public void release( final Object object )
    {
        // if the ServiceManager handled pooling, it would be
        // returned to the pool here.
    }

    /**
     * Populate the ServiceSelector.
     * @param hint the hint to be used to retrieve the Object later
     * @param object the Object to hold
     */
    public void put( final Object hint, final Object object )
    {
        checkWriteable();
        m_objects.put( hint, object );
    }

    /**
     * Helper method for subclasses to retrieve object map.
     *
     * @return the object map
     */
    protected final Map getObjectMap()
    {
        return m_objects;
    }

    /**
     * Makes this service selector read-only.
     *
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Checks if this service selector is writeable.
     *
     * @throws IllegalStateException if this service selector is read-only
     */
    protected final void checkWriteable()
        throws IllegalStateException
    {
        if( m_readOnly )
        {
            throw new IllegalStateException
                ( "ServiceSelector is read only and can not be modified" );
        }
    }
}
