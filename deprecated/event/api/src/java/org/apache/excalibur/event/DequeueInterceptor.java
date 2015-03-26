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
package org.apache.excalibur.event;

/**
 * The dequeue executable interface describes operations that
 * are executed before and after elements are pulled from a
 * queue.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $
 */
public interface DequeueInterceptor
{

    /**
     * An operation executed before dequeing events from
     * the queue. The Source is passed in so the implementation
     * can determine to execute based on the queue properties.
     *
     * <p>
     *   This method is called once at the beginning of any <code>dequeue</code>
     *   method regardless of how many queue elements are dequeued.
     * </p>
     *
     * @since Feb 10, 2003
     *
     * @param context  The source from which the dequeue is performed.
     */
    public void before(Source context);

    /**
     * An operation executed after dequeing events from
     * the queue. The Source is passed in so the implementation
     * can determine to execute based on the queue properties.
     *
     * <p>
     *   This method is called once at the end of any <code>dequeue</code>
     *   method regardless of how many queue elements are dequeued.
     * </p>
     *
     * @since Feb 10, 2003
     *
     * @param context  The source from which the dequeue is performed.
     */
    public void after(Source context);
}
