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
import org.apache.avalon.fortress.RoleEntry;
import org.apache.avalon.fortress.impl.handler.ThreadSafeComponentHandler;
import org.apache.avalon.fortress.test.data.Component1;
import org.apache.avalon.fortress.test.data.Role1;

/**
 * RoleEntryTestCase tests the RoleEntry class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class RoleEntryTestCase extends TestCase
{
    public RoleEntryTestCase( String name )
    {
        super( name );
    }

    public void testRoleEntry()
    {
        String role = Role1.class.getName();
        String name = "component1";
        Class componentClass = Component1.class;
        Class handlerClass = ThreadSafeComponentHandler.class;

        RoleEntry entry = new RoleEntry( role, name, componentClass, handlerClass );

        assertNotNull( entry );
        assertNotNull( entry.getRole() );
        assertNotNull( entry.getShortname() );
        assertNotNull( entry.getComponentClass() );
        assertNotNull( entry.getHandlerClass() );

        assertEquals( role, entry.getRole() );
        assertEquals( name, entry.getShortname() );
        assertEquals( componentClass, entry.getComponentClass() );
        assertEquals( handlerClass, entry.getHandlerClass() );
    }

    public void testNullPointerExceptions()
    {
        String role = Role1.class.getName();
        String name = "component1";
        Class componentClass = Component1.class;
        Class handlerClass = ThreadSafeComponentHandler.class;

        try
        {
            new RoleEntry( null, name, componentClass, handlerClass );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "threw the wrong exception: " + e.getClass().getName() );
        }

        try
        {
            new RoleEntry( role, null, componentClass, handlerClass );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "threw the wrong exception: " + e.getClass().getName() );
        }

        try
        {
            new RoleEntry( role, name, null, handlerClass );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "threw the wrong exception: " + e.getClass().getName() );
        }

        try
        {
            new RoleEntry( role, name, componentClass, null );
            fail( "Did not throw an exception" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!
        }
        catch ( Exception e )
        {
            fail( "threw the wrong exception: " + e.getClass().getName() );
        }
    }
}
