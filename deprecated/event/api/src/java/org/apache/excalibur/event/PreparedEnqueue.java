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
 * A <code>PreparedEnqueue</code> is an object returned from a
 * <code>prepareEnqueue</code> method that allows you to either
 * commit or abort the enqueue operation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface PreparedEnqueue
{
    /**
     * Commit a previously prepared provisional enqueue operation (from
     * the <code>prepareEnqueue</code> method). Causes the provisionally
     * enqueued elements to appear on the queue for future dequeue
     * operations.  Note that once a <code>prepareEnqueue</code> has returned
     * an enqueue key, the queue cannot reject the entries.
     */
    void commit();

    /**
     * Abort a previously prepared provisional enqueue operation (from
     * the <code>prepareEnqueue</code> method). Causes the queue to discard
     * the provisionally enqueued elements.
     */
    void abort();
}
