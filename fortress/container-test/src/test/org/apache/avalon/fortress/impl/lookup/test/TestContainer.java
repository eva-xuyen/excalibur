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

package org.apache.avalon.fortress.impl.lookup.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.impl.AbstractContainer;
import org.apache.avalon.fortress.impl.lookup.FortressServiceManager;
import org.apache.avalon.fortress.impl.lookup.FortressServiceSelector;
import org.apache.avalon.fortress.impl.test.TestComponentHandler;
import org.apache.avalon.fortress.test.data.Role1;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

public class TestContainer implements Container
{
    private String                  m_key;
    private Object                  m_hint = AbstractContainer.DEFAULT_ENTRY;
    private TestComponentHandler    m_component;
    private FortressServiceSelector m_selector;

    public TestContainer() {
        m_component = new TestComponentHandler();
    }

    public void setExpectedKey( String key )
    {
        m_key = key;
        m_selector = new FortressServiceSelector( this, m_key );
    }

    public void setExpectedHint( Object hint )
    {
        m_hint = hint;
    }

    public Object get( String key, Object hint ) throws ServiceException
    {
        if ( exists( key, hint ) )
        {
            if ( hint.equals( AbstractContainer.SELECTOR_ENTRY ) )
            {
                return m_selector;
            }
            else
            {
                return m_component;
            }
        }

        throw new ServiceException( m_key, "Unexpected key/hint combo" );
    }

    public boolean has( String key, Object hint )
    {
        if ( exists( key, hint ) )
        {
            return true;
        }

        return false;
    }

    private boolean exists( String key, Object hint )
    {
        boolean exists = false;

        if ( m_key.equals( key ) )
        {
            if ( null == m_hint )
            {
                exists = hint == null;
            }
            else
            {
                exists = m_hint.equals( hint );
            }
        }

        return exists;
    }
}
