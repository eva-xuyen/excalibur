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

import org.apache.avalon.framework.activity.Initializable;

/**
 * The Excalibur Role Manager is used for Excalibur Role Mappings.  All of
 * the information is hard-coded.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.11 $ $Date: 2004/02/28 15:16:25 $
 */
public final class FortressRoleManager
    extends AbstractRoleManager
    implements Initializable
{
    /**
     * Default constructor--this RoleManager has no parent.
     */
    public FortressRoleManager()
    {
        this( null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public FortressRoleManager( final org.apache.avalon.fortress.RoleManager parent )
    {
        this( parent, null );
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent and a classloader.
     *
     * @param parent  The parent <code>RoleManager</code>.
     * @param loader  the classloader
     */
    public FortressRoleManager( final org.apache.avalon.fortress.RoleManager parent, final ClassLoader loader )
    {
        super( parent, loader );
    }

    /**
     * Initialize the role manager.
     */
    public void initialize()
    {
        /* Set up DataSource relations */
        addRole( "jdbc-datasource",
            "org.apache.avalon.excalibur.datasource.DataSourceComponent",
            "org.apache.avalon.excalibur.datasource.JdbcDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        addRole( "j2ee-datasource",
            "org.apache.avalon.excalibur.datasource.DataSourceComponent",
            "org.apache.avalon.excalibur.datasource.J2eeDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        addRole( "informix-datasource",
            "org.apache.avalon.excalibur.datasource.DataSourceComponent",
            "org.apache.avalon.excalibur.datasource.InformixDataSource",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        /* Set up Monitor relations */
        addRole( "monitor",
            "org.apache.avalon.excalibur.monitor.Monitor",
            "org.apache.avalon.excalibur.monitor.ActiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        addRole( "passive-monitor",
            "org.apache.avalon.excalibur.monitor.Monitor",
            "org.apache.avalon.excalibur.monitor.PassiveMonitor",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        /* Set up XPath relations */
        addRole( "xalan-xpath",
            "org.apache.excalibur.xml.xpath.XPathProcessor",
            "org.apache.excalibur.xml.xpath.XPathProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );
        addRole( "jaxpath",
            "org.apache.excalibur.xml.xpath.XPathProcessor",
            "org.apache.excalibur.xml.xpath.JaxenProcessorImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        /* Set up SourceResolver relations */
        addRole( "resolver",
            "org.apache.excalibur.source.SourceResolver",
            "org.apache.excalibur.source.impl.SourceResolverImpl",
            "org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler" );

        /* Set up XML parser relations */
        addRole( "parser",
            "org.apache.excalibur.xml.dom.DOMParser",
            "org.apache.excalibur.xml.impl.JaxpParser",
            "org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler" );
        addRole( "xerces-parser",
            "org.apache.excalibur.xml.dom.DOMParser",
            "org.apache.excalibur.xml.impl.XercesParser",
            "org.apache.avalon.fortress.impl.handler.FactoryComponentHandler" );
    }
}

