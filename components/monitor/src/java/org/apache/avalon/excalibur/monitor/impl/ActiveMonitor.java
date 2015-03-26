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
package org.apache.avalon.excalibur.monitor.impl;

/**
 * The ActiveMonitor is used to actively check a set of resources to see if they have
 * changed. It will poll the resources with a frequency as specified or if
 * unspecified with the default (60 seconds).
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ActiveMonitor.java,v 1.5 2004/02/28 11:47:35 cziegeler Exp $
 */
public class ActiveMonitor
    extends AbstractMonitor
    implements Runnable
{
    private static final long DEFAULT_FREQUENCY = 1000L * 60L;

    /**
     * The frequency to scan resources for changes measured
     * in milliseconds.
     */
    private long m_frequency = DEFAULT_FREQUENCY;

    /**
     * The priority of the thread that monitors resources. Defaults
     * to System specific {@link Thread#MIN_PRIORITY}.
     */
    private int m_priority = Thread.MIN_PRIORITY;

    /**
     * The thread that does the monitoring.
     */
    private final Thread m_monitorThread = new Thread( this );

    /**
     * Set to false to shutdown the thread.
     */
    private volatile boolean m_keepRunning = true;

    /**
     * Set the frequency with which the monitor
     * checks the resources. This can be changed
     * anytime and will be enabled the next time
     * through the check.
     *
     * @param frequency the frequency to scan resources for changes
     */
    public void setFrequency( final long frequency )
    {
        m_frequency = frequency;
    }

    /**
     * Set the priority of the active monitors thread.
     *
     * @param priority the priority of the active monitors thread.
     */
    public void setPriority( final int priority )
    {
        m_priority = priority;
    }

    public void start()
        throws Exception
    {
        m_keepRunning = true;
        m_monitorThread.setDaemon( true );
        m_monitorThread.setPriority( m_priority );
        m_monitorThread.start();
    }

    public void stop()
        throws Exception
    {
        m_keepRunning = false;
        m_monitorThread.interrupt();
        m_monitorThread.join();
    }

    public final void run()
    {
        try
        {
            while( m_keepRunning )
            {
                Thread.sleep( m_frequency );
                scanAllResources();
            }
        }
        catch( InterruptedException e )
        {
            // clears the interrupted status
            Thread.interrupted();
        }
    }
}
