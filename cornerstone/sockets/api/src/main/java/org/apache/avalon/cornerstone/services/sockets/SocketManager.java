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

/**
 * Service to manager the socket factories.
 *
 * @author Peter Donald
 */
public interface SocketManager
{
    String ROLE = SocketManager.class.getName();

    /**
     * Retrieve a server socket factory by name.
     *
     * @param name the name of server socket factory
     * @return the ServerSocketFactory
     * @exception Exception if server socket factory is not available
     */
    ServerSocketFactory getServerSocketFactory( String name )
        throws Exception;

    /**
     * Retrieve a client socket factory by name.
     *
     * @param name the name of client socket factory
     * @return the SocketFactory
     * @exception Exception if socket factory is not available
     */
    SocketFactory getSocketFactory( String name )
        throws Exception;
}
