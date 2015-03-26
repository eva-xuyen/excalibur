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
package org.apache.avalon.framework.service.test;

import junit.framework.TestCase;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.DefaultServiceSelector;

/**
 * Test the basic public methods of DefaultComponentSelector.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class DefaultServiceSelectorTestCase
    extends TestCase
{
    class FeatureComponent
    {
        Object  m_feature;
        public FeatureComponent( final Object feature )
        {
            m_feature = feature;
        }

        public Object getFeature()
        {
            return m_feature;
        }
    }

    class Hint
    {
        String  m_name;

        public Hint( final String name )
        {
            m_name = name;
        }

        public String getName()
        {
            return m_name;
        }
    }

    private DefaultServiceSelector m_componentSelector;
    protected boolean m_exceptionThrown;

    public DefaultServiceSelectorTestCase()
    {
        this("DefaultComponentSelector Test Case");
    }

    public DefaultServiceSelectorTestCase( final String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        m_componentSelector = new DefaultServiceSelector();
        m_exceptionThrown =false;
    }

    protected  void tearDown()
        throws Exception
    {
        m_componentSelector = null;
    }

    /**
     * lookup contract:
     * return  the component that was put with this hint
     * if no compnent exist for hint
     * throw ComponentException
     */
    public void testlookup()
        throws Exception
    {
        Hint hintA = new Hint("a");
        Hint hintB = new Hint("b");
        m_componentSelector.put(hintA,new FeatureComponent(hintA));
        m_componentSelector.put(hintB,new FeatureComponent(hintB));
        FeatureComponent  fComponent = (FeatureComponent)m_componentSelector.select(hintA);
        assertEquals( hintA, fComponent.getFeature() );
        Object o = null;
        try
        {
            o = (FeatureComponent)m_componentSelector.select(new Hint("no component"));
        }
        catch        (ServiceException ce)
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
        Hint hintA = new Hint("a");
        Hint hintB = new Hint("b");
        m_componentSelector.put(hintA,new FeatureComponent(hintA));
        assertTrue(m_componentSelector.isSelectable(hintA));
        assertTrue(!m_componentSelector.isSelectable(hintB));
    }

    //makeReadOnly contract:put after makeReadOnly throws IllegalStateException
    public void testmakeReadOnly()
        throws Exception
    {
        Hint hintA = new Hint("a");
        Hint hintB = new Hint("b");
        //before read only
        m_componentSelector.put(hintA,new FeatureComponent(hintA));
        FeatureComponent  fComponent = (FeatureComponent)m_componentSelector.select(hintA);
        assertEquals( hintA, fComponent.getFeature() );
        m_componentSelector.makeReadOnly();
        //after read only
        try
        {
            m_componentSelector.put(hintB,new FeatureComponent(hintB));
        }
        catch        (IllegalStateException se)
        {
            m_exceptionThrown = true;
        }
        assertTrue("IllegalStateException was not thrown in  put after makeReadOnly." , m_exceptionThrown );
    }
}






