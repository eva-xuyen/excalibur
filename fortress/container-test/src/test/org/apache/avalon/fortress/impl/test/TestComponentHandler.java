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

package org.apache.avalon.fortress.impl.test;

import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.test.data.Component1;

/**
 * TestComponentHandler does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class TestComponentHandler implements ComponentHandler
{
    Object m_component = new Component1();

    public Class getComponentClass()
    {
        return m_component.getClass();
    }

    public void prepareHandler() throws Exception
    {
    }

    public Object get() throws Exception
    {
        return m_component;
    }

    public void put( Object component )
    {
        // do nothing
    }

}
