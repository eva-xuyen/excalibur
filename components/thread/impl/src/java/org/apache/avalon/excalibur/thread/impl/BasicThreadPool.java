/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.thread.impl;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.excalibur.pool.Pool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.thread.ThreadPool;
import org.apache.excalibur.thread.ThreadControl;
import org.apache.excalibur.thread.impl.AbstractThreadPool;
import org.apache.excalibur.thread.impl.WorkerThread;

/**
 * The ThreadPool that binds to Legacy Pooling implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class BasicThreadPool
    extends AbstractThreadPool
    implements ObjectFactory, LogEnabled, Disposable, ThreadPool
{
    /**
     * The underlying pool.
     */
    private Pool m_pool;

    /**
     * The logger to use for debugging purposes.
     */
    private Logger m_logger;

    /**
     * Create a new ThreadPool with specified capacity.
     *
     * @param threadGroup the thread group used in pool
     * @param name the name of pool (used in naming threads)
     * @param pool the underling pool
     * @throws Exception if unable to create pool
     */
    public BasicThreadPool( final ThreadGroup threadGroup,
                            final String name,
                            final Pool pool )
        throws Exception
    {
        super( name, threadGroup );
        if( null == pool )
        {
            throw new NullPointerException( "pool" );
        }

        m_pool = pool;
    }

    /**
     * Setup Logging.
     *
     * @param logger the logger
     */
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
        ContainerUtil.enableLogging( m_pool, logger );
    }

    /**
     * Dispose of underlying pool and cleanup resources.
     */
    public void dispose()
    {
        ContainerUtil.dispose( m_pool );
        m_pool = null;
    }

    /**
     * Create new Poolable instance.
     *
     * @return the new Poolable instance
     */
    public Object newInstance()
    {
        return createWorker();
    }

    /**
     * Overide newWorkerThread to provide a WorkerThread
     * that is Poolable and LogEnabled.
     *
     * @param name the name of WorkerThread
     * @return the created WorkerThread
     */
    protected WorkerThread newWorkerThread( final String name )
    {
        final SimpleWorkerThread thread =
            new SimpleWorkerThread( this, getThreadGroup(), name );
        ContainerUtil.enableLogging( thread, m_logger.getChildLogger( "worker" ) );
        return thread;
    }

    public void decommission( final Object object )
    {
        if( object instanceof WorkerThread )
        {
            destroyWorker( (WorkerThread)object );
        }
    }

    /**
     * Return the class of poolable instance.
     *
     * @return the class of poolable instance.
     */
    public Class getCreatedClass()
    {
        return SimpleWorkerThread.class;
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final Executable work )
    {
         return execute( new ExecutableExecuteable( work ) );
    }

    /**
     * Retrieve a worker thread from pool.
     *
     * @return the worker thread retrieved from pool
     */
    protected WorkerThread getWorker()
    {
        try
        {
            return (WorkerThread)m_pool.get();
        }
        catch( final Exception e )
        {
            final String message =
                "Unable to access thread pool due to " + e;
            throw new IllegalStateException( message );
        }
    }

    /**
     * Release worker back into pool.
     *
     * FIX ME: do we want to verify if it is interrupted or interrupt the worker
     *         thread?
     *
     * @param worker the worker (Should be a {@link SimpleWorkerThread}).
     */
    protected void releaseWorker( final WorkerThread worker )
    {
        worker.clearInterruptFlag();
        
        // If the pool is disposed before the last worker has been released
        //  m_pool will be null.  This can be difficult to avoid as there
        //  is no way to query whether or not all workers have actually been
        //  released.  Underlying pool implementations should probably block
        //  on their dispose methods until all outstanding objects have been
        //  returned.
        Pool pool = m_pool;  // Be thread safe
        if ( pool != null )
        {
            pool.put( (SimpleWorkerThread)worker );
        }
    }
}
