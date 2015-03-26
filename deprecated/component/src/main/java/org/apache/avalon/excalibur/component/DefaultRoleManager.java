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
package org.apache.avalon.excalibur.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Default RoleManager implementation.  It populates the RoleManager
 * from a configuration file.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class DefaultRoleManager
    extends AbstractDualLogEnabled
    implements RoleManager, Configurable
{
    /** Map for shorthand to role mapping */
    private Map m_shorthands;

    /** Map for role to default classname mapping */
    private Map m_classNames;

    /** Map for role->hint to classname mapping */
    private Map m_hintClassNames;

    /** Parent <code>RoleManager</code> for nested resolution */
    private final RoleManager m_parent;

    /**
     * Default constructor--this RoleManager has no parent.
     */
    public DefaultRoleManager()
    {
        m_parent = null;
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public DefaultRoleManager( RoleManager parent )
    {
        m_parent = parent;
    }

    /**
     * Retrieves the real role name from a shorthand name.  Usually
     * the shorthand name refers to a configuration element name.  If
     * this RoleManager does not have the match, and there is a parent
     * RoleManager, the parent will be asked to resolve the role.
     *
     * @param shorthandName  The shortname that is an alias for the role.
     * @return the official role name.
     */
    public final String getRoleForName( final String shorthandName )
    {
        final String role = (String)m_shorthands.get( shorthandName );

        if( null == role && null != m_parent )
        {
            return m_parent.getRoleForName( shorthandName );
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "looking up shorthand " + shorthandName +
                               ", returning " + role );
        }

        return role;
    }

    /**
     * Retrieves the default class name for the specified role.  This
     * is only called when the configuration does not specify the
     * class explicitly.  If this RoleManager does not have the match,
     * and there is a parent RoleManager, the parent will be asked
     * to resolve the class name.
     *
     * @param role  The role that has a default implementation.
     * @return the Fully Qualified Class Name (FQCN) for the role.
     */
    public final String getDefaultClassNameForRole( final String role )
    {
        final String className = (String)m_classNames.get( role );

        if( null == className && null != m_parent )
        {
            return m_parent.getDefaultClassNameForRole( role );
        }

        return className;
    }

    /**
     * Retrieves a default class name for a role/hint combination.
     * This is only called when a role is mapped to a
     * DefaultComponentSelector, and the configuration elements use
     * shorthand names for the type of component.  If this RoleManager
     * does not have the match, and there is a parent RoleManager, the
     * parent will be asked to resolve the class name.
     *
     * @param role  The role that this shorthand refers to.
     * @param shorthand  The shorthand name for the type of Component
     * @return the FQCN for the role/hint combination.
     */
    public final String getDefaultClassNameForHint( final String role,
                                                    final String shorthand )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "looking up hintmap for role " + role );
        }

        final Map hintMap = (Map)m_hintClassNames.get( role );

        if( null == hintMap )
        {
            if( null != m_parent )
            {
                return m_parent.getDefaultClassNameForHint( role, shorthand );
            }
            else
            {
                return "";
            }
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "looking up classname for hint " + shorthand );
        }

        final String s = ( String ) hintMap.get( shorthand );

        if( s == null && null != m_parent )
        {
            return m_parent.getDefaultClassNameForHint( role, shorthand );
        }
        else
        {
            return s;
        }
    }

    /**
     * Reads a configuration object and creates the role, shorthand,
     * and class name mapping.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    public final void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Map shorts = new HashMap();
        final Map classes = new HashMap();
        final Map hintclasses = new HashMap();

        final Configuration[] roles = configuration.getChildren( "role" );

        for( int i = 0; i < roles.length; i++ )
        {
            final String name = roles[ i ].getAttribute( "name" );
            final String shorthand = roles[ i ].getAttribute( "shorthand" );
            final String defaultClassName =
                roles[ i ].getAttribute( "default-class", null );

            shorts.put( shorthand, name );

            if( null != defaultClassName )
            {
                classes.put( name, defaultClassName );
            }

            final Configuration[] hints = roles[ i ].getChildren( "hint" );
            if( hints.length > 0 )
            {
                HashMap hintMap = new HashMap();

                for( int j = 0; j < hints.length; j++ )
                {
                    final String shortHand = hints[ j ].getAttribute( "shorthand" ).trim();
                    final String className = hints[ j ].getAttribute( "class" ).trim();

                    hintMap.put( shortHand, className );
                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Adding hint type " + shortHand +
                                           " associated with role " + name +
                                           " and class " + className );
                    }
                }

                hintclasses.put( name, Collections.unmodifiableMap( hintMap ) );
            }

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "added Role " + name + " with shorthand " +
                                   shorthand + " for " + defaultClassName );
            }
        }

        m_shorthands = Collections.unmodifiableMap( shorts );
        m_classNames = Collections.unmodifiableMap( classes );
        m_hintClassNames = Collections.unmodifiableMap( hintclasses );
    }
}
