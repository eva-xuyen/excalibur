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
package org.apache.avalon.framework.parameters;

/**
 * Components should implement this interface if they wish to
 * be provided with parameters during its lifetime. This interface
 * will be called after Startable.start() and before
 * Startable.stop(). It is incompatible with the
 * Reconfigurable interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Reparameterizable.java 506231 2007-02-12 02:36:54Z crossley $
 */
public interface Reparameterizable extends Parameterizable
{
    /**
     * Provide component with parameters.
     *
     * @param parameters the parameters
     * @throws ParameterException if parameters are invalid
     */
    void reparameterize( Parameters parameters )
        throws ParameterException;
}
