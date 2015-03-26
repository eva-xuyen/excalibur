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

import org.apache.avalon.excalibur.pool.Poolable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.thread.impl.AbstractThreadPool;
import org.apache.excalibur.thread.impl.WorkerThread;

/**
 * This class extends the Thread class to add recyclable functionalities.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class SimpleWorkerThread
    extends WorkerThread
    implements Poolable, LogEnabled
{
    /** Log major events like uncaught exceptions and worker creation
     *   and deletion.  Stuff that is useful to be able to see over long
     *   periods of time. */
    private Logger m_logger;
    
    /**
     * Log minor detail events like 
     */
    private Logger m_detailLogger;

    /**
     * Allocates a new <code>Worker</code> object.
     */
    protected SimpleWorkerThread( final AbstractThreadPool pool,
                                  final ThreadGroup group,
                                  final String name )
    {
        super( pool, group, name );
    }

    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
        m_detailLogger = logger.getChildLogger( "detail" );
        
        // Log a created message here rather as we can't in the constructor
        //  due to the lack of a logger.
        debug( "created." );
    }

    /**
     * Used to log major events against the worker.  Creation, deletion,
     *  uncaught exceptions etc.
     *
     * @param message Message to log.
     */
    protected void debug( final String message )
    {
        if ( m_logger.isDebugEnabled() )
        {
            // As we are dealing with threads where more than one thread is
            //  always involved, log both the name of the thread that triggered
            //  event along with the name of the worker involved.  This
            //  increases the likely hood of walking away sane after a
            //  debugging session.
            m_logger.debug( "\"" + getName() + "\" "
                + "(in " + Thread.currentThread().getName() + ") : " + message );
        }
    }

    /**
     * Used to log major events against the worker.  Creation, deletion,
     *  uncaught exceptions etc.
     *
     * @param message Message to log.
     * @param throwable Throwable to log with the message.
     */
    protected void debug( final String message, final Throwable throwable )
    {
        if ( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "\"" + getName() + "\" "
                + "(in " + Thread.currentThread().getName() + ") : " + message, throwable );
        }
    }

    /**
     * Used to log minor events against the worker.  Start and stop of
     *  individual pieces of work etc.  Separated from the major events
     *  so that they are not lost in a sea of minor events.
     *
     * @param message Message to log.
     */
    protected void detailDebug( final String message )
    {
        if ( m_detailLogger.isDebugEnabled() )
        {
            m_detailLogger.debug( "\"" + getName() + "\" "
                + "(in " + Thread.currentThread().getName() + ") : " + message );
        }
    }

    /**
     * Used to log minor events against the worker.  Start and stop of
     *  individual pieces of work etc.  Separated from the major events
     *  so that they are not lost in a sea of minor events.
     *
     * @param message Message to log.
     * @param throwable Throwable to log with the message.
     */
    protected void detailDebug( final String message, final Throwable throwable )
    {
        if ( m_detailLogger.isDebugEnabled() )
        {
            m_detailLogger.debug( "\"" + getName() + "\" "
                + "(in " + Thread.currentThread().getName() + ") : " + message, throwable );
        }
    }
}

