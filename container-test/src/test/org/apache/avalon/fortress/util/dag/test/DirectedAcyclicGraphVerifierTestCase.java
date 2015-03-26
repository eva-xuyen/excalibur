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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.avalon.fortress.util.dag.*;


/**
 * DirectedAcyclicGraphVerifierTestCase.java does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class DirectedAcyclicGraphVerifierTestCase extends TestCase
{
    public DirectedAcyclicGraphVerifierTestCase( String name )
    {
        super( name );
    }

    public void testIsDAG()
    {
        try
        {
            Vertex root = new Vertex( "Root" );
            root.addDependency( new Vertex( "Child1" ) );
            root.addDependency( new Vertex( "Child2" ) );

            DirectedAcyclicGraphVerifier.verify( root );
        }
        catch ( CyclicDependencyException cde )
        {
            fail( "Incorrectly found a Cycle" );
        }

        try
        {
            Vertex root = new Vertex( "Root" );
            root.addDependency( new Vertex( "Child1" ) );
            root.addDependency( new Vertex( "Child2" ) );

            Vertex child3 = new Vertex( "Child3" );
            child3.addDependency( root );

            root.addDependency( child3 );

            DirectedAcyclicGraphVerifier.verify( root );

            fail( "Incorrectly missed the Cycle" );
        }
        catch ( CyclicDependencyException cde )
        {
            // Success!
        }
    }

    /**
     * This test waas written to test the algorithm used to search for cycles.
     *  It makes sure that cycles that start a ways into the dependency tree
     *  are handled correctly.
     */
    public void testCycleTest() throws Exception
    {
        Vertex component1 = new Vertex( "Component1" );
        Vertex component2 = new Vertex( "Component2" );
        Vertex component3 = new Vertex( "Component3" );
        Vertex component4 = new Vertex( "Component4" );
        Vertex component5 = new Vertex( "Component5" );
        
        List vertices = new ArrayList( 5 );
        vertices.add( component1 );
        vertices.add( component2 );
        vertices.add( component3 );
        vertices.add( component4 );
        vertices.add( component5 );
        
        component1.addDependency( component2 );
        component2.addDependency( component3 );
            
        component3.addDependency( component4 );
        component4.addDependency( component5 );
        component5.addDependency( component3 ); // Cycle
        
        try
        {
            DirectedAcyclicGraphVerifier.topologicalSort( vertices );
            fail( "Did not detect the expected cyclic dependency" );
        }
        catch ( CyclicDependencyException cde )
        {
            //Success!
        }
    }
        
    
    public void testSortDAG() throws Exception
    {
        Vertex component1 = new Vertex( "Component1" );
        Vertex component2 = new Vertex( "Component2" );
        Vertex component3 = new Vertex( "Component3" );
        Vertex component4 = new Vertex( "Component4" );
        Vertex component5 = new Vertex( "Component5" );

        component1.addDependency( component2 );
        component1.addDependency( component3 );

        component3.addDependency( component4 );

        component5.addDependency( component2 );
        component5.addDependency( component4 );

        List vertices = new ArrayList( 5 );
        vertices.add( component1 );
        vertices.add( component2 );
        vertices.add( component3 );
        vertices.add( component4 );
        vertices.add( component5 );

        DirectedAcyclicGraphVerifier.topologicalSort( vertices );
        verifyGraphOrders( vertices );
        verifyListOrder( vertices );

        Collections.shuffle( vertices );
        DirectedAcyclicGraphVerifier.topologicalSort( vertices );
        verifyGraphOrders( vertices );
        verifyListOrder( vertices );

        component4.addDependency( component1 );
        Collections.shuffle( vertices );

        try
        {
            DirectedAcyclicGraphVerifier.topologicalSort( vertices );
            fail( "Did not detect the expected cyclic dependency" );
        }
        catch ( CyclicDependencyException cde )
        {
            //Success!
        }
    }
    
    private void verifyGraphOrders( List vertices )
    {
        for ( Iterator iter = vertices.iterator(); iter.hasNext(); )
        {
            Vertex v = (Vertex)iter.next();
            
            // Make sure that the orders of all dependencies are less than
            //  the order of v.
            for ( Iterator iter2 = v.getDependencies().iterator(); iter2.hasNext(); )
            {
                Vertex dv = (Vertex)iter2.next();
                assertTrue( "The order of " + dv.getName() + " (" + dv.getOrder() + ") should be "
                    + "less than the order of " + v.getName() + " (" + v.getOrder() + ")",
                    dv.getOrder() < v.getOrder() );
            }
        }
    }
    
    private void verifyListOrder( List vertices )
    {
        Vertex[] ary = new Vertex[vertices.size()];
        vertices.toArray( ary );
        for ( int i = 1; i < ary.length; i++ )
        {
            assertTrue( "The order of vertex #" + ( i - 1 ) + " (" + ary[i - 1].getOrder() + ") "
                + "should be <= the order of vertex #" + ( i ) + " (" + ary[i].getOrder() + ")",
                ary[i - 1].getOrder() <= ary[i].getOrder() );
        }
    }
}
