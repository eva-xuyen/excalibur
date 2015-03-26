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
package org.apache.excalibur.source.test;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collections;

import org.apache.excalibur.source.impl.URLSource;

/**
 * Test case for URLSource.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: URLSourceTestCase.java,v 1.0 2005/04/16 11:47:22 shash Exp $
 */
public class URLSourceTestCase extends TestCase
{

    URLSource m_urlSource;
    File tempFile;

    protected void setUp() throws Exception
    {
        super.setUp();
        m_urlSource = new URLSource();

        tempFile = File.createTempFile( "filesource", "test-exists" );
	FileOutputStream out = new FileOutputStream(tempFile);
	out.write(1);
	out.close();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        tempFile.delete();
    }

    public void testFileExists() throws Exception
    {
        m_urlSource.init( tempFile.toURL(), Collections.EMPTY_MAP );
        assertTrue( m_urlSource.exists() );
    }

    public void testFileDoesNotExist() throws Exception
    {
        File tempFile = new File("phantom-file");

        m_urlSource.init( tempFile.toURL(), Collections.EMPTY_MAP );
        assertFalse( m_urlSource.exists() );
    }

    public void testHttpHostDoesNotExist() throws Exception
    {
        m_urlSource.init( new URL( "http://some.invalid.host/no_such_file" ),
                Collections.EMPTY_MAP );
        assertFalse( m_urlSource.exists() );
    }

    public void testHttpDoesExist() throws Exception
    {
        m_urlSource.init( new URL( "http://excalibur.apache.org/" ),
                Collections.EMPTY_MAP );
        assertTrue( m_urlSource.exists() );
    }
}
