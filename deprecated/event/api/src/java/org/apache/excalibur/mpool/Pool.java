/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.excalibur.mpool;

/**
 * This interface is to define how a Pool is used.  We have determined by
 * using the previous Pool implementations that the Pool marker interface
 * is considered harmful.  When generics are introduced in JDK 1.5, this
 * interface will be a prime candidate for those improvements.
 *
 * <p>
 *  It is important to realize that some objects are cheaper to simply allow
 *  the garbage collector to take care of them.  Therefore, only pool objects
 *  that are computationally expensive to create.  Prime candidates would be
 *  Components, JDBC Connection objects, Socket connections, etc.
 * </p>
 * <p>
 *  The interface is inspired by both the Mutex acquire/release and the
 *  structure of the ThreadLocal object.  In fact, it would be trivial
 *  to implement a "ThreadLocal" pool.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:33 $
 * @since 4.1.2
 */
public interface Pool
{
    /**
     * Acquire an instance of the pooled object.
     *
     * @return the pooled Object instance
     *
     * @throws Exception if the Pool is not able to return an object.
     */
    Object acquire() throws Exception;

    /**
     * Release the instance of the pooled object.
     *
     * @param pooledObject  The pooled object to release to the pool.
     */
    void release( Object pooledObject );

    /**
     * Create a new instance of the object being pooled.
     *
     * @return the pooled Object instance
     *
     * @throws Exception if the instance cannot be created
     */
    Object newInstance() throws Exception;
}
