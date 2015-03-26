/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.avalon.excalibur.testcase;

import junit.framework.AssertionFailedError;

import org.apache.avalon.framework.CascadingThrowable;

/**
 * This is an extention to the testing framework so that we can get detailed
 * messages from JUnit (The AssertionFailedError hides the underlying cause)
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CascadingAssertionFailedError.java,v 1.4 2004/02/28 11:47:27 cziegeler Exp $
 */
public class CascadingAssertionFailedError
    extends AssertionFailedError
    implements CascadingThrowable
{
    private final Throwable m_throwable;

    /**
     * Constructor with no message
     */
    public CascadingAssertionFailedError()
    {
        this( null, null );
    }

    /**
     * Constructor with a message
     */
    public CascadingAssertionFailedError( String message )
    {
        this( message, null );
    }

    /**
     * Constructor with a message and a parent exception
     */
    public CascadingAssertionFailedError( String message,
                                          Throwable parentThrowable )
    {
        super( message );
        m_throwable = parentThrowable;
    }

    /**
     * Return the parent exception
     */
    public final Throwable getCause()
    {
        return m_throwable;
    }
}
