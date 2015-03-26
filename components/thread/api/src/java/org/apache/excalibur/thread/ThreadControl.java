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
package org.apache.excalibur.thread;

/**
 * This interface defines the method through which Threads can be controller.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface ThreadControl
{
    /**
     * Wait for specified time for thread to complete it's work.
     *
     * @param milliSeconds the duration in milliseconds to wait until the thread has finished work
     * @throws IllegalStateException if isValid() == false
     * @throws InterruptedException if another thread has interrupted the current thread.
     *            The interrupted status of the current thread is cleared when this exception
     *            is thrown.
     */
    void join( long milliSeconds )
        throws IllegalStateException, InterruptedException;

    /**
     * Call {@link Thread#interrupt()} on thread being controlled.
     *
     * @throws IllegalStateException if isValid() == false
     * @throws SecurityException if caller does not have permission to call interupt()
     */
    void interrupt()
        throws IllegalStateException, SecurityException;

    /**
     * Determine if thread has finished execution
     *
     * @return true if thread is finished, false otherwise
     */
    boolean isFinished();

    /**
     * Retrieve throwable that caused thread to cease execution.
     * Only valid when true == isFinished()
     *
     * @return the throwable that caused thread to finish execution
     */
    Throwable getThrowable();
}
