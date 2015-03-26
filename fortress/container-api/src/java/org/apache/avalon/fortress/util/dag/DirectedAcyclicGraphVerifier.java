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

package org.apache.avalon.fortress.util.dag;

import java.util.*;

/**
 * DirectedAcyclicGraphVerifier provides methods to verify that any set of
 * vertices has no cycles.  A Directed Acyclic Graph is a "graph" or set of
 * vertices where all connections between each vertex goes in a particular
 * direction and there are no cycles or loops.  It is used to track dependencies
 * and ansure that dependencies can be loaded and unloaded in the proper order.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class DirectedAcyclicGraphVerifier
{
    /**
     * Verify that a vertex and its set of dependencies have no cycles.
     *
     * @param vertex  The vertex we want to test.
     *
     * @throws CyclicDependencyException  if there is a cycle.
     */
    public static void verify( Vertex vertex )
        throws CyclicDependencyException
    {
        // We need a list of vertices that contains the entire graph, so build it.
        List vertices = new ArrayList();
        addDependencies( vertex, vertices );
        
        verify( vertices );
    }
    
    /**
     * Recursively add a vertex and all of its dependencies to a list of
     *  vertices
     *
     * @param vertex Vertex to be added.
     * @param vertices Existing list of vertices.
     */
    private static void addDependencies( final Vertex vertex, final List vertices )
    {
        if ( !vertices.contains( vertex ) )
        {
            vertices.add( vertex );
            
            for ( Iterator iter = vertex.getDependencies().iterator(); iter.hasNext(); )
            {
                addDependencies( (Vertex)iter.next(), vertices );
            }
        }
    }

    /**
     * Verify a set of vertices and all their dependencies have no cycles.  All
     *  Vertices in the graph must exist in the list.
     *
     * @param vertices  The list of vertices we want to test.
     *
     * @throws CyclicDependencyException  if there is a cycle.
     */
    public static void verify( List vertices )
        throws CyclicDependencyException
    {
        // Reset the orders of all the vertices.
        resetVertices( vertices );
        
        // Assert that all vertices are in the vertices list and resolve each of their orders.
        Iterator it = vertices.iterator();
        while ( it.hasNext() )
        {
            Vertex v = (Vertex) it.next();
            
            // Make sure that any dependencies are also in the vertices list.  This adds
            //  a little bit to the load, but we don't test this and the test would have
            //  failed, this would lead to some very hard to track down problems elsewhere.
            Iterator dit = v.getDependencies().iterator();
            while( dit.hasNext() )
            {
                Vertex dv = (Vertex) dit.next();
                if ( !vertices.contains( dv ) )
                {
                    throw new IllegalStateException( "A dependent vertex (" + dv.getName() + ") of "
                        + "vertex (" + v.getName() + ") was not included in the vertices list." );
                }
            }
            
            v.resolveOrder();
        }
    }

    /**
     * Sort a set of vertices so that no dependency is before its vertex.  If
     * we have a vertex named "Parent" and one named "Child" that is listed as
     * a dependency of "Parent", we want to ensure that "Child" always comes
     * after "Parent".  As long as there are no cycles in the list, we can sort
     * any number of vertices that may or may not be related.  Both "Parent"
     * and "Child" must exist in the vertices list, but "Child" will also be
     * referenced as a dependency of "Parent".
     *
     * <p>
     *   <b>Implementation Detail:</b> This particular algorithm is a more
     *   efficient variation of the typical Topological Sort algorithm.  It uses
     *   a Queue (Linked List) to ensure that each edge (connection between
     *   two vertices) or vertex is checked only once.  The efficiency is
     *   O = (|V| + |E|).
     * </p>
     *
     * @param vertices
     * @throws CyclicDependencyException
     */
    public static void topologicalSort( final List vertices ) throws CyclicDependencyException
    {
        // Verify the graph and set the vertex orders in the process.
        verify( vertices );
        
        // We now that there are no cycles and that each of the vertices has an order
        //  that will allow them to be sorted.
        Collections.sort( vertices );
    }

    /**
     * Resets all the vertices so that the visitation flags and indegrees are
     * reset to their start values.
     *
     * @param vertices
     */
    public static void resetVertices( List vertices )
    {
        Iterator it = vertices.iterator();
        while ( it.hasNext() )
        {
            ( (Vertex) it.next() ).reset();
        }
    }
}
