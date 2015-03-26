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

package org.apache.avalon.fortress.test;

import junit.framework.TestCase;

import org.apache.avalon.fortress.ContainerListener;
import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.test.data.*;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * A testcase for the events infrastructure
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class EventsTestCase extends TestCase implements ContainerListener
{
    private boolean creationEventCalled = false;
    private boolean destructionEventCalled = false;
    
    public EventsTestCase( final String name )
    {
        super( name );
    }

    public void testCreatedEvent() throws Exception
    {
        final DefaultContainer container = createContainer();
        
        container.getEventManager().addListener( this );
        assertFalse( creationEventCalled );
        assertFalse( destructionEventCalled );
        
        final ServiceManager serviceManager = container.getServiceManager();
        final String key = Role4.ROLE;
        final BaseRole object = (BaseRole) serviceManager.lookup( key );
        
        assertTrue( creationEventCalled );
        assertFalse( destructionEventCalled );
        
        serviceManager.release( object );
        
        assertTrue( destructionEventCalled );
    }

    private DefaultContainer createContainer() throws Exception
    {
        final FortressConfig config = new FortressConfig();
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        final String BASE = "resource://org/apache/avalon/fortress/test/data/";
        config.setContainerConfiguration( BASE + "test1.xconf" );
        config.setLoggerManagerConfiguration( BASE + "test1.xlog" );

        final ContainerManager cm = new DefaultContainerManager( config.getContext() );
        ContainerUtil.initialize( cm );

        return (DefaultContainer) cm.getContainer();
    }

    public Object componentCreated(MetaInfoEntry entry, Object instance)
    {
        assertNotNull(entry);
        assertNotNull(instance);
        creationEventCalled = true;
        return instance;
    }

    public void componentDestroyed(MetaInfoEntry entry, Object instance)
    {
        assertNotNull(entry);
        assertNotNull(instance);
        destructionEventCalled = true;
    }
}
