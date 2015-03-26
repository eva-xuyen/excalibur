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

package org.apache.avalon.fortress.impl.lookup;

/**
 * Hack that is temporarily necessary until the next version of
 * Commons Collections is released.
 */
final class ComponentKey
{
    private final Object m_component;

    public ComponentKey( final Object component )
    {
        m_component = component;
    }

    public boolean equals( final Object other )
    {
        return ( other instanceof ComponentKey ) &&
            ( (ComponentKey) other ).m_component == m_component;
    }

    public int hashCode()
    {
        return System.identityHashCode(m_component);
    }
}
