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

package org.apache.avalon.cornerstone.services.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * The interface used to create client sockets.
 *
 * @author Peter Donald
 */
public interface SocketFactory
{
    /**
     * Create a socket and connect to remote address specified.
     *
     * @param address the remote address
     * @param port the remote port
     * @return the socket
     * @exception IOException if an error occurs
     */
    Socket createSocket( InetAddress address, int port )
        throws IOException;

    /**
     * Create a socket and connect to remote address specified
     * originating from specified local address.
     *
     * @param address the remote address
     * @param port the remote port
     * @param localAddress the local address
     * @param localPort the local port
     * @return the socket
     * @exception IOException if an error occurs
     */
    Socket createSocket( InetAddress address, int port,
                         InetAddress localAddress, int localPort )
        throws IOException;
}
