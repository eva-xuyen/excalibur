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

package org.apache.avalon.fortress.impl.role.test;

import org.apache.avalon.fortress.impl.role.FortressRoleManager;
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
public class FortressRoleManagerTestCase
    extends AbstractRoleManagerTestCase
{
    public FortressRoleManagerTestCase( String name )
    {
        super( name );
    }

    /** Null test. */
    public void testTest() {}

    /**
     * Test the shorthand return values.
     * Comment this out until we get rid of FortressRoleManager
     */
    public void DONTtestShorthandReturnValues()
        throws Exception
    {
        FortressRoleManager roles = new FortressRoleManager( null, this.getClass().getClassLoader() );
        roles.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_INFO ) );
        roles.initialize();

        checkRole( roles,
            "jdbc-datasource",
            "org.apache.avalon.excalibur.datasource.DataSourceComponent",
            "org.apache.avalon.excalibur.datasource.JdbcDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "j2ee-datasource",
            "org.apache.avalon.excalibur.datasource.DataSourceComponent",
            "org.apache.avalon.excalibur.datasource.J2eeDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        if ( isInformixClassExists() )
        {
            checkRole( roles,
                "informix-datasource",
                "org.apache.avalon.excalibur.datasource.DataSourceComponent",
                "org.apache.avalon.excalibur.datasource.InformixDataSource",
                "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        }
        checkRole( roles,
            "monitor",
            "org.apache.avalon.excalibur.monitor.Monitor",
            "org.apache.avalon.excalibur.monitor.ActiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "passive-monitor",
            "org.apache.avalon.excalibur.monitor.Monitor",
            "org.apache.avalon.excalibur.monitor.PassiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "xalan-xpath",
            "org.apache.excalibur.xml.xpath.XPathProcessor",
            "org.apache.excalibur.xml.xpath.XPathProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "jaxpath",
            "org.apache.excalibur.xml.xpath.XPathProcessor",
            "org.apache.excalibur.xml.xpath.JaxenProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "resolver",
            "org.apache.excalibur.source.SourceResolver",
            "org.apache.excalibur.source.impl.SourceResolverImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        checkRole( roles,
            "parser",
            "org.apache.excalibur.xml.dom.DOMParser",
            "org.apache.excalibur.xml.impl.JaxpParser",
            "org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler" );
        checkRole( roles,
            "xerces-parser",
            "org.apache.excalibur.xml.dom.DOMParser",
            "org.apache.excalibur.xml.impl.XercesParser",
            "org.apache.avalon.fortress.impl.handler.FactoryComponentHandler" );
    }

}

