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
package org.apache.excalibur.source.impl;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.factories.HTTPClientSource;

/**
 * {@link HTTPClientSource} Factory class.
 *
 * @avalon.component
 * @avalon.service type=org.apache.excalibur.source.SourceFactory
 * @x-avalon.info name=httpsclient-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: HTTPSClientSourceFactory.java 587637 2007-10-23 20:05:10Z cziegeler $
 */
public class HTTPSClientSourceFactory extends org.apache.excalibur.source.factories.HTTPSClientSourceFactory
{
    /**
     * Parameterize this {@link org.apache.excalibur.source.SourceFactory SourceFactory}.
     *
     * @param params {@link Parameters} instance
     * @exception ParameterException if an error occurs
     */
    public void parameterize( final Parameters params )
        throws ParameterException
    {
        this.setProxyHost(params.getParameter( HTTPClientSourceFactory.PROXY_HOST, null ));
        this.setProxyPort(params.getParameterAsInteger( HTTPClientSourceFactory.PROXY_PORT, -1 ));
        try {
            if ( params.getParameter(SSL_PROVIDER, null ) != null ) {
                this.setProvider( params.getParameter(SSL_PROVIDER) );
            }
            if ( params.getParameter(SOCKET_FACTORY, null ) != null ) {
                this.setSocketFactory( params.getParameter(SOCKET_FACTORY) );
            }
        } catch (ParameterException pe) {
            throw pe;
        } catch (Exception e) {
            throw new ParameterException("Exception during configuration.", e);
        }
    }
}
