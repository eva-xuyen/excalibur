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

package org.apache.avalon.fortress.tools;

import junit.framework.TestCase;

public class FortressBeanTestCase extends TestCase
{
    private FortressBean m_bean;

    public FortressBeanTestCase(String name)
    {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
		m_bean = new FortressBean();
		m_bean.setRoleManagerConfiguration("resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.roles");
		m_bean.setContainerConfiguration("resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.xconf");
		m_bean.setLoggerManagerConfiguration(
            "resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.xlog");
		m_bean.setLookupComponentRole(TestInterface.ROLE);
		m_bean.setInvokeMethod("run");
		m_bean.setSystemExitOnDispose(false);
		m_bean.initialize();
		m_bean.run();
    }

    protected void tearDown() throws Exception
    {
		m_bean.dispose();
    }

    public void test() throws Exception
    {
        TestInterface ti = (TestInterface) m_bean.getServiceManager().lookup(TestInterface.ROLE);
        assertNotNull(ti);
        assertTrue(ti.isRunning());
    }
}
