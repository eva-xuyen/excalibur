/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.excalibur.event;

/**
 * A SinkClosedException is thrown when an enqueue operation occurs on a
 * queue that is already closed.
 *
 * <p>
 *   The interface design is heavily influenced by
 *   <a href="mailto:mdw@cs.berkeley.edu">Matt Welsh</a>'s SandStorm server,
 *   his demonstration of the SEDA architecture.  We have deviated where we
 *   felt the design differences where better.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class SinkClosedException extends SinkException
{
    /**
     * Create a <code>SinkClosedException</code> with an associated message.
     *
     * @param message  The string message to print in the stack trace
     */
    public SinkClosedException( String message )
    {
        super( message );
    }

    /**
     * Create a <code>SinkClosedException</code> with an associated message
     * and the original exception that caused the problem.
     *
     * @param message  The string message to print in the stack trace
     * @param e        The exception that caused this one.
     */
    public SinkClosedException( String message, Throwable e )
    {
        super( message, e );
    }
}
