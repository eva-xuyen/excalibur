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
package org.apache.excalibur.source.jnetbridge;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.excalibur.source.factories.FileSourceFactory;
import org.apache.excalibur.source.jnet.Installer;
import org.apache.excalibur.source.jnetbridge.SourceFactoriesManager;
import org.apache.excalibur.source.jnetbridge.SourceURLStreamHandlerFactory;

public class Test extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        Installer.setURLStreamHandlerFactory(new SourceURLStreamHandlerFactory());
    }

    public void testFileSourceFactory() throws Exception {
        final Map factories = new HashMap();
        factories.put("custom", new FileSourceFactory());
        SourceFactoriesManager.setGlobalFactories(factories);
        final URL url = new URL("custom:pom.xml");
        final InputStream is = (InputStream)url.getContent();
        final byte[] b = new byte[100000];
        int l = is.read(b);
        is.close();
    }

}
