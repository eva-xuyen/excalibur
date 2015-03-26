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

package org.apache.avalon.fortress.impl.role;

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.fortress.RoleManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides the foundation for MetaInfoManagers.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $
 */
public abstract class AbstractMetaInfoManager extends AbstractLogEnabled implements MetaInfoManager
{
    /**
     * The classloader used to load and check roles and components.
     */
    private final ClassLoader m_loader;

    /**
     * Map for shorthand to MetaInfoEntry.
     */
    private final Map m_shorthands = new HashMap();

    /**
     * Map for classname to MetaInfoEntry.
     */
    private final Map m_classnames = new HashMap();

    /**
     * Parent <code>MetaInfoManager</code> for nested resolution.
     */
    private final MetaInfoManager m_parent;

    /**
     * Default constructor--this RoleManager has no parent.
     */
    public AbstractMetaInfoManager()
    {
        this( (MetaInfoManager) null );
    }

    /**
     * Create a MetaInfoManager with a parent manager.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public AbstractMetaInfoManager( final RoleManager parent )
    {
        this( new Role2MetaInfoManager( parent ) );
    }

    /**
     * Create a MetaInfoManager with a parent manager.
     *
     * @param parent  The parent <code>MetaInfoManager</code>.
     */
    public AbstractMetaInfoManager( final MetaInfoManager parent )
    {
        this( parent, Thread.currentThread().getContextClassLoader() );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>MetaInfoManager</code>
     * @param loader  The class loader
     */
    public AbstractMetaInfoManager( final MetaInfoManager parent,
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
     * Addition of a component to the meta info manager.
     * @param role      the role associated with the component
     * @param className the class name
     * @param meta the properties object for the meta info
     */
    protected void addComponent( final String role,
                                 final String className,
                                 final Properties meta,
                                 final List deps )
    {
        final Class klass;

        MetaInfoEntry entry = (MetaInfoEntry) m_classnames.get( className );

        if ( null != entry )
        {
            entry.addRole( role );
            return;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "addComponent component: type='" + className +
                "', meta='" + meta.toString() + "', role='" + role + "', with deps=" + deps );
        }

        try
        {
            klass = m_loader.loadClass( className );
        }
        catch ( final ClassNotFoundException e )
        {
            final String message =
                "Unable to load class " + className + ". Skipping.";
            getLogger().warn( message );
            // Do not store reference if class does not exist.
            return;
        }

        try
        {
            entry = new MetaInfoEntry( klass, meta, deps );
            entry.addRole( role );

            m_shorthands.put( entry.getConfigurationName(), entry );
            m_classnames.put( className, entry );
        }
        catch ( ClassNotFoundException cfne )
        {
            final String message =
                "Unable to load the handler class for " + className + ".  Skipping.";
            getLogger().warn( message );
        }
    }

    /**
     * Get a <code>MetaInfoEntry</code> for a component type.  This facilitates
     * self-healing configuration files where the impl reads the
     * configuration and translates all <code>&lt;component/&gt;</code>
     * entries to use the short hand name for readability.
     *
     * @param classname  The component type name
     *
     * @return the proper {@link MetaInfoEntry}
     */
    public MetaInfoEntry getMetaInfoForClassname( final String classname )
    {
        final MetaInfoEntry metaEntry = (MetaInfoEntry) m_classnames.get( classname );
        if ( null != metaEntry )
        {
            return metaEntry;
        }
        else if ( null != m_parent )
        {
            return m_parent.getMetaInfoForClassname( classname );
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the meta info relative to a supplied short name.
     *
     * @param shortname the short name
     * @return the proper {@link MetaInfoEntry}
     */
    public MetaInfoEntry getMetaInfoForShortName( final String shortname )
    {
        final MetaInfoEntry metaEntry = (MetaInfoEntry) m_shorthands.get( shortname );
        if ( null != metaEntry )
        {
            return metaEntry;
        }
        else if ( null != m_parent )
        {
            return m_parent.getMetaInfoForShortName( shortname );
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
    protected ClassLoader getLoader()
    {
        return m_loader;
    }
    
    /**
     * Let us know that the meta and dependency info has already been
     * loaded for a given class name.
     * 
     * @param className  The name of the class to check
     * @return <code>true</code> if it has been added
     */
    protected boolean isAlreadyAdded( String className )
    {
        return m_classnames.containsKey( className );
    }
}
