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
package org.apache.excalibur.store.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * This class provides a cache algorithm for the requested documents.
 * It combines a HashMap and a LinkedList to create a so called MRU
 * (Most Recently Used) cache.
 * 
 * @avalon.component
 * @avalon.service type=Store
 * @x-avalon.info name=mru-store
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: MRUMemoryStore.java,v 1.5 2004/02/28 11:47:31 cziegeler Exp $
 */
public class MRUMemoryStore
    extends AbstractLogEnabled
    implements Store, Parameterizable, Serviceable, Disposable, ThreadSafe, Instrumentable, Component
{
    private String m_instrumentableName;
    private int m_maxobjects;
    private boolean m_persistent;
    private Hashtable m_cache;
    private LinkedList m_mrulist;
    private Store m_persistentStore;
    private StoreJanitor m_storeJanitor;
    private ServiceManager m_manager;
    
    private ValueInstrument m_sizeInstrument = new ValueInstrument("size");
    private CounterInstrument m_hitsInstrument = new CounterInstrument("hits");
    private CounterInstrument m_missesInstrument = new CounterInstrument("misses");

    /**
     * Get components of the ComponentLocator
     *
     * @param manager The ComponentLocator
     * @avalon.dependency type=org.apache.excalibur.store.Store
     * @avalon.dependency type=org.apache.excalibur.store.StoreJanitor
     */
    public void service( ServiceManager manager )
        throws ServiceException
    {
        m_manager = manager;
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Looking up " + StoreJanitor.ROLE );
        }
        m_storeJanitor = (StoreJanitor)manager.lookup( StoreJanitor.ROLE );
    }

    /**
     * Initialize the MRUMemoryStore.
     * A few options can be used:
     * <UL>
     *  <LI>maxobjects: Maximum number of objects stored in memory (Default: 100 objects)</LI>
     *  <LI>use-persistent-cache: Use persistent cache to keep objects persisted after
     *      container shutdown or not (Default: false)</LI>
     * </UL>
     *
     * @param params Store parameters
     * @exception ParameterException
     */
    public void parameterize( Parameters params ) throws ParameterException
    {
        m_maxobjects = params.getParameterAsInteger( "maxobjects", 100 );
        m_persistent = params.getParameterAsBoolean( "use-persistent-cache", false );
        if( ( m_maxobjects < 1 ) )
        {
            throw new ParameterException( "MRUMemoryStore maxobjects must be at least 1!" );
        }

        if ( m_persistent )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Looking up " + Store.PERSISTENT_STORE );
            }
            try 
            {
                m_persistentStore = (Store)m_manager.lookup( Store.PERSISTENT_STORE );
            }
            catch (ServiceException se)
            {
                throw new ParameterException("Unable to look up persistent store.", se);
            }
        }

        m_cache = new Hashtable( (int)( m_maxobjects * 1.2 ) );
        m_mrulist = new LinkedList();
        m_storeJanitor.register( this );
    }

    /**
     * Dispose the component
     */
    public void dispose()
    {
        if( m_manager != null )
        {
            getLogger().debug( "Disposing component!" );

            if( m_storeJanitor != null )
            {
                m_storeJanitor.unregister( this );
            }
            m_manager.release( m_storeJanitor );
            m_storeJanitor = null;

            // save all cache entries to filesystem
            if( m_persistent )
            {
                getLogger().debug( "Final cache size: " + m_cache.size() );
                Enumeration enumer = m_cache.keys();
                while( enumer.hasMoreElements() )
                {
                    Object key = enumer.nextElement();
                    if( key == null )
                    {
                        continue;
                    }
                    try
                    {
                        Object value = m_cache.remove( key );
                        if( checkSerializable( value ) )
                        {
                            m_persistentStore.store( key, value );
                        }
                    }
                    catch( IOException ioe )
                    {
                        getLogger().error( "Error in dispose()", ioe );
                    }
                }
            }
            m_manager.release( m_persistentStore );
            m_persistentStore = null;
        }

        m_manager = null;
    }

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     *
     * @param key The key for the object to store
     * @param value The object to store
     */
    public synchronized void store( Object key, Object value )
    {
        hold( key, value );
    }

    /**
     * This method holds the requested object in a HashMap combined
     * with a LinkedList to create the MRU.
     * It also stores objects onto the filesystem if configured.
     *
     * @param key The key of the object to be stored
     * @param value The object to be stored
     */
    public synchronized void hold( Object key, Object value )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Holding object in memory:" );
            getLogger().debug( "  key: " + key );
            getLogger().debug( "  value: " + value );
        }
        /** ...first test if the max. objects in cache is reached... */
        while( m_mrulist.size() >= m_maxobjects )
        {
            /** ...ok, heapsize is reached, remove the last element... */
            free();
        }
        /** ..put the new object in the cache, on the top of course ... */
        m_cache.put( key, value );
        m_mrulist.remove( key );
        m_mrulist.addFirst( key );
        m_sizeInstrument.setValue( m_mrulist.size() );
    }

    /**
     * Get the object associated to the given unique key.
     *
     * @param key The key of the requested object
     * @return the requested object
     */
    public synchronized Object get( Object key )
    {
        Object value = m_cache.get( key );
        if( value != null )
        {
            /** put the accessed key on top of the linked list */
            m_mrulist.remove( key );
            m_mrulist.addFirst( key );
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Found key: " + key.toString() );
            }
            m_hitsInstrument.increment();
            return value;
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "NOT Found key: " + key.toString() );
        }

        /** try to fetch from filesystem */
        if( m_persistent )
        {
            value = m_persistentStore.get( key );
            if( value != null )
            {
                try
                {
                    if( !m_cache.containsKey( key ) )
                    {
                        hold( key, value );
                    }
                    m_hitsInstrument.increment();
                    return value;
                }
                catch( Exception e )
                {
                    getLogger().error( "Error in get()!", e );
                }
            }
        }
        m_missesInstrument.increment();
        return null;
    }

    /**
     * Remove the object associated to the given key.
     *
     * @param key The key of to be removed object
     */
    public synchronized void remove( Object key )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Removing object from store" );
            getLogger().debug( "  key: " + key );
        }
        m_cache.remove( key );
        m_mrulist.remove( key );
        m_sizeInstrument.setValue( m_mrulist.size() );
        
        if( m_persistent && key != null )
        {
            m_persistentStore.remove( key );
        }
    }

    /**
     * Clear the Store of all elements
     */
    public synchronized void clear()
    {
        Enumeration enumer = m_cache.keys();
        while( enumer.hasMoreElements() )
        {
            Object key = enumer.nextElement();
            if( key == null )
            {
                continue;
            }
            remove( key );
        }
        m_sizeInstrument.setValue( 0 );
    }

    /**
     * Indicates if the given key is associated to a contained object.
     *
     * @param key The key of the object
     * @return true if the key exists
     */
    public synchronized boolean containsKey( Object key )
    {
        if( m_persistent )
        {
            return ( m_cache.containsKey( key ) || m_persistentStore.containsKey( key ) );
        }
        else
        {
            return m_cache.containsKey( key );
        }
    }

    /**
     * Returns the list of used keys as an Enumeration.
     *
     * @return the enumeration of the cache
     */
    public synchronized Enumeration keys()
    {
        return m_cache.keys();
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public synchronized int size()
    {
        return m_cache.size();
    }

    /**
     * Frees some of the fast memory used by this store.
     * It removes the last element in the store.
     */
    public synchronized void free()
    {
        try
        {
            if( m_cache.size() > 0 )
            {
                // This can throw NoSuchElementException
                Object key = m_mrulist.removeLast();
                Object value = m_cache.remove( key );
                if( value == null )
                {
                    getLogger().warn( "Concurrency condition in free()" );
                }

                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Freeing cache." );
                    getLogger().debug( "  key: " + key );
                    getLogger().debug( "  value: " + value );
                }

                if( m_persistent )
                {
                    // Swap object on fs.
                    if( checkSerializable( value ) )
                    {
                        try
                        {
                            m_persistentStore.store( key, value );
                        }
                        catch( Exception e )
                        {
                            getLogger().error( "Error storing object on fs", e );
                        }
                    }
                }
                
                m_sizeInstrument.setValue( m_mrulist.size() );
            }
        }
        catch( NoSuchElementException e )
        {
            getLogger().warn( "Concurrency error in free()", e );
        }
        catch( Exception e )
        {
            getLogger().error( "Error in free()", e );
        }
    }

    /**
     * This method checks if an object is seriazable.
     *
     * @param object The object to be checked
     * @return true if the object is storeable
     */
    private boolean checkSerializable( Object object )
    {

        if( object == null ) return false;

        return ( object instanceof java.io.Serializable );
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
}

