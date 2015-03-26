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
import org.apache.avalon.framework.Version;

/**
 * TestCase for {@link Version}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: VersionTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class VersionTestCase
    extends TestCase
{
    public VersionTestCase( final String name )
    {
        super( name );
    }

    public void testValidVersionString()
    {
        final Version v1 = Version.getVersion( "1" );
        assertTrue( new Version( 1, 0, 0 ).equals( v1 ) );

        final Version v2 = Version.getVersion( "0.3" );
        assertTrue( new Version( 0, 3, 0 ).equals( v2 ) );

        final Version v3 = Version.getVersion( "78.10.03" );
        assertTrue( new Version( 78, 10, 3 ).equals( v3 ) );


        try
        {
            final Version v4 = Version.getVersion( null );

            fail( "Expected an exception!" );
        }
        catch( NullPointerException th )
        {}
    }

    public void testInvalidVersionString()
    {
        try
        {
            assertEquals( -1, Version.getVersion( "" ).getMajor() );
        }
        catch ( final IllegalArgumentException iae )
        {
            fail( "Empty string is legal version string" );
        }

        try
        {
            Version.getVersion( "1.F" );
            Version.getVersion( "1.0-dev" );
            fail( "Version string do contains only '.' and number" );
        }
        catch ( final NumberFormatException nfe )
        {
            //OK
        }
    }

    public void testComplies()
    {
        final Version v0 = new Version( -1, 0 , 0 );
        final Version v1 = new Version( 1, 3 , 6 );
        final Version v2 = new Version( 1, 3 , 7 );
        final Version v3 = new Version( 1, 4 , 0 );
        final Version v4 = new Version( 2, 0 , 1 );

        assertTrue(   v1.complies( v0 ) );
        assertTrue(   v4.complies( v0 ) );
        assertTrue( ! v0.complies( v1 ) );
        assertTrue( ! v0.complies( v4 ) );
        
        assertTrue(   v1.complies( v1 ) );
        assertTrue( ! v1.complies( v2 ) );
        assertTrue(   v2.complies( v1 ) );
        assertTrue( ! v1.complies( v3 ) );
        assertTrue(   v3.complies( v1 ) );
        assertTrue( ! v1.complies( v4 ) );
        assertTrue( ! v4.complies( v1 ) );

        assertTrue( ! v4.complies( null ) );
    }
    
    public void testHashCode()
    {
        final Version v1 = new Version( 5, 1, 0 );
        final Version v2 = new Version( 1, 0, 3 );
        final Version v3 = new Version( 1, 0, 3 );
        
        assertEquals( calculateHash(v1), v1.hashCode() );
        assertEquals( calculateHash(v2), v2.hashCode() );
        
        assertTrue( v1.hashCode() != v2.hashCode() );
        assertTrue( ! v1.equals(v2) );
        
        assertEquals( v2.hashCode(), v3.hashCode() );
        assertEquals( v2, v3 );
    }
    
    public void testComparable()
    {
        final Version v1 = new Version( 1, 0, 0 );
        final Version v2 = new Version( 2, 0, 0 );
        final Version v3 = new Version( 2, 1, 0 );
        final Version v4 = new Version( 2, 1, 1 );
        final Version v5 = new Version( 1, 0, 0 );
        
        assertEquals( 0, v1.compareTo(v5) );
        assertEquals( 0, v5.compareTo(v1) );
        
        assertEquals( -1, v1.compareTo(v2) );
        assertEquals( 1, v2.compareTo(v1) );
        
        assertEquals( -1, v2.compareTo(v3) );
        assertEquals( 1, v3.compareTo(v2) );
        
        assertEquals( -1, v3.compareTo(v4) );
        assertEquals( 1, v4.compareTo(v3) );

        try
        {
            v4.compareTo(null);
            fail( "Expected an exception!" );
        }
        catch( NullPointerException th )
        {}
    }

    public void testEquals()
    {
        assertFalse( new Version( 1, 0, 0 ).equals( this ) );
        assertFalse( new Version( 1, 0, 0 ).equals( null ) );
    }

    public void testToString()
    {
        assertEquals( "1.0.0", new Version( 1, 0, 0 ).toString() );
        assertEquals( "230.21.-123456", new Version( 230, 21, -123456 ).toString() );
    }

    private int calculateHash(final Version v) {
        int hash = v.getMajor();
        hash >>>= 17;
        hash += v.getMinor();
        hash >>>= 17;
        hash += v.getMicro();
        return hash;
    }
}
