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

import junit.framework.TestCase;

import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * Test case for ResourceSource.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ResourceSourceTestCase.java 587635 2007-10-23 19:54:23Z cziegeler $
 */
public class ResourceSourceTestCase extends TestCase
{

    public ResourceSourceTestCase()
    {
        this("ResourceSource");
    }

    public ResourceSourceTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testExistingSource() throws Exception
    {
        ResourceSource src = new ResourceSource("resource://org/apache/excalibur/source/factories/ResourceSourceTestCase.class");
        assertTrue("Resource doesn't exist", src.exists());
        assertTrue("Lentgh should be positive", src.getContentLength() > 0);
        assertNotNull("InputStream shouldn't be null", src.getInputStream());
    }

    public void testNonExistingSource() throws Exception
    {
        ResourceSource src = new ResourceSource("resource://org/apache/excalibur/source/factories/DoesNotExist");
        assertFalse("Resource shouldn't exist", src.exists());
        assertEquals("Should have no length", -1, src.getContentLength());
        assertEquals("Should have no lastModified", 0, src.getLastModified());

        try
        {
            src.getInputStream();
            fail("getInputStream should fail");
        } catch(SourceNotFoundException e) {
            // This is what we're waiting for
        }
    }

    public void testValidity() throws Exception
    {
        ResourceSource src1 = new ResourceSource("resource://org/apache/excalibur/source/factories/ResourceSourceTestCase.class");
        ResourceSource src2 = new ResourceSource("resource://org/apache/excalibur/source/factories/ResourceSourceTestCase.class");

        SourceValidity val1 = src1.getValidity();
        SourceValidity val2 = src2.getValidity();

        assertEquals("Validities should match", SourceValidity.VALID, val1.isValid(val2));
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

}
