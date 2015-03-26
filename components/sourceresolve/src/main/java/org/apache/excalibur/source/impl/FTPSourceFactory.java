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

import org.apache.excalibur.source.Source;

/**
 * A factory for an @link FTPSource.
 *
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=ftp-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class FTPSourceFactory extends URLSourceFactory
{

    public FTPSourceFactory()
    {
        super();
    }

    /**
     * Creates an FTPSource.
     */
    protected Source createURLSource( final URL url, final Map parameters )
        throws MalformedURLException, IOException
    {
        URLSource result = new FTPSource();
        result.init(url, parameters);
        return result;
    }

}
