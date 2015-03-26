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

package org.apache.avalon.cornerstone.blocks.scheduler;

import java.util.NoSuchElementException;

/**
 * A thread safe version of the PriorityQueue.
 * Provides synchronized wrapper methods for all the methods
 * defined in the PriorityQueue interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/16 12:49:52 $
 * @since 4.0
 */
public final class SynchronizedPriorityQueue
    implements PriorityQueue
{
    private final PriorityQueue m_priorityQueue;

    public SynchronizedPriorityQueue( final PriorityQueue priorityQueue )
    {
        if( null == priorityQueue )
        {
            throw new NullPointerException( "priorityQueue" );
        }
        m_priorityQueue = priorityQueue;
    }

    /**
     * Clear all elements from queue.
     */
    public void clear()
    {
        synchronized( m_priorityQueue )
        {
            m_priorityQueue.clear();
        }
    }

    /**
     * Test if queue is empty.
     *
     * @return true if queue is empty else false.
     */
    public boolean isEmpty()
    {
        synchronized( m_priorityQueue )
        {
            return m_priorityQueue.isEmpty();
        }
    }

    /**
     * Insert an element into queue.
     *
     * @param element the element to be inserted
     */
    public void insert( final Object element )
    {
        synchronized( m_priorityQueue )
        {
            m_priorityQueue.insert( element );
        }
    }

    /**
     * Return element on top of heap but don't remove it.
     *
     * @return the element at top of heap
     * @throws NoSuchElementException if isEmpty() == true
     */
    public Object peek() throws NoSuchElementException
    {
        synchronized( m_priorityQueue )
        {
            return m_priorityQueue.peek();
        }
    }

    /**
     * Return element on top of heap and remove it.
     *
     * @return the element at top of heap
     * @throws NoSuchElementException if isEmpty() == true
     */
    public Object pop() throws NoSuchElementException
    {
        synchronized( m_priorityQueue )
        {
            return m_priorityQueue.pop();
        }
    }

    public String toString()
    {
        synchronized( m_priorityQueue )
        {
            return m_priorityQueue.toString();
        }
    }
}

