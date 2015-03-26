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

package org.apache.avalon.fortress.util.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.util.CompositeException;

/**
 * CompositeExceptionTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CompositeExceptionTestCase extends TestCase
{
    private Exception[] m_exceptions;

    public CompositeExceptionTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_exceptions = new Exception[2];
        m_exceptions[0] = new RuntimeException( "Test1" );
        m_exceptions[1] = new RuntimeException( "Test2" );
    }

    public void testRegularCreation()
    {
        CompositeException exc = new CompositeException( m_exceptions );
        assertNotNull( exc );
        assertNotNull( exc.getMessage() );
        assertTrue( null == exc.getCause() );
        assertNotNull( exc.getExceptions() );

        final StringBuffer msg = new StringBuffer();
        for ( int i = 0; i < m_exceptions.length; i++ )
        {
            if ( i > 0 ) msg.append( '\n' );
            msg.append( m_exceptions[i].getMessage() );
        }
        final String message = msg.toString();

        assertEquals( message, exc.getMessage() );

        Exception[] exceptions = exc.getExceptions();
        assertEquals( m_exceptions.length, exceptions.length );

        for ( int i = 0; i < exceptions.length; i++ )
        {
            assertEquals( m_exceptions[i], exceptions[i] );
        }
    }

    public void testNestedCreation()
    {
        final String message = "Message";
        CompositeException exc = new CompositeException( m_exceptions, message );
        assertNotNull( exc );
        assertNotNull( exc.getMessage() );
        assertTrue( null == exc.getCause() );
        assertNotNull( exc.getExceptions() );

        assertEquals( message, exc.getMessage() );

        Exception[] exceptions = exc.getExceptions();
        assertEquals( m_exceptions.length, exceptions.length );

        for ( int i = 0; i < exceptions.length; i++ )
        {
            assertEquals( m_exceptions[i], exceptions[i] );
        }
    }

    public void testIllegalArgument()
    {
        try
        {
            new CompositeException( null );
            fail( "Did not throw an IllegalArgumentException" );
        }
        catch ( IllegalArgumentException iae )
        {
            // SUCCESS!!
        }
        catch ( Exception e )
        {
            fail( "Threw the wrong exception: " + e.getClass().getName() );
        }

        try
        {
            new CompositeException( new Exception[]{} );
            fail( "Did not throw an IllegalArgumentException" );
        }
        catch ( IllegalArgumentException iae )
        {
            // SUCCESS!!
        }
        catch ( Exception e )
        {
            fail( "Threw the wrong exception: " + e.getClass().getName() );
        }
    }
}
