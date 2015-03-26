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

package org.apache.avalon.cornerstone.blocks.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.avalon.cornerstone.services.sockets.ServerSocketFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Manufactures TLS server sockets. Configuration element inside a
 * SocketManager would look like:
 * <pre>
 *  &lt;factory name="secure"
 *            class="org.apache.avalon.cornerstone.blocks.sockets.TLSServerSocketFactory" &gt;
 *   &lt;ssl-factory /&gt; &lt;!-- see {@link SSLFactoryBuilder} --&gt;
 *   &lt;timeout&gt; 0 &lt;/timeout&gt;
 *   &lt;!-- With this option set to a non-zero timeout, a call to
 *     accept() for this ServerSocket will block for only this amount of
 *     time. If the timeout expires, a java.io.InterruptedIOException is
 *     raised, though the ServerSocket is still valid. Default value is 0. --&gt;
 *   &lt;authenticate-client&gt;false&lt;/authenticate-client&gt;
 *   &lt;!-- Whether or not the client must present a certificate to
 *      confirm its identity. Defaults to false. --&gt;
 * &lt;/factory&gt;
 * </pre>
 *
 * @author Peter Donald
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 * @author <a href="mailto:charles@benett1.demon.co.uk">Charles Benett</a>
 * @author <a href="mailto:">Harish Prabandham</a>
 * @author <a href="mailto:">Costin Manolache</a>
 * @author <a href="mailto:">Craig McClanahan</a>
 * @author <a href="mailto:myfam@surfeu.fi">Andrei Ivanov</a>
 * @author <a href="mailto:greg-avalon-apps at nest.cx">Greg Steuck</a>
 */
public class TLSServerSocketFactory
    extends AbstractTLSSocketFactory
    implements ServerSocketFactory
{
    private SSLServerSocketFactory m_factory;
    protected boolean m_keyStoreAuthenticateClients;

    /**
     * Configures the factory.
     *
     * @param configuration the Configuration
     * @exception ConfigurationException if an error occurs
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        super.configure( configuration );
        m_keyStoreAuthenticateClients =
            configuration.getChild( "authenticate-client" ).getValueAsBoolean( false );
    }

    protected void visitBuilder( SSLFactoryBuilder builder )
    {
        m_factory = builder.buildServerSocketFactory();
    }

    /**
     * Creates a socket on specified port.
     *
     * @param port the port
     * @return the created ServerSocket
     * @exception IOException if an error occurs
     */
    public ServerSocket createServerSocket( final int port )
        throws IOException
    {
        final ServerSocket serverSocket = m_factory.createServerSocket( port );
        initServerSocket( serverSocket );
        return serverSocket;
    }

    /**
     * Creates a socket on specified port with a specified backLog.
     *
     * @param port the port
     * @param backLog the backLog
     * @return the created ServerSocket
     * @exception IOException if an error occurs
     */
    public ServerSocket createServerSocket( int port, int backLog )
        throws IOException
    {
        final ServerSocket serverSocket = m_factory.createServerSocket( port, backLog );
        initServerSocket( serverSocket );
        return serverSocket;
    }

    /**
     * Creates a socket on a particular network interface on specified port
     * with a specified backLog.
     *
     * @param port the port
     * @param backLog the backLog
     * @param bindAddress the network interface to bind to.
     * @return the created ServerSocket
     * @exception IOException if an error occurs
     */
    public ServerSocket createServerSocket( int port, int backLog, InetAddress bindAddress )
        throws IOException
    {
        final ServerSocket serverSocket =
            m_factory.createServerSocket( port, backLog, bindAddress );
        initServerSocket( serverSocket );
        return serverSocket;
    }

    protected void initServerSocket( final ServerSocket serverSocket )
        throws IOException
    {
        final SSLServerSocket socket = (SSLServerSocket)serverSocket;

        // Set client authentication if necessary
        socket.setNeedClientAuth( m_keyStoreAuthenticateClients );
        // Sets socket timeout
        socket.setSoTimeout( m_socketTimeOut );
    }
}

