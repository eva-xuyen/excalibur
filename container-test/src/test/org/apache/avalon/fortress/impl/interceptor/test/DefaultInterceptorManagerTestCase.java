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

import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.interceptor.TailInterceptor;
import org.apache.avalon.fortress.impl.interceptor.test.components.CustomerDataAccessObject;
import org.apache.avalon.fortress.impl.interceptor.test.examples.FakeTransactionManager;
import org.apache.avalon.fortress.impl.interceptor.test.examples.ValidInterceptor;
import org.apache.avalon.fortress.interceptor.Interceptor;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class DefaultInterceptorManagerTestCase extends AbstractInterceptorManagerTest
{
    public void testAddInterceptor() throws Exception
    {
        addValidInterceptor();
        String[] families = m_interManager.getFamilies();
        
        assertNotNull( families );
        assertEquals( 1, families.length );
        assertEquals( "dao", families[0] );
    }

    public void testRemoveInterceptor() throws Exception
    {
        testAddInterceptor();
        
        m_interManager.remove( "dao", "key" );
        String[] families = m_interManager.getFamilies();
        
        assertNotNull( families );
        assertEquals( 0, families.length );
    }

    public void testChain() throws Exception
    {
        testAddInterceptor();
        
        Interceptor interceptor = m_interManager.buildChain( "dao" );
        assertNotNull( interceptor );
        assertEquals( ValidInterceptor.class, interceptor.getClass() );
        assertNotNull( interceptor.getNext() );
        
        // Setting the next 
        
        interceptor = interceptor.getNext();
        assertNotNull( interceptor );
        assertEquals( TailInterceptor.class, interceptor.getClass() );
        assertNull( interceptor.getNext() );
    }

    
    public void testGetComponent() throws Exception
    {
        testAddInterceptor();
        
        ComponentHandler handler = (ComponentHandler) 
            m_container.get( CustomerDataAccessObject.ROLE, "*" );
        
        CustomerDataAccessObject dao = (CustomerDataAccessObject) handler.get();
        
        assertEquals( 0, FakeTransactionManager.instance().getTransactionsStarted() );
        assertEquals( 0, FakeTransactionManager.instance().getTransactionsFinished() );
        
        dao.save( "Data" );

        assertEquals( 1, FakeTransactionManager.instance().getTransactionsStarted() );
        assertEquals( 1, FakeTransactionManager.instance().getTransactionsFinished() );
        
        dao.save( "More data" );

        assertEquals( 2, FakeTransactionManager.instance().getTransactionsStarted() );
        assertEquals( 2, FakeTransactionManager.instance().getTransactionsFinished() );
        
        handler.put( dao );
    }
}
