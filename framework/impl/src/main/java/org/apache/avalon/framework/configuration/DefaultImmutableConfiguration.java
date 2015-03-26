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
package org.apache.avalon.framework.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An immutable implementation of the <code>Configuration</code> interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultImmutableConfiguration.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class DefaultImmutableConfiguration
    extends AbstractConfiguration
    implements Serializable
{
    /**
     * An empty (length zero) array of configuration objects.
     */
    protected static final Configuration[] EMPTY_ARRAY = new Configuration[ 0 ];
    
    private final String m_name;
    private final String m_location;
    private final String m_namespace;
    private final String m_prefix;
    private final HashMap m_attributes;
    private final ArrayList m_children;
    private final String m_value;
    
    /**
     * Deep copy constructor.
     * 
     * @param config the <code>Configuration</code> to do a deep copy of.
     * @throws ConfigurationException if an error occurs when copying
     */
    public DefaultImmutableConfiguration( Configuration config ) throws ConfigurationException
    {
        m_name = config.getName();
        m_location = config.getLocation();
        m_namespace = config.getNamespace();
        m_prefix = (config instanceof AbstractConfiguration) ? ((AbstractConfiguration)config).getPrefix() : ""; 

        m_value = config.getValue( null );
        
        final String[] attributes = config.getAttributeNames();
        if( attributes.length > 0 )
        {
            m_attributes = new HashMap ();
            for( int i = 0; i < attributes.length; i++ )
            {
                final String name = attributes[ i ];
                final String value = config.getAttribute( name, null );
                m_attributes.put( name, value );
            }
        } 
        else
        {
             m_attributes = null;
        }
    
        Configuration[] children = config.getChildren();
        if( children.length > 0 )
        {
            m_children = new ArrayList ();
            for( int i = 0; i < children.length; i++ )
            {
                // Deep copy
                m_children.add( new DefaultImmutableConfiguration( children[i] ) );
            }
        } 
        else
        {
            m_children = null;
        }
    }
    
    /**
     * Returns the name of this configuration element.
     * @return a <code>String</code> value
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Returns the namespace of this configuration element
     * @return a <code>String</code> value
     * @throws ConfigurationException if an error occurs
     * @since 4.1
     */
    public String getNamespace() throws ConfigurationException
    {
        if( null != m_namespace )
        {
            return m_namespace;
        }
        else
        {
            throw new ConfigurationException
                ( "No namespace (not even default \"\") is associated with the "
                + "configuration element \"" + getName()
                + "\" at " + getLocation() );
        }
    }
    
    /**
     * Returns the prefix of the namespace
     * @return a <code>String</code> value
     * @throws ConfigurationException if prefix is not present (<code>null</code>).
     * @since 4.1
     */
    protected String getPrefix() throws ConfigurationException
    {
        if( null != m_prefix )
        {
            return m_prefix;
        }
        else
        {
            throw new ConfigurationException
                ( "No prefix (not even default \"\") is associated with the "
                + "configuration element \"" + getName()
                + "\" at " + getLocation() );
        }
        
    }
    
    /**
     * Returns a description of location of element.
     * @return a <code>String</code> value
     */
    public String getLocation()
    {
        return m_location;
    }
    
    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @param defaultValue the default value to return if value malformed or empty
     * @return a <code>String</code> value
     */
    public String getValue( final String defaultValue )
    {
        if( null != m_value )
        {
            return m_value;
        }
        else
        {
            return defaultValue;
        }
    }
    
    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @return a <code>String</code> value
     * @throws ConfigurationException If the value is not present.
     */
    public String getValue() throws ConfigurationException
    {
        if( null != m_value )
        {
            return m_value;
        }
        else
        {
            throw new ConfigurationException( "No value is associated with the "
                + "configuration element \"" + getName()
                + "\" at " + getLocation() );
        }
    }
    
    /**
     * Return an array of all attribute names.
     * @return a <code>String[]</code> value
     */
    public String[] getAttributeNames()
    {
        if( null == m_attributes )
        {
            return new String[ 0 ];
        }
        else
        {
            return (String[])m_attributes.keySet().toArray( new String[ 0 ] );
        }
    }
    
    /**
     * Return an array of <code>Configuration</code>
     * elements containing all node children.
     *
     * @return The child nodes with name
     */
    public Configuration[] getChildren()
    {
        if( null == m_children )
        {
            return new Configuration[ 0 ];
        }
        else
        {
            return (Configuration[])m_children.toArray( new Configuration[ 0 ] );
        }
    }
    
    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     * @throws ConfigurationException If the attribute is not present.
     */
    public String getAttribute( final String name )
        throws ConfigurationException
    {
        final String value =
            ( null != m_attributes ) ? (String)m_attributes.get( name ) : null;
        
        if( null != value )
        {
            return value;
        }
        else
        {
            throw new ConfigurationException(
                "No attribute named \"" + name + "\" is "
                + "associated with the configuration element \""
                + getName() + "\" at " + getLocation() );
        }
    }
    
    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name.
     * @param name a <code>String</code> value
     * @param createNew a <code>boolean</code> value
     * @return a <code>Configuration</code> value
     */
    public Configuration getChild( final String name, final boolean createNew )
    {
        if( null != m_children )
        {
            final int size = m_children.size();
            for( int i = 0; i < size; i++ )
            {
                final Configuration configuration = (Configuration)m_children.get( i );
                if( name.equals( configuration.getName() ) )
                {
                    return configuration;
                }
            }
        }
        
        if( createNew )
        {
            return new DefaultConfiguration( name, "<generated>" + getLocation(), m_namespace, m_prefix );
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Return an array of <code>Configuration</code> objects
     * children of this associated with the given name.
     * <br>
     * The returned array may be empty but is never <code>null</code>.
     *
     * @param name The name of the required children <code>Configuration</code>.
     * @return a <code>Configuration[]</code> value
     */
    public Configuration[] getChildren( final String name )
    {
        if( null == m_children )
        {
            return new Configuration[ 0 ];
        }
        else
        {
            final ArrayList children = new ArrayList();
            final int size = m_children.size();
            
            for( int i = 0; i < size; i++ )
            {
                final Configuration configuration = (Configuration)m_children.get( i );
                if( name.equals( configuration.getName() ) )
                {
                    children.add( configuration );
                }
            }
            
            return (Configuration[])children.toArray( new Configuration[ 0 ] );
        }
    }
    
    /**
     * Return count of children.
     * @return an <code>int</code> value
     */
    public int getChildCount()
    {
        if( null == m_children )
        {
            return 0;
        }
        
        return m_children.size();
    }
    
    /**
     * Compare if this configuration is equal to another.
     *
     * @param other  The other configuration
     * @return <code>true</code> if they are the same.
     */
    public boolean equals( Object other )
    {
        if( other == null ) return false;
        if( !( other instanceof Configuration ) ) return false;
        return ConfigurationUtil.equals( this, (Configuration) other );
    }
    
    /**
     * Obtaine the hashcode for this configuration.
     *
     * @return the hashcode.
     */
    public int hashCode()
    {
        int hash = m_prefix.hashCode();
        if( m_name != null ) hash ^= m_name.hashCode();
        hash >>>= 7;
        if( m_location != null ) hash ^= m_location.hashCode();
        hash >>>= 7;
        if( m_namespace != null ) hash ^= m_namespace.hashCode();
        hash >>>= 7;
        if( m_attributes != null ) hash ^= m_attributes.hashCode();
        hash >>>= 7;
        if( m_children != null ) hash ^= m_children.hashCode();
        hash >>>= 7;
        if( m_value != null ) hash ^= m_value.hashCode();
        hash >>>= 7;
        return hash;
    }
}
