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
package org.apache.avalon.excalibur.pool;

/**
 * This interface standardizes the behaviour of a recyclable object.
 * A recyclable object is defined as an object that can be used to
 * encapsulate another object without being altered by its content.
 * Therefore, a recyclable object may be recycled and reused many times.
 *
 * This is helpful in cases where recyclable objects are continously
 * created and destroyed, causing a much greater amount of garbage to
 * be collected by the JVM garbage collector. By making it recyclable,
 * it is possible to reduce the GC execution time, thus incrementing the
 * overall performance of a process and decrementing the chance of
 * memory overflow.
 *
 * Every implementation must provide their own method to allow this
 * recyclable object to be reused by setting its content.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public interface Recyclable
    extends Poolable
{
    /**
     * This method should be implemented to remove all costly resources
     * in object. These resources can be object references, database connections,
     * threads, etc. What is categorised as "costly" resources is determined on
     * a case by case analysis.
     */
    void recycle();
}
