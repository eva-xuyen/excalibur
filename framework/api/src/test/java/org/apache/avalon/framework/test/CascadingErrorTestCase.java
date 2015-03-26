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

import org.apache.avalon.framework.CascadingError;
import org.apache.avalon.framework.CascadingThrowable;

import junit.framework.TestCase;

/**
 * TestCase for {@link CascadingError}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CascadingErrorTestCase.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class CascadingErrorTestCase extends TestCase
{
    public void testConstructor()
    {
        assertNotNull( new CascadingError( null, null ) );
        assertNotNull( new CascadingError( "msg", null ) );
        assertNotNull( new CascadingError( "msg", new RuntimeException() ) );
        assertNotNull( new CascadingError( null, new RuntimeException() ) );

        //assertNotNull( new CascadingError( "msg" ) );
        //ambiguous assertNotNull( new CascadingError( null ) );
        //assertNotNull( new CascadingError() );
    }

    public void testGetCause()
    {
        RuntimeException re = new RuntimeException();
        CascadingError e = new CascadingError( "msg", re );

        assertEquals( re, e.getCause() );

        e = new CascadingError( "msg", null );
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
                throw new CascadingError();
            }
        }
        catch( CascadingError ex )
        {
            ex.getCause();
        }*/
    }

    public void testCasts()
    {
        CascadingError e = new CascadingError( "msg", null );
        assertTrue( e instanceof Error );
        assertTrue( e instanceof CascadingThrowable );
    }

}
