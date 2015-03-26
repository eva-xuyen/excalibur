/* 
 * Copyright 2004 The Apache Software Foundation
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

import org.apache.avalon.fortress.impl.interceptor.ComponentFamily;
import org.apache.avalon.fortress.impl.interceptor.test.examples.AnotherValidInterceptor;
import org.apache.avalon.fortress.impl.interceptor.test.examples.InvalidInterceptor;
import org.apache.avalon.fortress.impl.interceptor.test.examples.ValidInterceptor;
import org.apache.avalon.fortress.interceptor.Interceptor;
import org.apache.avalon.fortress.interceptor.InterceptorManagerException;

import junit.framework.TestCase;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class ComponentFamilyTestCase extends TestCase
{
    private ComponentFamily m_family;
    
    /**
     * Constructor for ComponentFamilyTestCase.
     * @param name
     */
    public ComponentFamilyTestCase(String name)
    {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_family = new ComponentFamily( "Simpsons" );
    }

    public void testAdd() throws Exception
    {
        m_family.add( "key", ValidInterceptor.class.getName() );
        assertEquals( 1, m_family.interceptorsCount() );
    }

    public void testAddClassNotImplementingInterceptorInterface() throws Exception
    {
        try
        {
            m_family.add( "key", InvalidInterceptor.class.getName() );
            fail( "Should not allow to register an invalid class" );
        }
        catch(InterceptorManagerException ex)
        {
            // Expected
        }
        
        assertEquals( 0, m_family.interceptorsCount() );
    }

    public void testAddInvalidClass() throws Exception
    {
        try
        {
            m_family.add( "key", "org.apache.avalon.fortress.impl.interceptor.test.examples.HomerInterceptor" );
            fail( "Should not allow to register an invalid class" );
        }
        catch(ClassNotFoundException ex)
        {
            // Expected
        }
        
        assertEquals( 0, m_family.interceptorsCount() );
    }

    public void testRemove() throws Exception
    {
        testAdd();
        m_family.remove( "key" );
        assertEquals( 0, m_family.interceptorsCount() );
    }

    public void testBuildOrderedChain() throws Exception
    {
        testAdd();
        Interceptor[] interceptors = m_family.buildOrderedChain();
        
        assertNotNull( interceptors );
        assertEquals( 1, interceptors.length );
        assertNull( interceptors[0].getNext() );
    }

    public void testBuildOrderedChainWithAnotherSetup() throws Exception
    {
        m_family.add( "key1", ValidInterceptor.class.getName() );
        m_family.add( "key2", AnotherValidInterceptor.class.getName() );
        m_family.remove( "key1" ); 
        m_family.add( "key1", ValidInterceptor.class.getName() );
        m_family.add( "key1", ValidInterceptor.class.getName() );

        Interceptor[] interceptors = m_family.buildOrderedChain();
        
        assertNotNull( interceptors );
        assertEquals( 2, interceptors.length );
        assertNull( interceptors[0].getNext() );
        assertEquals( ValidInterceptor.class, interceptors[0].getClass() );
        assertEquals( AnotherValidInterceptor.class, interceptors[1].getClass() );
    }

    public void testToString()
    {
        assertEquals( "ComponentFamily [ Simpsons ] ", m_family.toString() );
    }
}
