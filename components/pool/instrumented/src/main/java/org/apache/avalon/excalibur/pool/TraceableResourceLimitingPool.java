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
package org.apache.avalon.excalibur.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A ResourceLimitingPool which can be configured so that it will trace the
 *  where get is being called fron.  The pool can then be queried for its
 *  status.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/31 08:07:28 $
 * @since 4.1
 */
public class TraceableResourceLimitingPool
    extends InstrumentedResourceLimitingPool
{
    /*---------------------------------------------------------------
     * Private Fields
     *-------------------------------------------------------------*/
    /** True if tracing is enabled for the pool. */
    private boolean m_tracing;
    
    /** Map of elements describing each poolable. */
    private Map m_elementMap;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new TraceableResourceLimitingPool
     *
     * @param factory The ObjectFactory which will be used to create new Poolables as needed by
     *  the pool.
     * @param max Maximum number of Poolables which can be stored in the pool, 0 implies no limit.
     * @param maxStrict true if the pool should never allow more than max Poolable to be created.
     *  Will cause an exception to be thrown if more than max Poolables are requested and blocking
     *  is false.
     * @param blocking true if the pool should cause a thread calling get() to block when Poolables
     *  are not currently available on the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused poolables will be removed
     *  from the pool.  A value of 0 will cause the pool to never trim poolables.
     * @param trace True if tracing of gets is enabled for the pool.
     */
    public TraceableResourceLimitingPool( final ObjectFactory factory,
                                          int max,
                                          boolean maxStrict,
                                          boolean blocking,
                                          long blockTimeout,
                                          long trimInterval,
                                          boolean trace )
    {

        super( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        
        m_tracing = trace;
        if ( m_tracing )
        {
            m_elementMap = new HashMap();
        }
    }

    /*---------------------------------------------------------------
     * InstrumentedResourceLimitingPool Methods
     *-------------------------------------------------------------*/
    /**
     * Gets a Poolable from the pool.  If there is room in the pool, a new Poolable will be
     *  created.  Depending on the parameters to the constructor, the method may block or throw
     *  an exception if a Poolable is not available on the pool.
     *
     * @return Always returns a Poolable.  Contract requires that put must always be called with
     *  the Poolable returned.
     * @throws Exception An exception may be thrown as described above or if there is an exception
     *  thrown by the ObjectFactory's newInstance() method.
     */
    public Poolable get() throws Exception
    {
        if ( m_tracing )
        {
            synchronized ( m_semaphore )
            {
                Poolable poolable = (Poolable)super.get();
                
                PoolElement element = (PoolElement)m_elementMap.get( poolable );
                if ( element == null )
                {
                    element = new PoolElement( poolable );
                    m_elementMap.put( poolable, element );
                }
                element.trace();
                
                return poolable;
            }
        }
        else
        {
            return super.get();
        }
    }

    /**
     * Returns a poolable to the pool and notifies any thread blocking.
     *
     * @param poolable Poolable to return to the pool.
     */
    public void put( Poolable poolable )
    {
        if ( m_tracing )
        {
            synchronized ( m_semaphore )
            {
                PoolElement element = (PoolElement)m_elementMap.get( poolable );
                if ( element == null )
                {
                    getLogger().error( "PoolElement not found in put for poolable: " + poolable );
                }
                else
                {
                    element.clear();
                }
                
                super.put( poolable );
            }
        }
        else
        {
            super.put( poolable );
        }
    }

    /**
     * Called when an object is being removed permanently from the pool.
     * This is the method to override when you need to enforce destructional
     * policies.
     * <p>
     * This method is only called by threads that have m_semaphore locked.
     *
     * @param poolable Poolable to be completely removed from the pool.
     */
    protected void removePoolable( Poolable poolable )
    {
        if ( m_tracing )
        {
            PoolElement element = (PoolElement)m_elementMap.remove( poolable );
            if ( element == null )
            {
                getLogger().error(
                    "PoolElement not found in removePoolable for poolable: " + poolable );
            }
        }
        
        super.removePoolable( poolable );
    }

    /*---------------------------------------------------------------
     * Public Methods
     *-------------------------------------------------------------*/
    /**
     * Returns a snapshot of the current state of the pool.
     *
     * @return A snapshot of the current pool state.
     */
    public State getState()
    {
        if ( m_tracing )
        {
            synchronized ( m_semaphore )
            {
                // Count how poolables are outstanding.
                int count = 0;
                for ( Iterator iter = m_elementMap.values().iterator(); iter.hasNext(); )
                {
                    PoolElement element = (PoolElement)iter.next();
                    if ( element.m_thread != null )
                    {
                        count++;
                    }
                }
                
                // Go back and extract the state.
                Thread[] threads = new Thread[count];
                TraceException[] traceExceptions = new TraceException[count];
                long[] traceTimes = new long[count];
                if ( count > 0 )
                {
                    int i = 0;
                    for ( Iterator iter = m_elementMap.values().iterator(); iter.hasNext(); )
                    {
                        PoolElement element = (PoolElement)iter.next();
                        if ( element.m_thread != null )
                        {
                            threads[i] = element.m_thread;
                            traceExceptions[i] = element.m_traceException;
                            traceTimes[i] = element.m_time;
                            i++;
                        }
                    }
                }
                
                return new State( getSize(), getReadySize(), threads, traceExceptions, traceTimes );
            }
        }
        else
        {
            throw new IllegalStateException( "Trace is disabled for this pool." );
        }
    }
    
    /*---------------------------------------------------------------
     * Inner Classes
     *-------------------------------------------------------------*/
    public static class State
    {
        private int m_size;
        private int m_readySize;
        private Thread[] m_threads;
        private TraceException[] m_traceExceptions;
        private long[] m_traceTimes;
        
        private State( int size, int readySize,
                       Thread[] threads,
                       TraceException[] traceExceptions,
                       long[] traceTimes )
        {
            m_size = size;
            m_readySize = readySize;
            m_threads = threads;
            m_traceExceptions = traceExceptions;
            m_traceTimes = traceTimes;
        }
        
        public int getSize()
        {
            return m_size;
        }
        
        public int getReadySize()
        {
            return m_readySize;
        }
        
        public Thread[] getTraceThreads()
        {
            return m_threads;
        }
        
        public TraceException[] getTraceExceptions()
        {
            return m_traceExceptions;
        }
        
        public long[] getTraceTimes()
        {
            return m_traceTimes;
        }
    }
    
    private static class PoolElement
    {
        private Poolable m_poolable;
        private Thread m_thread;
        private TraceException m_traceException;
        private long m_time;
        
        private PoolElement( Poolable poolable )
        {
            m_poolable = poolable;
        }
        
        private void trace()
        {
            m_thread = Thread.currentThread();
            m_traceException = new TraceException();
            m_traceException.fillInStackTrace();
            m_time = System.currentTimeMillis();
        }
        
        private void clear()
        {
            m_thread = null;
            m_traceException = null;
        }
    }
    
    public static class TraceException extends RuntimeException
    {
        private TraceException()
        {
        }
    }
}

