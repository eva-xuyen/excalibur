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

import org.apache.excalibur.instrument.ValueInstrument;

/**
 * Test of the ValueInstrument instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public class ValueInstrumentTestCase
    extends TestCase
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public ValueInstrumentTestCase( String name )
    {
        super( name );
    }
    
    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    
    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    public void testSimpleValueDisconnected() throws Exception
    {
        ValueInstrument vi = new ValueInstrument( "testInstrument" );
        
        assertEquals( "A disconnected instrument should not be active.", vi.isActive(), false );
        
        vi.setValue( 0 );
        vi.setValue( -1 );
        vi.setValue( 1 );
    }
    
    public void testSimpleValueConnectedInactive() throws Exception
    {
        ValueInstrument vi = new ValueInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        vi.setInstrumentProxy( proxy );
        
        assertEquals( "The instrument should not be active.", vi.isActive(), false );
        
        vi.setValue( 0 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 0 );
        
        vi.setValue( -1 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), -1 );
        
        vi.setValue( 1 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 1 );
    }
    
    public void testLargeValueConnectedInactive() throws Exception
    {
        ValueInstrument vi = new ValueInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        vi.setInstrumentProxy( proxy );
        
        assertEquals( "The instrument should not be active.", vi.isActive(), false );
        
        vi.setValue( 1313123123 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 1313123123 );
        
        vi.setValue( -325353253 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), -325353253 );
    }
    
    public void testSimpleValueConnectedActive() throws Exception
    {
        ValueInstrument vi = new ValueInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        vi.setInstrumentProxy( proxy );
        proxy.activate();
        
        assertEquals( "The instrument should br active.", vi.isActive(), true );
        
        vi.setValue( 0 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 0 );
        
        vi.setValue( -1 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), -1 );
        
        vi.setValue( 1 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 1 );
    }
    
    public void testLargeValueConnectedActive() throws Exception
    {
        ValueInstrument vi = new ValueInstrument( "testInstrument" );
        TestInstrumentProxy proxy = new TestInstrumentProxy();
        vi.setInstrumentProxy( proxy );
        proxy.activate();
        
        assertEquals( "The instrument should br active.", vi.isActive(), true );
        
        vi.setValue( 1313123123 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), 1313123123 );
        
        vi.setValue( -325353253 );
        assertEquals( "The expected value was incorrect.", proxy.getValue(), -325353253 );
    }
}

