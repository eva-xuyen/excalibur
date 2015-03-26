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

package org.apache.avalon.fortress.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.test.data.*;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * A testcase for the different handlers.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 */
public class HandlersTestCase extends TestCase
{
    protected Exception m_exception;

    public HandlersTestCase( final String name )
    {
        super( name );
    }

    public void testThreadsafe()
        throws Exception
    {
        final ServiceManager serviceManager = getServiceManager();
        final String key = Role1.ROLE;
        final BaseRole object1 = (BaseRole) serviceManager.lookup( key );
        final BaseRole object2 = (BaseRole) serviceManager.lookup( key );

        assertSame( "Threadsafe objects (1 vs 2)", object1, object2 );
        assertEquals( "Threadsafe object IDs (1 vs 2)", object1.getID(), object2.getID() );

        final Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    final BaseRole object3 = (BaseRole) serviceManager.lookup( key );
                    final BaseRole object4 = (BaseRole) serviceManager.lookup( key );

                    assertSame( "Threadsafe objects (1 vs 3)", object1, object3 );
                    assertEquals( "Threadsafe object IDs (1 vs 3)", object1.getID(), object3.getID() );
                    assertSame( "Threadsafe objects (2 vs 4)", object2, object4 );
                    assertEquals( "Threadsafe object IDs (2 vs 4)", object2.getID(), object4.getID() );
                    assertSame( "Threadsafe objects (3 vs 4)", object3, object4 );
                    assertEquals( "Threadsafe object IDs (3 vs 4)", object3.getID(), object4.getID() );
                }
                catch ( final Exception e )
                {
                    m_exception = e;
                }
            }
        };
        thread.start();
        thread.join();

        checkException();
    }

    public void testPerThread()
        throws Exception
    {
        final String key = Role3.ROLE;
        final String type = "PerThread";

        final ServiceManager serviceManager = getServiceManager();
        final BaseRole object1 = (BaseRole) serviceManager.lookup( key );
        final BaseRole object2 = (BaseRole) serviceManager.lookup( key );

        assertEquals( type + " object IDs (1 vs 2)", object1.getID(), object2.getID() );

        final Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    final BaseRole object3 = (BaseRole) serviceManager.lookup( key );
                    final BaseRole object4 = (BaseRole) serviceManager.lookup( key );

                    assertTrue( type + " object IDs (1 vs 3)", object1.getID() != object3.getID() );
                    assertTrue( type + " object IDs (2 vs 4)", object2.getID() != object4.getID() );
                    assertEquals( type + " object IDs (3 vs 4)", object3.getID(), object4.getID() );
                }
                catch ( final Exception e )
                {
                    m_exception = e;
                }
            }
        };
        thread.start();
        thread.join();

        checkException();
    }

    public void testFactory()
        throws Exception
    {
        final String key = Role4.ROLE;
        final String type = "Factory";

        final ServiceManager serviceManager = getServiceManager();
        final BaseRole object1 = (BaseRole) serviceManager.lookup( key );
        final BaseRole object2 = (BaseRole) serviceManager.lookup( key );

        assertTrue( type + " object IDs (1 vs 2)", object1.getID() != object2.getID() );

        final Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    final BaseRole object3 = (BaseRole) serviceManager.lookup( key );
                    final BaseRole object4 = (BaseRole) serviceManager.lookup( key );

                    assertTrue( type + " object IDs (1 vs 3)", object1.getID() != object3.getID() );
                    assertTrue( type + " object IDs (2 vs 4)", object2.getID() != object4.getID() );
                    assertTrue( type + " object IDs (3 vs 4)", object3.getID() != object4.getID() );
                }
                catch ( final Exception e )
                {
                    m_exception = e;
                }
            }
        };
        thread.start();
        thread.join();

        checkException();
    }

    private void checkException() throws Exception
    {
        if ( null != m_exception )
        {
            final Exception exception = m_exception;
            m_exception = null;
            throw exception;
        }
    }

    public void testPoolable()
        throws Exception
    {
        final ServiceManager serviceManager = getServiceManager();
        final String key = Role2.ROLE;
        final BaseRole object1 = (BaseRole) serviceManager.lookup( key );
        final BaseRole object2 = (BaseRole) serviceManager.lookup( key );
        final BaseRole object3 = (BaseRole) serviceManager.lookup( key );

        serviceManager.release( object1 );
        serviceManager.release( object2 );
        serviceManager.release( object3 );
    }

    private ServiceManager getServiceManager() throws Exception
    {
        final FortressConfig config = new FortressConfig();
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        final String BASE = "resource://org/apache/avalon/fortress/test/data/";
        config.setContainerConfiguration( BASE + "test1.xconf" );
        config.setLoggerManagerConfiguration( BASE + "test1.xlog" );

        final ContainerManager cm = new DefaultContainerManager( config.getContext() );
        ContainerUtil.initialize( cm );

        final DefaultContainer container = (DefaultContainer) cm.getContainer();
        final ServiceManager serviceManager = container.getServiceManager();
        return serviceManager;
    }
}
