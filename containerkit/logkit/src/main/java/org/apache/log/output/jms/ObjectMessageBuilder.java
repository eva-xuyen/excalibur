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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.apache.log.LogEvent;

/**
 * Basic message factory that stores LogEvent in Message.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 */
public class ObjectMessageBuilder
    implements MessageBuilder
{

    /**
     * Creation of a new message.
     * @param session the session against which the message will be created
     * @param event the log event
     * @return the message
     * @exception JMSException if a messaging error occurs
     */
    public Message buildMessage( Session session, LogEvent event )
        throws JMSException
    {
        //session access is single threaded
        synchronized( session )
        {
            final ObjectMessage message = session.createObjectMessage();
            message.setObject( event );
            return message;
        }
    }
}
