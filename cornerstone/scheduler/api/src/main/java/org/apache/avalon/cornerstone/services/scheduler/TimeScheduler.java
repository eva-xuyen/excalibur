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

package org.apache.avalon.cornerstone.services.scheduler;

import java.util.NoSuchElementException;

/**
 * This service provides a way to regularly schedule jobs.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface TimeScheduler
{
    String ROLE = TimeScheduler.class.getName();

    /**
     * Schedule a time based trigger.
     * Note that if a TimeTrigger already has same name then it is removed.
     *
     * @param name the name of the trigger
     * @param trigger the trigger
     * @param target the target
     */
    void addTrigger( String name, TimeTrigger trigger, Target target );

    /**
     * Remove a scheduled trigger by name.
     *
     * @param name the name of the trigger
     * @exception NoSuchElementException if no trigger exists with that name
     */
    void removeTrigger( String name )
        throws NoSuchElementException;

    /**
     * Force a trigger time to be recalculated.
     *
     * @param name the name of the trigger
     * @exception NoSuchElementException if no trigger exists with that name
     */
    void resetTrigger( String name )
        throws NoSuchElementException;

    /**
     * Add a trigger failure listener
     * @param listener the listener
     */
    void addTriggerFailureListener( TriggerFailureListener listener );

    /**
     * Remove a trigger failure listener
     * @param listener the listener
     */
    void removeTriggerFailureListener( TriggerFailureListener listener );

}
