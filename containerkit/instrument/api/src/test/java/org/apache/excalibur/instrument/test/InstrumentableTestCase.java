/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 * Test of the AbstractInstrumentable instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public class InstrumentableTestCase
    extends TestCase
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public InstrumentableTestCase( String name )
    {
        super( name );
    }
    
    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private void generalTest( Instrument[] instruments, Instrumentable[] children )
        throws Exception
    {
        AbstractInstrumentableImpl impl = new AbstractInstrumentableImpl( "base" );
        
        // Set the name
        impl.setInstrumentableName( "test" );
        
        // Add the instruments
        for ( int i = 0; i < instruments.length; i++ )
        {
            impl.addInstrument( instruments[i] );
        }
        
        // Add the child instrumentables
        for ( int i = 0; i < children.length; i++ )
        {
            impl.addChildInstrumentable( children[i] );
        }
        
        // Verify the name
        assertEquals( "Instrumentable name incorrect.", impl.getInstrumentableName(), "test" );
        
        
        // Verify the instruments
        Instrument[] implInstruments = impl.getInstruments();
        assertEquals( "The number of instruments is not correct.",
            implInstruments.length, instruments.length );
        for ( int i = 0; i < instruments.length; i++ )
        {
            assertEquals( "Instrument[i] is not correct.", implInstruments[i], instruments[i] );
        }
        
        // Make sure that instruments can no longer be added
        try
        {
            impl.addInstrument( new CounterInstrument( "bad" ) );
            fail( "Should not have been able to add more instruments" );
        }
        catch ( IllegalStateException e )
        {
            // Ok
        }
        
        
        // Verify the child instrumentables
        Instrumentable[] implChildren = impl.getChildInstrumentables();
        assertEquals( "The number of child instrumentables is not correct.",
            implChildren.length, children.length );
        for ( int i = 0; i < children.length; i++ )
        {
            assertEquals( "Child[i] is not correct.", implChildren[i], children[i] );
        }
        
        // Make sure that child instrumentables can no longer be added
        try
        {
            impl.addChildInstrumentable( new AbstractInstrumentableImpl( "bad" ) );
            fail( "Should not have been able to add more child instrumentables" );
        }
        catch ( IllegalStateException e )
        {
            // Ok
        }
    }
    
    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    public void testEmpty() throws Exception
    {
        Instrument[] instruments = new Instrument[] {};
        Instrumentable[] children = new Instrumentable[] {};
        
        generalTest( instruments, children );
    }
    
    public void test1Instrument() throws Exception
    {
        Instrument[] instruments = new Instrument[]
            {
                new CounterInstrument( "c1" )
            };
        Instrumentable[] children = new Instrumentable[] {};
        
        generalTest( instruments, children );
    }
    
    public void testNInstrument() throws Exception
    {
        Instrument[] instruments = new Instrument[]
            {
                new CounterInstrument( "c1" ),
                new ValueInstrument( "v1" ),
                new CounterInstrument( "c2" ),
                new ValueInstrument( "v2" ),
                new CounterInstrument( "c3" ),
                new ValueInstrument( "v3" ),
                new CounterInstrument( "c4" ),
                new ValueInstrument( "v4" )
            };
        Instrumentable[] children = new Instrumentable[] {};
        
        generalTest( instruments, children );
    }
    
    public void test1ChildInstrumentable() throws Exception
    {
        Instrument[] instruments = new Instrument[] {};
        Instrumentable[] children = new Instrumentable[]
            {
                new AbstractInstrumentableImpl( "i1" )
            };
        
        generalTest( instruments, children );
    }
    
    public void testNChildInstrumentable() throws Exception
    {
        Instrument[] instruments = new Instrument[] {};
        Instrumentable[] children = new Instrumentable[]
            {
                new AbstractInstrumentableImpl( "i1" ),
                new AbstractInstrumentableImpl( "i2" ),
                new AbstractInstrumentableImpl( "i3" ),
                new AbstractInstrumentableImpl( "i4" ),
                new AbstractInstrumentableImpl( "i5" ),
                new AbstractInstrumentableImpl( "i6" )
            };
        
        generalTest( instruments, children );
    }
}

