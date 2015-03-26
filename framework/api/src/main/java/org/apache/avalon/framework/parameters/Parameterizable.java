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
 * be provided with parameters during startup.
 * <p>
 * The Parameterizable interface is a light-weight alternative to the
 * {@link org.apache.avalon.framework.configuration.Configurable}
 * interface.  As such, the <code>Parameterizable</code> interface and
 * the <code>Configurable</code> interface are <strong>not</strong>
 * compatible.
 * </p><p>
 * This interface will be called after
 * <code>Composable.compose()</code> and before
 * <code>Initializable.initialize()</code>.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Parameterizable.java 506231 2007-02-12 02:36:54Z crossley $
 */
public interface Parameterizable
{
    /**
     * Provide component with parameters.
     *
     * @param parameters the parameters. Must not be <code>null</code>.
     * @throws ParameterException if parameters are invalid
     */
    void parameterize( Parameters parameters )
        throws ParameterException;
}
