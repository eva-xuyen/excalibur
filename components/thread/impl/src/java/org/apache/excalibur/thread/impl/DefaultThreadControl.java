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

import org.apache.excalibur.thread.ThreadControl;

/**
 * Default implementation of ThreadControl interface.
 * Is used by worker thread to supply control information to the
 * clients of thread pool.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
final class DefaultThreadControl
    implements ThreadControl
{
    ///Thread that this control is associated with
    private Thread m_thread;

    ///Throwable that caused thread to terminate
    private Throwable m_throwable;

    /**
     * Construct thread control for a specific thread.
     *
     * @param thread the thread to control
     */
    protected DefaultThreadControl( final Thread thread )
    {
        m_thread = thread;
    }

    /**
     * Wait for specified time for thread to complete it's work.
     *
     * @param milliSeconds the duration in milliseconds to wait until the thread has finished work
     * @throws java.lang.IllegalStateException if isValid() == false
     * @throws java.lang.InterruptedException if another thread has interrupted the current thread.
     *            The interrupted status of the current thread is cleared when this exception
     *            is thrown.
     */
    public synchronized void join( final long milliSeconds )
        throws IllegalStateException, InterruptedException
    {
        if (! isFinished() )
        {
            m_thread.join(milliSeconds);
        }
    }

    public void interupt()
        throws IllegalStateException, SecurityException
    {
        interrupt();
    }

    /**
     * Call Thread.interrupt() on thread being controlled.
     *
     * @throws IllegalStateException if isValid() == false
     * @throws SecurityException if caller does not have permission to call interupt()
     */
    public synchronized void interrupt()
        throws IllegalStateException, SecurityException
    {
        if( !isFinished() )
        {
            m_thread.interrupt();
        }
    }

    /**
     * Determine if thread has finished execution
     *
     * @return true if thread is finished, false otherwise
     */
    public synchronized boolean isFinished()
    {
        return ( null == m_thread );
    }

    /**
     * Retrieve throwable that caused thread to cease execution.
     * Only valid when true == isFinished()
     *
     * @return the throwable that caused thread to finish execution
     */
    public Throwable getThrowable()
    {
        return m_throwable;
    }

    /**
     * Method called by thread to release control.
     *
     * @param throwable Throwable that caused thread to complete (may be null)
     */
    protected synchronized void finish( final Throwable throwable )
    {
        m_thread = null;
        m_throwable = throwable;
        notifyAll();
    }
}
