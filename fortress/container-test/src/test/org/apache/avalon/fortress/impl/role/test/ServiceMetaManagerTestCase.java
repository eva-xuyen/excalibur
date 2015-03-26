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

import org.apache.avalon.fortress.impl.handler.FactoryComponentHandler;
import org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler;
import org.apache.avalon.fortress.impl.handler.PoolableComponentHandler;
import org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler;
import org.apache.avalon.fortress.impl.role.ServiceMetaManager;
import org.apache.avalon.fortress.test.data.*;
import org.apache.avalon.fortress.tools.ComponentMetaInfoCollector;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * ServiceMetaManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class ServiceMetaManagerTestCase extends AbstractMetaInfoManagerTestCase
{
    public ServiceMetaManagerTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        m_manager = new ServiceMetaManager();
        ContainerUtil.enableLogging( m_manager, new NullLogger() );
        ContainerUtil.initialize( m_manager );
    }

    public void testTestRoles() throws Exception
    {
        String[] roles = new String[]{Role1.class.getName()};
        checkRole( "component1", roles, Component1.class.getName(), ThreadSafeComponentHandler.class.getName() );

        roles[0] = Role2.class.getName();
        checkRole( "component2", roles, Component2.class.getName(), PoolableComponentHandler.class.getName() );

        roles[0] = Role4.class.getName();
        checkRole( "component4", roles, Component4.class.getName(), FactoryComponentHandler.class.getName() );

        roles = new String[]
        {
            Role3.class.getName(),
            BaseRole.class.getName()
        };

        checkRole( "component3", roles, Component3.class.getName(), PerThreadComponentHandler.class.getName() );
    }
}
