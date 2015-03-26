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
package org.apache.excalibur.store.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * This TestCase fills a Jisp store with <code>MAX_ENTRIES</code>
 * and then tests the remove() and clear() methods.
 *  
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ClearJispFilesystemStoreTestCase extends TestCase {
    
    /** permanent Jisp store */
    private JispFilesystemStore m_store;
    
    /** logger for this test */
    private final Logger m_logger =
        new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
    
    /** max entries to fill the store */
    private int MAX_ENTRIES = 100;
    
    /** temp dir for this test */
    private File m_tempDir;
    
    /**
     * Set up the the jisp database before running each test
     */
    public void setUp() throws Exception {
        m_logger.debug("-BGN--> setUp() fixture for " + getName());
        
        m_tempDir = File.createTempFile("jisp", "test");
        m_tempDir.delete();
        m_tempDir.mkdir();
        
        m_store = new JispFilesystemStore();
        
        //enable logging
        m_store.enableLogging(new NullLogger());
        
        //parameters
        final Parameters params = new Parameters();
        params.setParameter("directory", m_tempDir.toString());
        params.makeReadOnly();
        
        //parameterize it
        m_store.parameterize(params);
        
        //fill the store
        
        fillStore();
        
        m_logger.debug("-END--> setUp() fixture for " + getName());
        
    }
    
    /**
     * Fills the store
     * @throws IOException
     */
    private void fillStore() throws IOException {
        String key = null;
        String value = null;
        m_logger.debug("filling the database...");
        for (int i = 0; i < MAX_ENTRIES; i++) {
            key = "key" + i;
            value = "value" + i;
            m_store.store(key, value);
        }
        m_logger.debug("filling the database...OK");
    }
    
    /**
     * Test remove() on <code>JispFilesystemStore</code>
     * This test succeeds if the a removed item points to null.
     */
    public void testRemove() {
        m_logger.debug("-BGN--> testRemove()");
        //get a key to remove
        final String key = "key" + (MAX_ENTRIES - 10);
        m_logger.debug("removing key: " + key);
        m_store.remove(key);        
        // check that the item for the removed key is null
        Object item = m_store.get(key);
        if (item != null) {
            assertTrue("The store item for key=" + key + " has value of " + item.toString(), item == null);
        } else {
            assertTrue(item == null);
        } 
        m_logger.debug("-END--> testRemove()");
        
    }
    
    /**
     * Test clear() on <code>JispFilesystemStore</code>
     * This test succeeds if the store size after cleaning is 0.
     * @throws Exception
     */
    public void testClear() throws Exception {
        m_logger.debug("-BGN--> testClear()");        
        final int sizeBefore = m_store.size();
        m_logger.debug("store size before clear:" + sizeBefore);
        m_logger.debug("index count before clear:" + m_store.m_Index.count());
        m_store.clear();
        final int sizeAfter = m_store.size();
        m_logger.debug("store size after clear:" + sizeAfter);
        m_logger.debug("index count after clear:" + m_store.m_Index.count());
        assertTrue(sizeAfter == 0);
        m_logger.debug("-END--> testClear()");
    }
    
    /**
     * Clean the resources after running each test
     */
    protected void tearDown() throws Exception {
        m_logger.debug("-BGN--> tearUp() fixture for test " + getName());
        m_logger.debug("deleting index and database");
        deleteAll(m_tempDir);
        m_logger.debug("-END--> tearUp() fixture for test " + getName());
    }
    
    /**
     * Deletes files in directory recursively
     * @param f
     */
    private void deleteAll(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            for (int i = 0; i < children.length; i++) {
                deleteAll(children[i]);
            }
        }
        
        f.delete();
    }
}


