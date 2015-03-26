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
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.SourceFactory;

/**
 * {@link HTTPClientSource} Factory class.
 *
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=httpclient-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: HTTPClientSourceFactory.java 587633 2007-10-23 19:43:27Z cziegeler $
 */
public class HTTPClientSourceFactory extends org.apache.excalibur.source.factories.HTTPClientSourceFactory
    implements Parameterizable, ThreadSafe
{
    /**
     * Constant used for configuring the proxy hostname.
     */
    public static final String PROXY_HOST     = "proxy.host";

    /**
     * Constant used for configuring the proxy port number.
     */
    public static final String PROXY_PORT     = "proxy.port";

    /**
     * Parameterize this {@link SourceFactory}.
     *
     * @param params {@link Parameters} instance
     * @exception ParameterException if an error occurs
     */
    public void parameterize( final Parameters params )
        throws ParameterException
    {
        this.setProxyHost(params.getParameter( PROXY_HOST, null ));
        this.setProxyPort(params.getParameterAsInteger( PROXY_PORT, -1 ));
    }
}
