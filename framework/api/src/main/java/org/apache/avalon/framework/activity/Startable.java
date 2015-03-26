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
package org.apache.avalon.framework.activity;

/**
 * The Startable interface is used when components need to
 * be "running" to be active. It provides a method through
 * which components can be "started" and "stopped" without
 * requiring a thread.
 * Note that these methods should start the component but return
 * imediately.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Startable.java 506231 2007-02-12 02:36:54Z crossley $
 */
public interface Startable
{
    /**
     * Starts the component.
     *
     * @throws Exception if Component can not be started
     */
    void start()
        throws Exception;

    /**
     * Stops the component.
     *
     * @throws Exception if the Component can not be Stopped.
     */
    void stop()
        throws Exception;
}
