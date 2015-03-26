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

package org.apache.avalon.cornerstone.blocks.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.avalon.cornerstone.services.scheduler.Target;
import org.apache.avalon.cornerstone.services.scheduler.TimeTrigger;

/**
 * Class use internally to package to hold scheduled time entries.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class TimeScheduledEntry
    implements Comparable
{
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat();

    private final String m_name;
    private final TimeTrigger m_trigger;
    private final Target m_target;

    //cached version of time from TimeTrigger class
    private long m_time;
    private boolean m_isValid;

    public TimeScheduledEntry( String name, TimeTrigger trigger, Target target )
    {
        m_name = name;
        m_trigger = trigger;
        m_target = target;
        //m_time = m_trigger.getTimeAfter( System.currentTimeMillis() );
        m_isValid = true;
    }

    /**
     * Return name of trigger.
     *
     * @return the name of trigger
     */
    public String getName()
    {
        return m_name;
    }

    public Target getTarget()
    {
        return m_target;
    }

    public TimeTrigger getTimeTrigger()
    {
        return m_trigger;
    }

    /**
     * Determine if this entry is valid
     *
     * @return true if trigger is valid, false otherwise
     */
    public boolean isValid()
    {
        return m_isValid;
    }

    /**
     * Invalidate trigger
     */
    public void invalidate()
    {
        m_isValid = false;
    }

    /**
     * Retrieve cached time when trigger should run next.
     *
     * @return the time in milliseconds when trigger should run
     */
    public long getNextTime()
    {
        return m_time;
    }

    /**
     * Set cached time in milliseconds when trigger should run
     *
     * @param time the time
     */
    public void setNextTime( long time )
    {
        m_time = time;
    }

    /**
     * Implement comparable interface used to help sort triggers.
     * Triggers are compared based on next time to run
     *
     * @param object the other trigger
     * @return -'ve value if other trigger occurs before this trigger
     */
    public int compareTo( final Object object )
    {
        final TimeScheduledEntry other = (TimeScheduledEntry)object;
		final long diff= other.m_time - m_time;
		if( diff < 0 )
		{
			return 1;
		}
		else
		if( diff == 0 )
		{
			return 0;
		}
		else
		{
			return -1;
		}
    }

    public String toString()
    {
        return "TimeEntry[ name=" + m_name + " valid=" + m_isValid + " time=" + DATEFORMAT.format( new Date( m_time ) ) + " ]";
    }
}

