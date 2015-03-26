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
package org.apache.avalon.framework.availability;
 
/** Exception to signal component is not available.
 * 
 * UnavailableException is thrown in any method to another component,
 * but only if the caller implements AvailabilityAware.
 *
 * @since 4.5
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public class UnavailableException extends RuntimeException
{
    private String m_Key;
    
    /** Constructor.
     * @param message The non-localized message to be embedded into the
     *        exception.
     *
     * @param key The key that the caller used to lookup the component
     *            with.
     */
    public UnavailableException( String message, String key )
    {
        super( message );
        m_Key = key;
    }
   
    /** Returns the key to the component that is no longer available.
     *
     */ 
    public String getLookupKey()
    {
        return m_Key;
    }
}