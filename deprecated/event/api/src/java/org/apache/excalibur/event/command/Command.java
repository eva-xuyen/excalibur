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

import org.apache.avalon.framework.activity.Executable;
import org.apache.excalibur.event.Signal;

/**
 * A Command is a specific type of Signal that denotes an asynchronous
 * execution unit that must be performed by the CommandManager.
 *
 * <p>
 *   The interface design is heavily influenced by
 *   <a href="mailto:mdw@cs.berkeley.edu">Matt Welsh</a>'s SandStorm server,
 *   his demonstration of the SEDA architecture.  We have deviated where we
 *   felt the design differences where better.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface Command extends Signal, Executable
{
}
