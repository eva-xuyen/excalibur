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
package org.apache.avalon.excalibur.pool;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 * General Pool implementation which supports; weak and strong pool size limits,
 *  optional blocking gets when poolables are not available, and automatic pool
 *  trimming of unused poolables.
 * <p>
 * Whenever get() is called, the pool tests to see whether it is time to trim old
 *  poolables from the pool.  If any old poolables exist then they are removed at
 *  this time.  This means that old poolables will not be removed if get() is never
 *  called.  Applications can optionally call trim() to force old objects to be
 *  trimmed.  See the {@link #trim()} method for details of how trimming works.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.1
 */
public class InstrumentedResourceLimitingPool
    extends AbstractLogEnabled
    implements Pool, LogEnabled, Disposable, ThreadSafe, Instrumentable
{
    public static final String DEFAULT_INSTRUMENTABLE_NAME = "pool";
    public static final String INSTRUMENT_SIZE_NAME = "size";
    public static final String INSTRUMENT_READY_SIZE_NAME = "ready-size";
    public static final String INSTRUMENT_GETS_NAME = "gets";
    public static final String INSTRUMENT_PUTS_NAME = "puts";
    public static final String INSTRUMENT_BLOCKS_NAME = "blocks";
    public static final String INSTRUMENT_CREATES_NAME = "creates";
    public static final String INSTRUMENT_DECOMMISSIONS_NAME = "decommissions";

    /*---------------------------------------------------------------
     * Protected Fields
     *-------------------------------------------------------------*/
    /**
     * Object used to synchronize access to the get and put methods
     */
    protected final Object m_semaphore = new Object();

    /*---------------------------------------------------------------
     * Private Fields
     *-------------------------------------------------------------*/
    /**
     * Keeps track of whether or not the Pool has been disposed.
     */
    private boolean m_disposed = false;

    /**
     * The Object Factory used to generate new Poolable instances for the pool.
     */
    private final ObjectFactory m_factory;

    /**
     * The maximum size of the pool.
     */
    private final int m_max;

    /**
     * Whether or not the pool allows for the creation of objects beyond the maximum pool size.
     */
    private final boolean m_maxStrict;

    /**
     * Whether or not the pool should cause threads requesting a Poolable to block when m_maxStrict
     *  is true, the pool size is equal to m_max and there are no Poolable instances available.
     */
    private final boolean m_blocking;

    /**
     * The maximum amount of time in milliseconds that the pool will block.  If 0, blocking will
     *  wait indeffinately.
     */
    private final long m_blockTimeout;

    /**
     * The minimum interval with which old unused poolables will be removed from the pool.
     */
    private final long m_trimInterval;

    /**
     * The last time that the pool was trimmed.
     */
    private long m_lastTrim;

    /**
     * List of the Poolable instances which are available for use.
     */
    private LinkedList m_ready;

    /**
     * Store the size of the ready list to optimize operations which require this value.
     */
    private int m_readySize;

    /**
     * List of the Poolable instance which are available for use but have been idle for a while.
     */
    private LinkedList m_oldReady;

    /**
     * Store the size of the old ready list to optimize operations which require this value.
     */
    private int m_oldReadySize;

    /**
     * Total number of Poolable instances in the pool
     */
    private int m_size;

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName = DEFAULT_INSTRUMENTABLE_NAME;

    /** Instrument used to profile the size of the pool. */
    private ValueInstrument m_sizeInstrument;

    /** Instrument used to profile the number of available poolables. */
    private ValueInstrument m_readySizeInstrument;

    /** Instrument used to profile the number of gets. */
    private CounterInstrument m_getsInstrument;

    /** Instrument used to profile the number of puts. */
    private CounterInstrument m_putsInstrument;

    /** Instrument used to profile the number of blocks. */
    private CounterInstrument m_blocksInstrument;

    /** Instrument used to profile the number of created poolables. */
    private CounterInstrument m_createsInstrument;

    /** Instrument used to profile the number of decommissioned poolables. */
    private CounterInstrument m_decommissionsInstrument;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentedResourceLimitingPool
     *
     * @param factory The ObjectFactory which will be used to create new Poolables as needed by
     *  the pool.
     * @param max Maximum number of Poolables which can be stored in the pool, 0 implies no limit.
     * @param maxStrict true if the pool should never allow more than max Poolable to be created.
     *  Will cause an exception to be thrown if more than max Poolables are requested and blocking
     *  is false.
     * @param blocking true if the pool should cause a thread calling get() to block when Poolables
     *  are not currently available in the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused poolables will be removed
     *  from the pool.  A value of 0 will cause the pool to never trim poolables.
     */
    public InstrumentedResourceLimitingPool( final ObjectFactory factory,
                                 int max,
                                 boolean maxStrict,
                                 boolean blocking,
                                 long blockTimeout,
                                 long trimInterval )
    {
        m_factory = factory;
        m_max = ( max <= 0 ? Integer.MAX_VALUE : max );
        m_maxStrict = maxStrict;
        m_blocking = blocking;
        m_blockTimeout = blockTimeout;
        m_trimInterval = trimInterval;

        // Create the pool lists.
        m_ready = new LinkedList();
        if( m_trimInterval > 0 )
        {
            m_oldReady = new LinkedList();
        }

        // Initialize the Instrumentable elements.
        m_sizeInstrument = new ValueInstrument( INSTRUMENT_SIZE_NAME );
        m_readySizeInstrument = new ValueInstrument( INSTRUMENT_READY_SIZE_NAME );
        m_getsInstrument = new CounterInstrument( INSTRUMENT_GETS_NAME );
        m_putsInstrument = new CounterInstrument( INSTRUMENT_PUTS_NAME );
        m_blocksInstrument = new CounterInstrument( INSTRUMENT_BLOCKS_NAME );
        m_createsInstrument = new CounterInstrument( INSTRUMENT_CREATES_NAME );
        m_decommissionsInstrument = new CounterInstrument( INSTRUMENT_DECOMMISSIONS_NAME );
    }

    /*---------------------------------------------------------------
     * Pool Methods
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
        if( m_disposed ) throw new IllegalStateException( "Already Disposed" );

        Poolable poolable;
        int readySize;
        synchronized( m_semaphore )
        {
            // If trimming is enabled then trim if it is time
            if( ( m_oldReady != null ) &&
                ( System.currentTimeMillis() - m_lastTrim >= m_trimInterval ) )
            {
                trimInner();
            }

            // Look for a Poolable at the end of the m_ready list
            if( m_readySize > 0 )
            {
                // A poolable is ready and waiting in the pool
                poolable = (Poolable)m_ready.removeLast();
                m_readySize--;
            }
            else if( m_oldReadySize > 0 )
            {
                // An old poolable is ready and waiting in the pool
                poolable = (Poolable)m_oldReady.removeLast();
                m_oldReadySize--;
            }
            else
            {
                // Are we allowed to create a new poolable here?
                if( ( m_size >= m_max ) && m_maxStrict )
                {
                    // The pool has as many active Poolables as it is allowed and
                    //  we are not allowed to create any more.

                    // Are we allowed to wait for a Poolable to become available?
                    if( m_blocking )
                    {
                        long blockStart = System.currentTimeMillis();

                        if( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Blocking until a Poolable is available. "
                                               + "Thread: " + Thread.currentThread().getName() );
                        }

                        // Notify the InstrumentManager
                        m_blocksInstrument.increment();

                        if( m_blockTimeout > 0 )
                        {
                            // Wait for a limited amount of time for a poolable is made
                            //  available.
                            // Other threads may grab a connection before this thread gets the
                            //  semaphore, so be careful.
                            long blockWait = m_blockTimeout;
                            do
                            {
                                if( blockWait > 0 )
                                {
                                    try
                                    {
                                        m_semaphore.wait( blockWait );
                                    }
                                    catch( InterruptedException e )
                                    {
                                    }

                                    // The dispose() method might have woken us up.
                                    if( m_disposed )
                                    {
                                        throw new IllegalStateException( "Already Disposed" );
                                    }

                                    if( m_readySize == 0 )
                                    {
                                        // Not available yet, calculate how much longer to wait.
                                        long now = System.currentTimeMillis();
                                        blockWait = m_blockTimeout - ( now - blockStart );
                                    }
                                }
                                else
                                {
                                    // We timed out waiting.
                                    long now = System.currentTimeMillis();

                                    if( getLogger().isDebugEnabled() )
                                    {
                                        getLogger().debug(
                                            "Timed out waiting for a Poolable to become "
                                            + "available.  Blocked for " + ( now - blockStart )
                                            + "ms. Thread: " + Thread.currentThread().getName() );
                                    }
                                    throw new Exception
                                        ( "Could not create enough Components to service your "
                                          + "request (Timed out)." );
                                }
                            } while( m_readySize == 0 );
                        }
                        else
                        {
                            // Wait until we get a poolable no matter how long it takes.
                            // Other threads may grab a connection before this thread gets the
                            //  semaphore, so be careful.
                            do
                            {
                                try
                                {
                                    m_semaphore.wait();
                                }
                                catch( InterruptedException e )
                                {
                                }

                                // The dispose() method might have woken us up.
                                if( m_disposed )
                                {
                                    throw new IllegalStateException( "Already Disposed" );
                                }
                            } while( m_readySize == 0 );
                        }

                        // A poolable is ready and waiting in the pool
                        poolable = (Poolable)m_ready.removeLast();
                        m_readySize--;

                        if( getLogger().isDebugEnabled() )
                        {
                            long now = System.currentTimeMillis();
                            getLogger().debug( "Blocked for " + ( now - blockStart ) + "ms "
                                               + "waiting for a Poolable to become available. "
                                               + "Thread: " + Thread.currentThread().getName() );
                        }
                    }
                    else
                    {
                        // We must fail.
                        throw new Exception
                            ( "Could not create enough Components to service your request." );
                    }
                }
                else
                {
                    // Create a new poolable.  May throw an exception if the poolable can not be
                    //  instantiated.
                    poolable = newPoolable();
                    m_size++;

                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Created a new " + poolable.getClass().getName()
                                           + " from the object factory." );
                    }
                }
            }
            
            readySize = getReadySizeSync();
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Got a " + poolable.getClass().getName() + " from the pool." );
        }

        // Notify the InstrumentManager
        m_getsInstrument.increment();
        m_readySizeInstrument.setValue( readySize );

        return poolable;
    }

    /**
     * Returns a poolable to the pool and notifies any thread blocking.
     *
     * @param poolable Poolable to return to the pool.
     */
    public void put( Poolable poolable )
    {
        // Handle Recyclable objects
        if( poolable instanceof Recyclable )
        {
            ( (Recyclable)poolable ).recycle();
        }

        int readySize;
        synchronized( m_semaphore )
        {
            if( m_size <= m_max )
            {
                if( m_disposed )
                {
                    // The pool has already been disposed.
                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Put called for a " + poolable.getClass().getName()
                                           + " after the pool was disposed." );
                    }

                    permanentlyRemovePoolable( poolable );
                }
                else
                {
                    // There is room in the pool to keep this poolable.
                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Put a " + poolable.getClass().getName()
                                           + " back into the pool." );
                    }

                    m_ready.addLast( poolable );
                    m_readySize++;

                    // Let any waiting threads know that a poolable has become available.
                    if( m_blocking )
                    {
                        m_semaphore.notify();
                    }
                }
            }
            else
            {
                // More Poolables were created than can be held in the pool, so remove.
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "No room to put a " + poolable.getClass().getName()
                                       + " back into the pool, so remove it." );
                }

                permanentlyRemovePoolable( poolable );
            }
            
            readySize = getReadySizeSync();
        }

        // Notify the InstrumentManager
        m_putsInstrument.increment();
        m_readySizeInstrument.setValue( readySize );
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * The dispose operation is called at the end of a components lifecycle.
     * This method will be called after Startable.stop() method (if implemented
     * by component). Components use this method to release and destroy any
     * resources that the Component owns.
     */
    public void dispose()
    {
        m_disposed = true;

        // Any Poolables in the m_ready list need to be disposed of
        int size;
        int readySize;
        synchronized( m_semaphore )
        {
            // Remove objects in the ready list.
            for( Iterator iter = m_ready.iterator(); iter.hasNext(); )
            {
                Poolable poolable = (Poolable)iter.next();
                iter.remove();
                m_readySize--;
                permanentlyRemovePoolable( poolable );
            }

            // Remove objects in the old ready list.
            if( m_oldReady != null )
            {
                for( Iterator iter = m_oldReady.iterator(); iter.hasNext(); )
                {
                    Poolable poolable = (Poolable)iter.next();
                    iter.remove();
                    m_oldReadySize--;
                    permanentlyRemovePoolable( poolable );
                }
            }

            // Notify any threads currently waiting for objects so they can abort
            if( m_blocking )
            {
                m_semaphore.notifyAll();
            }

            if( ( m_size > 0 ) && getLogger().isDebugEnabled() )
            {
                getLogger().debug( "There were " + m_size
                                   + " outstanding objects when the pool was disposed." );
            }
            
            size = getSize();
            readySize = getReadySizeSync();
        }

        // Notify the InstrumentManager
        m_sizeInstrument.setValue( size );
        m_readySizeInstrument.setValue( readySize );
    }

    /*---------------------------------------------------------------
     * Instrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the name for the Instrumentable.  The Instrumentable Name is used
     *  to uniquely identify the Instrumentable during the configuration of
     *  the InstrumentManager and to gain access to an InstrumentableDescriptor
     *  through the InstrumentManager.  The value should be a string which does
     *  not contain spaces or periods.
     * <p>
     * This value may be set by a parent Instrumentable, or by the
     *  InstrumentManager using the value of the 'instrumentable' attribute in
     *  the configuration of the component.
     *
     * @param name The name used to identify a Instrumentable.
     */
    public void setInstrumentableName( String name )
    {
        m_instrumentableName = name;
    }

    /**
     * Gets the name of the Instrumentable.
     *
     * @return The name used to identify a Instrumentable.
     */
    public String getInstrumentableName()
    {
        return m_instrumentableName;
    }

    /**
     * Obtain a reference to all the Instruments that the Instrumentable object
     *  wishes to expose.  All sampling is done directly through the
     *  Instruments as opposed to the Instrumentable interface.
     *
     * @return An array of the Instruments available for profiling.  Should
     *         never be null.  If there are no Instruments, then
     *         EMPTY_INSTRUMENT_ARRAY can be returned.  This should never be
     *         the case though unless there are child Instrumentables with
     *         Instruments.
     */
    public Instrument[] getInstruments()
    {
        return new Instrument[]
        {
            m_sizeInstrument,
            m_readySizeInstrument,
            m_getsInstrument,
            m_putsInstrument,
            m_blocksInstrument,
            m_createsInstrument,
            m_decommissionsInstrument
        };
    }

    /**
     * Any Object which implements Instrumentable can also make use of other
     *  Instrumentable child objects.  This method is used to tell the
     *  InstrumentManager about them.
     *
     * @return An array of child Instrumentables.  This method should never
     *         return null.  If there are no child Instrumentables, then
     *         EMPTY_INSTRUMENTABLE_ARRAY can be returned.
     */
    public Instrumentable[] getChildInstrumentables()
    {
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Permanently removes a poolable from the pool's active list and
     *  destroys it so that it will not ever be reused.
     * <p>
     * This method is only called by threads that have m_semaphore locked.
     */
    protected void permanentlyRemovePoolable( Poolable poolable )
    {
        m_size--;
        removePoolable( poolable );
    }

    /**
     * Returns the total number of Poolables created by the pool.  Includes active and ready.
     *
     * @return The total size.
     */
    public int getSize()
    {
        return m_size;
    }

    /**
     * Returns the number of available Poolables waiting in the pool.
     * Only called when synchronized.
     *
     * @return The ready size.
     */
    private int getReadySizeSync()
    {
        return m_readySize + m_oldReadySize;
    }
    
    /**
     * Returns the number of available Poolables waiting in the pool.
     *
     * @return The ready size.
     */
    public int getReadySize()
    {
        synchronized( m_semaphore )
        {
            return getReadySizeSync();
        }
    }

    /**
     * Create a new poolable instance by by calling the newInstance method
     *  on the pool's ObjectFactory.
     * <p>
     * This is the method to override when you need to enforce creational
     *  policies.
     * <p>
     * This method is only called by threads that have m_semaphore locked.
     */
    protected Poolable newPoolable() throws Exception
    {
        Object obj = m_factory.newInstance();

        // Notify the InstrumentManager
        m_createsInstrument.increment();
        // The size is incremented after this call in case an error is thrown.
        m_sizeInstrument.setValue( getSize() + 1 );

        return (Poolable)obj;
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
        try
        {
            m_factory.decommission( poolable );

            // Notify the InstrumentManager
            m_decommissionsInstrument.increment();
            m_sizeInstrument.setValue( getSize() );
        }
        catch( Exception e )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Error decommissioning object", e );
            }
        }
    }

    /**
     * Forces the pool to trim, remove, old Poolables from the pool.  If the Pool
     *  was created with a non-zero value for trimInterval, then this method will
     *  be called at that interval when get() is called.  If get() is not called
     *  for long periods of time then if may be necessary to call this method
     *  manually.
     * <p>
     * Trimming is done by maintaining two lists of objects.  The first is a ready list
     *  of new poolables. The second is a list of old poolables.  Each time trim() is
     *  called, the contents of the old list are removed from the pool.  Then the
     *  contents of the new list is moved into the old list.
     * <p>
     * Each time get() is called on the pool, the new list is checked first, then the
     *  old list is checked, finally a new poolable may be created if both lists are
     *  empty.  Then whenever put() is called, the poolables are always returned to
     *  the new list.  In this way, the need for maining time stamps for each poolable
     *  can be avoided while at the same time avoiding unnecessary removal and creation
     *  on poolables.
     * <p>
     * This works out to a poolable having a maximum idle time of two calls to trim() or
     *  twice the value of trimInterval.
     * <p>
     * NOTE - The trimming feature does not harm performance because pools with high
     *  load will not have old poolables to be trimmed, and the benefits to system
     *  resources from not keeping around unused poolables makes up for any hit.
     *
     * @return the number of Poolables that were trimmed.
     */
    public int trim()
    {
        if( m_oldReady != null )
        {
            synchronized( m_semaphore )
            {
                return trimInner();
            }
        }
        else
        {
            throw new IllegalStateException( "This pool is not configured to do trimming." );
        }
    }

    /**
     * See trim() for details.
     *
     * This method is only called by threads that have m_semaphore locked.
     */
    private int trimInner()
    {
        int trimCount = 0;

        // Remove any poolables in the m_oldReady list.
        if( m_oldReadySize > 0 )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Trimming " + m_oldReadySize + " idle objects from pool." );
            }

            trimCount = m_oldReadySize;

            for( Iterator iter = m_oldReady.iterator(); iter.hasNext(); )
            {
                Poolable poolable = (Poolable)iter.next();
                iter.remove();
                m_oldReadySize--;
                permanentlyRemovePoolable( poolable );
            }
        }

        // Move the poolables in m_ready into m_oldReady (swap lists)
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Marking " + m_readySize + " objects as old in pool." );
        }
        LinkedList tempList = m_oldReady;
        m_oldReady = m_ready;
        m_oldReadySize = m_readySize;
        m_ready = tempList;
        m_readySize = 0;

        m_lastTrim = System.currentTimeMillis();

        return trimCount;
    }
}
