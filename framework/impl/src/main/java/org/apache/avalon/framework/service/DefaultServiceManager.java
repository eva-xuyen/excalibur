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
import java.util.Iterator;
import java.util.Map;

/**
 * This class is a static implementation of a <code>ServiceManager</code>. Allow ineritance
 * and extension so you can generate a tree of <code>ServiceManager</code> each defining
 * Object scope.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultServiceManager.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class DefaultServiceManager
    implements ServiceManager
{
    private final HashMap m_objects = new HashMap();
    private final ServiceManager m_parent;
    private boolean m_readOnly;

    /**
     * Construct <code>ServiceManager</code> with no parent.
     *
     */
    public DefaultServiceManager()
    {
        this( null );
    }

    /**
     * Construct <code>ServiceManager</code> with specified parent.
     *
     * @param parent this <code>ServiceManager</code>'s parent
     */
    public DefaultServiceManager( final ServiceManager parent )
    {
        m_parent = parent;
    }

    /**
     * Retrieve <code>Object</code> by key from <code>ServiceManager</code>.
     *
     * @param key the key
     * @return the <code>Object</code>
     * @throws ServiceException if an error occurs
     */
    public Object lookup( final String key )
        throws ServiceException
    {
        final Object object = m_objects.get( key );
        if( null != object )
        {
            return object;
        }
        else if( null != m_parent )
        {
            return m_parent.lookup( key );
        }
        else
        {
            final String message = "Unable to provide implementation for " + key;
            throw new ServiceException( key, message, null );
        }
    }

    /**
     * Check to see if a <code>Object</code> exists for a key.
     *
     * @param key  a string identifying the key to check.
     * @return True if the object exists, False if it does not.
     */
    public boolean hasService( final String key )
    {
        try
        {
            lookup( key );
            return true;
        }
        catch( final Throwable t )
        {
            return false;
        }
    }

    /**
     * Place <code>Object</code> into <code>ServiceManager</code>.
     *
     * @param key the object's key
     * @param object an <code>Object</code> value
     */
    public void put( final String key, final Object object )
    {
        checkWriteable();
        m_objects.put( key, object );
    }

    /**
     * Build a human readable representation of this
     * <code>ServiceManager</code>.
     *
     * @return the description of this <code>ServiceManager</code>
     */
    public String toString()
    {
        final StringBuffer buffer = new StringBuffer();
        final Iterator objects = m_objects.keySet().iterator();
        buffer.append( "Services:" );

        while( objects.hasNext() )
        {
            buffer.append( "[" );
            buffer.append( objects.next() );
            buffer.append( "]" );
        }

        return buffer.toString();
    }

    /**
     * Helper method for subclasses to retrieve parent.
     *
     * @return the parent <code>ServiceManager</code>
     */
    protected final ServiceManager getParent()
    {
        return m_parent;
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
     * Makes this <code>ServiceManager</code> read-only.
     *
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Checks if this <code>ServiceManager</code> is writeable.
     *
     * @throws IllegalStateException if this <code>ServiceManager</code> is
     * read-only
     */
    protected final void checkWriteable()
        throws IllegalStateException
    {
        if( m_readOnly )
        {
            final String message =
                "ServiceManager is read only and can not be modified";
            throw new IllegalStateException( message );
        }
    }

    /**
     * Release the <code>Object</code>.
     * @param object The <code>Object</code> to release.
     */
    public void release( Object object )
    {
    }
}
