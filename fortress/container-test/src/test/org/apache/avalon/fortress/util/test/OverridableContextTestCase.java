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

package org.apache.avalon.fortress.util.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.util.OverridableContext;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;

/**
 * OverridableContextTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class OverridableContextTestCase extends TestCase
{
    public OverridableContextTestCase( String name )
    {
        super( name );
    }

    public void testOverride() throws Exception
    {
        OverridableContext context = new OverridableContext( new DefaultContext() );
        context.put( "name", "value" );

        assertNotNull( context.get( "name" ) );
        assertEquals( "value", context.get( "name" ) );

        context.put( "name", "" );

        assertNotNull( context.get( "name" ) );
        assertEquals( "", context.get( "name" ) );

        context.put( "name", null );

        try
        {
            context.get( "name" );
            fail( "Did not throw the expected exception" );
        }
        catch ( ContextException ce )
        {
            // SUCCESS!!
        }
        catch ( Exception e )
        {
            fail( "Threw the wrong exception: " + e.getClass().getName() );
        }
    }
}
