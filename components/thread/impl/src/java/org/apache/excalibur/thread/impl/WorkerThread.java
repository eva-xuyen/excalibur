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
package org.apache.excalibur.thread.impl;

import org.apache.excalibur.thread.Executable;
import org.apache.excalibur.thread.ThreadControl;

/**
 * This class extends the Thread class to add recyclable functionalities.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class WorkerThread
    extends Thread
{
    /**
     * Enables debug output of major events.  Subclasses which implement
     *  their own logging do not require this to be true.
     */
    private static final boolean ENABLE_DEBUG = false;
    
    /**
     * Enables debug output of minor events.  Subclasses which implement
     *  their own logging do not require this to be true.
     */
    private static final boolean ENABLE_DETAIL_DEBUG = false;
    
    /**
     * The work currentlyy associated with worker (May be null).
     */
    private Executable m_work;

    /**
     * The thread control associated with current work.
     * Should be null if work is null.
     */
    private DefaultThreadControl m_threadControl;

    /**
     * True if this thread is alive and not scheduled for shutdown.
     */
    private boolean m_alive;
    
    /**
     * True if this thread needs to clear the interrupt flag
     */
    private boolean m_clearInterruptFlag;

    /**
     * The thread pool this thread is associated with.
     */
    private final AbstractThreadPool m_pool;

    /**
     * Allocates a new <code>Worker</code> object.
     */
    protected WorkerThread( final AbstractThreadPool pool,
                            final ThreadGroup group,
                            final String name )
    {
        super( group, "" );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == pool )
        {
            throw new NullPointerException( "pool" );
        }

        setName( name );
        m_work = null;
        m_alive = true;
        m_clearInterruptFlag = false;
        m_pool = pool;

        setDaemon( false );
    }
    
    /**
     * The main execution loop.
     */
    public final synchronized void run()
    {
        debug( "starting." );
        try
        {
            while( m_alive )
            {
                waitForWork();
                if ( m_alive )
                {
                    detailDebug( "start with work: " + m_work );
                    
                    try
                    {
                        try
                        {
                            preExecute();
                            
                            // Actually do the work.
                            m_work.execute();
                            
                            // Completed without error, so notify the thread control
                            m_threadControl.finish( null );
                        }
                        catch( final ThreadDeath threadDeath )
                        {
                            debug( "thread has died." );
                            m_threadControl.finish( threadDeath );
                            
                            // This is to let the thread death propagate to the runtime
                            // enviroment to let it know it must kill this worker
                            throw threadDeath;
                        }
                        catch( final Throwable throwable )
                        {
                            // Error thrown while working.
                            debug( "error caught", throwable );
                            m_threadControl.finish( throwable );
                        }
                    }
                    finally
                    {
                        detailDebug( "done with work: " + m_work );
                        
                        m_work = null;
                        m_threadControl = null;
                        
                        if ( m_clearInterruptFlag )
                        {
                            clearInterruptFlag();
                        }
                        
                        postExecute();
                    }
                    
                    /*
                    try
                    {
                        preExecute();
                        m_work.execute();
                        if (m_clearInterruptFlag) clearInterruptFlag();
                        m_threadControl.finish( null );
                    }
                    catch( final ThreadDeath threadDeath )
                    {
                        debug( "thread has died." );
                        m_threadControl.finish( threadDeath );
                        // This is to let the thread death propagate to the runtime
                        // enviroment to let it know it must kill this worker
                        throw threadDeath;
                    }
                    catch( final Throwable throwable )
                    {
                        // Error thrown while working.
                        debug( "error caught", throwable );
                        m_threadControl.finish( throwable );
                    }
                    finally
                    {
                        detailDebug( "done." );
                        m_work = null;
                        m_threadControl = null;
                        if (m_clearInterruptFlag) clearInterruptFlag();
                        postExecute();
                    }
                    */
        
                    //should this be just notify or notifyAll ???
                    //It seems to resource intensive option to use notify()
                    //notifyAll();
                    
                    // Let the thread, if any, waiting for the work to complete
                    //  know we are done.  Should never me more than one thread
                    //  waiting, so notifyAll is not necessary.
                    notify();
        
                    // recycle ourselves
                    recycleThread();
                }
            }
        }
        finally
        {
            debug( "stopped." );
        }
    }

    /**
     * Implement this method to replace thread back into pool.
     */
    protected void recycleThread()
    {
        if ( !m_alive )
        {
            throw new IllegalStateException( "Attempted to recycle dead thread." );
        }
        
        detailDebug( "recycle." );
        if ( m_clearInterruptFlag )
        {
            clearInterruptFlag();
        }
        m_pool.releaseWorker( this );
    }

    /**
     * Overide this method to execute something after
     * each bit of "work".
     */
    protected void postExecute()
    {
    }

    /**
     * Overide this method to execute something before
     * each bit of "work".
     */
    protected void preExecute()
    {
        clearInterruptFlag();
        //TODO: Thread name setting should reuse the
        //ThreadContext code if ThreadContext used.
    }

    /**
     * Clears the interrupt flag for this thread.  Since Java does
     * not provide a method that does this for an external thread,
     * we have to verify that we are in the WorkerThread.  If the
     * code calling this method does not originate from this thread,
     * we set a flag and wait for it to be called internally.
     */
    public void clearInterruptFlag()
    {
        if (Thread.currentThread().equals(this))
        {
            Thread.interrupted();
            m_clearInterruptFlag = false;
        }
        else
        {
            m_clearInterruptFlag = true;
        }
    }

    /**
     * Set the <tt>alive</tt> variable to false causing the worker to die.
     * If the worker is stalled and a timeout generated this call, this method
     * does not change the state of the worker (that must be destroyed in other
     * ways).
     * <p>
     * This is called by the pool when it is removed.
     */
    public void dispose()
    {
        debug( "destroying." );
        
        m_alive = false;
        
        // Notify the thread so it will be woken up and notice that it has been destroyed.
        synchronized(this)
        {
            this.notify();
        }
        
        debug( "destroyed." );
    }

    /**
     * Set the <tt>Work</tt> code this <tt>Worker</tt> must
     * execute and <i>notifies</i> its thread to do it.
     */
    protected synchronized ThreadControl execute( final Executable work )
    {
        m_work = work;
        m_threadControl = new DefaultThreadControl( this );

        detailDebug( "notifying this worker of new work: " + work.toString() );
        notify();

        return m_threadControl;
    }

    /**
     * Set the <tt>Work</tt> code this <tt>Worker</tt> must
     * execute and <i>notifies</i> its thread to do it. Wait
     * until the executable has finished before returning.
     */
    protected void executeAndWait( final Executable work )
    {
        // Assign work to the thread.
        execute( work );
        
        // Wait for the work to complete.
        synchronized(this)
        {
            while( null != m_work )
            {
                try
                {
                    detailDebug( "waiting for work to complete." );
                    wait();
                    detailDebug( "notified." );
                }
                catch( final InterruptedException ie )
                {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * For for new work to arrive or for the thread to be destroyed.
     */
    private void waitForWork()
    {
        synchronized(this)
        {
            while( m_alive && ( null == m_work ) )
            {
                try
                {
                    detailDebug( "waiting for work." );
                    wait();
                    detailDebug( "notified." );
                }
                catch( final InterruptedException ie )
                {
                    // Ignore
                }
            }
        }
    }

    /**
     * Used to log major events against the worker.  Creation, deletion,
     *  uncaught exceptions etc.
     * <p>
     * This implementation is a Noop.  Subclasses can override to actually
     *  do some logging.
     *
     * @param message Message to log.
     */
    protected void debug( final String message )
    {
        if( ENABLE_DEBUG )
        {
            System.out.println( getName() + ": " + message );
        }
    }
    
    /**
     * Used to log major events against the worker.  Creation, deletion,
     *  uncaught exceptions etc.
     * <p>
     * This implementation is a Noop.  Subclasses can override to actually
     *  do some logging.
     *
     * @param message Message to log.
     * @param throwable Throwable to log with the message.
     */
    protected void debug( final String message, final Throwable throwable )
    {
        if( ENABLE_DEBUG )
        {
            System.out.println( getName() + ": " + message + ": " + throwable );
        }
    }
    
    /**
     * Used to log minor events against the worker.  Start and stop of
     *  individual pieces of work etc.  Separated from the major events
     *  so that they are not lost in a sea of minor events.
     * <p>
     * This implementation is a Noop.  Subclasses can override to actually
     *  do some logging.
     *
     * @param message Message to log.
     */
    protected void detailDebug( final String message )
    {
        if ( ENABLE_DETAIL_DEBUG )
        {
            System.out.println( getName() + ": " + message );
        }
    }
    
    /**
     * Used to log minor events against the worker.  Start and stop of
     *  individual pieces of work etc.  Separated from the major events
     *  so that they are not lost in a sea of minor events.
     * <p>
     * This implementation is a Noop.  Subclasses can override to actually
     *  do some logging.
     *
     * @param message Message to log.
     * @param throwable Throwable to log with the message.
     */
    protected void detailDebug( final String message, final Throwable throwable )
    {
        if ( ENABLE_DETAIL_DEBUG )
        {
            System.out.println( getName() + ": " + message + ": " + throwable );
        }
    }
}
