/* 
 * Copyright 2003-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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

package org.apache.avalon.fortress;

import java.util.*;

/**
 * Keeps track of the relationship of all the associated meta data for a
 * component type.  It records all the roles, short name, component class, and
 * the handler class used to manage it.  The short name is included strictly
 * to enable "self-healing" configuration files.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.10 $ $Date: 2004/02/28 15:16:24 $
 */
public final class MetaInfoEntry
{
    public static final String THREADSAFE_HANDLER = "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler";
    public static final String POOLABLE_HANDLER = "org.apache.avalon.fortress.impl.handler.PoolableComponentHandler";
    public static final String FACTORY_HANDLER = "org.apache.avalon.fortress.impl.handler.FactoryComponentHandler";
    public static final String PER_THREAD_HANDLER = "org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler";

    private final Class m_klass;
    private final String m_configName;
    private final Class m_handler;
    private final Set m_roles;
    private volatile boolean m_readOnly = false;

    /** Translate from lifestyle to component handler. */
    private static final Map m_lifecycleMap;
    private final List m_dependencies;
    private static final String TYPE_SINGLETON = "singleton";
    private static final String TYPE_THREAD = "thread";
    private static final String TYPE_POOLED = "pooled";
    private static final String TYPE_TRANSIENT = "transient";

    // Initialize the scope map
    static
    {
        Map lifecycleMap = new HashMap();
        lifecycleMap.put( TYPE_SINGLETON, THREADSAFE_HANDLER );
        lifecycleMap.put( TYPE_THREAD, PER_THREAD_HANDLER );
        lifecycleMap.put( TYPE_POOLED, POOLABLE_HANDLER );
        lifecycleMap.put( TYPE_TRANSIENT, FACTORY_HANDLER );

        m_lifecycleMap = Collections.unmodifiableMap( lifecycleMap );
    }

    /**
     * Create a MetaInfoEntry from the supplied component class, and the
     * supplied meta information.
     *
     * @param componentClass  The <code>Class</code> for the component type
     * @param properties      The <code>Properties</code> object for meta info
     *
     * @throws ClassNotFoundException if the component handler class could not be found
     */
    public MetaInfoEntry( final Class componentClass, final Properties properties, final List deps ) throws ClassNotFoundException
    {
        if ( null == componentClass ) throw new NullPointerException( "\"componentClass\" cannot be null." );
        if ( null == properties ) throw new NullPointerException( "\"properties\" cannot be null." );
        if ( null == deps ) throw new NullPointerException( "\"deps\" cannot be null." );

        m_klass = componentClass;
        m_configName = properties.getProperty( "x-avalon.name", createShortName( componentClass.getName() ) );
        m_handler = Thread.currentThread().getContextClassLoader().loadClass( getHandler( properties ) );
        m_roles = new HashSet();
        m_dependencies = deps;
    }

    /**
     * Create a MetaInfoEntry from the supplied <code>RoleEntry</code>.
     *
     * @param roleEntry  The <code>RoleEntry</code> to convert
     */
    public MetaInfoEntry( final RoleEntry roleEntry )
    {
        if ( null == roleEntry ) throw new NullPointerException( "\"roleEntry\" cannot be null." );

        m_klass = roleEntry.getComponentClass();
        m_configName = roleEntry.getShortname();
        m_handler = roleEntry.getHandlerClass();
        m_roles = new HashSet();
        m_roles.add( roleEntry.getRole() );
        m_dependencies = new ArrayList();
        makeReadOnly();
    }

    /**
     * Make the component entry read only, so no more services can be added.
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Get the <code>Class</code> for the component type.
     *
     * @return the <code>Class</code>
     */
    public Class getComponentClass()
    {
        return m_klass;
    }

    /**
     * Get the <code>Class</code> for the component type's
     * {@link org.apache.avalon.fortress.impl.handler.ComponentHandler}.
     *
     * @return the <code>Class</code>
     */
    public Class getHandlerClass()
    {
        return m_handler;
    }

    /**
     * Get the configuration name for the component type.  This is used in
     * "self-healing" configuration files.
     *
     * @return the config name
     */
    public String getConfigurationName()
    {
        return m_configName;
    }

    /**
     * Add a service/role for the component entry.
     *
     * @param role  The new role
     *
     * @throws SecurityException if this MetaInfoEntry is read-only
     */
    public void addRole( final String role )
    {
        if ( null == role ) throw new NullPointerException( "\"role\" cannot be null" );
        if ( m_readOnly ) throw new SecurityException( "This MetaInfoEntry is read-only." );

        m_roles.add( role );
    }

    /**
     * Tests to see if a component exposes a role.
     *
     * @param role  The role to check
     * @return <code>true</code> if it does
     */
    public boolean containsRole( final String role )
    {
        if ( null == role ) throw new NullPointerException( "\"role\" cannot be null" );
        return m_roles.contains( role );
    }

    /**
     * Get an iterator for all the roles.
     *
     * @return the iterator
     */
    public Iterator getRoles()
    {
        return m_roles.iterator();
    }

    /**
     * Get a reference to the dependencies list.
     *
     * @return the dependency list
     */
    public List getDependencies()
    {
        return m_dependencies;
    }

    /**
     * Get the name of the requested component handler.
     *
     * @param meta  The properties object from the constructor
     * @return String name of the component handler
     */
    private String getHandler( final Properties meta )
    {
        final String lifecycle = meta.getProperty( "x-avalon.lifestyle", null );
        String handler;

        if ( null != lifecycle )
        {
            handler = (String) m_lifecycleMap.get( lifecycle );
        }
        else
        {
            handler = meta.getProperty( "fortress.handler" );
        }

        if ( null == handler )
        {
            handler = PER_THREAD_HANDLER;
        }

        return handler;
    }

    /**
     * Convert a Component implmentation classname into a shorthand
     * name.  It assumes all classnames for a particular component is
     * unique.
     *
     * @param className  The classname of a component
     * @return String the short name
     */
    public static final String createShortName( final String className )
    {
        final StringBuffer shortName = new StringBuffer();

        final char[] name = className.substring(
            className.lastIndexOf( '.' ) + 1 ).toCharArray();
        char last = '\0';

        for (int i = 0; i < name.length; i++)
        {
            if (Character.isUpperCase(name[i]))
            {
                if ( Character.isLowerCase( last ) )
                {
                    shortName.append('-');
                }

                shortName.append(Character.toLowerCase(name[i]));
            }
            else
            {
                shortName.append(name[i]);
            }

            last = name[i];
        }

        return shortName.toString().toLowerCase();
    }

}
