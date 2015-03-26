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
 * Enqueue predicates allow users to specify a method that
 * will 'screen' elements being enqueued onto a sink, either
 * accepting or rejecting them. This mechanism can be used
 * to implement many interesting load-conditioning policies,
 * for example, simple thresholding, rate control, credit-based
 * flow control, and so forth. Note that the enqueue predicate
 * runs in the context of the <b>caller of enqueue()</b>, which
 * means it must be simple and fast.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $
 */
public interface EnqueuePredicate
{
    /**
     * Tests the given element for acceptance onto the m_sink.
     * @since Feb 10, 2003
     *
     * @param  element  The element to enqueue
     * @param  modifyingSink  The sink that is used for this predicate
     * @return
     *  <code>true</code> if the sink accepts the element;
     *  <code>false</code> otherwise.
     */
    boolean accept(Object element, Sink modifyingSink);

    /**
     * Tests the given element for acceptance onto the m_sink.
     * @since Feb 10, 2003
     *
     * @param  elements  The array of elements to enqueue
     * @param  modifyingSink  The sink that is used for this predicate
     * @return
     *  <code>true</code> if the sink accepts all the elements;
     *  <code>false</code> otherwise.
     */
    boolean accept(Object elements[], Sink modifyingSink);
}
