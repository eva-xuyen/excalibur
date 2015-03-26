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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;
import org.apache.log.format.Formatter;

/**
 * Basic message factory that stores LogEvent in Message.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 */
public class TextMessageBuilder
    implements MessageBuilder
{
    private final PropertyInfo[] m_properties;
    private final Formatter m_formatter;

    /**
     * Creation of a new text message builder.
     * @param formatter the message formatter
     */
    public TextMessageBuilder( final Formatter formatter )
    {
        m_properties = new PropertyInfo[ 0 ];
        m_formatter = formatter;
    }

    /**
     * Creation of a new text message builder.
     * @param properties the property info set
     * @param formatter the message formatter
     */
    public TextMessageBuilder( final PropertyInfo[] properties,
                               final Formatter formatter )
    {
        m_properties = properties;
        m_formatter = formatter;
    }

    /**
     * Build a message from the supplied session for the supplied event
     * @param session the session
     * @param event the log event
     * @return the message
     * @exception JMSException if a messaging related error occurs
     */
    public Message buildMessage( final Session session, final LogEvent event )
        throws JMSException
    {
        synchronized( session )
        {
            final TextMessage message = session.createTextMessage();

            message.setText( getText( event ) );
            for( int i = 0; i < m_properties.length; i++ )
            {
                setProperty( message, i, event );
            }

            return message;
        }
    }

    /**
     * Set a property
     * @param message the text message
     * @param index the index
     * @param event the log event
     */
    private void setProperty( final TextMessage message,
                              final int index,
                              final LogEvent event )
        throws JMSException
    {
        final PropertyInfo info = m_properties[ index ];
        final String name = info.getName();

        switch( info.getType() )
        {
            case PropertyType.MESSAGE:
                message.setStringProperty( name, event.getMessage() );
                break;

            case PropertyType.RELATIVE_TIME:
                message.setLongProperty( name, event.getRelativeTime() );
                break;

            case PropertyType.TIME:
                message.setLongProperty( name, event.getTime() );
                break;

            case PropertyType.CATEGORY:
                message.setStringProperty( name, event.getCategory() );
                break;

            case PropertyType.PRIORITY:
                message.setStringProperty( name, event.getPriority().getName() );
                break;

            case PropertyType.CONTEXT:
                message.setStringProperty( name, getContextMap( event.getContextMap(),
                                                                info.getAux() ) );
                break;

            case PropertyType.STATIC:
                message.setStringProperty( name, info.getAux() );
                break;

            case PropertyType.THROWABLE:
                message.setStringProperty( name, getStackTrace( event.getThrowable() ) );
                break;

            default:
                throw new IllegalStateException( "Unknown PropertyType: " + info.getType() );
        }

    }

    private String getText( final LogEvent event )
    {
        if( null == m_formatter )
        {
            return event.getMessage();
        }
        else
        {
            return m_formatter.format( event );
        }
    }

    private String getStackTrace( final Throwable throwable )
    {
        if( null == throwable )
        {
            return "";
        }

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter( stringWriter );
        throwable.printStackTrace( printWriter );

        return stringWriter.getBuffer().toString();
    }

    private String getContextMap( final ContextMap map, final String aux )
    {
        if( null == map )
        {
            return "";
        }
        return map.get( aux, "" ).toString();
    }
}
