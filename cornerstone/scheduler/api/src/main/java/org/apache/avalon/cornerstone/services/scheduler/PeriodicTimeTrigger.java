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

package org.apache.avalon.cornerstone.services.scheduler;

/**
 * Goes off every <tt>period</tt> milliseconds after waiting for
 * <tt>offset</tt> milliseconds from the moment the trigger was
 * <tt>reset</tt>.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class PeriodicTimeTrigger
    implements TimeTrigger
{
    protected final long m_offset;
    protected final long m_period;
    private long m_triggerTime;

    /**
     * Creates a periodic trigger. It goes off the first time after
     * <tt>offset</tt> milliseconds from the time it was
     * <tt>reset</tt> and then every <tt>period</tt>
     * milliseconds. The trigger is <tt>reset</tt> as
     * part of its construction.
     *
     * @param offset initial delay in milliseconds, -1 means fire immediately
     * @param period after initial delay in milliseconds, -1 means fire only once after initial delay
     */
    public PeriodicTimeTrigger( final int offset, final int period )
    {
        m_offset = offset;
        m_period = period;

        reset();
    }

    /**
     * Returns the next time after the given <tt>moment</tt> when
     * this trigger goes off.
     *
     * @param moment base point in milliseconds
     * @return the time in milliseconds when this trigger goes off
     */
    public long getTimeAfter( final long moment )
    {
        if( moment <= m_triggerTime )
        {
            return m_triggerTime;
        }
        else
        {
            if( -1 == m_period )
            {
                return -1;
            }

            final long over = moment - m_triggerTime;
            final long remainder = over % m_period;

            return moment + ( m_period - remainder );
        }
    }

    public long getOffset()
    {
        return m_offset;
    }

    public long getPeriod()
    {
        return m_period;
    }

    /**
     * Reset the original TimeTrigger.
     * This will recalculate the activation time for this trigger.
     */
    public void reset()
    {
        final long current = System.currentTimeMillis();

        if( -1 == m_offset )
        {
            m_triggerTime = current;
        }
        else
        {
            m_triggerTime = current + m_offset;
        }
    }

    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append( "PeriodicTimeTrigger[ " );

        sb.append( "trigger time=" );
        sb.append( m_triggerTime );
        sb.append( " " );

        sb.append( "offset=" );
        sb.append( m_offset );
        sb.append( " " );

        if( -1 != m_period )
        {
            sb.append( "period=" );
            sb.append( m_period );
            sb.append( " " );
        }

        sb.append( "]" );

        return sb.toString();
    }
}



