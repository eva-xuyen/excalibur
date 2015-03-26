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
package org.apache.excalibur.event.impl;

import org.apache.excalibur.event.EnqueuePredicate;
import org.apache.excalibur.event.Sink;

/**
 * The ThresholdEnqueuePredicate limits the elements that can be enqueued
 * based on the size of the Queue.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class ThresholdEnqueuePredicate implements EnqueuePredicate
{
    private final int m_threshold;

    /**
     * Create a new ThresholdEnqueuePredicate with the supplied limit.
     *
     * @param limit  A number greater than zero
     */
    public ThresholdEnqueuePredicate(int limit)
    {
        m_threshold = limit;
    }

    /**
     * Returns true if the Sink size + 1 (the element) is less than the
     * threshold.
     */
    public boolean accept(Object element, Sink modifyingSink)
    {
        if ( m_threshold <=0 ) return true;

        return (modifyingSink.size() + 1) < m_threshold;
    }

    /**
     * Returns true if the Sink size + the number of elements is less than
     * the threshold.
     */
    public boolean accept(Object[] elements, Sink modifyingSink)
    {
        if ( m_threshold <=0 ) return true;

        return (modifyingSink.size() + elements.length) < m_threshold;
    }
}
