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
 * Extends Contextualizable to allow recontextualizing.
 * This allows a component to re-receive it's context if
 * container environment has changed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Recontextualizable.java 506231 2007-02-12 02:36:54Z crossley $
 */
public interface Recontextualizable
    extends Contextualizable
{
    /**
     * Pass the new Context to the component.
     * This method is usually called when component is suspended via use of
     * Suspendable.suspend() method.
     *
     * @param context the context
     * @throws ContextException if context is invalid
     */
    void recontextualize( Context context )
        throws ContextException;
}
