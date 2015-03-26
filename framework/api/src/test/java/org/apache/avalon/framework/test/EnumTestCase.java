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

package org.apache.avalon.framework.test;

import junit.framework.TestCase;
import org.apache.avalon.framework.Enum;

import java.util.Map;
import java.util.HashMap;

/**
 * TestCase for {@link Enum}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: EnumTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class EnumTestCase
    extends TestCase
{
    private final static class Color extends Enum 
    {
        public static final Color RED = new Color( "Red" );
        public static final Color GREEN = new Color( "Green" );
        public static final Color BLUE = new Color( "Blue" );
        
        public Color( final String color )
        {
            super( color );
        }

        public Color( final String color, Map stuff )
        {
            super( color, stuff );
        }
    }
    
    private final static class OtherColor extends Enum 
    {
        public static final OtherColor RED = new OtherColor( "Red" );
        public static final OtherColor GREEN = new OtherColor( "Green" );
        public static final OtherColor BLUE = new OtherColor( "Blue" );
        
        public OtherColor( final String color )
        {
            super( color );
        }

        public OtherColor( final String color, Map stuff )
        {
            super( color, stuff );
        }
    }
    
    public EnumTestCase( final String name )
    {
        super( name );
    }
    
    public void testConstructor()
    {
        assertNotNull( new Color( "blah", null ) );

        Map entries = new HashMap();

        Color c = new Color( "blah", entries );

        assertTrue( entries.containsKey("blah") );
        assertTrue( entries.containsValue(c) );

        OtherColor c2 = new OtherColor( "blah", entries );
        assertTrue( entries.containsKey("blah") );
        assertFalse( entries.containsValue(c) );
        assertTrue( entries.containsValue(c2) );
    }

    public void testEquals()
    {
        assertTrue( Color.RED.equals( Color.RED ) );
        assertTrue( Color.GREEN.equals( Color.GREEN ) );
        assertTrue( Color.BLUE.equals( Color.BLUE ) );

        assertTrue( !OtherColor.RED.equals( Color.RED ) );
        assertTrue( !OtherColor.GREEN.equals( Color.GREEN ) );
        assertTrue( !OtherColor.BLUE.equals( Color.BLUE ) );

        assertTrue( !Color.RED.equals( OtherColor.RED ) );
        assertTrue( !Color.GREEN.equals( OtherColor.GREEN ) );
        assertTrue( !Color.BLUE.equals( OtherColor.BLUE ) );

        assertTrue( !Color.RED.equals( Color.GREEN ) );
        assertTrue( !Color.GREEN.equals( Color.BLUE ) );
        assertTrue( !Color.BLUE.equals( Color.RED ) );

        assertTrue( !Color.BLUE.equals( null ) );

        assertTrue( new Color(null).equals( new Color( null ) ) );
        assertFalse( new Color(null).equals( new Color( "hi" ) ) );
        assertFalse( new Color("hi").equals( new Color( null ) ) );
    }

    public void testHashCode()
    {
        assertTrue( Color.RED.hashCode() ==  Color.RED.hashCode() );
        assertTrue( Color.GREEN.hashCode() ==  Color.GREEN.hashCode() );
        assertTrue( Color.BLUE.hashCode() ==  Color.BLUE.hashCode() );

        assertTrue( OtherColor.RED.hashCode() !=  Color.RED.hashCode() );
        assertTrue( OtherColor.GREEN.hashCode() !=  Color.GREEN.hashCode() );
        assertTrue( OtherColor.BLUE.hashCode() !=  Color.BLUE.hashCode() );

        assertTrue( Color.RED.hashCode() !=  OtherColor.RED.hashCode() );
        assertTrue( Color.GREEN.hashCode() !=  OtherColor.GREEN.hashCode() );
        assertTrue( Color.BLUE.hashCode() !=  OtherColor.BLUE.hashCode() );

        assertTrue( Color.RED.hashCode() !=  Color.GREEN.hashCode() );
        assertTrue( Color.GREEN.hashCode() !=  Color.BLUE.hashCode() );
        assertTrue( Color.BLUE.hashCode() !=  Color.RED.hashCode() );
    }

    public void testGet()
    {
        assertEquals( "Red", Color.RED.getName() );
        assertNull( (new Color(null)).getName() );
    }

    public void testToString()
    {
        assertTrue( Color.RED.toString().indexOf( "Red") != -1 );
        assertTrue( Color.RED.toString().indexOf( Color.class.getName() ) != -1 );

        Color c = new Color(null);
        assertTrue( c.toString().indexOf( "null") != -1 );

    }
}
