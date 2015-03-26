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
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import org.apache.log.ErrorHandler;

/**
 * A target that writes to a JMS Topic.
 *
 * @author Peter Donald
 */
public class JMSTopicTarget
    extends AbstractJMSTarget
{
    ///ConnectionFactory to use
    private TopicConnectionFactory m_factory;

    ///Topic we will send messages to
    private Topic m_topic;

    ///Session associated with topic
    private TopicSession m_session;

    ///Publisher for topic
    private TopicPublisher m_publisher;

    ///JMS topic Connection
    private TopicConnection m_connection;

    public JMSTopicTarget( final MessageBuilder builder,
                           final TopicConnectionFactory factory,
                           final Topic topic )
    {
        super( builder );
        m_factory = factory;
        m_topic = topic;
        open();
    }

    public JMSTopicTarget( final MessageBuilder builder,
                           final TopicConnectionFactory factory,
                           final Topic topic,
                           final ErrorHandler handler )
    {
        super( builder, handler );
        m_factory = factory;
        m_topic = topic;
        open();
    }

    protected void send( final Message message )
    {
        try
        {
            m_publisher.publish( message );
        }
        catch( final Exception e )
        {
            getErrorHandler().error( "Error publishing message", e, null );
        }
    }

    protected Session getSession()
    {
        return m_session;
    }

    protected synchronized void openConnection()
    {
        try
        {
            m_connection = m_factory.createTopicConnection();
            m_connection.start();

            m_session =
                m_connection.createTopicSession( false, Session.AUTO_ACKNOWLEDGE );

            m_publisher = m_session.createPublisher( m_topic );
            //if( m_persistent ) publisher.setDeliveryMode( DeliveryMode.PERSISTENT );
            //else publisher.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
            //publisher.setPriority( m_priority );
            //publisher.setTimeToLive( m_timeToLive );
        }
        catch( final Exception e )
        {
            getErrorHandler().error( "Error starting connection", e, null );
        }
    }

    protected synchronized void closeConnection()
    {
        try
        {
            if( null != m_publisher ) m_publisher.close();
            if( null != m_session ) m_session.close();
            if( null != m_connection ) m_connection.close();
        }
        catch( Exception e )
        {
            getErrorHandler().error( "Error closing connection", e, null );
        }

        m_publisher = null;
        m_session = null;
        m_connection = null;
    }
}
