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

package org.apache.excalibur.instrument.manager.impl.test;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.ConsoleLogger;

import org.apache.excalibur.instrument.manager.impl.DefaultInstrumentManagerImpl;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentException;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentableException;

/**
 * Test of the DefaultInstrumentManagerImpl.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:36 $
 */
public class DefaultInstrumentManagerImplTestCase
    extends TestCase
{
    private DefaultInstrumentManagerImpl m_instrumentManager;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public DefaultInstrumentManagerImplTestCase( String name )
    {
        super( name );
    }
    
    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    public void setUp()
        throws Exception
    {
        System.out.println( "setUp()" );
        
        super.setUp();
        
        DefaultConfiguration instrumentConfig = new DefaultConfiguration( "instrument" );
        
        m_instrumentManager = new DefaultInstrumentManagerImpl();
        m_instrumentManager.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG ) );
        m_instrumentManager.configure( instrumentConfig );
        m_instrumentManager.initialize();
    }
    
    public void tearDown()
        throws Exception
    {
        System.out.println( "tearDown()" );
        m_instrumentManager.dispose();
        m_instrumentManager = null;
        
        super.tearDown();
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private void assertInstrumentableExists( String name )
    {
        InstrumentableDescriptor descriptor =
            m_instrumentManager.locateInstrumentableDescriptor( name );
        assertEquals( "Looked up instrumentable name incorrect.", descriptor.getName(), name );
    }
    
    private void assertInstrumentableNotExists( String name )
    {
        try
        {
            m_instrumentManager.locateInstrumentableDescriptor( name );
            fail( "Found an instrumentable named " + name + " when it should not have existed." );
        }
        catch( NoSuchInstrumentableException e )
        {
            // Ok
        }
    }
    
    private void assertInstrumentExists( String name )
    {
        InstrumentDescriptor descriptor =
            m_instrumentManager.locateInstrumentDescriptor( name );
        assertEquals( "Looked up instrument name incorrect.", descriptor.getName(), name );
    }
    
    private void assertInstrumentNotExists( String name )
    {
        try
        {
            m_instrumentManager.locateInstrumentDescriptor( name );
            fail( "Found an instrument named " + name + " when it should not have existed." );
        }
        catch( NoSuchInstrumentException e )
        {
            // Ok
        }
    }
    
    /* Never called
    private void assertInstrumentSampleExists( String name )
    {
        InstrumentSampleDescriptor descriptor =
            m_instrumentManager.locateInstrumentSampleDescriptor( name );
        assertEquals( "Looked up instrument sample name incorrect.", descriptor.getName(), name );
    }
    */
    /* Never called
    private void assertInstrumentSampleNotExists( String name )
    {
        try
        {
            InstrumentSampleDescriptor descriptor =
                m_instrumentManager.locateInstrumentSampleDescriptor( name );
            fail( "Found an instrument sample named " + name + " when it should not have existed." );
        }
        catch( NoSuchInstrumentSampleException e )
        {
            // Ok
        }
    }
    */
    
    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    public void testCreateDestroy() throws Exception
    {
    }

    public void testLookupDefaultInstruments() throws Exception
    {
        // Look for elements which should always exist.
        assertInstrumentableExists( "instrument-manager" );
        assertInstrumentExists( "instrument-manager.total-memory" );
        assertInstrumentExists( "instrument-manager.free-memory" );
        assertInstrumentExists( "instrument-manager.memory" );
        assertInstrumentExists( "instrument-manager.active-thread-count" );
        
        // Look for elements which should not exist.
        assertInstrumentableNotExists( "instrument-manager.total-memory" );
        assertInstrumentableNotExists( "instrument-manager.foobar" );
        assertInstrumentableNotExists( "foobar" );
        assertInstrumentNotExists( "instrument-manager.foobar" );
    }
    
    public void testLookupSamples() throws Exception
    {
        
    }
}

