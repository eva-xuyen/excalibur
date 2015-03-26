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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.apache.log.format.Formatter;
import org.apache.log.output.AbstractOutputTarget;

/**
 * A datagram output target.
 * Useful for writing using custom protocols or writing to syslog daemons.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 */
public class DatagramOutputTarget
    extends AbstractOutputTarget
{
    ///Default encoding of datagram
    private static final String DEFAULT_ENCODING = "US-ASCII";

    ///Socket on which to send datagrams
    private DatagramSocket m_socket;

    ///The encoding to use when creating byte array from string
    private String m_encoding;

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @param formatter the message formatter
     * @param encoding the encoding to use when encoding string
     * @exception IOException if an error occurs
     */
    public DatagramOutputTarget( final InetAddress address,
                                 final int port,
                                 final Formatter formatter,
                                 final String encoding )
        throws IOException
    {
        super( formatter );
        m_socket = new DatagramSocket();
        m_socket.connect( address, port );
        m_encoding = encoding;
        open();
    }

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @param formatter the message formatter
     * @exception IOException if an error occurs
     */
    public DatagramOutputTarget( final InetAddress address,
                                 final int port,
                                 final Formatter formatter )
        throws IOException
    {
        this( address, port, formatter, DEFAULT_ENCODING );
    }

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @exception IOException if an error occurs
     */
    public DatagramOutputTarget( final InetAddress address, final int port )
        throws IOException
    {
        this( address, port, null );
    }

    /**
     * Method to write output to datagram.
     *
     * @param stringData the data to be output
     */
    protected void write( final String stringData )
    {

        try
        {
            final byte[] data = stringData.getBytes( m_encoding );
            final DatagramPacket packet = new DatagramPacket( data, data.length );
            m_socket.send( packet );
        }
        catch( final IOException ioe )
        {
            getErrorHandler().error( "Error sending datagram.", ioe, null );
        }
    }

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     */
    public synchronized void close()
    {
        super.close();
        m_socket = null;
    }
}
