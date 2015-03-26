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

package org.apache.avalon.fortress.impl.factory;

/**
 * <p>
 *  The interface <code>WrapperClass</code> provides a means of asking a dynamically
 *  created wrapper for a given object for said wrapped object.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */

public interface WrapperClass
{
    /**
     * Return the object that is being wrapped by this wrapper class instance.
     *
     * @return Object                The wrapped object
     * @throws IllegalStateException
     */
    public Object getWrappedObject()
        throws IllegalStateException;
}
