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
 * A factory for the Resource protocol
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ResourceSourceFactory.java 587637 2007-10-23 20:05:10Z cziegeler $
 */
public class ResourceSourceFactory
    extends AbstractLoggable
    implements SourceFactory
{
    /**
     * Get a {@link Source} object.
     * The factory creates a new {@link Source} object that can be used
     * by the application. However, when this source object is not needed
     * anymore it has to be released again using the {@link #release(Source)}
     * method.
     *
     * @param location   The URI to resolve - this URI includes the protocol.
     * @param parameters This is optional.
     */
    public Source getSource( String location, Map parameters )
        throws MalformedURLException, IOException, SourceException
    {
        if( this.getLogger().isDebugEnabled() )
        {
            final String message = "Creating source object for " + location;
            this.getLogger().debug( message );
        }
        return new ResourceSource( location );
    }

    /**
     * Release a {@link Source} object.
     */
    public void release( Source source )
    {
        if( null != source && this.getLogger().isDebugEnabled() )
        {
            final String message = "Releasing source object for " + source.getURI();
            this.getLogger().debug( message );
        }
        // do nothing here
    }

}
