/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.component.test;

import junit.framework.TestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.DefaultComponentManager;

/**
 * Test the basic public methods of DefaultComponentManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class DefaultComponentManagerTestCase
    extends TestCase
{

    class DefaultRoleA
        implements Component,RoleA
    {
        public DefaultRoleA()
        {
        }
    }

    class DefaultRoleB
        implements Component,RoleB
    {
        public DefaultRoleB()
        {
        }
    }


    private DefaultComponentManager m_componentManager;

    protected boolean m_exceptionThrown;


    public DefaultComponentManagerTestCase()
    {
        this("DefaultComponentManager Test Case");
    }

    public DefaultComponentManagerTestCase( final String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        m_componentManager = new DefaultComponentManager();
        m_exceptionThrown = false;
    }

    protected  void tearDown()
        throws Exception
    {
        m_componentManager = null;
    }

    /**
     * lookup contract:
     * return first component found for role
     * search in hirarchy from current componentManager up.
     * if no compnent exist for role a in hierarchy
     * throw ComponentException
     */


    public void testlookup1()
        throws Exception
    {
        DefaultRoleB roleBinBase = new DefaultRoleB();
        DefaultRoleB roleBinParent = new DefaultRoleB();
        DefaultRoleA roleAinParent = new DefaultRoleA();

        m_componentManager.put(RoleA.ROLE,roleAinParent);
        m_componentManager.put(RoleB.ROLE,roleBinParent);
        DefaultComponentManager baseComponentManager = new DefaultComponentManager(m_componentManager);
        baseComponentManager.put(RoleB.ROLE,roleBinBase);
        Object lookupAinBase = baseComponentManager.lookup(RoleA.ROLE);
        Object lookupBinBase = baseComponentManager.lookup(RoleB.ROLE);
        Object lookupBinParent = m_componentManager.lookup(RoleB.ROLE);
        assertTrue( lookupAinBase instanceof RoleA);
        assertEquals( lookupBinBase, roleBinBase );
        assertEquals( lookupBinParent, roleBinParent );
        assertEquals( lookupAinBase,roleAinParent);
    }

    public void testlookup2()
        throws Exception
    {
        m_componentManager.put(RoleA.ROLE,new DefaultRoleA());
        Object o = null;
        try
        {
            o = m_componentManager.lookup(RoleB.ROLE);
        }
        catch        (ComponentException ce)
        {
            m_exceptionThrown = true;
        }
        if (o == null)
            assertTrue("ComponentException was not thrown when component was not found by lookup." ,m_exceptionThrown );
        else
            assertTrue("component was found by lookup ,when there was no component.",false);

    }

    public void testhasComponent()
        throws Exception
    {
        m_componentManager.put(RoleA.ROLE,new DefaultRoleA());
        assertTrue(m_componentManager.hasComponent(RoleA.ROLE));
        assertTrue(!m_componentManager.hasComponent(RoleB.ROLE));
    }

    public void testmakeReadOnly()
        throws Exception
    {
        //before read only
        m_componentManager.put(RoleA.ROLE,new DefaultRoleA());
        Object a = m_componentManager.lookup(RoleA.ROLE);
        assertTrue( a instanceof RoleA);
        m_componentManager.makeReadOnly();
        //after read only
        try
        {
            m_componentManager.put(RoleB.ROLE,new DefaultRoleB());
        }
        catch        (IllegalStateException se)
        {
            m_exceptionThrown = true;
        }
        assertTrue("IllegalStateException was not thrown in  put after makeReadOnly." , m_exceptionThrown );
    }
}







