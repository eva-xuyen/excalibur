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
package org.apache.excalibur.event;

/**
 * A Sink implements the end of a finite-length event queue where
 * elements are enqueued. These operations can throw a
 * <code>SinkException</code> if the sink is closed or becomes full, allowing
 * event queues to support thresholding and backpressure.
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
public interface Sink
{
    String ROLE = Sink.class.getName();

    /**
     * Enqueues the given element onto the Sink.
     *
     * @param element  The elements to enqueue
     * @throws SinkFullException Indicates that the sink is temporarily full.
     * @throws SinkClosedException Indicates that the sink is
     *         no longer being serviced.
     */
    void enqueue( Object element )
        throws SinkException;

    /**
     * Given an array of elements, atomically enqueues all of the elements
     * in the array. This guarantees that no other thread can interleave its
     * own elements with those being inserted from this array. The
     * implementation must enqueue all of the elements or none of them;
     * if a SinkFullException or SinkClosedException is thrown, none of
     * the elements will have been enqueued.
     *
     * @param elements The element array to enqueue
     * @throws SinkFullException Indicates that the sink is temporarily full.
     * @throws SinkClosedException Indicates that the sink is
     *   no longer being serviced.
     *
     */
    void enqueue( Object[] elements )
        throws SinkException;

    /**
     * Tries to enqueue an event, but instead of throwing exceptions, it
     * returns a boolean value of whether the attempt was successful.
     *
     * @param element The element to attempt to enqueue
     * @return <code>true</code> if successful, <code>false</code> if not.
     */
    boolean tryEnqueue( Object element );

    /**
     * Support for transactional enqueue.
     *
     * <p>This method allows a client to provisionally enqueue a number
     * of elements onto the queue, and then later commit the enqueue (with
     * a <code>commitEnqueue</code> call), or abort (with an
     * <code>abortEnqueue</code> call). This mechanism can be used to
     * perform "split-phase" enqueues, where a client first enqueues a
     * set of elements on the queue and then performs some work to "fill in"
     * those elements before performing a commit. This can also be used
     * to perform multi-queue transactional enqueue operations, with an
     * "all-or-nothing" strategy for enqueueing events on multiple Sinks.
     * </p>
     *
     * <p>This method would generally be used in the following manner:</p>
     * <pre>
     *   PreparedEnqueue enqueue = sink.prepareEnqueue(someElements);
     *   if (canCommit) {
     *     enqueue.commit();
     *   } else {
     *     enqueue.abort();
     *   }
     * </pre>
     *
     * <p> Note that this method does <strong>not</strong> protect against
     * "dangling prepares" -- that is, a prepare without an associated
     * commit or abort operation. This method should be used with care.
     * In particular, be sure that all code paths (such as exceptions)
     * after a prepare include either a commit or an abort.</p>
     *
     * @param elements The element array to provisionally enqueue
     * @return A <code>PreparedEnqueue</code> that may be used to commit or
     *         abort the provisional enqueue
     * @throws SinkFullException Indicates that the sink is
     *            temporarily full and that the requested elements could not
     *            be provisionally enqueued.
     * @throws SinkClosedException Indicates that the sink is
     *            no longer being serviced.
     *
     * @see PreparedEnqueue
     */
    PreparedEnqueue prepareEnqueue( Object[] elements )
        throws SinkException;

    /**
     * Returns the length threshold of the sink. This is for informational
     * purposes only; an implementation may allow more (or fewer) new
     * entries to be enqueued than maxSize() - size(). This may be the
     * case, for example, if the sink implements some form of dynamic
     * thresholding, and does not always accurately report maxSize().
     *
     * @return -1 if the sink has no length threshold.
     *
     * @deprecated  Use the EnqueuePredicate to control this instead.
     */
    int maxSize();

    /**
     * Returns true if this sink has reached its threshold; false otherwise.
     * Like maxSize(), this is also informational, and isFull() returning
     * false does not guarantee that future enqueue operations will succeed.
     * Clearly, isFull() returning true does not guarantee that they will
     * fail, since the Sink may be serviced in the meantime.
     *
     * @return true if the Sink is full
     *
     * @deprecated  Use the EnqueuePredicate to control this instead
     */
    boolean isFull();

    /**
     * Returns the number of elements it can currently accept.  This is
     * typically the difference between <code>size()</code> and
     * <code>maxSize()</code>.  It will return -1 if the sink is unbounded.
     *
     * @return the number of elements the Sink can accept
     *
     * @deprecated  Use the EnqueuePredicate to control this instead.
     */
    int canAccept();

    /**
     * Returns the number of elements waiting in this Sink.
     *
     * <p><span style="color: blue;"><i>Important:</i></span>
     *   The contract for this method was updated to account for any elements
     *   that were prepared for enqueueing.  It provides a more predictable
     *   and consistent environment, as well as making it easier for
     *   EnqueuePredicates to account for those elements.
     * </p>
     *
     * @return the number of elements in the Sink
     */
    int size();
}
