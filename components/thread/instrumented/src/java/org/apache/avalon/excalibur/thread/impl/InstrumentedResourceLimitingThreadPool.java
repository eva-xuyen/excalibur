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
import org.apache.avalon.excalibur.pool.InstrumentedResourceLimitingPool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;

import org.apache.excalibur.thread.ThreadControl;
import org.apache.excalibur.thread.ThreadPool;

/**
 * A Thread Pool which can be configured to have a hard limit on the maximum number of threads
 *  which will be allocated.  This is very important for servers to avoid running out of system
 *  resources.  The pool can be configured to block for a new thread or throw an exception.
 *  The maximum block time can also be set.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 17:22:49 $
 * @since 4.1
 */
public class InstrumentedResourceLimitingThreadPool
    extends ThreadGroup
    implements ObjectFactory, LogEnabled, Disposable, ThreadPool, Instrumentable
{
    private InstrumentedResourceLimitingPool m_underlyingPool;

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName;

    /**
     * The associated thread pool.
     */
    private BasicThreadPool m_pool;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/

    /**
     * Creates a new <code>ResourceLimitingThreadPool</code>.
     *
     * @param max Maximum number of Poolables which can be stored in the pool, 0 implies no limit.
     */
    public InstrumentedResourceLimitingThreadPool( final int max )
    {
        this( "Worker Pool", max );
    }

    /**
     * Creates a new <code>ResourceLimitingThreadPool</code> with maxStrict enabled,
     *  blocking enabled, no block timeout and a trim interval of 10 seconds.
     *
     * @param name Name which will used as the thread group name as well as the prefix of the
     *  names of all threads created by the pool.
     * @param max Maximum number of WorkerThreads which can be stored in the pool,
     *  0 implies no limit.
     */
    public InstrumentedResourceLimitingThreadPool( final String name, final int max )
    {
        this( name, max, true, true, 0, 10000 );
    }

    /**
     * Creates a new <code>ResourceLimitingThreadPool</code>.
     *
     * @param name Name which will used as the thread group name as well as the prefix of the
     *  names of all threads created by the pool.
     * @param max Maximum number of WorkerThreads which can be stored in the pool,
     *  0 implies no limit.
     * @param maxStrict true if the pool should never allow more than max WorkerThreads to
     *  be created.  Will cause an exception to be thrown if more than max WorkerThreads are
     *  requested and blocking is false.
     * @param blocking true if the pool should cause a thread calling get() to block when
     *  WorkerThreads are not currently available on the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused WorkerThreads will be
     *  removed from the pool.  A value of 0 will cause the pool to never trim WorkerThreads.
     */
    public InstrumentedResourceLimitingThreadPool( final String name,
                                       final int max,
                                       final boolean maxStrict,
                                       final boolean blocking,
                                       final long blockTimeout,
                                       final long trimInterval )
    {
        super( name );

        m_underlyingPool =
            new InstrumentedResourceLimitingPool( this, max, maxStrict,
                                      blocking, blockTimeout,
                                      trimInterval );
        try
        {
            m_pool = new BasicThreadPool( this, name, m_underlyingPool );
        }
        catch( Exception e )
        {
            final String message = "Unable to create ThreadPool due to " + e;
            throw new IllegalStateException( message );
        }
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
        return Instrumentable.EMPTY_INSTRUMENT_ARRAY;
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
        return new Instrumentable[]{m_underlyingPool};
    }

    /**
     * Return the number of worker threads in the pool.
     *
     * @return the numebr of worker threads in the pool.
     */
    public int getSize()
    {
        return m_underlyingPool.getSize();
    }

    public void enableLogging( final Logger logger )
    {
        ContainerUtil.enableLogging( m_pool, logger );
    }

    public void dispose()
    {
        m_pool.dispose();
    }

    public Object newInstance()
    {
        return m_pool.newInstance();
    }

    public void decommission( final Object object )
    {
        m_pool.decommission( object );
    }

    public Class getCreatedClass()
    {
        return m_pool.getCreatedClass();
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
        return m_pool.execute( work );
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final Runnable work )
    {
        return m_pool.execute( work );
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final org.apache.excalibur.thread.Executable work )
    {
        return m_pool.execute( work );
    }
}
