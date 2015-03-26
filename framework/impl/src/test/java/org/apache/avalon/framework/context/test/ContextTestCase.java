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
package org.apache.avalon.framework.context.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Resolvable;

/**
 * TestCase for Context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ContextTestCase
    extends TestCase
{
    private static class ResolvableString implements Resolvable
    {
        private final String m_content;

        public ResolvableString( final String content )
        {
            this.m_content = content;
        }

        public ResolvableString()
        {
            this( "This is a ${test}." );
        }

        public final Object resolve( final Context context )
            throws ContextException
        {
            int index = this.m_content.indexOf( "${" );

            if ( index < 0 )
            {
                return this.m_content;
            }

            StringBuffer buf = new StringBuffer( this.m_content.substring( 0, index ) );

            while ( index >= 0 && index <= this.m_content.length() )
            {
                index += 2;
                int end = this.m_content.indexOf( "}", index);

                if ( end < 0 )
                {
                    end = this.m_content.length();
                }

                buf.append( context.get( this.m_content.substring( index, end ) ) );
                end++;

                index = this.m_content.indexOf( "${", end ) + 2;

                if ( index < 2 )
                {
                    index = -1;
                    buf.append( this.m_content.substring( end, this.m_content.length() ) );
                }

                if ( index >=0 && index <= this.m_content.length() )
                {
                    buf.append( this.m_content.substring( end, index ) );
                }
            }

            return buf.toString();
        }
    }

    public ContextTestCase( final String name )
    {
        super( name );
    }

    public void testAddContext()
        throws Exception
    {
        final DefaultContext context = new DefaultContext();
        context.put( "key1", "value1" );
        assertTrue( "value1".equals( context.get( "key1" ) ) );
        context.put( "key1", "" );
        assertTrue( "".equals( context.get( "key1" ) ) );

        context.put( "key1", "value1" );
        context.makeReadOnly();

        try
        {
            context.put( "key1", "" );
            throw new AssertionFailedError( "You are not allowed to change a value after it has been made read only" );
        }
        catch ( IllegalStateException ise )
        {
            assertTrue( "Value is null", "value1".equals( context.get( "key1" ) ) );
        }
    }

    public void testResolveableObject()
        throws ContextException
    {
        final DefaultContext context = new DefaultContext();
        context.put( "key1", new ResolvableString() );
        context.put( "test", "Cool Test" );
        context.makeReadOnly();

        final Context newContext = (Context) context;
        assertTrue( "Cool Test".equals( newContext.get( "test" ) ) );
        assertTrue( ! "This is a ${test}.".equals( newContext.get( "key1" ) ) );
        assertTrue( "This is a Cool Test.".equals( newContext.get( "key1" ) ) );
    }

    public void testCascadingContext()
         throws ContextException
    {
        final DefaultContext parent = new DefaultContext();
        parent.put( "test", "ok test" );
        parent.makeReadOnly();
        final DefaultContext child = new DefaultContext( parent );
        child.put( "check", new ResolvableString("This is an ${test}.") );
        child.makeReadOnly();
        final Context context = (Context) child;

        assertTrue ( "ok test".equals( context.get( "test" ) ) );
        assertTrue ( ! "This is an ${test}.".equals( context.get( "check" ) ) );
        assertTrue ( "This is an ok test.".equals( context.get( "check" ) ) );
    }
    
    public void testHiddenItems()
        throws ContextException
    {
        final DefaultContext parent = new DefaultContext();
        parent.put( "test", "test" );
        parent.makeReadOnly();
        final DefaultContext child = new DefaultContext( parent );
        child.put( "check", "check" );
        final Context context = (Context) child;
        
        assertTrue ( "check".equals( context.get( "check" ) ) );
        assertTrue ( "test".equals( context.get( "test" ) ) );
                
        child.hide( "test" );
        try 
        {
            context.get( "test" );
            fail( "The item \"test\" was hidden in the child context, but could still be retrieved via get()." );
        }
        catch (ContextException ce)
        {
            // Supposed to be thrown.
        }
        
        child.makeReadOnly();
        
        try 
        {
            child.hide( "test" );
            fail( "hide() did not throw an exception, even though the context is supposed to be read-only." );
        }
        catch (IllegalStateException ise)
        {
            // Supposed to be thrown.
        }
    }
    
    public void testEquals()
        throws Exception
    {
        // Different set of parents.
        DefaultContext p1 = new DefaultContext();
        p1.put( "test", "CoolTest" );
        DefaultContext p2 = new DefaultContext();
        p2.put( "test", "Cool Test" );
        
        DefaultContext c1 = new DefaultContext( p1 );
        DefaultContext c2 = new DefaultContext( p1 );
        DefaultContext c3 = new DefaultContext( p1 );
        DefaultContext c4 = new DefaultContext( p1 );
        DefaultContext c5 = new DefaultContext( p2 );
        
        c1.put( "test", "Cool Test" );
        c2.put( "test", "Cool Test" );
        c3.put( "test", "Cool Test" );
        c3.put( "test2", "Cool Test" );
        c4.put( "test", "Cool Test" );
        c4.makeReadOnly();
        c5.put( "test", "Cool Test" );
        
        assertEquals( "Identical", c1, c2 );
        assertTrue( "ContextData", ! c1.equals( c3 ) );
        assertTrue( "ReadOnly", ! c1.equals( c4 ) );
        assertTrue( "Parent", ! c1.equals( c5 ) );
        
    }

    public void testHashcode()
        throws Exception
    {
        // Different set of parents.
        DefaultContext p1 = new DefaultContext();
        p1.put( "test", "CoolTest" );
        DefaultContext p2 = new DefaultContext();
        p2.put( "test", "Cool Test" );
        
        DefaultContext c1 = new DefaultContext( p1 );
        DefaultContext c2 = new DefaultContext( p1 );
        DefaultContext c3 = new DefaultContext( p1 );
        DefaultContext c4 = new DefaultContext( p1 );
        DefaultContext c5 = new DefaultContext( p2 );
        
        c1.put( "test", "Cool Test" );
        c2.put( "test", "Cool Test" );
        c3.put( "test", "Cool Test" );
        c3.put( "test2", "Cool Test" );
        c4.put( "test", "Cool Test" );
        c4.makeReadOnly();
        c5.put( "test", "Cool Test" );
        
        assertEquals( "Identical", c1.hashCode(), c2.hashCode() );
        assertTrue( "ContextData", c1.hashCode() != c3.hashCode() );
        assertTrue( "ReadOnly", c1.hashCode() != c4.hashCode() );
        assertTrue( "Parent", c1.hashCode() != c5.hashCode() );
    }
}
