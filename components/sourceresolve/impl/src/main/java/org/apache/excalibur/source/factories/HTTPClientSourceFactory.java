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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.impl.AbstractLoggable;

/**
 * {@link HTTPClientSource} Factory class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: HTTPClientSourceFactory.java 587620 2007-10-23 19:15:09Z cziegeler $
 */
public class HTTPClientSourceFactory extends AbstractLoggable
    implements SourceFactory
{
    /**
     * Proxy port if set via configuration.
     */
    private int m_proxyPort = -1;

    /**
     * Proxy host if set via configuration.
     */
    private String m_proxyHost;

    public void setProxyHost(String proxyHost)
    {
        this.m_proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort)
    {
        this.m_proxyPort = proxyPort;
    }


    /**
     * Creates a {@link HTTPClientSource} instance.
     */
    public Source getSource( final String uri, final Map sourceParams )
        throws MalformedURLException, IOException
    {
        try
        {
            final HTTPClientSource source =
                new HTTPClientSource( uri, sourceParams, null );
            source.setProxyHost(this.m_proxyHost);
            source.setProxyPort(this.m_proxyPort);
            source.initialize();
            return source;
        }
        catch ( final MalformedURLException e )
        {
            throw e;
        }
        catch ( final IOException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            final StringBuffer message = new StringBuffer();
            message.append( "Exception thrown while creating " );
            message.append( HTTPClientSource.class.getName() );

            throw new SourceException( message.toString(), e );
        }
    }

    /**
     * Releases the given {@link Source} object.
     *
     * @param source {@link Source} object to be released
     */
    public void release( final Source source )
    {
        // empty for the moment
    }
}
