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

import org.apache.excalibur.event.DequeueInterceptor;
import org.apache.excalibur.event.EnqueuePredicate;
import org.apache.excalibur.event.Queue;

/**
 * Provides the base functionality for the other <code>Queue</code> types.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractQueue implements Queue
{
    /** An empty array used as a return value when the Queue is empty */
    protected final static Object[] EMPTY_ARRAY = new Object[ 0 ];
    /** The number of milliseconds to wait */
    protected long m_timeout = 0;
    protected EnqueuePredicate m_predicate = new NullEnqueuePredicate();
    protected DequeueInterceptor m_interceptor = new NullDequeueInterceptor();

    /**
     * Default for canAccept()
     *
     * @return how many elements we can enqueue
     */
    public int canAccept()
    {
        return ( maxSize() > 0 ) ? maxSize() - size() : maxSize();
    }

    /**
     * Default maxSize to -1 which is unbounded
     *
     * @return the maximum number of elements
     */
    public int maxSize()
    {
        return -1;
    }

    /**
     * Check to see if the <code>Queue</code> is full. The method uses the
     * <code>maxSize</code> and <code>size</code> methods to determine
     * whether the queue is full.
     *
     * @return true if there is no room in the Queue
     */
    public boolean isFull()
    {
        return maxSize() != -1  /* There exists an upper bound... */
            && maxSize() - size() <= 0; /* ...and it is reached. */
    }

    /**
     * Set the timeout for the <code>Queue</code> in milliseconds.  The
     * default timeout is 0, which means that we don't wait at all.
     *
     * @param millis  The number of milliseconds to block waiting for events to be enqueued
     */
    public void setTimeout( final long millis )
    {
        if( millis > 0 )
        {
            m_timeout = millis;
        }
        else
        {
            m_timeout = 0;
        }
    }

    /**
     * Encapsulates the logic to block the <code>Queue</code> for the amount
     * of time specified by the timeout.
     *
     * @param lock  The object used as the mutex.
     */
    protected void block( Object lock )
    {
        if( m_timeout > 0 )
        {
            long start = System.currentTimeMillis();
            long end = start + m_timeout;

            while( start < end || size() > 0 )
            {
                try
                {
                    lock.wait( m_timeout );
                }
                catch( InterruptedException ie )
                {
                    // ignore
                }
            }
        }
    }

    /**
     * Set the EnqueuePredicate to limit entries into this Queue.
     */
    public void setEnqueuePredicate( EnqueuePredicate predicate )
    {
        if ( null == predicate ) throw new NullPointerException( "predicate" );

        m_predicate = predicate;
    }

    /**
     * Return the EnqueuePredicate that is already set for this Queue.
     */
    public EnqueuePredicate getEnqueuePredicate()
    {
        return m_predicate;
    }

    /**
     * Set the dequeue executable for this sink. This mechanism
     * allows users to define a methods that will be executed
     * before or after dequeuing elements from a source
     * @since Sep 23, 2002
     *
     * @param executable
     *  The dequeue executable for this sink.
     */
    public void setDequeueInterceptor(DequeueInterceptor executable)
    {
        if ( null == executable ) throw new NullPointerException( "executable" );

        m_interceptor = executable;
    }

    /**
     * Return the dequeue executable for this sink.
     * @since Sep 23, 2002
     *
     * @return {@link DequeueInterceptor}
     *  The dequeue executable for this sink.
     */
    public DequeueInterceptor getDequeueInterceptor()
    {
        return m_interceptor;
    }
}
