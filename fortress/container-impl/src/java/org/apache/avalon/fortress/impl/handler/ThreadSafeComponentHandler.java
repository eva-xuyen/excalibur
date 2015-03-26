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

package org.apache.avalon.fortress.impl.handler;

/**
 * The ThreadSafeComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/02/28 15:16:25 $
 * @since 4.0
 */
public final class ThreadSafeComponentHandler
    extends AbstractComponentHandler
{
    private Object m_instance;

    /**
     * Initialize the ComponentHandler.
     * @exception Exception if a handler preparation error occurs
     */
    protected void doPrepare()
        throws Exception
    {
        m_instance = newComponent();
    }

    /**
     * Return instance for a get.
     *
     * @return the instance
     * @exception Exception if a handler access error occurs
     */
    protected Object doGet()
        throws Exception
    {
        return m_instance;
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    protected void doDispose()
    {
        disposeComponent( m_instance );
        m_instance = null;
    }
}
