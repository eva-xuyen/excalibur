/* 
 * Copyright 2002-2004 The Apache Software Foundation
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

package org.apache.excalibur.instrument.test;

import junit.framework.TestCase;

import org.apache.excalibur.instrument.CounterInstrument;

/**
 * Test of the CounterInstrument instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public class CounterInstrumentTestCase
    extends TestCase
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public CounterInstrumentTestCase( String name )
    {
        super( name );
    }
    
    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    
    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    public void testSimpleIncrementDisconnected() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        assertEquals( "A disconnected instrument should not be active.", ci.isActive(), false );
        
        ci.increment( 1 );
    }
    
    public void testCount1IncrementDisconnected() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        assertEquals( "A disconnected instrument should not be active.", ci.isActive(), false );
        
        ci.increment( 1 );
    }
    
    public void testCount0IncrementDisconnected() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        try
        {
            ci.increment( 0 );
            fail( "calling increment with a count of 0 should fail." );
        }
        catch ( IllegalArgumentException e )
        {
            // Ok
        }
    }
    
    public void testCountNegIncrementDisconnected() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        try
        {
            ci.increment( -1 );
            fail( "calling increment with a negative count should fail." );
        }
        catch ( IllegalArgumentException e )
        {
            // Ok
        }
    }
    
    public void testSimpleIncrementConnectedInactive() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        ci.setInstrumentProxy( proxy );
        
        assertEquals( "The instrument should not be active.", ci.isActive(), false );
        
        ci.increment();
        
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 1 );
        
        ci.increment();
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 2 );
    }
    
    public void testCount1IncrementConnectedInactive() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        ci.setInstrumentProxy( proxy );
        
        assertEquals( "The instrument should not be active.", ci.isActive(), false );
        
        ci.increment( 1 );
        
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 1 );
        
        ci.increment( 2 );
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 3 );
    }
    
    public void testSimpleIncrementConnectedActive() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        ci.setInstrumentProxy( proxy );
        proxy.activate();
        
        assertEquals( "The instrument should br active.", ci.isActive(), true );
        
        ci.increment();
        
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 1 );
    }
    
    public void testCount1IncrementConnectedActive() throws Exception
    {
        CounterInstrument ci = new CounterInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        ci.setInstrumentProxy( proxy );
        proxy.activate();

        assertEquals( "The instrument should br active.", ci.isActive(), true );
        
        ci.increment( 1 );
        
        assertEquals( "The expected count was incorrect.", proxy.getValue(), 1 );
    }
}

