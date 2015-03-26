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
package org.apache.log.output.jms;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.log.ErrorHandler;
import org.apache.log.LogEvent;
import org.apache.log.output.AbstractTarget;

/**
 * A target that writes to a JMS Topic.
 *
 * @author Peter Donald
 */
public abstract class AbstractJMSTarget
    extends AbstractTarget
{
    ///Appropriate MessageBuilder
    private MessageBuilder m_builder;

    public AbstractJMSTarget( final MessageBuilder builder )
    {
        m_builder = builder;
    }

    public AbstractJMSTarget( final MessageBuilder builder,
                              final ErrorHandler errorHandler )
    {
        super( errorHandler );
        m_builder = builder;
    }

    protected abstract void send( Message message );

    protected abstract Session getSession();

    /**
     * Process a log event, via formatting and outputting it.
     *
     * @param event the log event
     */
    protected void doProcessEvent( final LogEvent event )
        throws Exception
    {
        final Message message =
            m_builder.buildMessage( getSession(), event );
        send( message );
    }

    /**
     * Startup log session.
     *
     */
    protected synchronized void open()
    {
        if( !isOpen() )
        {
            super.open();
            openConnection();
        }
    }

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     *
     */
    public synchronized void close()
    {
        if( isOpen() )
        {
            closeConnection();
            super.close();
        }
    }

    protected abstract void openConnection();

    protected abstract void closeConnection();
}
