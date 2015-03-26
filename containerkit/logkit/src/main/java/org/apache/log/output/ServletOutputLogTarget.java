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
package org.apache.log.output;

import javax.servlet.ServletContext;
import org.apache.log.format.Formatter;

/**
 * Generic logging interface. Implementations are based on the strategy
 * pattern.
 *
 * @author <a href="mailto:Tommy.Santoso@osa.de">Tommy Santoso</a>
 */
public class ServletOutputLogTarget
    extends AbstractOutputTarget
{
    ///The servlet context written to (may be null in which case it won't log at all)
    private ServletContext m_context;

    /**
     * Constructor.
     *
     * @param context ServletContext to use for logging.
     */
    public ServletOutputLogTarget( final ServletContext context, final Formatter formatter )
    {
        super( formatter );
        m_context = context;
        open();
    }

    /**
     * Constructor.
     *
     * @param context ServletContext to use for logging.
     */
    public ServletOutputLogTarget( final ServletContext context )
    {
        m_context = context;
        open();
    }

    /**
     * Logs message to servlet context log file
     *
     * @param message message to log to servlet context log file.
     */
    protected void write( final String message )
    {
        final int len = message.length();
        final char last = len > 0 ? message.charAt( len - 1 ) : 0;
        final char prev = len > 1 ? message.charAt( len - 2 ) : 0;
        final String trimmedMessage;
        if( prev == '\r' && last == '\n' )
        {
            trimmedMessage = message.substring( 0, len - 2 );
        }
        else if( last == '\n' )
        {
            trimmedMessage = message.substring( 0, len - 1 );
        }
        else
        {
            trimmedMessage = message;
        }

        final ServletContext context = m_context;
        if( null != context )
        {
            synchronized( context )
            {
                context.log( trimmedMessage );
            }
        }
    }

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     */
    public synchronized void close()
    {
        super.close();
        m_context = null;
    }
}
