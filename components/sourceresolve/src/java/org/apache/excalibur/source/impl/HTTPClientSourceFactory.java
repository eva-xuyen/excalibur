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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
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
 * @version CVS $Id: HTTPClientSourceFactory.java,v 1.4 2004/02/28 11:47:24 cziegeler Exp $
 */
public class HTTPClientSourceFactory extends AbstractLogEnabled
    implements SourceFactory, Parameterizable, ThreadSafe
{
    /**
     * Configuration information.
     */
    private Parameters m_parameters;

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
            ContainerUtil.enableLogging( source, getLogger() );
            ContainerUtil.parameterize( source, m_parameters );
            ContainerUtil.initialize( source );
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
     * Parameterize this {@link SourceFactory}.
     *
     * @param params {@link Parameters} instance
     * @exception ParameterException if an error occurs
     */
    public void parameterize( final Parameters params )
        throws ParameterException
    {
        m_parameters = params;
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
