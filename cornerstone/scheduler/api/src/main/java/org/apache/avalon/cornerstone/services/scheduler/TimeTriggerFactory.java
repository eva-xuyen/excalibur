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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Factory for <code>TimeTrigger</code>s.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class TimeTriggerFactory
{
    /**
     * Create <code>TimeTrigger</code> from configuration.
     *
     * @param conf configuration for time trigger
     */
    public TimeTrigger createTimeTrigger( final Configuration conf )
        throws ConfigurationException
    {
        final String type = conf.getAttribute( "type" );

        TimeTrigger trigger;
        if( "periodic".equals( type ) )
        {
            final int offset =
                conf.getChild( "offset", true ).getValueAsInteger( 0 );
            final int period =
                conf.getChild( "period", true ).getValueAsInteger( -1 );

            trigger = new PeriodicTimeTrigger( offset, period );
        }
        else if( "cron".equals( type ) )
        {
            final int minute =
                conf.getChild( "minute" ).getValueAsInteger( -1 );
            final int hour =
                conf.getChild( "hour" ).getValueAsInteger( -1 );
            final int day =
                conf.getChild( "day" ).getValueAsInteger( -1 );
            final int month =
                conf.getChild( "month" ).getValueAsInteger( -1 );
            final int year =
                conf.getChild( "year" ).getValueAsInteger( -1 );
            final boolean dayOfWeek =
                conf.getChild( "day" ).getAttributeAsBoolean( "week", false );

            trigger = new CronTimeTrigger( minute, hour, day, month, year,
                                           dayOfWeek );
        }
        else
        {
            throw new ConfigurationException( "Unknown trigger type" );
        }

        return trigger;
    }
}
