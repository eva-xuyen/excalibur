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

package org.apache.avalon.fortress.impl.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.impl.ComponentHandlerEntry;
import org.apache.avalon.fortress.impl.ComponentHandlerMetaData;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.test.data.Component1;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

/**
 * ComponentHandlerEntryTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class ComponentHandlerEntryTestCase extends TestCase
{
    public ComponentHandlerEntryTestCase( String name )
    {
        super( name );
    }

    public void testComponentHandlerEntry()
    {
        ComponentHandler handler = new TestComponentHandler();
        ComponentHandlerMetaData meta = new ComponentHandlerMetaData(
            "component1", Component1.class.getName(),
            new DefaultConfiguration( "test" ), true );
        ComponentHandlerEntry entry = new ComponentHandlerEntry( handler, meta );

        assertNotNull( entry );
        assertNotNull( entry.getHandler() );
        assertNotNull( entry.getMetaData() );

        assertEquals( handler, entry.getHandler() );
        assertSame( handler, entry.getHandler() );

        assertEquals( meta, entry.getMetaData() );
        assertSame( meta, entry.getMetaData() );
    }

    public void testNullPointerException()
    {
        ComponentHandler handler = new TestComponentHandler();
        ComponentHandlerMetaData meta = new ComponentHandlerMetaData(
            "component1", Component1.class.getName(),
            new DefaultConfiguration( "test" ), true );

        try
        {
            new ComponentHandlerEntry( null, meta );
            fail( "No NullPointerException was thrown" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!!
        }
        catch ( Exception e )
        {
            fail( "Incorrect exception thrown: " + e.getClass().getName() );
        }

        try
        {
            new ComponentHandlerEntry( handler, null );
            fail( "No NullPointerException was thrown" );
        }
        catch ( NullPointerException npe )
        {
            // SUCCESS!!
        }
        catch ( Exception e )
        {
            fail( "Incorrect exception thrown: " + e.getClass().getName() );
        }
    }
}
