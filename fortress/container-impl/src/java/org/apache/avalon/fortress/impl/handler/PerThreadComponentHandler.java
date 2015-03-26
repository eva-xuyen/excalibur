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

package org.apache.avalon.fortress.impl.handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * The PerThreadComponentHandler implements a singleton with a slight difference:
 * one single instance per thread.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 * @since 4.0
 */
public final class PerThreadComponentHandler
    extends AbstractComponentHandler
{
    private static ThreadLocal m_slot;
    private static List m_instances;

    public void initialize()
        throws Exception
    {
        super.initialize();
        m_slot = new ThreadLocal();
        m_instances = Collections.synchronizedList(new LinkedList());
    }

    /**
     * Get a reference of the desired Component
     */
    protected Object doGet()
        throws Exception
    {
        synchronized(m_slot)
        {
            Map map = (Map) m_slot.get();

            if (map == null)
            {
                Object instance = null;
                map = new HashMap();
                m_slot.set(map);
            }
		
            Object instance = map.get( super.m_factory );

            if (instance == null)
            {
                instance = newComponent();
                map.put( super.m_factory, instance );
                m_instances.add(instance);
            }

            return instance;
        }
    }

    protected void doDispose()
    {
        Iterator it = m_instances.iterator();
        while (it.hasNext())
        {
            disposeComponent( it.next() );
            it.remove();
        }
        m_slot = null;
        m_instances = null;
    }
}

