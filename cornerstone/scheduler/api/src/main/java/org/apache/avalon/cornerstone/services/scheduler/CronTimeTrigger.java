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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This is the holder triggers based on standard crontabs format.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class CronTimeTrigger
    implements TimeTrigger
{
    protected final int m_minute;
    protected final int m_hour;
    protected final int m_dayOfMonth;
    protected final int m_month;
    protected final int m_dayOfWeek;
    protected final int m_year;

    /**
     * Constructor for CronTimeTrigger.
     * Day is either day of week or day of month depending on value of isDayOfWeek.
     * if (isDayOfWeek == true) then valid values are 1-7 otherwise the values
     * are 1-31
     *
     * @param minute the minute at which job is scheduled. (0-59)
     * @param hour hour at which job is scheduled. (0-23 or -1 for every hour)
     * @param month the month at which job is scheduled. (0-11 or -1 for every month)
     * @param year the year when job is scheduled (-1 implies every year)
     * @param day the day
     * @param isDayOfWeek true if day is a day of week or false if day is day of month
     */
    public CronTimeTrigger( final int minute,
                            final int hour,
                            final int day,
                            final int month,
                            final int year,
                            final boolean isDayOfWeek )
    {
        m_minute = minute;
        m_hour = hour;
        m_month = month;
        m_year = year;

        if( isDayOfWeek )
        {
            m_dayOfMonth = -1;
            m_dayOfWeek = day;
        }
        else
        {
            m_dayOfMonth = day;
            m_dayOfWeek = -1;
        }
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
        //first create calendars
        final Date timeMarker = new Date( moment );
        final GregorianCalendar relativeTo = new GregorianCalendar();
        relativeTo.setTime( timeMarker );
        relativeTo.set( Calendar.SECOND, 0 );

        final GregorianCalendar next = (GregorianCalendar)relativeTo.clone();

        if( -1 != m_minute )
            next.set( Calendar.MINUTE, m_minute );
        else
        {
            if( -1 == m_hour && -1 == m_month && -1 == m_year )
            {
                //roll minutes if all other values -1
                next.add( Calendar.MINUTE, 1 );
            }
            else
            {
                next.set( Calendar.MINUTE, 0 );
            }
        }

        if( -1 != m_hour )
        {
            next.set( Calendar.HOUR_OF_DAY, m_hour );
            if( -1 == m_minute ) next.set( Calendar.MINUTE, 0 );
        }

        if( -1 != m_month )
        {
            next.set( Calendar.MONTH, m_month );
            if( -1 == m_hour ) next.set( Calendar.HOUR_OF_DAY, 0 );
            if( -1 == m_minute ) next.set( Calendar.MINUTE, 0 );
        }

        if( -1 != m_year )
        {
            next.set( Calendar.YEAR, m_year );
            if( -1 == m_month ) next.set( Calendar.MONTH, 0 );
            if( -1 == m_hour ) next.set( Calendar.HOUR_OF_DAY, 0 );
            if( -1 == m_minute ) next.set( Calendar.MINUTE, 0 );
        }

        //use zeroed constant to make if statements easier to read
        final int minute = ( -1 != m_minute ) ? m_minute : 0;
        final int rminute = relativeTo.get( Calendar.MINUTE );

        if( -1 == m_year && -1 == m_month && -1 == m_hour &&
            -1 != m_minute && rminute >= minute )
        {
            //for every hour jobs and job is done this hour
            next.add( Calendar.HOUR_OF_DAY, 1 );
        }

        //use zeroed constant to make if statements easier to read
        final int hour = ( -1 != m_hour ) ? m_hour : 0;
        final int rhour = relativeTo.get( Calendar.HOUR_OF_DAY );

        if( -1 == m_dayOfMonth && -1 == m_dayOfWeek &&
            (
            //for when past hour that was scheduled to run
            ( -1 != m_hour && rhour > hour ) ||

            //for the case where you have to wrap over day
            //when hour is not specified
            ( -1 == m_hour && rhour == 24 && rminute >= minute ) ||

            //for when you are past time of day where both minute and
            //hour are specified
            ( -1 != m_hour && rhour == hour && rminute >= minute )
            )
        )
        {
            //for jobs scheduled everyday and job is done this day
            next.add( Calendar.DAY_OF_YEAR, 1 );
        }

        int realDayOfMonth = m_dayOfMonth;
        {
            //This block will update day of month if it is out of bounds
            //For instance if you ask to schedule on 30th of everymonth
            //this section will set the day to 28th (or 29th) in febuary
            //as there is no 30th
            final Calendar targetMonth = (GregorianCalendar)next.clone();
            targetMonth.set( Calendar.DAY_OF_MONTH, 1 );
            targetMonth.set( Calendar.MONTH, m_month );

            final int maxDayCount = targetMonth.getActualMaximum( Calendar.DAY_OF_MONTH );
            if( maxDayCount < realDayOfMonth )
            {
                realDayOfMonth = maxDayCount;
                next.add( Calendar.MONTH, -1 );
            }
        }

        final int month = ( -1 != m_month ) ? m_month : 0;
        final int dayOfMonth = ( -1 != m_dayOfMonth ) ? m_dayOfMonth : 0;

        //update the year if ran job for this year
        if( -1 != m_month && -1 == m_year &&
            ( relativeTo.get( Calendar.MONTH ) > month ||
            ( relativeTo.get( Calendar.MONTH ) == month &&
            ( relativeTo.get( Calendar.DAY_OF_MONTH ) > dayOfMonth ||
            ( relativeTo.get( Calendar.DAY_OF_MONTH ) == dayOfMonth &&
            ( relativeTo.get( Calendar.HOUR_OF_DAY ) > hour ||
            ( relativeTo.get( Calendar.HOUR_OF_DAY ) == hour &&
            ( relativeTo.get( Calendar.MINUTE ) >= minute ) ) ) ) ) ) ) )
        {
            next.add( Calendar.YEAR, 1 );
        }

        if( -1 != m_year )
        {
            //if past current year or already executed job this year then
            //bail out
            if( relativeTo.get( Calendar.YEAR ) > m_year ||
                ( relativeTo.get( Calendar.YEAR ) == m_year &&
                ( relativeTo.get( Calendar.MONTH ) > month ||
                ( relativeTo.get( Calendar.MONTH ) == month &&
                ( relativeTo.get( Calendar.DAY_OF_MONTH ) > dayOfMonth ||
                ( relativeTo.get( Calendar.DAY_OF_MONTH ) == dayOfMonth &&
                ( relativeTo.get( Calendar.HOUR_OF_DAY ) > hour ||
                ( relativeTo.get( Calendar.HOUR_OF_DAY ) == hour &&
                ( relativeTo.get( Calendar.MINUTE ) >= minute ) ) ) ) ) ) ) ) )
            {
                return -1;
            }
        }

        //schedule weekly jobs
        if( -1 != m_dayOfWeek )
        {
            final int dayWait =
                ( 7 + m_dayOfWeek - relativeTo.get( Calendar.DAY_OF_WEEK ) ) % 7;

            if( 0 != dayWait )
            {
                next.add( Calendar.DAY_OF_YEAR, dayWait );
            }
            else if( relativeTo.get( Calendar.HOUR_OF_DAY ) > hour ||
                ( relativeTo.get( Calendar.HOUR_OF_DAY ) == hour &&
                relativeTo.get( Calendar.MINUTE ) >= minute ) )
            {
                //if job scheduled for today has passed then schedule on next week
                next.add( Calendar.DAY_OF_YEAR, 7 );
            }
        }
        // Schedule monthly jobs
        else if( -1 != m_dayOfMonth )
        {
            //System.out.println( "Setting to maxday: " + realDayOfMonth );
            next.set( Calendar.DAY_OF_MONTH, realDayOfMonth );
            //next.set( Calendar.DAY_OF_MONTH, m_dayOfMonth );

            //if this months job has already run then schedule next week
            if( m_month == -1 &&
                ( relativeTo.get( Calendar.DAY_OF_MONTH ) > m_dayOfMonth ||
                ( relativeTo.get( Calendar.DAY_OF_MONTH ) == m_dayOfMonth &&
                ( relativeTo.get( Calendar.HOUR_OF_DAY ) > hour ||
                ( relativeTo.get( Calendar.HOUR_OF_DAY ) == hour &&
                relativeTo.get( Calendar.MINUTE ) >= minute ) ) ) ) )
            {
                next.roll( Calendar.MONTH, true );
            }
        }

        //return time in millis
        return next.getTime().getTime();
    }

    /**
     * Reset the cron-trigger.
     */
    public void reset()
    {
        // nothing to reset for CronTimeTrigger
    }

    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append( "CronTimeTrigger[ " );

        if( -1 != m_minute )
        {
            sb.append( "minute=" );
            sb.append( m_minute );
            sb.append( " " );
        }

        if( -1 != m_hour )
        {
            sb.append( "hour=" );
            sb.append( m_hour );
            sb.append( " " );
        }

        if( -1 != m_month )
        {
            sb.append( "month=" );
            sb.append( m_month );
            sb.append( " " );
        }

        if( -1 != m_year )
        {
            sb.append( "year=" );
            sb.append( m_year );
            sb.append( " " );
        }

        if( -1 != m_dayOfMonth )
        {
            sb.append( "dayOfMonth=" );
            sb.append( m_dayOfMonth );
            sb.append( " " );
        }

        if( -1 != m_dayOfWeek )
        {
            sb.append( "dayOfWeek=" );
            sb.append( m_dayOfWeek );
            sb.append( " " );
        }

        sb.append( "]" );

        return sb.toString();
    }
}



