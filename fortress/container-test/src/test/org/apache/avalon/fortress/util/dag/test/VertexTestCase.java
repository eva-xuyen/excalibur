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

package org.apache.avalon.fortress.util.dag.test;

import java.util.List;

import junit.framework.TestCase;

import org.apache.avalon.fortress.util.dag.CyclicDependencyException;
import org.apache.avalon.fortress.util.dag.Vertex;

/**
 * VertexTest does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class VertexTestCase extends TestCase
{
    public VertexTestCase( String name )
    {
        super( name );
    }

    public void testSortMethods()
    {
        Vertex v = new Vertex( "Root" );
        List deps = v.getDependencies();
        assertNotNull( deps );
        assertEquals( 0, deps.size() );
        assertEquals( "Root", v.getNode() );
        assertEquals( "Root", v.getName() );
        assertEquals( 0, v.getOrder() );

        Vertex w = new Vertex( "Child" );
        v.addDependency( w );
        deps = v.getDependencies();
        assertNotNull( deps );
        assertEquals( 1, deps.size() );

        v.reset();
        w.reset();
        
        try
        {
            v.resolveOrder();
            w.resolveOrder();
        }
        catch ( CyclicDependencyException e )
        {
            fail( "Unexpected cyclic exception: " + e );
        }
        
        assertEquals( 1, v.getOrder() );
        assertEquals( 0, w.getOrder() );
    }
}
