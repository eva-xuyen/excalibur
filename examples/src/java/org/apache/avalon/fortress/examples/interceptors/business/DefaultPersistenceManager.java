/*
 * Copyright 2003-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.avalon.fortress.examples.interceptors.business;

/**
 * @avalon.component
 * @avalon.service type=PersistenceManager
 * @x-avalon.info name=persistenceManager
 * @x-avalon.lifestyle type=singleton
 * @excalibur.interceptable family="businessObject"
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class DefaultPersistenceManager implements PersistenceManager
{
    /**
     * @transaction.required
     * @security.enabled roles="Admin,Director,Worker"
     */
    public void persist(Object data)
    {
        // Working, working, working
    }

    /**
     * @transaction.supported
     */
    public Object load()
    {
        return "Data";
    }
}
