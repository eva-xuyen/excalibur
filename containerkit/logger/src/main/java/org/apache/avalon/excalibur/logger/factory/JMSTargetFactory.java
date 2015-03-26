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
package org.apache.avalon.excalibur.logger.factory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.avalon.excalibur.logger.LogTargetFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.jms.JMSQueueTarget;
import org.apache.log.output.jms.JMSTopicTarget;
import org.apache.log.output.jms.MessageBuilder;
import org.apache.log.output.jms.ObjectMessageBuilder;
import org.apache.log.output.jms.PropertyInfo;
import org.apache.log.output.jms.PropertyType;
import org.apache.log.output.jms.TextMessageBuilder;

/**
 * Factory for JMS LogTarget-s. The configuration looks like this:
 *
 * <pre>
 *   &lt;jms id="name"&gt;
 *           &lt;connection-factory&gt;java:/TopicConectionFactory&lt;/connection-factory&gt;
 *           &lt;destination type="topic|queue"&gt;jms/LogDestination&lt;/destination&gt;
 *           &lt;message type="object|text"&gt;
 *
 * -if type="text":
 *                   &lt;property&gt;
 *                           &lt;category&gt;CATEGORY&lt;/category&gt;
 *                           &lt;priority&gt;PRIORITY&lt;/priority&gt;
 *                           &lt;time&gt;TIME&lt;/time&gt;
 *                           &lt;rtime&gt;RTIME&lt;/rtime&gt;
 *                           &lt;throwable&gt;THROWABLE&lt;/throwable&gt;
 *                           &lt;hostname&gt;HOSTNAME&lt;/hostname&gt;
 *                           &lt;static aux="234523454325"&gt;SYSTEM&lt;/static&gt;
 *                           &lt;context aux="principal"&gt;PRINCIPAL&lt;/context&gt;
 *                           &lt;context aux="ipaddress"&gt;IPADDRESS&lt;/context&gt;
 *                           &lt;context aux="username"&gt;USERNAME&lt;/context&gt;
 *                   &lt;/property&gt;
 *                   &lt;format type="exteded"&gt;%7.7{priority} %5.5{time}   [%8.8{category}] (%{context}): %{message}\n%{throwable}&lt;/format&gt;
 *           &lt;/message&gt;
 *   &lt;/jms&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/10 13:54:50 $
 */
public class JMSTargetFactory implements LogTargetFactory
{

    public LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        final String factoryName =
            configuration.getChild( "connection-factory", true ).getValue();

        final Configuration destinationConf =
            configuration.getChild( "destination", true );

        final String destinationName = destinationConf.getValue();
        final String destinationType =
            destinationConf.getAttribute( "type", "topic" );

        final Configuration messageConf =
            configuration.getChild( "message", true );

        final MessageBuilder messageBuilder = getMessageBuilder( messageConf );
        final ConnectionFactory factory;
        final Destination destination;
        final LogTarget logTarget;

        try
        {
            Context ctx = new InitialContext();
            factory = (ConnectionFactory)ctx.lookup( factoryName );
            destination = (Destination)ctx.lookup( destinationName );
        }
        catch( NameNotFoundException nnfe )
        {
            throw new ConfigurationException( "Cannot lookup object", nnfe );
        }
        catch( NamingException ne )
        {
            throw new ConfigurationException( "Cannot get naming context", ne );
        }

        if( "queue".equals( destinationType ) )
        {
            logTarget = new JMSQueueTarget( messageBuilder,
                                            (QueueConnectionFactory)factory, (Queue)destination );
        }
        else
        {
            logTarget = new JMSTopicTarget( messageBuilder,
                                            (TopicConnectionFactory)factory, (Topic)destination );
        }

        return logTarget;
    }

    private MessageBuilder getMessageBuilder( final Configuration configuration )
        throws ConfigurationException
    {
        final String messageType = configuration.getAttribute( "type", "object" );

        if( "text".equals( messageType ) )
        {
            final Configuration[] propertyConf =
                configuration.getChild( "property", true ).getChildren();
            final Configuration formatterConf = configuration.getChild( "format" );

            final PropertyInfo[] properties = new PropertyInfo[ propertyConf.length ];

            for( int i = 0; i < properties.length; i++ )
            {
                final String name = propertyConf[ i ].getValue();
                final int type = PropertyType.getTypeIdFor( propertyConf[ i ].getName() );
                final String aux = propertyConf[ i ].getAttribute( "aux", null );

                properties[ i ] = new PropertyInfo( name, type, aux );
            }

            final Formatter formatter = getFormatter( formatterConf );

            return new TextMessageBuilder( properties, formatter );
        }

        return new ObjectMessageBuilder();
    }

    protected Formatter getFormatter( final Configuration conf )
    {
        Formatter formatter = null;

        if( null != conf )
        {
            final FormatterFactory formatterFactory = new FormatterFactory();
            formatter = formatterFactory.createFormatter( conf );
        }

        return formatter;
    }
}
