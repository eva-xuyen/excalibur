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
package org.apache.excalibur.store;

import java.io.IOException;
import java.util.Enumeration;

/**
 * A Store is an object managing arbitrary data. It holds data stored
 * under a given key persistently. So if you put something in a store
 * you can be sure that the next time (even if the application restarted)
 * your data is in the store (of course unless noone else did remove it).
 * In some cases (like for example a cache) the data needs not to be
 * persistent. Therefore with the two role TRANSIENT_STORE and
 * PERSISTENT_STORE you get a store with exactly that behaviour.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: Store.java,v 1.5 2004/02/28 11:47:34 cziegeler Exp $
 */
public interface Store
{
    /** The role for a persistent store */
    String ROLE = Store.class.getName();

    /** The role for a transient store */
    String TRANSIENT_STORE = ROLE + "/TransientStore";
    
    /** The role for a persistent store */
    String PERSISTENT_STORE = ROLE + "/PersistentStore";

    /**
     * Get the object associated to the given unique key.
     */
    Object get( Object key );

    /**
     * Store the given object. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    void store( Object key, Object value ) throws IOException;

    /**
     * Try to free some used memory. The transient store can simply remove
     * some hold data, the persistent store can free all memory by
     * writing the data to a persistent store etc.
     */
    void free();

    /**
     * Remove the object associated to the given key.
     */
    void remove( Object key );

    /**
     * Clear the Store of all data it holds 
     */
    void clear();

    /**
     * Indicates if the given key is associated to a contained object.
     */
    boolean containsKey( Object key );

    /**
     * Returns the list of used keys as an Enumeration of Objects.
     */
    Enumeration keys();

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    int size();
}
