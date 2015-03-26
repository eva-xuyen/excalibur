/*
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.net.SMTPOutputLogTarget;

/**
 * SMTPTargetFactory class.
 *
 * <p>
 * This factory creates SMTPOutputLogTarget's. It uses the
 * context-key attribute to locate the required JavaMail Session from
 * the Context object passed to this factory.  The default context-key
 * is <code>session-context</code>.
 * </p>
 *
 * <p>
 * <pre>
 * &lt;smtp id="target-id" context-key="context-key-to-session-object"&gt;
 *   &lt;format type="raw|pattern|extended"&gt;pattern to be used if needed&lt;/format&gt;
 *   &lt;to&gt;address-1@host&lt;/to&gt;
 *   &lt;to&gt;address-N@host&lt;/to&gt;
 *   &lt;from&gt;address@host&lt;/from&gt;
 *   &lt;subject&gt;subject line&lt;/subject&gt;
 *   &lt;maximum-size&gt;number&lt;/maximum-size&gt;
 *   &lt;maximum-delay-time&gt;seconds&lt;/maximum-delay-time&gt;
 * &lt;/smtp&gt;
 * </pre>
 *
 * The Factory will look for a javax.mail.Session instance in the Context using
 *  the specified context-key.   If your needs are simple, then it is also possible
 *  to define a Session within the configuration by replacing the context-key
 *  attribute with a session child element as follows:
 * <p>
 * <pre>
 * &lt;session&gt;
 *   &lt;parameter name="mail.host" value="mail.tanukisoftware.com"/&gt;
 * &lt;/session&gt;
 * </pre>
 * The Session is created by calling Session.getInstance, providing a Properties
 *  object whose values are defined in the above block.  Any valid name value
 *  pair can be specified.
 * <p>
 *
 * <dl>
 *  <dt>&lt;format&gt;</dt>
 *  <dd>
 *   The type attribute of the pattern element denotes the type of
 *   Formatter to be used and according to it the pattern to use for.
 *   This elements defaults to:
 *   <p>
 *    %7.7{priority} %5.5{time}   [%8.8{category}] (%{context}): %{message}\\n%{throwable}
 *   </p>
 *  </dd>
 * </dl>
 * <p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.14 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.1
 */
public class SMTPTargetFactory
    extends AbstractTargetFactory
{
    /**
     * Creates an SMTPOutputLogTarget based on a Configuration
     *
     * @param config a <code>Configuration</code> instance
     * @return <code>LogTarget</code> instance
     * @exception ConfigurationException if an error occurs
     */
    public final LogTarget createTarget( final Configuration config )
        throws ConfigurationException
    {
        try
        {
            return new SMTPOutputLogTarget(
                getSession( config ),
                getToAddresses( config ),
                getFromAddress( config ),
                getSubject( config ),
                getMaxSize( config ),
                getMaxDelayTime( config ),
                getFormatter( config )
            );
        }
        catch( final ContextException ce )
        {
            throw new ConfigurationException( "Cannot find Session object in " +
                                              "application context", ce );
        }
        catch( final AddressException ae )
        {
            throw new ConfigurationException( "Cannot create address", ae );
        }
    }

    /**
     * Helper method to obtain a formatter for this factory.
     *
     * @param config a <code>Configuration</code> instance
     * @return a <code>Formatter</code> instance
     */
    protected Formatter getFormatter( final Configuration config )
    {
        final Configuration confFormat = config.getChild( "format" );

        if( null != confFormat )
        {
            final FormatterFactory formatterFactory = new FormatterFactory();
            return formatterFactory.createFormatter( confFormat );
        }

        return null;
    }

    /**
     * Helper method to create a JavaMail <code>Session</code> object.
     *
     * If your session object has simple needs, you can nest a configuration element
     * named <b>session</b> containing name-value pairs that are passed to
     * <code>Session.getInstance()</code>.
     *
     * If no configuration is found, a <code>Session</code> will be loaded from this
     * factory's context object.
     *
     * You can override this method if you need ot obtain the JavaMail session using
     * some other means.
     *
     * @return JavaMail <code>Session</code> instance
     * @exception ContextException if an error occurs
     * @exception ConfigurationException if invalid session configuration
     */
    protected Session getSession( Configuration config )
        throws ContextException, ConfigurationException
    {
        final Configuration sessionConfig = config.getChild( "session", false );

        if( null == sessionConfig )
        {
            final String contextkey =
                m_configuration.getAttribute( "context-key", "session-context" );

            if( m_context == null )
            {
                throw new ConfigurationException( "Context not available" );
            }

            return (Session)m_context.get( contextkey );
        }
        else
        {
            return Session.getInstance(
                Parameters.toProperties(
                    Parameters.fromConfiguration( sessionConfig ) ) );
        }
    }

    /**
     * Helper method to obtain the subject line to use from the given
     * configuration object.
     *
     * @param config a <code>Configuration</code> instance
     * @return subject line
     */
    private String getSubject( Configuration config )
        throws ConfigurationException
    {
        return config.getChild( "subject" ).getValue();
    }

    /**
     * Helper method to obtain the maximum size any particular SMTP
     * message can be from a given configuration object.
     *
     * @param config a <code>Configuration</code> instance
     * @return maximum SMTP mail size
     */
    private int getMaxSize( Configuration config )
        throws ConfigurationException
    {
        return config.getChild( "maximum-size" ).getValueAsInteger( 1 );
    }

    /**
     * Helper method to obtain the maximum delay time any particular SMTP
     * message can be queued from a given configuration object.
     *
     * @param config a <code>Configuration</code> instance
     * @return maximum SMTP mail delay time
     */
    private int getMaxDelayTime( Configuration config )
        throws ConfigurationException
    {
        return config.getChild( "maximum-delay-time" ).getValueAsInteger( 0 );
    }

    /**
     * Helper method to obtain the <i>to</i> address/es from the
     * given configuration.
     *
     * @param config <code>Configuration</code> instance
     * @return an array of <code>Address</code> objects
     * @exception ConfigurationException if a configuration error occurs
     * @exception AddressException if a addressing error occurs
     */
    private Address[] getToAddresses( final Configuration config )
        throws ConfigurationException, AddressException
    {
        final Configuration[] toAddresses = config.getChildren( "to" );
        final Address[] addresses = new Address[ toAddresses.length ];

        for( int i = 0; i < toAddresses.length; ++i )
        {
            addresses[ i ] = createAddress( toAddresses[ i ].getValue() );
        }

        return addresses;
    }

    /**
     * Helper method to obtain the <i>from</i> address from
     * the given configuration.
     *
     * @param config a <code>Configuration</code> instance
     * @return an <code>Address</code> object
     * @exception ConfigurationException if a configuration error occurs
     * @exception AddressException if a addressing error occurs
     */
    private Address getFromAddress( final Configuration config )
        throws ConfigurationException, AddressException
    {
        return createAddress( config.getChild( "from" ).getValue() );
    }

    /**
     * Helper factory method to create a new <code>Address</code>
     * object. Override this method in a subclass if you wish to
     * create other Address types rather than
     * <code>InternetAddress</code> (eg. <code>NewsAddress</code>)
     *
     * @param address address string from configuration
     * @return an <code>Address</code> object
     * @exception AddressException if an error occurs
     */
    protected Address createAddress( final String address )
        throws AddressException
    {
        return new InternetAddress( address );
    }
}
