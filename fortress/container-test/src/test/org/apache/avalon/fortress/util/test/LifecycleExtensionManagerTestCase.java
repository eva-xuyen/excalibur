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

package org.apache.avalon.fortress.util.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.lifecycle.Creator;
import org.apache.avalon.lifecycle.Accessor;

import java.util.Iterator;

/**
 * LifecycleExtensionManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class LifecycleExtensionManagerTestCase extends TestCase
{
    private LifecycleExtensionManager m_manager;

    public LifecycleExtensionManagerTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_manager = new LifecycleExtensionManager();
        m_manager.enableLogging(new NullLogger());
    }

    public void testCreators()
    {
        TestCreator testCreator = new TestCreator(0);
        assertEquals(0, m_manager.creatorExtensionsCount());

        m_manager.addCreatorExtension(testCreator);

        assertEquals(1, m_manager.creatorExtensionsCount());
        assertEquals( testCreator, m_manager.getCreatorExtension( 0 ) );

        int count = 0;
        Iterator it = m_manager.creatorExtensionsIterator();
        while (it.hasNext())
        {
            count++;
            TestCreator creator = (TestCreator)it.next();
            assertEquals(testCreator, creator);
            assertEquals( testCreator.m_id, creator.m_id );
        }
        assertEquals(1, count);

        TestCreator second = new TestCreator( 1 );
        m_manager.insertCreatorExtension( 0, second );

        assertEquals( 2, m_manager.creatorExtensionsCount() );
        assertEquals( second.m_id, ( (TestCreator) m_manager.getCreatorExtension( 0 ) ).m_id );
        assertEquals( testCreator.m_id, ( (TestCreator) m_manager.getCreatorExtension( 1 ) ).m_id );

        m_manager.removeCreatorExtension( 0 );
        assertEquals( 1, m_manager.creatorExtensionsCount() );
        assertEquals( testCreator.m_id, ( (TestCreator) m_manager.getCreatorExtension( 0 ) ).m_id );

        m_manager.clearCreatorExtensions();
        assertEquals(0, m_manager.creatorExtensionsCount());
    }

    public void testAccessors()
    {
        TestAccessor testAccessor = new TestAccessor(0);
        assertEquals( 0, m_manager.accessorExtensionsCount() );

        m_manager.addAccessorExtension( testAccessor );

        assertEquals(1, m_manager.accessorExtensionsCount());
        assertEquals( testAccessor, m_manager.getAccessorExtension( 0 ) );

        int count = 0;
        Iterator it = m_manager.accessorExtensionsIterator();
        while ( it.hasNext() )
        {
            count++;
            TestAccessor accessor = (TestAccessor) it.next();
            assertEquals( testAccessor, accessor );
            assertEquals( testAccessor.m_id, accessor.m_id);
        }
        assertEquals( 1, count );

        TestAccessor second = new TestAccessor(1);
        m_manager.insertAccessorExtension(0, second);

        assertEquals( 2, m_manager.accessorExtensionsCount());
        assertEquals( second.m_id, ( (TestAccessor) m_manager.getAccessorExtension( 0 ) ).m_id );
        assertEquals( testAccessor.m_id, ( (TestAccessor) m_manager.getAccessorExtension( 1 ) ).m_id );

        m_manager.removeAccessorExtension(0);
        assertEquals( 1, m_manager.accessorExtensionsCount() );
        assertEquals( testAccessor.m_id, ( (TestAccessor) m_manager.getAccessorExtension( 0 ) ).m_id );

        m_manager.clearAccessorExtensions();
        assertEquals( 0, m_manager.accessorExtensionsCount());
    }

    public void testLifecycle() throws Exception
    {
        Accessor testAccessor = new TestAccessor(1);
        Creator testCreator = new TestCreator(1);
        m_manager.addCreatorExtension( testCreator );
        m_manager.addAccessorExtension( testAccessor );

        TestComponent component = new TestComponent();
        Context context = new DefaultContext();
        m_manager.executeCreationExtensions( component, context );
        m_manager.executeAccessExtensions( component, context );
        m_manager.executeReleaseExtensions( component, context );
        m_manager.executeDestructionExtensions( component, context );
    }
}
