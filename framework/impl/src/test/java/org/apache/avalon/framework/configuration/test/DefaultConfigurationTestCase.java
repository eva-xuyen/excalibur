/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.configuration.test;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.ConfigurationUtil;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.MutableConfiguration;

/**
 * Test the basic public methods of DefaultConfiguration.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class DefaultConfigurationTestCase extends TestCase
{
    private DefaultConfiguration m_configuration;
    
    public DefaultConfigurationTestCase()
    {
        this("DefaultConfiguration Test Case");
    }
    
    public DefaultConfigurationTestCase( String name )
    {
        super( name );
    }
    
    public void setUp()
    {
        m_configuration = new DefaultConfiguration( "a", "b" );
    }
    
    public void tearDowm()
    {
        m_configuration = null;
    }
    
    public void testGetValue()
        throws Exception
    {
        final String orgValue = "Original String";
        m_configuration.setValue( orgValue );
        assertEquals( orgValue, m_configuration.getValue() );
    }
    
    public void testGetValueAsInteger()
        throws Exception
    {
        final int orgValue = 55;
        final String strValue = Integer.toHexString( orgValue );
        m_configuration.setValue( "0x" + strValue );
        assertEquals( orgValue, m_configuration.getValueAsInteger() );
    }
    
    public void testGetValueAsBoolen()
        throws Exception
    {
        final boolean b = true;
        m_configuration.setValue("TrUe");
        assertEquals( b, m_configuration.getValueAsBoolean() );
    }
    
    public void testGetAttribute()
        throws Exception
    {
        final String key = "key";
        final String value = "original value";
        final String defaultStr = "default";
        m_configuration.setAttribute( key, value );
        assertEquals( value, m_configuration.getAttribute( key, defaultStr ) );
        assertEquals(defaultStr , m_configuration.getAttribute( "newKey", defaultStr ) );
    }
    
    public void testMakeReadOnly()
    {
        final String key = "key";
        final String value = "original value";
        String exception = "exception not thrown";
        final String exceptionStr ="Configuration is read only";
        m_configuration.makeReadOnly();
        
        try
        {
            m_configuration.setAttribute( key, value );
        }
        catch( final IllegalStateException ise )
        {
            exception = exceptionStr;
        }
        
        assertEquals( exception, exceptionStr );
    }
    
    public void testAddRemoveChild()
    {
        final String childName = "child";
        final Configuration child = new DefaultConfiguration( childName, "child location" );
        
        m_configuration.addChild( child );
        assertEquals( child, m_configuration.getChild( childName ) );
        
        m_configuration.removeChild( child );
        assertEquals( null, m_configuration.getChild( childName, false ) );
    }
    
    public void testCopying() throws Exception
    {
        DefaultConfiguration root = new DefaultConfiguration( "root", "0:0", "http://root", "root" );
        root.setAttribute( "attr1", "1" );
        root.setAttribute( "attr2", "2" );
        
        DefaultConfiguration child1 = new DefaultConfiguration( "child1", "0:1", "http://root/child1", "child1" );
        DefaultConfiguration child2 = new DefaultConfiguration( "child2", "0:2", "http://root/child2", "child2" );
        
        root.addChild( child1 );
        root.addChild( child2 );
        
        root.makeReadOnly();
        
        DefaultConfiguration modifiableRoot = new DefaultConfiguration( root );
        assertTrue( ConfigurationUtil.equals( root, modifiableRoot ) );
        
        modifiableRoot.setAttribute( "attr1", "0" );
        
        assertEquals( "0", modifiableRoot.getAttribute( "attr1" ) );
        
        DefaultConfiguration modifiableChild1 = new DefaultConfiguration( root.getChild("child1") );
        modifiableChild1.setValue( "1" );
        
        modifiableRoot.removeChild( modifiableRoot.getChild("child1") );
        modifiableRoot.addChild( modifiableChild1 );
        
        assertEquals( "1", modifiableRoot.getChild( "child1" ).getValue() );
    }
    
    public void testConvenienceSetters() throws Exception
    {
        DefaultConfiguration config = new DefaultConfiguration( "root", "0:0", "http://root", "root" );
        config.setAttribute( "integer", 12 );
        config.setAttribute( "long", 8000000000L );
        config.setAttribute( "float", 1.2345679f );
        config.setAttribute( "double", 1.2345678901234567 );
        config.setAttribute( "boolean", true );
        config.setAttribute( "string", "string" );
        
        assertEquals( "12", config.getAttribute("integer") );
        assertEquals( "8000000000", config.getAttribute("long") );
        assertEquals( 1.2345679f, config.getAttributeAsFloat("float"), 0 );
        assertEquals( 1.2345678901234567, config.getAttributeAsDouble("double"), 0 );
        assertEquals( "string", config.getAttribute("string") );
        assertEquals( "true", config.getAttribute("boolean") );
        
        assertEquals( 12, config.getAttributeAsInteger("integer") );
        assertEquals( 8000000000L, config.getAttributeAsLong("long") );
        assertEquals( "string", config.getAttribute("string") );
        assertEquals( true, config.getAttributeAsBoolean("boolean") );
    }
    
    public void testSetToNull() throws Exception
    {
        DefaultConfiguration config = new DefaultConfiguration( "root", "0:0", "http://root", "root" );
        config.setAttribute( "integer", "12" );
        assertEquals( "12", config.getAttribute("integer") );
        
        config.setAttribute( "integer", null );
        try 
        {
            config.getAttribute("integer");
            fail( "attribute 'integer' was present despite it being set to null" );
        } 
        catch( ConfigurationException e )
        {
            // OK, this is what we expect - the attribute wasn't found.
        }
    }
    
    public void testMutable() throws Exception   
    {   
        MutableConfiguration root = new DefaultConfiguration( "root", "-" );   
        root.setAttribute( "root1", "root1" );   
        root.setAttribute( "root2", "root2" );   
        root.getMutableChild( "child1" ).setAttribute( "child1-attr1", "child1-attr1" );   
        root.getMutableChild( "child1" ).setAttribute( "child1-attr2", "child1-attr2" );   
        root.getMutableChild( "child2" ).setAttribute( "child2-attr1", "child2-attr1" );   
        root.getMutableChild( "child2" ).setAttribute( "child2-attr2", "child2-attr2" );   
        
        assertEquals( "root1", root.getAttribute( "root1" ) );   
        assertEquals( "root2", root.getAttribute( "root2" ) );   
        assertEquals( "child1-attr1", root.getChild( "child1" ).getAttribute( "child1-attr1" ) );   
        assertEquals( "child1-attr2", root.getChild( "child1" ).getAttribute( "child1-attr2" ) );   
        assertEquals( "child2-attr1", root.getChild( "child2" ).getAttribute( "child2-attr1" ) );   
        assertEquals( "child2-attr2", root.getChild( "child2" ).getAttribute( "child2-attr2" ) );   
        
        assertEquals( null, root.getMutableChild( "child3", false ) );   
        
        assertEquals( 2, root.getChildren().length );   
        
        assertEquals( 2, root.getMutableChildren().length );   
        assertEquals( 1, root.getMutableChildren( "child1" ).length );   
        assertEquals( 1, root.getMutableChildren( "child2" ).length );   
        assertTrue( root.getMutableChildren( "child1" )[0] == root.getChild( "child1" ) );   
        
        // Add an immutable child.   
        DefaultConfiguration immutableChild = new DefaultConfiguration( "immutable-child", "-" );   
        immutableChild.makeReadOnly();   
        
        try   
        {   
            immutableChild.setAttribute( "attr", "attr" );   
            fail( "Read-only DefaultConfiguration wasn't read-only!" );   
        }   
        catch (IllegalStateException ise)   
        {   
            // expected   
        }   
        
        root.addChild( immutableChild );   
        
        // OK, ask to have it back.   
        root.getMutableChild( "immutable-child" ).setAttribute( "attr", "attr" );   
        
        assertEquals( 1, root.getChildren( "immutable-child" ).length );   
        assertEquals( "attr", root.getChild( "immutable-child" ).getAttribute( "attr" ) );   
    }     
    
    public void testEquals()
        throws Exception
    {
        DefaultConfiguration a = createSimple( "a1", "a2" );
        DefaultConfiguration b = createSimple( "a1", "a2" );
        
        assertEquals( "equal test", a, b );
        
        String value1 = a.getChild( "child" ).getValue();
        String value2 = b.getChild( "child" ).getValue();
        
        assertEquals( "value equality", value1, value2 );
        
        a = createSimple( "a1", "a2" );
        b = createSimple( "a2", "a1" );
        
        assertTrue( "order test", ! a.equals( b ) );
        
        value1 = a.getChild( "child" ).getValue();
        value2 = b.getChild( "child" ).getValue();
        
        assertEquals( "value equality", "a1", value1 );
        assertEquals( "value equality", "a2", value2 );
    }
    
    private DefaultConfiguration createSimple( String value1, String value2 )
        throws Exception
    {
        DefaultConfiguration conf = new DefaultConfiguration( "root", "0:0", "http://root", "root" );
        DefaultConfiguration child1 = new DefaultConfiguration( "child", "0:1", "http://root/child", "child" );
        child1.setValue( value1 );
        child1.setAttribute( value1, value1 );
        child1.setAttribute( value2, value2 );
        child1.setAttribute( value1, value2 );
        child1.setAttribute( value2, value1 );
        conf.addChild( child1 );
        
        DefaultConfiguration child2 = new DefaultConfiguration( "child", "0:2", "http://root/child", "child" );
        child2.setValue( value2 );
        child1.setAttribute( value2, value2 );
        child1.setAttribute( value1, value1 );
        child1.setAttribute( value2, value1 );
        child1.setAttribute( value1, value2 );
        child2.setAttribute( value2, value2 );
        conf.addChild( child2 );
        
        conf.makeReadOnly();
        return conf;
    }
}

