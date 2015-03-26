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
 * A Source implements the side of an event queue where QueueElements are
 * dequeued operations only.
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
public interface Source
{
    /**
     * Sets the timeout on a blocking Source.  Values above <code>1</code>
     * will force all <code>dequeue</code> operations to block for up to that
     * number of milliseconds waiting for new elements.  Values below
     * <code>1</code> will turn off blocking for Source.  This is intentional
     * because a Source should never block indefinitely.
     *
     * @param millis Number of milliseconds to block
     */
    void setTimeout( long millis );

    /**
     * Dequeues the next element, or <code>null</code> if there is
     * nothing left on the queue or in case of a timeout while
     * attempting to obtain the mutex
     *
     * @return the next queue element on the Source
     */
    Object dequeue();

    /**
     * Dequeues all available elements. Returns a zero-sized array in
     * case of a timeout while attempting to obtain the mutex or if
     * there is nothing left on the Source.
     *
     * @return all pending elements on the Source
     */
    Object[] dequeueAll();

    /**
     * Dequeues at most <code>num</code> available elements. Returns a
     * zero-sized array in case of a timeout while attempting to
     * obtain the mutex or if there is nothing left on the Source.
     *
     * @param num  The maximum number of elements to dequeue
     *
     * @return At most <code>num</code> elements from the
     *         Source
     */
    Object[] dequeue( int num );

    /**
     * Returns the number of elements waiting in this Source.
     *
     * @return the number of elements in the Source
     */
    int size();
}
