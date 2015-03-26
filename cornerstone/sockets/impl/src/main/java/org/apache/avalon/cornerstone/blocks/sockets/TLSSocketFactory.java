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
import java.net.Socket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.avalon.cornerstone.services.sockets.SocketFactory;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Contextualizable;

/**
 * Manufactures TLS client sockets. Configuration element inside a
 * SocketManager would look like:
 * <pre>
 *  &lt;factory name="secure"
 *            class="org.apache.avalon.cornerstone.blocks.sockets.TLSSocketFactory" &gt;
 *   &lt;ssl-factory /&gt; &lt;!-- see {@link SSLFactoryBuilder} --&gt;
 *   &lt;timeout&gt; 0 &lt;/timeout&gt;
 *   &lt;!-- if the value is greater than zero, a read() call on the
 *           InputStream associated with this Socket will block for only this
 *           amount of time in milliseconds. Default value is 0. --&gt;
 *   &lt;verify-server-identity&gt;true|false&lt;/verify-server-identity&gt;
 *   &lt;!-- whether or not the server identity should be verified.
 *           Defaults to false. --&gt;
 * &lt;/factory&gt;
 * </pre>
 * <p>
 * Server identity verification currently includes only comparing the
 * certificate Common Name received with the host name in the
 * passed address. Identity verification requires that SSL
 * handshake is completed for the socket, so it takes longer
 * to get a verified socket (and won't play well with non-blocking
 * application like SEDA).
 * </p>
 * <p>
 * Another thing to keep in mind when using identity verification is
 * that <tt>InetAddress</tt> objects for the remote hosts should be
 * built using {@link java.net.InetAddress#getByName} with
 * the host name (matching the certificate CN) as the
 * argument. Failure to do so may cause relatively costly DNS lookups
 * and false rejections caused by inconsistencies between forward and
 * reverse resolution.
 * </p>
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
public class TLSSocketFactory
    extends AbstractTLSSocketFactory
    implements SocketFactory, Contextualizable, Configurable, Initializable
{
    private SSLSocketFactory m_factory;
    private boolean m_verifyServerIdentity;

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
        m_verifyServerIdentity = configuration.getChild( "verify-server-identity" ).getValueAsBoolean( false );
    }

    protected void visitBuilder( SSLFactoryBuilder builder )
    {
        m_factory = builder.buildSocketFactory();
    }

    /**
     * Performs the unconditional part of socket initialization that
     * applies to all Sockets.
     */
    private Socket initSocket( final Socket socket )
        throws IOException
    {
        socket.setSoTimeout( m_socketTimeOut );
        return socket;
    }

    /**
     * Wraps an ssl socket over an existing socket and compares the
     * host name from the address to the common name in the server
     * certificate.
     * @param bareSocket plain socket connected to the server
     * @param address destination of the <tt>bareSocket</tt>
     * @param port destination of the <tt>bareSocket</tt>
     * @return SSL socket wrapped around original socket with server
     *         identity verified
     */
    private SSLSocket sslWrap( Socket bareSocket, InetAddress address,
                               int port )
        throws IOException
    {
        final String hostName = address.getHostName();
        final SSLSocket sslSocket = (SSLSocket)
            m_factory.createSocket( bareSocket, hostName, port, true );
        sslSocket.startHandshake();
        final SSLSession session = sslSocket.getSession();
        final String DN =
            session.getPeerCertificateChain()[ 0 ].getSubjectDN().getName();
        final String CN = getCN( DN );
        if( !hostName.equals( CN ) )
        {
            final String message = "Host name mismatch, expected '" +
                hostName + "' recevied DN is " + DN;
            throw new IOException( message );
        }
        if( getLogger().isDebugEnabled() )
        {
            final String message = "DN of the server " + DN;
            getLogger().debug( message );
            final String message2 = "Session id " +
                bytesToString( session.getId() );
            getLogger().debug( message2 );
        }
        return sslSocket;
    }

    private StringBuffer bytesToString( byte[] data )
    {
        final StringBuffer result = new StringBuffer( data.length * 3 );
        String sep = "";
        for( int i = 0; i < data.length; i++ )
        {
            final byte signedValue = data[ i ];
            final int unsignedByteValue =
                ( signedValue >= 0 ) ? signedValue : 256 + signedValue;
            result.append( sep )
                .append( Integer.toHexString( unsignedByteValue ) );
            sep = ":";
        }
        return result;
    }

    /**
     * Extracts the Common Name from the given Distinguished
     * Name. Normally CN is the first part of the DN.
     * <b>If you know of a more direct way to determine the CN,
     * please let us know</b>.
     *
     * @return the common name or null if DN is malformed
     */
    private String getCN( String DN )
    {
        final int startOfCN = DN.indexOf( "CN=" );
        if( startOfCN < 0 )
        {
            return null;
        }
        final int startOfHostName = startOfCN + "CN=".length();
        final int endOfHostName = DN.indexOf( ',', startOfHostName );
        if( endOfHostName > 0 )
        {
            return DN.substring( startOfHostName, endOfHostName );
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates a socket connected to the specified remote address.
     *
     * @param address the remote address
     * @param port the remote port
     * @return the socket
     * @exception IOException if an error occurs
     */
    public Socket createSocket( InetAddress address, int port ) throws IOException
    {
        // Uses 2 different approaches to socket construction, due to
        // sslWrap dependency on wrapping createSocket which in turn
        // requires that address be resolved to the host name.
        if( m_verifyServerIdentity )
        {
            return sslWrap( initSocket( new Socket( address, port ) ),
                            address, port );
        }
        else
        {
            return initSocket( m_factory.createSocket( address, port ) );
        }
    }

    /**
     * Creates a socket and connected to the specified remote address
     * originating from specified local address.
     *
     * @param address the remote address
     * @param port the remote port
     * @param localAddress the local address
     * @param localPort the local port
     * @return the socket
     * @exception IOException if an error occurs
     */
    public Socket createSocket( final InetAddress address,
                                final int port,
                                final InetAddress localAddress,
                                final int localPort )
        throws IOException
    {
        // Uses 2 different approaches to socket construction, due to
        // sslWrap dependency on wrapping createSocket which in turn
        // requires that address be resolved to the host name.
        if( m_verifyServerIdentity )
        {
            return sslWrap( initSocket( new Socket( address, port,
                                                    localAddress,
                                                    localPort ) ),
                            address, port );
        }
        else
        {
            return initSocket( m_factory.createSocket( address, port ) );
        }
    }

}
