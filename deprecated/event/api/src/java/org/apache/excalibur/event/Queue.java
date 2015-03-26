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
public interface Queue extends Source, Sink
{
    String ROLE = Queue.class.getName();

    /**
     * Set the enqueue predicate for this sink. This mechanism
     * allows user to define a method that will 'screen'
     * QueueElementIF's during the enqueue procedure to either
     * accept or reject them. The enqueue predicate runs in the
     * context of the caller of {@link #enqueue(Object)},
     * which means it must be simple and fast. This can be used
     * to implement many interesting m_sink-thresholding policies,
     * such as simple count threshold, credit-based mechanisms,
     * and more.
     * @since Feb 10, 2003
     *
     * @param enqueuePredicate
     *  the enqueue predicate for this sink
     */
    public void setEnqueuePredicate(EnqueuePredicate enqueuePredicate);

    /**
     * Return the enqueue predicate for this sink.
     * @since Feb 10, 2003
     *
     * @return {@link EnqueuePredicate}
     *  the enqueue predicate for this sink.
     */
    public EnqueuePredicate getEnqueuePredicate();

    /**
     * Set the dequeue executable for this sink. This mechanism
     * allows users to define a methods that will be executed
     * before or after dequeuing elements from a source
     * @since Feb 10, 2003
     *
     * @param executable
     *  The dequeue executable for this sink.
     */
    public void setDequeueInterceptor(DequeueInterceptor executable);

    /**
     * Return the dequeue executable for this sink.
     * @since Feb 10, 2003
     *
     * @return {@link DequeueInterceptor}
     *  The dequeue executable for this sink.
     */
    public DequeueInterceptor getDequeueInterceptor();
}
