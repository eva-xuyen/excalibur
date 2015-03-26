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
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;

/**
 * A factory for the Resource protocol
 * 
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=resource-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ResourceSourceFactory.java,v 1.4 2004/02/28 11:47:24 cziegeler Exp $
 */
public class ResourceSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe
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
        if( getLogger().isDebugEnabled() )
        {
            final String message = "Creating source object for " + location;
            getLogger().debug( message );
        }
        return new ResourceSource( location );
    }
    
    /**
     * Release a {@link Source} object.
     */
    public void release( Source source ) 
    {
        if( null != source && getLogger().isDebugEnabled() )
        {
            final String message = "Releasing source object for " + source.getURI();
            getLogger().debug( message );
        }
        // do nothing here
    }
    
}
