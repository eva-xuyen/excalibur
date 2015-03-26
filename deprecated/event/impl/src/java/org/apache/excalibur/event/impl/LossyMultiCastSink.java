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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.excalibur.event.PreparedEnqueue;
import org.apache.excalibur.event.Sink;
import org.apache.excalibur.event.SinkException;
import org.apache.excalibur.event.SinkFullException;

/**
 * This is a {@link org.apache.excalibur.event.seda.event.Sink}
 * implementation that multicasts enqueue operations to the
 * contained and concrete sink objects.  Compared to the
 * regular {@link org.apache.excalibur.event.seda.event.impl.MultiCastSink}
 * this sink works in that it delivers zero, one or more sinks.
 * It can be configured to fail when less than one sink was
 * delivered to.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $
 */
public class LossyMultiCastSink implements Sink
{
    /**
     * A collection of sink arrays representing the
     * sinks to enqueue the element to.
     */
    private final Collection m_sinks;

    /**
     * The size of the sink.
     */
    private final int m_size;

    /**
     * indicates if at least one enqueue operation must succeed.
     */
    private final boolean m_oneSuccess;

    //---------------------- LossyMultiCastSink constructors
    /**
     * This constructor creates a failure tolerant sink
     * based on the collection of sink arrays. None of
     * the enqueue operations must succeed.
     * @since May 16, 2002
     *
     * @param sinks
     *  A collection of sink arrays for each stage.
     */
    public LossyMultiCastSink(Collection sinks)
    {
        this(sinks, false);
    }

    /**
     * This constructor creates a failure tolerant sink
     * based on the collection of sink arrays. The additional
     * boolean flag describes whether at least one or none
     * of the enqueue operations must succeed.
     * @since May 16, 2002
     *
     * @param sinks
     *  A collection of sink arrays for each stage.
     */
    public LossyMultiCastSink(Collection sinks, boolean oneSuccess)
    {
        m_sinks = sinks;
        m_size = -1;
        m_oneSuccess = oneSuccess;
    }

    //---------------------- Sink implementation
    /**
     * @see Sink#canAccept()
     */
    public int canAccept()
    {
        return 0;
    }

    /**
     * @see Sink#isFull()
     */
    public boolean isFull()
    {
        return false;
    }

    /**
     * @see Sink#maxSize()
     */
    public int maxSize()
    {
        return 0;
    }

    /**
     * @see Sink#enqueue(Object)
     */
    public void enqueue(Object element) throws SinkException
    {
        final Iterator sinks = m_sinks.iterator();

        int successful = 0;

        //checkEnqueuePredicate(new Object[] { element });

        // iterate through the sinks and try to enqueue
        while (sinks.hasNext())
        {
            final Sink sink = (Sink) sinks.next();

            final boolean enqueued = sink.tryEnqueue(element);

            // enqueue only to the first successful sink
            if (enqueued)
            {
                successful++;
                break;
            }
        }

        if (successful == 0 && m_oneSuccess)
        {
            throw new SinkFullException("Could not deliver one single element.");
        }
    }

    /**
     * @see Sink#enqueue(Object[])
     */
    public void enqueue(Object[] elements) throws SinkException
    {
        final Iterator sinks = m_sinks.iterator();

        int successful = 0;

        //checkEnqueuePredicate(elements);

        // iterate through the sinks and try to enqueue
        while (sinks.hasNext())
        {
            final Sink sink = (Sink) sinks.next();

            try
            {
                sink.enqueue(elements);
            }
            catch (SinkFullException e)
            {
                continue;
            }

            // if enqueue successful break here
            successful++;
            break;
        }

        if (successful == 0 && m_oneSuccess)
        {
            throw new SinkFullException("Could not deliver one single elements.");
        }
    }

    /**
     * @see Sink#tryEnqueue(Object)
     */
    public boolean tryEnqueue(Object element)
    {
        try
        {
            enqueue(element);
            return true;
        }
        catch (SinkException e)
        {
            return !m_oneSuccess;
        }
    }

    /**
     * @see Sink#prepareEnqueue(Object[])
     */
    public PreparedEnqueue prepareEnqueue(Object[] elements)
        throws SinkException
    {
        final Iterator sinks = m_sinks.iterator();
        final DefaultPreparedEnqueue prepares = new DefaultPreparedEnqueue();

        int successful = 0;

        //checkEnqueuePredicate(elements);

        // iterate through the sinks and try to enqueue
        while (sinks.hasNext())
        {
            final Sink sink = (Sink) sinks.next();

            try
            {
                prepares.addPreparedEnqueue(sink.prepareEnqueue(elements));
            }
            catch (SinkFullException e)
            {
                continue;
            }

            // if enqueue successful break here
            successful++;
            break;
        }
        if (successful == 0 && m_oneSuccess)
        {
            throw new SinkFullException("Could not deliver elements at all.");
        }

        return prepares;
    }

    /**
     * @see Sink#size()
     */
    public int size()
    {
        return m_size;
    }

    //------------------------- LossyMultiCastSink inner classes
    /**
     * A prepared enqueue object that holds other prepared
     * enqueue objects and allows to perform a commit / abort
     * on all of these objects.
     * @since May 16, 2002
     *
     * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
     */
    private static final class DefaultPreparedEnqueue
        implements PreparedEnqueue
    {
        /**
         * A collection of prepared enqueue objects
         */
        private final Collection m_preparedEnqueues = new LinkedList();

        //------------------------ PreparedEnqueue implementation
        /**
         * @see PreparedEnqueue#abort()
         */
        public void abort()
        {
            final Iterator iter = m_preparedEnqueues.iterator();

            while (iter.hasNext())
            {
                ((PreparedEnqueue) iter.next()).abort();
            }
        }

        /**
         * @see PreparedEnqueue#commit()
         */
        public void commit()
        {
            final Iterator iter = m_preparedEnqueues.iterator();

            while (iter.hasNext())
            {
                ((PreparedEnqueue) iter.next()).commit();
            }
        }

        //------------------------ DefaultPreparedEnqueue specific implementation
        /**
         * Adds a prepared enqueue object to the list
         * of prepared enqueues.
         * @since May 16, 2002
         *
         * @param preparedEnqueue
         *  The prepared enqueue object to be added.
         */
        public void addPreparedEnqueue(PreparedEnqueue preparedEnqueue)
        {
            m_preparedEnqueues.add(preparedEnqueue);
        }
    } //-- end DefaultPreparedEnqueue inner class
}
