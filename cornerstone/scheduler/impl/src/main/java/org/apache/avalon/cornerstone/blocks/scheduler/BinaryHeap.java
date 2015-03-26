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

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * BinaryHeap implementation of priority queue.
 * The heap is either a minimum or maximum heap as determined
 * by parameters passed to constructor.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/16 12:49:52 $
 * @since 4.0
 */
public final class BinaryHeap
    implements PriorityQueue
{
    private static final class MinComparator
        implements Comparator
    {
        public final int compare( final Object lhs, final Object rhs )
        {
            return ( (Comparable)lhs ).compareTo( rhs );
        }
    }

    private static final class MaxComparator
        implements Comparator
    {
        public final int compare( final Object lhs, final Object rhs )
        {
            return ( (Comparable)rhs ).compareTo( lhs );
        }
    }

    /**
     * Comparator used to instantiate a min heap - assumes contents implement
     * the Comparable interface.
     */
    public static final Comparator MIN_COMPARATOR = new MinComparator();

    /**
     * Comparator used to instantiate a max heap - assumes contents implement
     * the Comparable interface.
     */
    public static final Comparator MAX_COMPARATOR = new MaxComparator();

    private static final int DEFAULT_CAPACITY = 13;
    private static final Comparator DEFAULT_COMPARATOR = MIN_COMPARATOR;
    private int m_size;
    private Object[] m_elements;
    private Comparator m_comparator;

    /**
     * Instantiates a new min binary heap with the default initial capacity.
     */
    public BinaryHeap()
    {
        this( DEFAULT_CAPACITY, DEFAULT_COMPARATOR );
    }

    /**
     * Instantiates a new min binary heap with the given initial capacity.
     *
     * @param capacity the size of the heap
     */
    public BinaryHeap( final int capacity )
    {
        this( capacity, DEFAULT_COMPARATOR );
    }

    /**
     * Instantiates a new binary heap with the default initial capacity and
     * ordered using the given Comparator.
     *
     * @param comparator to order the contents of the heap
     */
    public BinaryHeap( final Comparator comparator )
    {
        this( DEFAULT_CAPACITY, comparator );
    }

    /**
     * Instantiates a new binary heap with the given initial capacity and
     * ordered using the given Comparator.
     *
     * @param capacity the size of the heap
     * @param comparator to order the contents of the heap
     */
    public BinaryHeap( final int capacity, final Comparator comparator )
    {
        //+1 as 0 is noop
        m_elements = new Object[ capacity + 1 ];
        m_comparator = comparator;
    }

    /**
     * Create a binary heap of Comparables. Takes a parameter
     * to specify whether it is a minimum or maximum heap.
     *
     * @param isMinHeap true to make it a minimum heap, false to make it a max heap
     */
    public BinaryHeap( final boolean isMinHeap )
    {
        this( DEFAULT_CAPACITY, isMinHeap );
    }

    /**
     * Create a binary heap of Comparables. Takes a parameter
     * to specify whether it is a minimum or maximum heap and another
     * parameter to specify the size of the heap.
     *
     * @param capacity the size of the heap
     * @param isMinHeap true to make it a minimum heap, false to make it a max heap
     */
    public BinaryHeap( final int capacity, final boolean isMinHeap )
    {
        this( capacity, isMinHeap ? MIN_COMPARATOR : MAX_COMPARATOR );
    }

    /**
     * Clear all elements from queue.
     */
    public void clear()
    {
        m_size = 0;
    }

    /**
     * Test if queue is empty.
     *
     * @return true if queue is empty else false.
     */
    public boolean isEmpty()
    {
        return ( 0 == m_size );
    }

    /**
     * Test if queue is full.
     *
     * @return true if queue is full else false.
     */
    public boolean isFull()
    {
        //+1 as element 0 is noop
        return ( m_elements.length == m_size + 1 );
    }

    /**
     * Returns the number of elements currently on the heap.
     *
     * @return the size of the heap.
     */
    public int size()
    {
        return m_size;
    }

    /**
     * Insert an element into queue.
     *
     * @param element the element to be inserted
     */
    public void insert( final Object element )
    {
        if( isFull() )
        {
            grow();
        }

        percolateUpHeap( element );
    }

    /**
     * Return element on top of heap but don't remove it.
     *
     * @return the element at top of heap
     * @throws NoSuchElementException if isEmpty() == true
     */
    public Object peek() throws NoSuchElementException
    {
        if( isEmpty() )
        {
            throw new NoSuchElementException();
        }
        else
        {
            return m_elements[ 1 ];
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
        final Object result = peek();
        m_elements[ 1 ] = m_elements[ m_size-- ];

        //set the unused element to 'null' so that the garbage collector
        //can free the object if not used anywhere else.(remove reference)
        m_elements[ m_size + 1 ] = null;

        if( m_size != 0 )
        {
            percolateDownHeap( 1 );
        }

        return result;
    }

    /**
     * Percolate element down heap from top.
     *
     * @param index the index of element
     */
    private void percolateDownHeap( final int index )
    {
        final Object element = m_elements[ index ];

        int hole = index;
        int child = hole << 1;

        while( child <= m_size )
        {
            //if we have a right child and that child can not be percolated
            //up then move onto other child
            if( child != m_size &&
                m_comparator.compare( m_elements[ child + 1 ], m_elements[ child ] ) < 0 )
            {
                child++;
            }

            //if we found resting place of bubble then terminate search
            if( m_comparator.compare( m_elements[ child ], element ) >= 0 )
            {
                break;
            }

            m_elements[ hole ] = m_elements[ child ];
            hole = child;
            child = hole << 1;
        }

        m_elements[ hole ] = element;
    }

    /**
     * Percolate element up heap from bottom.
     *
     * @param element the element
     */
    private void percolateUpHeap( final Object element )
    {
        int hole = ++m_size;
        int next = hole >> 1;

        m_elements[ hole ] = element;

        while( hole > 1 &&
            m_comparator.compare( element, m_elements[ next ] ) < 0 )
        {
            m_elements[ hole ] = m_elements[ next ];
            hole = next;
            next = hole >> 1;
        }

        m_elements[ hole ] = element;
    }

    /**
     * Grows the heap by a factor of 2.
     */
    private void grow()
    {
        final Object[] elements =
            new Object[ m_elements.length * 2 ];
        System.arraycopy( m_elements, 0, elements, 0, m_elements.length );
        m_elements = elements;
    }

    /**
     * Create a string representing heap
     * and all elements in heap.
     *
     * @return the string representing heap
     */
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();

        sb.append( "[ " );

        for( int i = 1; i < m_size + 1; i++ )
        {
            if( i != 1 )
            {
                sb.append( ", " );
            }
            sb.append( m_elements[ i ] );
        }

        sb.append( " ]" );

        return sb.toString();
    }
}

