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

package org.apache.avalon.fortress.impl.lookup.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.impl.AbstractContainer;
import org.apache.avalon.fortress.impl.lookup.FortressServiceManager;
import org.apache.avalon.fortress.impl.lookup.FortressServiceSelector;
import org.apache.avalon.fortress.impl.test.TestComponentHandler;
import org.apache.avalon.fortress.test.data.Role1;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * FortressServiceManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class FortressServiceManagerTestCase extends TestCase
{
    TestContainer m_container = new TestContainer();

    public FortressServiceManagerTestCase( String name )
    {
        super( name );
    }

    public void testServiceManager() throws Exception
    {
        FortressServiceManager manager = new FortressServiceManager( m_container, null );

        m_container.setExpectedKey( Role1.ROLE );

        assertTrue( manager.hasService( Role1.ROLE ) );
        assertNotNull( manager.lookup( Role1.ROLE ) );

        String hint = "test";
        m_container.setExpectedHint( hint );
        assertTrue( manager.hasService( Role1.ROLE + "/" + hint ) );
        assertNotNull( manager.lookup( Role1.ROLE + "/" + hint ) );

        m_container.setExpectedHint( AbstractContainer.SELECTOR_ENTRY );
        assertTrue( manager.hasService( Role1.ROLE + "Selector" ) );
        assertNotNull( manager.lookup( Role1.ROLE + "Selector" ) );

        ServiceSelector selector = (ServiceSelector) manager.lookup( Role1.ROLE + "Selector" );
        m_container.setExpectedHint( hint );
        assertTrue( selector.isSelectable( hint ) );
        assertNotNull( selector.select( hint ) );
    }

    public void testServiceSelector() throws Exception
    {
        FortressServiceSelector selector = new FortressServiceSelector( m_container, Role1.ROLE );

        m_container.setExpectedKey( Role1.ROLE );

        String hint = "test";
        m_container.setExpectedHint( hint );
        assertTrue( selector.isSelectable( hint ) );
        assertNotNull( selector.select( hint ) );
    }
}
