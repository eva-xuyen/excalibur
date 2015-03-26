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

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingThrowable;

import junit.framework.TestCase;

/**
 * TestCase for {@link CascadingException}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CascadingExceptionTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class CascadingExceptionTestCase extends TestCase
{
    public void testConstructor()
    {
        assertNotNull( new CascadingException( null, null ) );
        assertNotNull( new CascadingException( "msg", null ) );
        assertNotNull(
                new CascadingException( "msg", new RuntimeException() ) );
        assertNotNull( new CascadingException( null, new RuntimeException() ) );

        assertNotNull( new CascadingException( "msg" ) );
        // ambiguous assertNotNull( new CascadingException( null ) );
        //assertNotNull( new CascadingException() );
    }

    public void testGetCause()
    {
        RuntimeException re = new RuntimeException();
        CascadingException e = new CascadingException( "msg", re );

        assertEquals( re, e.getCause() );

        e = new CascadingException( "msg", null );
        assertNull( e.getCause() );

        // default to jdk 1.3 cause (not that it seems to help,
        // but it still makes sense)
        /*Exception exc = new Exception("blah");
        try
        {
            try
            {
                throw exc;
            }
            catch( Exception ex )
            {
                throw new CascadingException();
            }
        }
        catch( CascadingException ex )
        {
            ex.getCause();
        }*/
    }

    public void testCasts()
    {
        CascadingException e = new CascadingException( "msg", null );
        assertTrue( e instanceof Exception );
        assertTrue( e instanceof CascadingThrowable );
    }
}
