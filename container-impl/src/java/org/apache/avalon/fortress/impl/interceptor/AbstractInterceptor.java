/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.interceptor.Interceptor;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public abstract class AbstractInterceptor implements Interceptor
{
    private Interceptor m_next;
    
    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.Interceptor#getNext()
     */
    public Interceptor getNext()
    {
        return m_next;
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.Interceptor#init(org.apache.avalon.fortress.interceptor.Interceptor)
     */
    public void init(Interceptor next)
    {
        m_next = next;
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.Interceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object intercept(Object instance, ExtendedMetaInfo meta, Method method, Object[] args)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return getNext().intercept( instance, meta, method, args );
    }
}
