/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.avalon.fortress.impl.role;

import org.apache.avalon.fortress.RoleEntry;
import org.apache.avalon.fortress.RoleManager;
import org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.util.HashMap;
import java.util.Map;

/**
 * The Excalibur Role Manager is used for Excalibur Role Mappings.  All of
 * the information is hard-coded.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.14 $ $Date: 2004/04/05 08:46:06 $
 * @since 4.1
 */
public abstract class AbstractRoleManager
    extends AbstractLogEnabled
    implements RoleManager
{
    /**
     * The classloader used to load and check roles and components.
     */
    private final ClassLoader m_loader;

    /**
     * Map for shorthand to RoleEntry.
     */
    private final Map m_shorthands = new HashMap();

    /**
     * Map for classname to RoleEntry.
     */
    private final Map m_classnames = new HashMap();

    /**
     * Parent <code>RoleManager</code> for nested resolution.
     */
    private final RoleManager m_parent;

    /**
     * Default constructor--this RoleManager has no parent.
     */
    public AbstractRoleManager()
    {
        this( null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public AbstractRoleManager( final RoleManager parent )
    {
        this( parent, Thread.currentThread().getContextClassLoader() );
    }

    /**
     * Create an AbstractRoleManager with the specified parent manager and the
     * supplied classloader.
     *
     * @param parent  The parent <code>RoleManager</code>
     * @param loader  The class loader
     */
    public AbstractRoleManager( final RoleManager parent,
                                final ClassLoader loader )
    {
        ClassLoader thisLoader = loader;
        if ( null == thisLoader )
        {
            thisLoader = Thread.currentThread().getContextClassLoader();
        }

        m_loader = thisLoader;
        m_parent = parent;
    }

    /**
     * Addition of a role to the role manager.
     * @param shortName the short name for the role
     * @param role the role
     * @param className the class name
     * @param handlerClassName the handler classname
     */
    protected final boolean addRole( final String shortName,
                                  final String role,
                                  final String className,
                                  final String handlerClassName )
    {
        final Class clazz;
        final Class handlerKlass;

        try
        {
            clazz = m_loader.loadClass( className );
        }
        catch ( final Exception e )
        {
            final String message =
                "Unable to load class " + className + ". Skipping.";
            getLogger().warn( message );
            return false;
        }

        if ( null != handlerClassName )
        {
            try
            {
                handlerKlass = m_loader.loadClass( handlerClassName );
            }
            catch ( final Exception e )
            {
                final String message = "Unable to load handler " +
                    handlerClassName + " for class " + className + ". Skipping.";
                getLogger().warn( message );
                return false;
            }
        }
        else
        {
            handlerKlass = getDefaultHandler();
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "addRole role: name='" + shortName + "', role='" + role + "', "
                + "class='" + className + "', handler='" + handlerClassName + "'" );
        }

        final RoleEntry entry = new RoleEntry( role, shortName, clazz, handlerKlass );
        m_shorthands.put( shortName, entry );
        m_classnames.put( className, entry );

        return true;
    }

    /**
     * Get the default component handler.
     *
     * @return the class for {@link PerThreadComponentHandler}
     */
    protected final Class getDefaultHandler()
    {
        return PerThreadComponentHandler.class;
    }

    public final RoleEntry getRoleForClassname( final String classname )
    {
        final RoleEntry roleEntry = (RoleEntry) m_classnames.get( classname );
        if ( null != roleEntry )
        {
            return roleEntry;
        }
        else if ( null != m_parent )
        {
            return m_parent.getRoleForClassname( classname );
        }
        else
        {
            return null;
        }
    }

    /**
     * Return a role name relative to a supplied short name.
     *
     * @param shortname the short name
     * @return the role entry
     */
    public final RoleEntry getRoleForShortName( final String shortname )
    {
        final RoleEntry roleEntry = (RoleEntry) m_shorthands.get( shortname );
        if ( null != roleEntry )
        {
            return roleEntry;
        }
        else if ( null != m_parent )
        {
            return m_parent.getRoleForShortName( shortname );
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the classloader used for the RoleManager for any class that
     * extends this one.
     *
     * @return ClassLoader
     */
    protected final ClassLoader getLoader()
    {
        return m_loader;
    }
}

