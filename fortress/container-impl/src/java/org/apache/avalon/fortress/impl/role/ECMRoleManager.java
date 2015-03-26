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

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.RoleManager;
import org.apache.avalon.fortress.util.Service;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * This role manager implementation is able to read ECM based role files.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/04/05 10:16:56 $
 */
public class ECMRoleManager
        extends AbstractRoleManager
        implements Configurable
{

    /**
     * Default constructor--this RoleManager has no parent.
     */
    public ECMRoleManager()
    {
        super( null, null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * classloader.
     *
     * @param loader  The <code>ClassLoader</code> used to resolve class names.
     */
    public ECMRoleManager( final ClassLoader loader )
    {
        super( null, loader );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public ECMRoleManager( final RoleManager parent )
    {
        super( parent, null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent and a classloader.
     *
     * @param parent The parent <code>RoleManager</code>.
     * @param loader the classloader
     */
    public ECMRoleManager( final RoleManager parent,
                            final ClassLoader loader )
    {
        super( parent, loader );
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
        final Configuration[] roles = configuration.getChildren( "role" );

        for ( int i = 0; i < roles.length; i++ )
        {
            final String role = roles[i].getAttribute( "name" );
            final String shorthand = roles[i].getAttribute( "shorthand" );
            final String defaultClassName = roles[i].getAttribute( "default-class", null );

            if ( ! addRole( shorthand, role, defaultClassName,  getComponentHandlerClassName(defaultClassName)) )
            {

                final String message = "Configuration error on invalid entry:\n\tRole: " + role +
                        "\n\tShorthand: " + shorthand +
                        "\n\tDefault Class: " + defaultClassName;

                getLogger().warn(message);
            }

        }
    }

    protected String getComponentHandlerClassName(final String defaultClassName)
    {
        if ( defaultClassName == null )
        {
            return null;
        }
        Class clazz;
        try
        {
            clazz = getLoader().loadClass( defaultClassName );
        }
        catch ( final Exception e )
        {
            final String message =
                "Unable to load class " + defaultClassName + ". Using dfault component handler.";
            getLogger().warn( message );
            return null;
        }

        if ( ThreadSafe.class.isAssignableFrom( clazz ) )
        {
            return MetaInfoEntry.THREADSAFE_HANDLER;
        }
        else if ( Service.isClassPoolable( clazz ) )
        {
            return MetaInfoEntry.POOLABLE_HANDLER;
        }
        else if ( SingleThreaded.class.isAssignableFrom( clazz) )
        {
            return MetaInfoEntry.FACTORY_HANDLER;
        }

        // Don't know, use default
        return null ;
    }
}
