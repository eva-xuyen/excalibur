/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.source.factories;

import java.security.Provider;
import java.security.Security;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * {@link HTTPClientSource} Factory class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: HTTPSClientSourceFactory.java 587620 2007-10-23 19:15:09Z cziegeler $
 */
public class HTTPSClientSourceFactory extends HTTPClientSourceFactory
{
    /**
     * SSL implementation provider.
     */
    public static final String SSL_PROVIDER   = "provider";

    /**
     * SSL socket factory.
     */
    public static final String SOCKET_FACTORY = "socket-factory";

    /**
     * HTTPS constant.
     */
    public static final String HTTPS          = "https";

    /**
     * Method to set up the SSL provider for this factory
     * instance.
     *
     */
    public void setProvider( final String provider )
        throws Exception
    {
        Security.addProvider( (Provider) this.getInstance( provider ) );
    }

    /**
     * Method to set up the SSL socket factory for this
     * source factory instance.
     *
     */
    public void setSocketFactory( final String factoryName )
        throws Exception
    {
        final Protocol protocol =
            new Protocol(
                HTTPS,
                ( ProtocolSocketFactory ) this.getInstance( factoryName ),
                443
            );
        Protocol.registerProtocol( HTTPS, protocol );
    }

    /**
     * Helper method to create a single instance from a class name. Assumes
     * given class name has a no-parameter constructor.
     *
     * @param className class name to instantiate
     * @return instantiated class
     */
    private Object getInstance( final String className )
        throws Exception
    {
        return Class.forName( className ).newInstance();
    }
}
