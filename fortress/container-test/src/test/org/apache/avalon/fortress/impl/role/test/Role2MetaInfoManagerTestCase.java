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

import org.apache.avalon.fortress.impl.role.FortressRoleManager;
import org.apache.avalon.fortress.impl.role.Role2MetaInfoManager;
import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 * Role2MetaInfoManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS Revision: 1.1 $
 */
public class Role2MetaInfoManagerTestCase extends AbstractMetaInfoManagerTestCase
{
    public Role2MetaInfoManagerTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        FortressRoleManager roles = new FortressRoleManager( null, this.getClass().getClassLoader() );
        roles.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_INFO ) );
        roles.initialize();
        m_manager = new Role2MetaInfoManager( roles );
    }

    /**
     * Temporarily skip this step--we will be removing FortressRoleManager soon.
     *
     * @throws Exception
     */
    public void testRole2MetaInfoManager() throws Exception
    {
        /**
        String[] roles = new String[]{"org.apache.avalon.excalibur.datasource.DataSourceComponent"};

        checkRole( "jdbc-datasource", roles,
            "org.apache.avalon.excalibur.datasource.JdbcDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( "j2ee-datasource", roles,
            "org.apache.avalon.excalibur.datasource.J2eeDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        if ( isInformixClassExists() )
        {
            checkRole( "informix-datasource", roles,
                "org.apache.avalon.excalibur.datasource.InformixDataSource",
                "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        }

        roles[0] = "org.apache.avalon.excalibur.monitor.Monitor";

        checkRole( "monitor", roles,
            "org.apache.avalon.excalibur.monitor.ActiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( "passive-monitor", roles,
            "org.apache.avalon.excalibur.monitor.PassiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        roles[0] = "org.apache.excalibur.xml.xpath.XPathProcessor";

        checkRole( "xalan-xpath", roles,
            "org.apache.excalibur.xml.xpath.XPathProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( "jaxpath", roles,
            "org.apache.excalibur.xml.xpath.JaxenProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        roles[0] = "org.apache.excalibur.source.SourceResolver";

        checkRole( "resolver", roles,
            "org.apache.excalibur.source.impl.SourceResolverImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        roles[0] = "org.apache.excalibur.xml.dom.DOMParser";

        checkRole( "parser", roles,
            "org.apache.excalibur.xml.impl.JaxpParser",
            "org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler" );
        checkRole( "xerces-parser", roles,
            "org.apache.excalibur.xml.impl.XercesParser",
            "org.apache.avalon.fortress.impl.handler.FactoryComponentHandler" );
         */
    }
}
