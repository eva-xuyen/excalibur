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
package org.apache.excalibur.event.command;

/**
 * A ThreadManager handles the thread policies for EventPipelines.  It works
 * hand in hand with the CommandManager, and can be expanded to work with a
 * SEDA like architecture.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface ThreadManager
{
    String ROLE = ThreadManager.class.getName();

    /**
     * Register an EventPipeline with the ThreadManager.
     *
     * @param pipeline  The pipeline to register
     */
    void register( EventPipeline pipeline );

    /**
     * Deregister an EventPipeline with the ThreadManager
     *
     * @param pipeline  The pipeline to unregister
     */
    void deregister( EventPipeline pipeline );

    /**
     * Deregisters all EventPipelines from this ThreadManager
     */
    void deregisterAll();
}
