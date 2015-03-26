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
 
package org.apache.avalon.fortress.impl.interceptor.test;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.impl.InterceptorEnabledContainer;
import org.apache.avalon.fortress.impl.interceptor.test.examples.ValidInterceptor;
import org.apache.avalon.fortress.interceptor.InterceptorManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public abstract class AbstractInterceptorManagerTest extends TestCase
{
    protected InterceptorEnabledContainer m_container;
    protected InterceptorManager m_interManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_container = createContainer();
        m_interManager = m_container.getInterceptorManager();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        m_container.dispose();
    }

    protected void addValidInterceptor() throws Exception
    {
        m_interManager.add( "dao", "key", ValidInterceptor.class.getName() );
    }

    protected InterceptorEnabledContainer createContainer() throws Exception
    {
        final FortressConfig config = new FortressConfig();
        config.setContainerClass( InterceptorEnabledContainer.class );
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        
        final String BASE = "resource://org/apache/avalon/fortress/test/data/";
        config.setContainerConfiguration( BASE + "test1.xconf" );
        config.setLoggerManagerConfiguration( BASE + "test1.xlog" );

        final ContainerManager cm = new DefaultContainerManager( config.getContext() );
        ContainerUtil.initialize( cm );

        return (InterceptorEnabledContainer) cm.getContainer();
    }
}
