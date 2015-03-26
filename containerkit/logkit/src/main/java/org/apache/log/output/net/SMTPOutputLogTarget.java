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
package org.apache.log.output.net;

import java.util.Date;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import org.apache.log.format.Formatter;
import org.apache.log.output.AbstractOutputTarget;

/** Logkit output target that logs data via SMTP.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version $Id: SMTPOutputLogTarget.java 506267 2007-02-12 04:06:06Z crossley $
 * @since 1.1.0
 */
public class SMTPOutputLogTarget extends AbstractOutputTarget
{
    /** Mail session. */
    private final Session m_session;

    /** Message to be sent. */
    private Message m_message;

    /** Address to sent mail to. */
    private final Address[] m_toAddresses;

    /** Address to mail is to be listed as sent from. */
    private final Address m_fromAddress;

    /** Mail subject. */
    private final String m_subject;

    /** Current size of mail, in units of log events. */
    private int m_msgSize;

    /** Maximum size of mail, in units of log events. */
    private final int m_maxMsgSize;

    /** Buffer containing current mail. */
    private StringBuffer m_buffer;
    
    /** The time that the first log entry in the current buffer arrived. */
    private long m_bufferTime;
    
    /** The maximun delay that a message will be allowed to wait in the queue
     *   before being sent. */
    private long m_maxDelayTime;
    
    /** Runner thread which is responsible for sending batched log entries in
     * the background. */
    private Thread m_runner;
    
    /** Flag which will be set in the close method when it is time to shutdown. */
    private boolean m_shutdown;
    
    /** Flag which controls whether debug is enabled for each message. */
    private boolean m_debug;

    /** SMTPOutputLogTarget constructor.
     *
     * It creates a logkit output target capable of logging to SMTP 
     * (ie. email, email gateway) targets.
     *
     * @param session mail session to be used
     * @param toAddresses addresses logs should be sent to
     * @param fromAddress address logs say they come from
     * @param subject subject line logs should use
     * @param maxMsgSize maximum size of any log mail, in units of log events.
     *                   If this is greater than one but maxDelayTime is 0 then
     *                   log events may stay queued for a long period of time
     *                   if less than the specified number of messages are
     *                   logged.  Any unset messages will be sent when the
     *                   target is closed.
     * @param maxDelayTime specifies the longest delay in seconds that a log
     *                     entry will be queued before being sent.  Setting
     *                     this to a value larger than 0 will cause a
     *                     background thread to be used to queue up and send
     *                     messages.  Ignored if maxMsgSize is 1 or less.
     * @param formatter log formatter to use
     */
    public SMTPOutputLogTarget(
        final Session session,
        final Address[] toAddresses,
        final Address fromAddress,
        final String subject,
        final int maxMsgSize,
        final int maxDelayTime,
        final Formatter formatter )
    {
        super( formatter );

        // setup log target
        m_maxMsgSize = maxMsgSize;
        m_toAddresses = toAddresses;
        m_fromAddress = fromAddress;
        m_subject = subject;
        m_session = session;
        m_maxDelayTime = maxDelayTime * 1000;
        
        if ( ( m_maxDelayTime > 0 ) && ( m_maxMsgSize > 1 ) ) 
        {
            // Create a runner thread which will
            m_runner = new Thread( "SMTPOutputLogTarget_Queue_Daemon" )
            {
                public void run()
                {
                    try
                    {
                        //System.out.println( "SMTPOutputLogTarget runner Started." );
                        synchronized( SMTPOutputLogTarget.this )
                        {
                            try
                            {
                                do
                                {
                                    // Wait up to a second for notification that a message is
                                    //  available.
                                    try
                                    {
                                        SMTPOutputLogTarget.this.wait( 1000 );
                                    }
                                    catch ( InterruptedException e )
                                    {
                                        // Ignore.
                                    }
                                    
                                    // Is a message waiting?
                                    if ( m_message != null )
                                    {
                                        // Is the message big enough to send or been
                                        //  waiting long enough?
                                        long now = System.currentTimeMillis();
                                        if ( m_shutdown || ( m_msgSize >= m_maxMsgSize ) ||
                                            ( now - m_bufferTime >= m_maxDelayTime ) )
                                        {
                                            // Time to send the message.
                                            send();
                                        }
                                    }
                                }
                                while ( !m_shutdown );
                            }
                            finally
                            {
                                //System.out.println( "SMTPOutputLogTarget runner Completed." );
                                m_runner = null;
                                
                                // The close method waits for this to complete.
                                SMTPOutputLogTarget.this.notifyAll();
                            }
                        }
                    }
                    catch ( Throwable t )
                    {
                        getErrorHandler().error(
                            "Unexpected error in the SMTPOutputLogTarget queue daemon", t, null );
                    }
                }
            };
            m_runner.setDaemon( true );
            m_runner.start();
        }

        // ready for business
        open();
    }

    /** SMTPOutputLogTarget constructor.
     *
     * It creates a logkit output target capable of logging to SMTP 
     * (ie. email, email gateway) targets.
     *
     * @param session mail session to be used
     * @param toAddresses addresses logs should be sent to
     * @param fromAddress address logs say they come from
     * @param subject subject line logs should use
     * @param maxMsgSize maximum size of any log mail, in units of log events.
     *                   Log events may stay queued for a long period of time
     *                   if less than the specified number of messages are
     *                   logged.  Any unset messages will be sent when the
     *                   target is closed.
     * @param formatter log formatter to use
     */
    public SMTPOutputLogTarget(
        final Session session,
        final Address[] toAddresses,
        final Address fromAddress,
        final String subject,
        final int maxMsgSize,
        final Formatter formatter )
    {
        this( session, toAddresses, fromAddress, subject, maxMsgSize, 0, formatter );
    }

    /** Method to write data to the log target. 
     * 
     * Logging data is stored in
     * an internal buffer until the size limit is reached. When this happens
     * the data is sent to the SMTP target, and the buffer is reset for
     * subsequent events.
     *
     * @param data logging data to be written to target
     */
    protected synchronized void write( final String data )
    {
        // If this is the first log entry then start a new Message.
        if ( m_message == null )
        {
            try
            {
                m_session.setDebug( m_debug );  // This is global, so always set it.
                
                m_message = new MimeMessage( m_session );
                m_message.setFrom( m_fromAddress );
                m_message.setRecipients( Message.RecipientType.TO, m_toAddresses );
                m_message.setSubject( m_subject );
                m_message.setSentDate( new Date() );
            }
            catch( MessagingException e )
            {
                getErrorHandler().error( "Error creating message", e, null );
                m_message = null;
                return;
            }
            
            m_buffer = new StringBuffer();
            m_bufferTime = System.currentTimeMillis();
            m_msgSize = 0;
        }
        
        // Add the new log entry to the buffer.
        m_buffer.append( data );
        if ( !data.endsWith( "\n" ) )
        {
            m_buffer.append( "\n" );
        }
        m_msgSize++;
        
        // Decide what to do with the message.
        if ( m_runner == null )
        {
            // Messages are sent in line.

            // Send mail if message size has reached it's size limit
            if ( m_msgSize >= m_maxMsgSize )
            {
                send();
            }
        }
        else
        {
            // Messages are sent by the runner thread.
            notifyAll();
        }
    }

    /** Closes this log target. 
     *
     * Sends currently buffered message, if existing.
     */
    public synchronized void close()
    {
        //System.out.println( "SMTPOutputLogTarget close Started." );
        
        super.close();
        
        if ( m_runner == null )
        {
            // Log Events are being handled in line.
            send();
        }
        else
        {
            // Log Events are being handled by a background thread.  Signal it
            //  and then wait for it to complete.
            m_shutdown = true;
            notifyAll();
            
            while ( m_runner != null )
            {
                try
                {
                    wait();
                }
                catch ( InterruptedException e )
                {
                    // Ignore.
                }
            }
        }
        
        //System.out.println( "SMTPOutputLogTarget close Completed." );
    }

    /**
     * Method to enable/disable debugging on the mail session.
     *
     * @param debug true to enable debugging, false to disable it
     */
    public void setDebug( boolean debug )
    {
        m_debug = debug;
    }

    /**
     * Helper method to send the currently buffered message,
     * if existing.
     * <p>
     * Only called when synchronized.
     */
    private void send()
    {
        try
        {
            if( m_message != null && m_buffer != null )
            {
                m_message.setText( m_buffer.toString() );
                Transport.send( m_message );
                m_message = null;
            }
        }
        catch( MessagingException e )
        {
            getErrorHandler().error( "Error sending message", e, null );
            
            // Always clear the message in the event there is an error as the
            //  problem will most likely repeat.
            m_message = null;
        }
    }
}

