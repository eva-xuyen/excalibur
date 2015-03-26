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

package org.apache.avalon.fortress.tools;

import junit.framework.TestCase;

public class FortressBeanTestCase extends TestCase {

    private FortressBean bean;

    public FortressBeanTestCase(String name) {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.bean = new FortressBean();
        this.bean.setRoleManagerConfiguration("resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.roles");
        this.bean.setContainerConfiguration("resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.xconf");
        this.bean.setLoggerManagerConfiguration("resource://org/apache/avalon/fortress/tools/FortressBeanTestCase.xlog");
        this.bean.setLookupComponentRole(TestInterface.ROLE);
        this.bean.setInvokeMethod("run");
        this.bean.setSystemExitOnDispose(false);
        this.bean.initialize();
        this.bean.run();
    }

    protected void tearDown() throws Exception {
        this.bean.dispose();
    }

    public void test() throws Exception {
        TestInterface ti = (TestInterface) this.bean.getServiceManager().lookup(TestInterface.ROLE);
        assertNotNull(ti);
        assertTrue(ti.isRunning());
    }

}
