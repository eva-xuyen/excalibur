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

package org.apache.avalon.fortress.impl.role.test;

import org.apache.avalon.fortress.impl.role.ConfigurableRoleManager;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.ConsoleLogger;


/**
 * Configurable RoleManager implementation.  It populates the RoleManager
 * from a configuration hierarchy.  This is based on the DefaultRoleManager
 * in the org.apache.avalon.component package.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 * @since 4.1
 */
public class ConfigurableRoleManagerTestCase
        extends AbstractRoleManagerTestCase
{
    public ConfigurableRoleManagerTestCase( String name )
    {
        super( name );
    }

    public void testShorthandReturnValues()
            throws Exception
    {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        ConfigurableRoleManager roles = new ConfigurableRoleManager( null, this.getClass().getClassLoader() );
        roles.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_INFO ) );
        roles.configure( builder.build( this.getClass().getClassLoader()
                .getResourceAsStream( "org/apache/avalon/fortress/impl/role/test/ConfigManager.roles" ) ) );

        checkRole( roles,
                "component1",
                "org.apache.avalon.fortress.test.data.Role1",
                "org.apache.avalon.fortress.test.data.Component1",
                "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
                "component2",
                "org.apache.avalon.fortress.test.data.Role2",
                "org.apache.avalon.fortress.test.data.Component2",
                "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
                "component3",
                "org.apache.avalon.fortress.test.data.Role3",
                "org.apache.avalon.fortress.test.data.Component3",
                "org.apache.avalon.fortress.impl.handler.PoolableComponentHandler" );
    }
}

