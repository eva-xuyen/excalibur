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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * A factory for a {@link URL} wrapper
 * 
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=url-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: URLSourceFactory.java 564 2006-03-25 08:33:32Z jbq $
 */
public class URLSourceFactory extends AbstractLogEnabled implements Parameterizable, SourceFactory, ThreadSafe
{
	Parameters parameters;

	/**
	 * Starting with JDK 1.5, accepts configuration parameters:
	 * <ul>
	 * <li><tt>connect-timeout</tt> used to set <tt>URLConnection.setConnectTimeout()</tt></li>
	 * <li><tt>read-timeout</tt> used to set <tt>URLConnection.setReadTimeout()</tt></li>
	 * </ul>
	 */
	public void parameterize(Parameters par) throws ParameterException {
		this.parameters = par;
	}

	/**
     * Create an URL-based source. This class actually creates an {@link URLSource}, but if another
     * implementation is needed, subclasses can override this method.
     */
    protected Source createURLSource(URL url, Map parameters) throws MalformedURLException, IOException
    {
        URLSource result = new URLSource();
        result.parameterize(this.parameters);
        result.init(url, parameters);
        return result;
    }

    /**
     * Create an file-based source. This class actually creates an {@link FileSource}, but if another
     * implementation is needed, subclasses can override this method.
     */
    protected Source createFileSource(String uri) throws MalformedURLException, IOException
    {
        return new FileSource(uri);
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String uri, Map parameters) throws MalformedURLException, IOException
    {
        if (getLogger().isDebugEnabled())
        {
            final String message = "Creating source object for " + uri;
            getLogger().debug(message);
        }

        // First check if it's a file
        if (uri.startsWith("file:"))
        {
            // Yes : return a file source
            return createFileSource(uri);
        }
        else
        {
            // Not a "file:" : create an URLSource
            // First try to create the URL
            URL url;
            try
            {
                url = new URL(uri);
            }
            catch (MalformedURLException mue)
            {
                // Maybe a file name containing a ':' ?
                if (getLogger().isDebugEnabled())
                {
                    this.getLogger().debug("URL " + uri + " is malformed. Assuming it's a file path.", mue);
                }
                return createFileSource(uri);
            }

            return createURLSource(url, parameters);
        }
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source)
    {
        // do nothing here
    }
}
