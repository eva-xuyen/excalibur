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
package org.apache.excalibur.source.impl;

import java.security.Provider;
import java.security.Security;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * {@link HTTPClientSource} Factory class.
 *
 * @avalon.component
 * @avalon.service type=org.apache.excalibur.source.SourceFactory
 * @x-avalon.info name=httpsclient-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: HTTPSClientSourceFactory.java,v 1.5 2004/02/28 11:47:24 cziegeler Exp $
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
     * Parameterize this {@link org.apache.excalibur.source.SourceFactory SourceFactory}.
     *
     * @param params {@link Parameters} instance
     * @exception ParameterException if an error occurs
     */
    public void parameterize( final Parameters params )
        throws ParameterException
    {
        super.parameterize( params );

        setProvider( params );
        setSocketFactory( params );
    }

    /**
     * Method to set up the SSL provider for this factory
     * instance.
     *
     * @param params configuration {@link Parameters}
     * @exception ParameterException if an error occurs
     */
    private void setProvider( final Parameters params )
        throws ParameterException
    {
        String provider = null;

        try
        {
            provider = params.getParameter( SSL_PROVIDER );
        }
        catch ( final ParameterException e )
        {
            return; // this is ok, means no custom SSL provider
        }

        Security.addProvider( (Provider) getInstance( provider ) );
    }

    /**
     * Method to set up the SSL socket factory for this
     * source factory instance.
     *
     * @param params configuration {@link Parameters}
     * @exception ParameterException if an error occurs
     */
    private void setSocketFactory( final Parameters params )
        throws ParameterException
    {
        String factoryName = null;

        try
        {
            factoryName = params.getParameter( SOCKET_FACTORY );
        }
        catch ( final ParameterException e )
        {
            return; // this is ok, means no custom socket factory
        }

        final Protocol protocol =
            new Protocol(
                HTTPS, 
                ( SecureProtocolSocketFactory ) getInstance( factoryName ),
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
     * @exception ParameterException if an error occurs
     */
    private Object getInstance( final String className )
        throws ParameterException
    {
        try
        {
            return Class.forName( className ).newInstance();
        }
        catch ( final Exception e )
        {
            throw new ParameterException(
                "Unable to instantiate: " + className, e
            );
        }
    }
}
