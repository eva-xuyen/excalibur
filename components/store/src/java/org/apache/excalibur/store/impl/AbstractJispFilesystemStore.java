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
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.BTreeIterator;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyNotFound;
import com.coyotegulch.jisp.KeyObject;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;

/**
 * This store is based on the Jisp library
 * (http://www.coyotegulch.com/jisp/index.html). This store uses B-Tree indexes
 * to access variable-length serialized data stored in files.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: AbstractJispFilesystemStore.java,v 1.7 2004/02/28 11:47:31 cziegeler Exp $
 */
public abstract class AbstractJispFilesystemStore
    extends AbstractReadWriteStore
    implements Store, ThreadSafe {
    
    /** The directory repository */
    protected File m_directoryFile;
    
    /** The database  */   
    protected IndexedObjectDatabase m_Database;
    
    /** And the index */
    protected BTreeIndex m_Index;
    
    /**
     * Sets the repository's location
     */
    public void setDirectory(final File directory)
        throws IOException 
    {
        this.m_directoryFile = directory;
        
        /* Does directory exist? */
        if (!this.m_directoryFile.exists()) 
        {
            /* Create it anew */
            if (!this.m_directoryFile.mkdirs()) 
            {
                throw new IOException(
                    "Error creating store directory '" + this.m_directoryFile.getAbsolutePath() + "'. ");
            }
        }
        
        /* Is given file actually a directory? */
        if (!this.m_directoryFile.isDirectory()) 
        {
            throw new IOException("'" + this.m_directoryFile.getAbsolutePath() + "' is not a directory");
        }
        
        /* Is directory readable and writable? */
        if (!(this.m_directoryFile.canRead() && this.m_directoryFile.canWrite())) 
        {
            throw new IOException(
                "Directory '" + this.m_directoryFile.getAbsolutePath() + "' is not readable/writable"
                );
        }
    }
    
    /**
     * Returns a Object from the store associated with the Key Object
     *
     * @param key the Key object
     * @return the Object associated with Key Object
     */
    protected Object doGet(Object key) 
    {
        Object value = null;
        
        try 
        {
            value = m_Database.read(this.wrapKeyObject(key), m_Index);
            if (getLogger().isDebugEnabled()) 
            {
                if (value != null) 
                {
                    getLogger().debug("Found key: " + key);
                } 
                else 
                {
                    getLogger().debug("NOT Found key: " + key);
                }
            }
        } 
        catch (Exception e) 
        {
            getLogger().error("get(..): Exception", e);
        }
        
        return value;
    }
    
    /**
     *  Store the given object in the indexed data file.
     *
     * @param key the key object
     * @param value the value object
     * @exception  IOException
     */
    protected void doStore(Object key, Object value)
        throws IOException 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("store(): Store file with key: "
                + key.toString());
            getLogger().debug("store(): Store file with value: "
                + value.toString());
        }
        
        if (value instanceof Serializable) 
        {
            try 
            {
                KeyObject[] keyArray = new KeyObject[1];
                keyArray[0] = this.wrapKeyObject(key);
                m_Database.write(keyArray, (Serializable) value);
            } 
            catch (Exception e) 
            {
                getLogger().error("store(..): Exception", e);
            }
        } 
        else 
        {
            throw new IOException("Object not Serializable");
        }
    }
    
    /**
     * Frees some values of the data file.<br>
     * TODO: implementation
     */
    public void free() 
    {
        // if we ever implement this, we should implement doFree()
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.impl.AbstractReadWriteStore#doFree()
     */
    protected void doFree() {
    }
    
    /**
     * Clear the Store of all elements
     */
    protected void doClear() 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("clear(): Clearing the database ");
        }
        
        try 
        {
            
            //empty the cache before clearing
            m_Index.emptyPageCache();
            final BTreeIterator iter = new BTreeIterator(m_Index);
            Object key;
            KeyObject[] keyArray = new KeyObject[1];
            while(iter.isValid()) {
                key = iter.getKey();
                if (key != null) {
                    //This key should not be wrapped in a JipsKey because it comes from the iterator.
                    keyArray[0] = (KeyObject) key;
                    m_Database.remove( keyArray );
                }
                if (!iter.moveNext()){
                    break;
                }
            } 
        } 
        catch (Exception ignore) 
        {
            getLogger().error("store(..): Exception", ignore);
        }
    }
    
    /**
     * Removes a value from the data file with the given key.
     *
     * @param key the key object
     */
    protected void doRemove(Object key)
    {
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("remove(..) Remove item");
        }
        
        try 
        {
            KeyObject[] keyArray = new KeyObject[1];
            keyArray[0] = wrapKeyObject(key);
            m_Database.remove(keyArray);
        } 
        catch (KeyNotFound ignore) 
        {
        } 
        catch (Exception e) 
        {
            getLogger().error("remove(..): Exception", e);
        }
    }
    
    /**
     *  Test if the the index file contains the given key
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    protected boolean doContainsKey(Object key) 
    {
        long res = -1;
        
        try 
        {
            res = m_Index.findKey(this.wrapKeyObject(key));
            if (getLogger().isDebugEnabled()) 
            {
                getLogger().debug("containsKey(..): res=" + res);
            }
        } 
        catch (KeyNotFound ignore) 
        {
        } 
        catch (Exception e)
        {
            getLogger().error("containsKey(..): Exception", e);
        }
        
        if (res > 0) 
        {
            return true;
        } 
        else 
        {
            return false;
        }
    }
    
    /**
     * Returns a Enumeration of all Keys in the indexed file.<br>
     *
     * @return  Enumeration Object with all existing keys
     */
    protected Enumeration doGetKeys() 
    {
        try 
        {
            return new BTreeObjectEnumeration(new BTreeIterator(m_Index), this);
        }
        catch (Exception ignore) 
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
    }
    
    protected int doGetSize() 
    {
        return m_Index.count();
    }
    
    /**
     * This method wraps around the key Object a Jisp KeyObject.
     *
     * @param key the key object
     * @return the wrapped key object
     */
    protected KeyObject wrapKeyObject(Object key) 
    {
        return new JispKey( key );
    }
    
    /**
     * Return the Null JispKey
     */
    protected KeyObject getNullKey() 
    {
        return new JispKey().makeNullKey();
    }
    
    class BTreeObjectEnumeration implements Enumeration
    {
        private Object m_Next;
        private BTreeIterator m_Iterator;
        private AbstractJispFilesystemStore m_Store;
        
        public BTreeObjectEnumeration(BTreeIterator iterator, AbstractJispFilesystemStore store) 
        {
            m_Iterator = iterator;
            m_Store = store;
            
            // Obtain first element. If any.
            try
            {
                m_Next = m_Iterator.getKey();
            }
            catch (IOException ioe)
            {
                m_Store.getLogger().error("store(..): Exception", ioe);
                m_Next = null;
            }
        }
        
        public boolean hasMoreElements() 
        {
            return (m_Next != null);
        }
        
        public Object nextElement() throws NoSuchElementException
        {
            if (m_Next == null)
            {
                throw new NoSuchElementException();
            }
            
            // Save current element
            Object tmp = m_Next;
            
            // Advance to the next element
            try
            {
                if (m_Iterator.moveNext())
                {
                    m_Next = m_Iterator.getKey();
                }
                else
                {
                    // Can't move to the next element - no more elements.
                    m_Next = null;
                }
            }
            catch (IOException ioe) 
            {
                m_Store.getLogger().error("store(..): Exception", ioe);
                m_Next = null;
            }
            catch (ClassNotFoundException cnfe) 
            {
                m_Store.getLogger().error("store(..): Exception", cnfe);
                m_Next = null;
            }
            
            // Return the real key
            return ((JispKey) tmp).getKey();
        }
    }
    
}
