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
 * This interface standardizes the behaviour of a resettable object.
 * A resettable object is defined as an object that can be used to
 * encapsulate another object without being altered by its content.
 * Therefore, a resettable object may be reset and reused many times.
 *
 * This is helpful in cases where resettable objects are continously
 * created and destroyed, causing a much greater amount of garbage to
 * be collected by the JVM garbage collector. By making it resettable,
 * it is possible to reduce the GC execution time, thus incrementing the
 * overall performance of a process and decrementing the chance of
 * memory overflow.
 *
 * Every implementation must provide their own method to allow this
 * recyclable object to be reused by setting its content.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/02/28 11:47:33 $
 * @since 4.0
 */
public interface Resettable
{
    /**
     * This method should be implemented to remove all costly resources
     * in object. These resources can be object references, database connections,
     * threads, etc. What is categorised as "costly" resources is determined on
     * a case by case analysis.
     */
    void reset();
}
