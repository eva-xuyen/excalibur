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

import org.apache.avalon.fortress.RoleManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Configurable RoleManager implementation.  It populates the RoleManager
 * from a configuration hierarchy.  This is based on the DefaultRoleManager
 * in the org.apache.avalon.component package.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.13 $ $Date: 2004/02/28 15:16:25 $
 * @since 4.1
 */
public class ConfigurableRoleManager
        extends AbstractRoleManager
        implements Configurable
{
    /**
     * Default constructor--this RoleManager has no parent.
     */
    public ConfigurableRoleManager()
    {
        super( null, null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * classloader.
     *
     * @param loader  The <code>ClassLoader</code> used to resolve class names.
     */
    public ConfigurableRoleManager( final ClassLoader loader )
    {
        super( null, loader );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public ConfigurableRoleManager( final RoleManager parent )
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
    public ConfigurableRoleManager( final RoleManager parent, final ClassLoader loader )
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
            final Configuration[] components = roles[i].getChildren( "component" );

            for ( int j = 0; j < components.length; j++ )
            {
                final String shorthand = components[j].getAttribute( "shorthand" );
                final String className =
                        components[j].getAttribute( "class", null );
                final String handlerClassName =
                        components[j].getAttribute( "handler",
                                org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler.class.getName() );

                if ( ! addRole( shorthand, role, className, handlerClassName ) )
                {
                    final String message = "Skipping invalid entry:\n\tRole: " + role +
                            "\n\tShorthand: " + shorthand +
                            "\n\tClass Name: " + className +
                            "\n\tHandler Class: " + handlerClassName;

                    getLogger().error( message );
                }
            }
        }
    }
}
