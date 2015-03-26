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

import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.interceptor.test.components.CustomerDataAccessObject;
import org.apache.avalon.fortress.impl.interceptor.test.components.SupplierDataAccessObject;
import org.apache.avalon.fortress.impl.interceptor.test.examples.FakeTransactionManager;

/**
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class PerformanceTestCase extends AbstractInterceptorManagerTest
{
    public void testGetInterceptableComponent() throws Exception
    {
        addValidInterceptor();
        
        ComponentHandler handler = (ComponentHandler) 
            m_container.get( CustomerDataAccessObject.ROLE, "*" );

        CustomerDataAccessObject dao = (CustomerDataAccessObject) handler.get();

        long begin = System.currentTimeMillis();

        for( int i = 0; i < 60000; i++ )
        {
            FakeTransactionManager.instance().clear();
            
            assertEquals( 0, FakeTransactionManager.instance().getTransactionsStarted() );
            assertEquals( 0, FakeTransactionManager.instance().getTransactionsFinished() );
            
            dao.save( "Data" );
    
            assertEquals( 1, FakeTransactionManager.instance().getTransactionsStarted() );
            assertEquals( 1, FakeTransactionManager.instance().getTransactionsFinished() );
        }
        
        long end = System.currentTimeMillis();

        handler.put( dao );
        
        System.out.println( "testGetInterceptableComponent took " + (end - begin) + " ms" );
    }

    public void testGetOrdinaryComponent() throws Exception
    {
        ComponentHandler handler = (ComponentHandler) 
            m_container.get( SupplierDataAccessObject.ROLE, "*" );

        SupplierDataAccessObject dao = (SupplierDataAccessObject) handler.get();

        long begin = System.currentTimeMillis();

        for( int i = 0; i < 60000; i++ )
        {
            FakeTransactionManager.instance().clear();
            
            assertEquals( 0, FakeTransactionManager.instance().getTransactionsStarted() );
            assertEquals( 0, FakeTransactionManager.instance().getTransactionsFinished() );
            
            dao.save( "Data" );
    
            assertEquals( 1, FakeTransactionManager.instance().getTransactionsStarted() );
            assertEquals( 1, FakeTransactionManager.instance().getTransactionsFinished() );
        }
        
        long end = System.currentTimeMillis();

        handler.put( dao );
        
        System.out.println( "testGetOrdinaryComponent took " + (end - begin) + " ms" );
    }
}
