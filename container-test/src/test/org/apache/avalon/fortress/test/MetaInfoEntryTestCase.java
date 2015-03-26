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
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.RoleEntry;
import org.apache.avalon.fortress.impl.handler.FactoryComponentHandler;
import org.apache.avalon.fortress.impl.handler.PerThreadComponentHandler;
import org.apache.avalon.fortress.impl.handler.PoolableComponentHandler;
import org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler;
import org.apache.avalon.fortress.test.data.BaseRole;
import org.apache.avalon.fortress.test.data.Component1;
import org.apache.avalon.fortress.test.data.Role1;
import org.apache.avalon.fortress.test.data.Role2;

import java.util.*;

/**
 * MetaInfoEntryTestCase does tests the meta info entry class
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS Revision: 1.1 $
 */
public class MetaInfoEntryTestCase extends TestCase
{
    private Class m_componentClass;
    private Properties m_properties;
    private List m_dependencies;
    private Map m_lifecycleMap;

    public MetaInfoEntryTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_componentClass = Component1.class;
        m_properties = new Properties();
        m_properties.setProperty( "x-avalon.name", "component1" );
        m_properties.setProperty( "x-avalon.lifestyle", "singleton" );

        m_dependencies = new ArrayList();

        Map lifecycleMap = new HashMap();
        lifecycleMap.put( "singleton", ThreadSafeComponentHandler.class );
        lifecycleMap.put( "thread", PerThreadComponentHandler.class );
        lifecycleMap.put( "pooled", PoolableComponentHandler.class );
        lifecycleMap.put( "transient", FactoryComponentHandler.class );

        m_lifecycleMap = Collections.unmodifiableMap( lifecycleMap );
    }

    public void testFullySpecified() throws Exception
    {
        MetaInfoEntry entry = new MetaInfoEntry( m_componentClass, m_properties, m_dependencies );
        checkMetaInfoEntry( entry, ThreadSafeComponentHandler.class, "component1", false );
    }

    public void testAutoDiscovery() throws Exception
    {
        m_properties.remove( "x-avalon.lifestyle" );
        m_properties.remove( "x-avalon.name" );
        m_properties.setProperty( "fortress.handler", ThreadSafeComponentHandler.class.getName() );
        m_componentClass = MetaInfoEntry.class;

        MetaInfoEntry entry = new MetaInfoEntry( m_componentClass, m_properties, m_dependencies );

        checkMetaInfoEntry( entry, ThreadSafeComponentHandler.class, "meta-info-entry", false );
    }

    public void testLifestyleMarkers() throws Exception
    {
        String name = "component1";

        Iterator it = m_lifecycleMap.keySet().iterator();
        while ( it.hasNext() )
        {
            String type = (String) it.next();
            m_properties.setProperty( "x-avalon.lifestyle", type );
            MetaInfoEntry entry = new MetaInfoEntry( m_componentClass, m_properties, m_dependencies );
            checkMetaInfoEntry( entry, (Class) m_lifecycleMap.get( type ), name, false );
        }
    }

    public void testRoleEntryParent() throws Exception
    {
        RoleEntry roleEntry = new RoleEntry( Role1.class.getName(), "component1",
            m_componentClass, ThreadSafeComponentHandler.class );

        MetaInfoEntry entry = new MetaInfoEntry( roleEntry );

        checkMetaInfoEntry( entry, ThreadSafeComponentHandler.class, "component1", true );
    }

    public void testNullPointerException() throws Exception
    {
        try
        {
            new MetaInfoEntry( null );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }

        try
        {
            new MetaInfoEntry( null, m_properties, m_dependencies );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }

        try
        {
            new MetaInfoEntry( m_componentClass, null, m_dependencies );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }

        try
        {
            new MetaInfoEntry( m_componentClass, m_properties, null );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }

        try
        {
            MetaInfoEntry entry = new MetaInfoEntry( m_componentClass, m_properties, m_dependencies );
            entry.addRole( null );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }

        try
        {
            MetaInfoEntry entry = new MetaInfoEntry( m_componentClass, m_properties, m_dependencies );
            entry.addRole( Role1.class.getName() );
            entry.containsRole( null );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw wrong exception type: " + e.getClass().getName() );
        }
    }

    public void testCreateShortName()
    {
        String start = "Regular";
        String end   = "regular";

        assertEquals( end, MetaInfoEntry.createShortName(start));

        start = "TwoWords";
        end = "two-words";

        assertEquals( end, MetaInfoEntry.createShortName(start));

        start = "MANYcaps";
        end = "manycaps";

        assertEquals( end, MetaInfoEntry.createShortName( start ) );

        start = "MANYcapsAndWords";
        end = "manycaps-and-words";

        assertEquals( end, MetaInfoEntry.createShortName( start ) );
    }

    private void checkMetaInfoEntry( MetaInfoEntry entry, Class handler, String name, boolean oneRole )
    {
        assertEquals( m_componentClass, entry.getComponentClass() );
        assertEquals( name, entry.getConfigurationName() );
        assertEquals( handler, entry.getHandlerClass() );

        if ( oneRole )
        {
            checkSize( 1, entry.getRoles() );
            // only one test does this
        }
        else
        {
            checkSize( 0, entry.getRoles() );
            entry.addRole( Role1.class.getName() );
            checkSize( 1, entry.getRoles() );
            entry.addRole( BaseRole.class.getName() );
            checkSize( 2, entry.getRoles() );
            entry.makeReadOnly();

            assertTrue( entry.containsRole( BaseRole.class.getName() ) );
        }

        assertTrue( entry.containsRole( Role1.class.getName() ) );

        try
        {
            entry.addRole( Role2.class.getName() );
            fail( "Should not allow Role2 to be added" );
        }
        catch ( SecurityException se )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "Threw the wrong exception: " + e.getMessage() );
        }

        assertTrue( !entry.containsRole( Role2.class.getName() ) );
    }

    private void checkSize( int numRoles, Iterator roles )
    {
        int i = 0;
        while ( roles.hasNext() )
        {
            i++;
            roles.next();
        }

        assertEquals( numRoles, i );
    }
}
