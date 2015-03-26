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
package org.apache.excalibur.source.test;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.ConcurrentModificationException;

import junit.framework.TestCase;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.FileSource;

/**
 * Test case for FileSource.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FileSourceTestCase.java,v 1.4 2004/02/28 11:47:22 cziegeler Exp $
 */
public class FileSourceTestCase extends TestCase
{

    private File m_tempDir;

    public FileSourceTestCase()
    {
        this("FileSource");
    }

    public FileSourceTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        // Create a temp file
        m_tempDir = File.createTempFile("filesource", "test");
        // and make it a directory
        m_tempDir.delete();
        m_tempDir.mkdir();
    }

    public void testDirExistence() throws Exception
    {
        m_tempDir.mkdirs();
        long time = m_tempDir.lastModified();
        FileSource src = new FileSource("file", m_tempDir);
        assertTrue("Temp dir doesn't exist", src.exists());
        assertTrue("Temp dir is not traversable", src.isCollection());
        // Check it was created less than 1 secs ago
        assertEquals("Wrong creation date", time, src.getLastModified());

        assertTrue("Temp dir is not empty", src.getChildren().isEmpty());
    }

    public void testChildCreation() throws Exception
    {
        final String text = "Writing to a source";

        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());

        // Should not have a validity, since it doesn't exist
        assertNull("New file has a validity", child.getValidity());

        // Test the name
        assertEquals("Wrong name", "child.txt", child.getName());

        // Feed with some content
        fillSource(child, text);

        // And test it
        assertEquals(
            "Wrong length",
            text.length() + System.getProperty("line.separator").length(),
            child.getContentLength());
        assertEquals("Wrong content-type", "text/plain", child.getMimeType());
        assertTrue("New file is traversable", !child.isCollection());

        // Check that parent now has children
        Collection children = src.getChildren();
        assertEquals("Wrong number of children", 1, children.size());

        // And also that crawling up the hierarchy is OK
        Source parent = child.getParent();
        assertEquals("Wrong parent URI", src.getURI(), parent.getURI());

    }

    public void testMove() throws Exception
    {
        final String text = "Original text";

        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());

        fillSource(child, text);
        assertTrue("New file doesn't exist", child.exists());
        long length = child.getContentLength();

        FileSource child2 = (FileSource) src.getChild("child2.txt");
        assertTrue("Second file already exist", !child2.exists());

        SourceUtil.move(child, child2);
        assertTrue("First file still exists", !child.exists());
        assertTrue("Second file doesn't exist", child2.exists());
        assertEquals("Wrong length of second file", length, child2.getContentLength());
    }

    public void testCopy() throws Exception
    {
        final String text = "Original text";

        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());

        fillSource(child, text);
        assertTrue("New file doesn't exist", child.exists());
        long length = child.getContentLength();

        FileSource child2 = (FileSource) src.getChild("child2.txt");
        assertTrue("Second file already exist", !child2.exists());

        SourceUtil.copy(child, child2);

        assertTrue("First file doesn't exist", child.exists());
        assertTrue("Second file doesn't exist", child2.exists());
        assertEquals("Wrong length of second file", length, child2.getContentLength());

    }

    public void testDelete() throws Exception
    {
        final String text = "Original text";

        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());
        fillSource(child, text);
        assertTrue("New file doesn't exist", child.exists());

        child.delete();
        assertTrue("File still exists", !child.exists());
    }

    public void testConcurrentAccess() throws Exception
    {
        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());

        child.getOutputStream();

        try
        {
            // Get it a second time
            child.getOutputStream();
        }
        catch (ConcurrentModificationException cme)
        {
            return; // This is what is expected
        }
        fail("Undedected concurrent modification");

    }

    public void testAtomicUpdate() throws Exception
    {
        final String text = "Blah, blah";
        FileSource src = new FileSource("file", m_tempDir);

        FileSource child = (FileSource) src.getChild("child.txt");
        assertTrue("New file already exists", !child.exists());
        fillSource(child, text + " and blah!");

        long length = child.getContentLength();

        SourceValidity validity = child.getValidity();
        assertEquals("Validity is not valid", 1, validity.isValid());

        // Wait 2 seconds before updating the file
        Thread.sleep(2 * 1000L);

        // Now change its content
        PrintWriter pw = new PrintWriter(child.getOutputStream());
        pw.write(text);

        assertEquals("File length modified", length, child.getContentLength());

        pw.close();

        assertTrue("File length not modified", length != child.getContentLength());

        assertEquals("Validity is valid", -1, validity.isValid());
    }

    protected void tearDown() throws Exception
    {
        deleteAll(m_tempDir);
    }

    // Recursively delete a file or directory
    private void deleteAll(File f)
    {
        if (f.isDirectory())
        {
            File[] children = f.listFiles();
            for (int i = 0; i < children.length; i++)
            {
                deleteAll(children[i]);
            }
        }

        f.delete();
    }

    private void fillSource(ModifiableSource src, String text) throws Exception
    {
        OutputStream os = src.getOutputStream();
        PrintWriter pw = new PrintWriter(os);

        pw.println(text);
        pw.close();
    }

}
