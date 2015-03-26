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

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;

/**
 * 
 * @avalon.component
 * @avalon.service type=Store
 * @x-avalon.info name=mem-store
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: MemoryStore.java,v 1.4 2004/02/28 11:47:31 cziegeler Exp $
 */
public class MemoryStore
    extends AbstractLogEnabled
    implements Store, ThreadSafe, Component {

    /* WARNING: Hashtable is threadsafe, whereas HashMap is not.
     * Should we move this class over to the Collections API,
     * use Collections.synchronizedMap(Map map) to ensure
     * accesses are synchronized.
     */

    /** The shared store */
    protected Hashtable m_table = new Hashtable();

    /**
     * Get the object associated to the given unique key.
     */
    public Object get(Object key) 
    {
        return m_table.get(key);
    }

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    public void store(Object key, Object value) 
    {
        m_table.put(key,value);
    }

    /**
     * Remove the object associated to the given key.
     */
    public void remove(Object key) 
    {
        m_table.remove(key);
    }

    /**
     * Clear the Store of all elements 
     */
    public void clear() 
    {
        m_table.clear();
    }

    public void free() {}

    /**
     * Indicates if the given key is associated to a contained object.
     */
    public boolean containsKey(Object key) 
    {
        return m_table.containsKey(key);
    }

    /**
     * Returns the list of used keys as an Enumeration of Objects.
     */
    public Enumeration keys() 
    {
        return m_table.keys();
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public int size()
    {
        return m_table.size();
    }
}
