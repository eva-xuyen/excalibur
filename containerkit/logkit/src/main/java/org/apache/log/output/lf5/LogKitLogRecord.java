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
package org.apache.log.output.lf5;

import java.util.Arrays;
import java.util.List;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.Formatter;
import org.apache.log.util.StackIntrospector;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;

/**
 * An implementation of a LogFactor5 <code>LogRecord</code> based on a
 * LogKit {@link LogEvent}.
 *
 * @author <a href="sylvain@apache.org">Sylvain Wallez</a>
 * @version $Id: LogKitLogRecord.java 506267 2007-02-12 04:06:06Z crossley $
 */

public class LogKitLogRecord
    extends LogRecord
{
    /** Is this a severe event ? */
    private boolean m_severe;

    /**
     * Create a LogFactor record from a LogKit event
     */
    public LogKitLogRecord( final LogEvent event, final Formatter fmt )
    {
        final ContextMap contextMap = event.getContextMap();

        Object contextObject;

        // Category
        setCategory( event.getCategory() );

        // Level
        setLevel( toLogLevel( event.getPriority() ) );
        m_severe = event.getPriority().isGreater( Priority.INFO );

        // Location
        if( null != contextMap && null != ( contextObject = contextMap.get( "method" ) ) )
        {
            setLocation( contextObject.toString() );
        }
        else
        {
            setLocation( StackIntrospector.getCallerMethod( Logger.class ) );
        }

        // Message
        setMessage( event.getMessage() );

        // Millis
        setMillis( event.getTime() );

        // NDC
        setNDC( fmt.format( event ) );

        // SequenceNumber
        //setSequenceNumber( 0L );

        // ThreadDescription
        if( null != contextMap && null != ( contextObject = contextMap.get( "thread" ) ) )
        {
            setThreadDescription( contextObject.toString() );
        }
        else
        {
            setThreadDescription( Thread.currentThread().getName() );
        }

        // Thrown
        setThrown( event.getThrowable() );

        // ThrownStackTrace
        //setThrownStackTrace("");
    }

    public boolean isSevereLevel()
    {
        return m_severe;
    }

    /**
     * Convert a LogKit <code>Priority</code> to a LogFactor <code>LogLevel</code>.
     */
    public LogLevel toLogLevel( final Priority priority )
    {
        if( Priority.DEBUG == priority )
        {
            return LogLevel.DEBUG;
        }
        else if( Priority.INFO == priority )
        {
            return LogLevel.INFO;
        }
        else if( Priority.WARN == priority )
        {
            return LogLevel.WARN;
        }
        else if( Priority.ERROR == priority )
        {
            return LogLevel.ERROR;
        }
        else if( Priority.FATAL_ERROR == priority )
        {
            return LogLevel.FATAL;
        }
        else
        {
            return new LogLevel( priority.getName(), priority.getValue() );
        }
    }

    /**
     * The <code>LogLevel</code>s corresponding to LogKit priorities.
     */
    public static final List LOGKIT_LOGLEVELS =
        Arrays.asList( new LogLevel[]{
            LogLevel.FATAL, LogLevel.ERROR, LogLevel.WARN, LogLevel.INFO, LogLevel.DEBUG
        } );
}
