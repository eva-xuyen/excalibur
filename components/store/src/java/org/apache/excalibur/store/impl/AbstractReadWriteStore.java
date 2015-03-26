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

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;
import org.apache.excalibur.store.Store;

/**
 * This is a base implementation for stores that are synchronized by
 * using a read/write lock.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: AbstractReadWriteStore.java,v 1.6 2004/03/11 21:38:47 unico Exp $
 */
public abstract class AbstractReadWriteStore
extends AbstractLogEnabled
implements Store, ThreadSafe {

    private ValueInstrument m_sizeInstrument = new ValueInstrument("size");
    private CounterInstrument m_hitsInstrument = new CounterInstrument("hits");
    private CounterInstrument m_missesInstrument = new CounterInstrument("misses");

    private String m_instrumentableName;

    /** The lock */
    protected ReadWriteLock lock = new FIFOReadWriteLock();
    
    /**
     * Returns a Object from the store associated with the Key Object
     *
     * @param key the Key object
     * @return the Object associated with Key Object
     */
    public Object get(Object key) 
    {
        Object value = null;
        Sync sync = this.lock.readLock();
        try
        {
            sync.acquire();
            try 
            {
                value = this.doGet(key);
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        } 
        
        if ( null == value )
        {
            m_missesInstrument.increment();
        }
        else
        {
            m_hitsInstrument.increment();
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
    public void store(Object key, Object value)
    throws IOException 
    {
        Sync sync = this.lock.writeLock();
        try
        {
            sync.acquire();

            try 
            {
                this.doStore(key, value);
                m_sizeInstrument.setValue( doGetSize() );
            } 
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        }
        
    }

    /**
     * Frees some values of the data file.<br>
     */
    public void free() 
    {
        Sync sync = this.lock.writeLock();
        try
        {
            sync.acquire();

            try 
            {
                this.doFree();
                m_sizeInstrument.setValue( doGetSize() );
            } 
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        } 
    }

    /**
     * Clear the Store of all elements
     */
    public void clear() 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("clear(): Clearing the database ");
        }

        Sync sync = this.lock.writeLock();
        try
        {
            sync.acquire();
            try 
            {
                this.doClear();
                m_sizeInstrument.setValue( 0 );
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        }
    }

    /**
     * Removes a value from the data file with the given key.
     *
     * @param key the key object
     */
    public void remove(Object key)
    {
        Sync sync = this.lock.writeLock();
        try
        {
            sync.acquire();
            try 
            {
                this.doRemove(key);
                m_sizeInstrument.setValue( doGetSize() );
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        }
    }

    /**
     *  Test if the the index file contains the given key
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    public boolean containsKey(Object key) 
    {
        Sync sync = this.lock.readLock();
        try
        {
            sync.acquire();
            try 
            {
                return this.doContainsKey(key);
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
            return false;
        } 
    }

    /**
     * Returns a Enumeration of all Keys in the indexed file.<br>
     *
     * @return  Enumeration Object with all existing keys
     */
    public Enumeration keys() 
    {
        Sync sync = this.lock.readLock();
        try
        {
            sync.acquire();
            try 
            {
                return this.doGetKeys();
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        } 
    }

    public int size() 
    {
        Sync sync = this.lock.readLock();
        try
        {
            sync.acquire();
            try 
            {
                return this.doGetSize();
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
            return 0;
        } 
    }

    public void setInstrumentableName(String name)
    {
        m_instrumentableName = name;    
    }

    public String getInstrumentableName()
    {
        return m_instrumentableName;
    }

    public Instrument[] getInstruments()
    {
        return new Instrument[] { m_sizeInstrument, m_hitsInstrument, m_missesInstrument };
    }

    public Instrumentable[] getChildInstrumentables() {
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }

    /**
     * Get the object associated to the given unique key.
     */
    protected abstract Object doGet( Object key );

    /**
     * Store the given object. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    protected abstract void doStore( Object key, Object value ) throws IOException;

    /**
     * Try to free some used memory. The transient store can simply remove
     * some hold data, the persistent store can free all memory by
     * writing the data to a persistent store etc.
     */
    protected abstract void doFree();

    /**
     * Remove the object associated to the given key.
     */
    protected abstract void doRemove( Object key );

    /**
     * Clear the Store of all data it holds 
     */
    protected abstract void doClear();

    /**
     * Indicates if the given key is associated to a contained object.
     */
    protected abstract boolean doContainsKey( Object key );

    /**
     * Returns the list of used keys as an Enumeration of Objects.
     */
    protected abstract Enumeration doGetKeys();

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    protected abstract int doGetSize();
}
