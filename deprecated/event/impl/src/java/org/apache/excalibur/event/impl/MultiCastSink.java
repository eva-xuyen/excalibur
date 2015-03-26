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
 * contained and concrete sink objects.  The multi cast sink
 * will try to enqueue and only succeeds if no element was
 * rejected from any sink. The sink can be configured to
 * enqueue into one sink alone or all sinks.
 * If a sink array in the collection of sinks contains more
 * than one sink the multicast sink will try to enqueue the
 * element always to <b>only one</b> of these sinks.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $
 */
public class MultiCastSink implements Sink
{
    /** A collection of sink arrays representing the  sinks to enqueue to. */
    private final Collection m_sinks;

    /** The size of the sink. */
    private final int m_size;

    /** Boolean value describing if one or all operations must succeed. */
    private final boolean m_single;

    //---------------------- LossyMultiCastSink constructors
    /**
     * This constructor creates a failure in-tolerant multicast
     * sink based on the collection of sink arrays. The delivery
     * must succeed for all sinks in the collection or it will
     * fail entirely.
     * @since May 16, 2002
     *
     * @param sinks
     *  A collection of sink arrays for each stage.
     */
    public MultiCastSink(Collection sinks)
    {
        this(sinks, false);
    }

    /**
     * This constructor creates a failure in-tolerant multicast
     * sink based on the collection of sink arrays.
     * @since May 16, 2002
     *
     * @param sinks
     *  A collection of sink arrays for each stage.
     * @param single
     *  <m_code>true</m_code> if just one operation must succeed.
     *  <m_code>false</m_code> if all operations must succeed.
     */
    public MultiCastSink(Collection sinks, boolean single)
    {
        m_sinks = sinks;
        m_size = -1;
        m_single = single;
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
        final PreparedEnqueue prepared;
        prepared = prepareEnqueue(new Object[] { element });
        prepared.commit();
    }

    /**
     * @see Sink#enqueue(Object[])
     */
    public void enqueue(Object[] elements) throws SinkException
    {
        final PreparedEnqueue prepared = prepareEnqueue(elements);
        prepared.commit();
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
            return false;
        }
    }

    /**
     * @see Sink#prepareEnqueue(Object[])
     */
    public PreparedEnqueue prepareEnqueue(Object[] elements)
        throws SinkException
    {

        //checkEnqueuePredicate(elements);

        final DefaultPreparedEnqueue prepares = new DefaultPreparedEnqueue();
        int successful = 0;

        final Iterator sinks = m_sinks.iterator();

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

            // if enqueue successful return here or just break and continue
            if (m_single)
            {
                return prepares;
            }
            successful++;
            break;

        }
        if (successful < m_sinks.size())
        {
            // rollback all enqueues.
            prepares.abort();

            throw new SinkFullException("Could not deliver elements.");
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
