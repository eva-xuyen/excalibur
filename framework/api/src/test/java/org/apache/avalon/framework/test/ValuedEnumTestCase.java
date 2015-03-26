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

import org.apache.avalon.framework.ValuedEnum;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ValuedEnumTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class ValuedEnumTestCase extends TestCase
{
    private final static class Color extends ValuedEnum
    {
        public static final Color RED = new Color( "Red", 0 );
        public static final Color RED_NEGATIVE = new Color( "Red", -1 );
        public static final Color GREEN = new Color( "Green", 1 );
        public static final Color BLUE = new Color( "Blue", 2 );
        public static final Color FAKE_BLUE = new Color( "Blue", 3 );

        public Color( final String color, final int value )
        {
            super( color, value );
        }

        public Color( final String color, final int value, Map stuff )
        {
            super( color, value, stuff );
        }
    }

    private final static class OtherColor extends ValuedEnum
    {
        public static final OtherColor RED = new OtherColor( "Red", 0 );
        public static final OtherColor RED_NEGATIVE = new OtherColor( "Red", -1 );
        public static final OtherColor GREEN = new OtherColor( "Green", 1 );
        public static final OtherColor BLUE = new OtherColor( "Blue", 2 );

        public OtherColor( final String color, final int value )
        {
            super( color, value );
        }

        public OtherColor( final String color, final int value, Map stuff )
        {
            super( color, value, stuff );
        }
    }

    public ValuedEnumTestCase( final String name )
    {
        super( name );
    }

    public void testConstructor()
    {
        assertNotNull( new Color( "blah", 0, null ) );

        Map entries = new HashMap();

        Color c = new Color( "blah", 0, entries );

        assertTrue( entries.containsKey("blah") );
        assertTrue( entries.containsValue(c) );

        OtherColor c2 = new OtherColor( "blah", 0, entries );
        assertTrue( entries.containsKey("blah") );
        assertFalse( entries.containsValue(c) );
        assertTrue( entries.containsValue(c2) );
    }

    public void testEquals()
    {
        assertTrue( Color.RED.equals( Color.RED ) );
        assertTrue( Color.GREEN.equals( Color.GREEN ) );
        assertTrue( Color.BLUE.equals( Color.BLUE ) );

        assertTrue( ! Color.BLUE.equals( Color.FAKE_BLUE ) );
        
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

        assertTrue( new Color(null,0).equals( new Color( null,0 ) ) );
        assertFalse( new Color(null,0).equals( new Color( "hi",0 ) ) );
        assertFalse( new Color("hi",0).equals( new Color( null,0 ) ) );
/*
        // todo: is this _really_ desired?
        assertTrue( Color.RED.equals( Color.RED_NEGATIVE ) );
        assertTrue( Color.RED_NEGATIVE.equals( Color.RED ) );
        assertTrue( OtherColor.RED.equals( OtherColor.RED_NEGATIVE ) );
        assertTrue( OtherColor.RED_NEGATIVE.equals( OtherColor.RED ) );
*/        
    }

    public void testHashCode()
    {
        assertTrue( Color.RED.hashCode() ==  Color.RED.hashCode() );
        assertTrue( Color.GREEN.hashCode() ==  Color.GREEN.hashCode() );
        assertTrue( Color.BLUE.hashCode() ==  Color.BLUE.hashCode() );

        assertTrue( Color.BLUE.hashCode() !=  Color.FAKE_BLUE.hashCode() );
        
        assertTrue( OtherColor.RED.hashCode() !=  Color.RED.hashCode() );
        assertTrue( OtherColor.GREEN.hashCode() !=  Color.GREEN.hashCode() );
        assertTrue( OtherColor.BLUE.hashCode() !=  Color.BLUE.hashCode() );

        assertTrue( Color.RED.hashCode() !=  OtherColor.RED.hashCode() );
        assertTrue( Color.GREEN.hashCode() !=  OtherColor.GREEN.hashCode() );
        assertTrue( Color.BLUE.hashCode() !=  OtherColor.BLUE.hashCode() );

        assertTrue( Color.RED.hashCode() !=  Color.GREEN.hashCode() );
        assertTrue( Color.GREEN.hashCode() !=  Color.BLUE.hashCode() );
        assertTrue( Color.BLUE.hashCode() !=  Color.RED.hashCode() );

        // todo: is this _really_ desired?
/*        
        assertTrue( Color.RED.hashCode() ==Color.RED_NEGATIVE.hashCode() );
        assertTrue( Color.RED_NEGATIVE.hashCode() ==Color.RED.hashCode() );
        assertTrue( OtherColor.RED.hashCode() ==OtherColor.RED_NEGATIVE.hashCode() );
        assertTrue( OtherColor.RED_NEGATIVE.hashCode() ==OtherColor.RED.hashCode() );
*/        
    }

    public void testGet()
    {
        assertEquals( "Red", Color.RED.getName() );
        assertNull( (new Color(null,0)).getName() );
    }

    public void testToString()
    {
        assertTrue( Color.RED.toString().indexOf( "Red") != -1 );
        assertTrue( Color.RED.toString().indexOf( Color.class.getName() ) != -1 );

        Color c = new Color(null,0);
        assertTrue( c.toString().indexOf( "null") != -1 );

    }
}
