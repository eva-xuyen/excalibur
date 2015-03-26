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
package org.apache.excalibur.event.command;

import java.util.*;

import org.apache.commons.collections.StaticBucketMap;
import org.apache.excalibur.event.EventHandler;
import org.apache.excalibur.event.Source;
import org.apache.excalibur.event.DequeueInterceptor;
import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.impl.NullDequeueInterceptor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * This is a <code>ThreadManager</code> which provides a threadpool per
 * <code>Sink</code> per <code>EventPipeline</code>. ::NOTE:: This is not
 * tested yet!
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class TPSPThreadManager implements ThreadManager
{
    private final StaticBucketMap m_pipelines = new StaticBucketMap();
    private final int m_maxThreadsPerPool;
    private final int m_threshold;
    private final int m_margin;

    /**
     * The default constructor assumes there is a system property named
     * "os.arch.cpus" that has a default for the number of CPUs on a system.
     * Otherwise, the value is 1.
     */
    public TPSPThreadManager()
    {
        this( 2, 1000 );
    }

    /**
     * Constructor provides a specified number of threads per processor. If
     * either value is less then one, then the value is rewritten as one.
     *
     * @param maxThreadPerPool  The number of processors in the machine
     * @param threshold         The number of events before a new thread is started
     */
    public TPSPThreadManager( int maxThreadPerPool, int threshold )
    {
        this(maxThreadPerPool, threshold, (threshold/4));
    }

    /**
     * Constructor provides a specified number of threads per processor. If
     * either value is less then one, then the value is rewritten as one.
     *
     * @param maxThreadPerPool  The number of processors in the machine
     * @param threshold         The number of events before a new thread is started
     * @param margin            The number of events +/- the threshold for thread evaluation
     */
    public TPSPThreadManager( int maxThreadPerPool, int threshold, int margin )
    {
        m_maxThreadsPerPool = maxThreadPerPool;
        m_threshold = threshold;
        m_margin = margin;
    }

    /**
     * Register an EventPipeline with the ThreadManager.
     *
     * @param pipeline  The pipeline we are registering
     */
    public void register( EventPipeline pipeline )
    {
        Source[] sources = pipeline.getSources();
        EventHandler handler = pipeline.getEventHandler();
        List sourceList = new ArrayList(sources.length);

        for (int i = 0; i < sources.length; i++)
        {
            PooledExecutor threadPool = new PooledExecutor();
            threadPool.setMinimumPoolSize(1);
            threadPool.setMaximumPoolSize(m_maxThreadsPerPool);
            SourceRunner initRunner = new SourceRunner(sources[i], handler);

            try
            {
                threadPool.execute(initRunner);
            }
            catch ( InterruptedException e )
            {
            }

            sourceList.add(new SourceDequeueInterceptor(initRunner, handler, threadPool, m_threshold, m_margin));
        }
        m_pipelines.put( pipeline, sourceList );
    }

    /**
     * Deregister an EventPipeline with the ThreadManager
     *
     * @param pipeline  The pipeline to unregister
     */
    public void deregister( EventPipeline pipeline )
    {
        List sources = (List) m_pipelines.remove( pipeline );
        Iterator it = sources.iterator();
        while(it.hasNext())
        {
            SourceDequeueInterceptor intercept = (SourceDequeueInterceptor)it.next();
            intercept.stop();
        }
    }

    /**
     * Deregisters all EventPipelines from this ThreadManager
     */
    public void deregisterAll()
    {
        Iterator it = m_pipelines.keySet().iterator();
        while(it.hasNext())
        {
            deregister((EventPipeline)it.next());
        }
    }

    /**
     * The SourceRunner is used to dequeue events one at a time.
     */
    protected static final class SourceRunner implements Runnable
    {
        private final Source m_source;
        private final EventHandler m_handler;
        private volatile boolean m_keepProcessing;

        /**
         * Create a new SourceRunner.
         *
         * @param source   The source to pull events from.
         * @param handler  The handler to send events to.
         */
        protected SourceRunner( final Source source, final EventHandler handler )
        {
            if ( source == null ) throw new NullPointerException("source");
            if(handler == null)throw new NullPointerException("handler");
            m_source = source;
            m_handler = handler;
            m_keepProcessing = true;
        }

        /**
         * Called by the PooledExecutor to ensure all components are working.
         */
        public void run()
        {
            while (m_keepProcessing)
            {
                Object event = m_source.dequeue();

                if ( event != null )
                {
                    m_handler.handleEvent( event );
                }

                yield();
            }
        }

        /**
         * A way to make sure we yield the processor up to the next thread.
         */
        private static void yield()
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie)
            {
                //Nothing to do.
            }
        }

        /**
         * Stop the runner nicely.
         */
        public void stop()
        {
            m_keepProcessing = false;
        }

        /**
         * Get a reference to the Source.
         *
         * @return the <code>Source</code>
         */
        public Source getSource()
        {
            return m_source;
        }
    }

    /**
     * This is used to plug into Queues so that we can intercept calls to the dequeue operation.
     */
    protected static final class SourceDequeueInterceptor implements DequeueInterceptor
    {
        private final Source m_source;
        private final PooledExecutor m_threadPool;
        private final int m_threshold;
        private final DequeueInterceptor m_parent;
        private final int m_margin;
        private final LinkedList m_runners;
        private final EventHandler m_handler;
        private final SourceRunner m_initRunner;

        /**
         * Create a new SourceDequeueInterceptor.  The parameters are used to ensure a working
         * environment.
         *
         * @param runner      The initial SourceRunner.
         * @param handler     The EventHandler to send events to.
         * @param threadPool  The PooledExecutor for the set of threads.
         * @param threshold   The threshold of events before a new thread is executed.
         * @param margin      The margin of error allowed for the events.
         */
        public SourceDequeueInterceptor( SourceRunner runner, EventHandler handler, PooledExecutor threadPool, int threshold, int margin )
        {
            if (runner == null) throw new NullPointerException("runner");
            if (handler == null) throw new NullPointerException("handler");
            if (threadPool == null) throw new NullPointerException("threadPool");
            if ( threshold < threadPool.getMinimumPoolSize())
                throw new IllegalArgumentException("threshold must be higher than the minimum number" +
                                                   " of threads for the pool");
            if ( margin < 0 )
                throw new IllegalArgumentException("margin must not be less then zero");
            if ( threshold - margin <= threadPool.getMinimumPoolSize() )
                throw new IllegalArgumentException( "The margin must not exceed or equal the" +
                                                    " differnece between threshold and the thread" +
                                                    " pool minimum size" );

            m_source = runner.getSource();
            m_initRunner = runner;
            m_threadPool = threadPool;
            m_threshold = threshold;
            m_runners = new LinkedList();
            m_handler = handler;

            if ( m_source instanceof Queue)
            {
                Queue queue = (Queue) m_source;
                m_parent = queue.getDequeueInterceptor();
                queue.setDequeueInterceptor(this);
            }
            else
            {
                m_parent = new NullDequeueInterceptor();
            }

            m_margin  = margin;
        }

        /**
         * An operation executed before dequeing events from
         * the queue. The Source is passed in so the implementation
         * can determine to execute based on the queue properties.
         *
         * <p>
         *   This method is called once at the beginning of any <code>dequeue</code>
         *   method regardless of how many queue elements are dequeued.
         * </p>
         *
         * @since Feb 10, 2003
         *
         * @param context  The source from which the dequeue is performed.
         */
        public void before( Source context )
        {
            if (m_source.size() > (m_threshold + m_margin))
            {
                SourceRunner runner = new SourceRunner(m_source, m_handler);
                try
                {
                    m_threadPool.execute(runner);
                }
                catch ( InterruptedException e )
                {
                }

                m_runners.add( runner );
            }
            m_parent.before(context);
        }

        /**
         * An operation executed after dequeing events from
         * the queue. The Source is passed in so the implementation
         * can determine to execute based on the queue properties.
         *
         * <p>
         *   This method is called once at the end of any <code>dequeue</code>
         *   method regardless of how many queue elements are dequeued.
         * </p>
         *
         * @since Feb 10, 2003
         *
         * @param context  The source from which the dequeue is performed.
         */
        public void after( Source context )
        {
            m_parent.after(context);

            if (m_source.size() < (m_threshold - m_margin))
            {
                if ( m_runners.size() > 0 )
                {
                    SourceRunner runner = (SourceRunner)m_runners.removeFirst();
                    runner.stop();
                }
            }
        }

        /**
         * Ensure all event runners are stopped for this partial pipeline.
         */
        public void stop()
        {
            Iterator it = m_runners.iterator();
            while(it.hasNext())
            {
                ((SourceRunner)it.next()).stop();
            }

            m_initRunner.stop();
        }
    }
}
