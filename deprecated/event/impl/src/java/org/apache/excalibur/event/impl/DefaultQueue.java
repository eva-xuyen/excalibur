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
package org.apache.excalibur.event.impl;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.UnboundedFifoBuffer;
import org.apache.excalibur.event.EnqueuePredicate;
import org.apache.excalibur.event.PreparedEnqueue;
import org.apache.excalibur.event.SinkException;
import org.apache.excalibur.event.SinkFullException;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * The default queue implementation is a variable size queue.  This queue is
 * thread safe, however the overhead in synchronization costs a few extra
 * milliseconds.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class DefaultQueue extends AbstractQueue
{
    private final Buffer m_elements;
    private final ReentrantLock m_mutex;
    protected int m_reserve;
    private final int m_maxSize;

    /**
     * Construct a new DefaultQueue with the specified number of elements.
     * if the number of elements is greater than zero, then the
     * <code>Queue</code> is bounded by that number.  Otherwise, the
     * <code>Queue</code> is not bounded at all.
     *
     * @param  size  The maximum number of elements in the <code>Queue</code>.
     *               Any number less than 1 means there is no limit.
     */
    public DefaultQueue( int size )
    {
        this( new ThresholdEnqueuePredicate( size ) );
    }

    public DefaultQueue( EnqueuePredicate predicate )
    {
        setEnqueuePredicate( predicate );

        m_mutex = new ReentrantLock();
        m_elements = new UnboundedFifoBuffer();
        m_reserve = 0;
        m_maxSize = -1;
    }

    /**
     * Create an unbounded DefaultQueue.
     */
    public DefaultQueue()
    {
        this( new NullEnqueuePredicate() );
    }

    /**
     * Return the number of elements currently in the <code>Queue</code>.
     *
     * @return <code>int</code> representing the number of elements (including the reserved ones).
     */
    public int size()
    {
        return m_elements.size() + m_reserve;
    }

    /**
     * Return the maximum number of elements that will fit in the
     * <code>Queue</code>.  A number below 1 indecates an unbounded
     * <code>Queue</code>, which means there is no limit.
     *
     * @return <code>int</code> representing the maximum number of elements
     */
    public int maxSize()
    {
        return m_maxSize;
    }

    public PreparedEnqueue prepareEnqueue( final Object[] elements )
        throws SinkException
    {
        PreparedEnqueue enqueue = null;

        try
        {
            m_mutex.acquire();
            try
            {
                if( getEnqueuePredicate().accept(elements, this) )
                {
                    enqueue = new DefaultPreparedEnqueue( this, elements );
                }
                else
                {
                    throw new SinkFullException( "Not enough room to enqueue these elements." );
                }
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( InterruptedException ie )
        {
            if ( null == enqueue )
            {
                throw new SinkException("The mutex was interrupted before it could be released");
            }
        }

        return enqueue;
    }

    public boolean tryEnqueue( final Object element )
    {
        boolean success = false;

        try
        {
            m_mutex.acquire();
            try
            {
                success = getEnqueuePredicate().accept( element, this );

                if ( success )
                {
                    m_elements.add( element );
                }
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( InterruptedException ie )
        {
        }

        return success;
    }

    public void enqueue( final Object[] elements )
        throws SinkException
    {
        final int len = elements.length;

        try
        {
            m_mutex.acquire();
            try
            {
                if( ! getEnqueuePredicate().accept( elements, this ) )
                {
                    throw new SinkFullException( "Not enough room to enqueue these elements." );
                }

                for( int i = 0; i < len; i++ )
                {
                    m_elements.add( elements[ i ] );
                }
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( InterruptedException ie )
        {
        }
    }

    public void enqueue( final Object element )
        throws SinkException
    {
        try
        {
            m_mutex.acquire();
            try
            {
                if( ! getEnqueuePredicate().accept(element, this) )
                {
                    throw new SinkFullException( "Not enough room to enqueue these elements." );
                }

                m_elements.add( element );
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( InterruptedException ie )
        {
        }
    }

    public Object[] dequeue( final int numElements )
    {
        getDequeueInterceptor().before(this);
        Object[] elements = EMPTY_ARRAY;

        try
        {
            if( m_mutex.attempt( m_timeout ) )
            {
                try
                {
                    elements = retrieveElements( m_elements,
                                                 Math.min( size(),
                                                           numElements ) );
                }
                finally
                {
                    m_mutex.release();
                }
            }
        }
        catch( InterruptedException ie )
        {
            //TODO: exception handling
        }

        getDequeueInterceptor().after(this);
        return elements;
    }

    public Object[] dequeueAll()
    {
        getDequeueInterceptor().before(this);
        Object[] elements = EMPTY_ARRAY;

        try
        {
            if( m_mutex.attempt( m_timeout ) )
            {
                try
                {
                    elements = retrieveElements( m_elements, size() );
                }
                finally
                {
                    m_mutex.release();
                }
            }
        }
        catch( InterruptedException ie )
        {
            // TODO: exception hanlding
        }

        getDequeueInterceptor().after(this);
        return elements;
    }

    /**
     * Removes the given number of elements from the given <code>buf</code>
     * and returns them in an array. Trusts the caller to pass in a buffer
     * full of <code>Object</code>s and with at least
     * <code>count</code> elements available.
     * <p>
     * @param buf to remove elements from, the caller is responsible
     *            for synchronizing access
     * @param count number of elements to remove/return
     * @return requested number of elements
     */
    private static Object[] retrieveElements( Buffer buf, int count )
    {
        Object[] elements = new Object[ count ];

        for( int i = 0; i < count; i++ )
        {
            elements[ i ] = buf.remove();
        }

        return elements;
    }

    public Object dequeue()
    {
        getDequeueInterceptor().before(this);
        Object element = null;

        try
        {
            if( m_mutex.attempt( m_timeout ) )
            {
                try
                {
                    if( size() > 0 )
                    {
                        element = m_elements.remove();
                    }
                }
                finally
                {
                    m_mutex.release();
                }
            }
        }
        catch( InterruptedException ie )
        {
            // TODO: exception handling
        }

        getDequeueInterceptor().after(this);
        return element;
    }

    private static final class DefaultPreparedEnqueue implements PreparedEnqueue
    {
        private final DefaultQueue m_parent;
        private Object[] m_elements;

        private DefaultPreparedEnqueue( DefaultQueue parent, Object[] elements )
        {
            m_parent = parent;
            m_elements = elements;
            m_parent.m_reserve += elements.length;
        }

        public void commit()
        {
            if( null == m_elements )
            {
                throw new IllegalStateException( "This PreparedEnqueue has already been processed!" );
            }

            try
            {
                m_parent.m_reserve -= m_elements.length;
                m_parent.enqueue( m_elements );
                m_elements = null;
            }
            catch( Exception e )
            {
                throw new IllegalStateException( "Default enqueue did not happen--should be impossible" );
                // will never happen
            }
        }

        public void abort()
        {
            if( null == m_elements )
            {
                throw new IllegalStateException( "This PreparedEnqueue has already been processed!" );
            }

            m_parent.m_reserve -= m_elements.length;
            m_elements = null;
        }
    }
}
