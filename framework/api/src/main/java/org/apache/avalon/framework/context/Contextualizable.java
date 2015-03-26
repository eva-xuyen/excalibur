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
package org.apache.avalon.framework.context;

/**
 * This inteface should be implemented by components that need
 * a Context to work. Context contains runtime generated object
 * provided by the Container to this Component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Contextualizable.java 506231 2007-02-12 02:36:54Z crossley $
 */
public interface Contextualizable
{
    /**
     * Pass the Context to the component.
     * This method is called after the LogEnabled.enableLogging( Logger ) (if present)
     * method and before any other method.
     *
     * @param context the context. Must not be <code>null</code>.
     * @throws ContextException if context is invalid
     */
    void contextualize( Context context )
        throws ContextException;
}
