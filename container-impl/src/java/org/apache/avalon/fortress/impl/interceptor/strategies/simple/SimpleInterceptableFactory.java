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

package org.apache.avalon.fortress.impl.interceptor.strategies.simple;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.impl.interceptor.InterceptableFactory;
import org.apache.avalon.fortress.interceptor.Interceptor;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class SimpleInterceptableFactory implements InterceptableFactory
{
    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.impl.interceptor.InterceptableFactory#createInterceptableInstance(java.lang.Object, org.apache.avalon.fortress.interceptor.Interceptor)
     */
    public Object createInterceptableInstance( Object realInstance, ExtendedMetaInfo meta, Class[] interfaces, Interceptor chain )
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InterceptorInvocationHandler handler = new InterceptorInvocationHandler( realInstance, chain, meta );
        
        return Proxy.newProxyInstance( loader, interfaces, handler );
    }
    
    public static class InterceptorInvocationHandler implements InvocationHandler
    {
        private final Object m_instance;
        private final Interceptor m_chain;
        private final ExtendedMetaInfo m_meta;
        
        public InterceptorInvocationHandler( Object instance, Interceptor chain, ExtendedMetaInfo meta )
        {
            m_instance = instance;
            m_chain = chain;
            m_meta = meta;
        }
        
        /**
         * Pending
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            return m_chain.intercept( m_instance, m_meta, method, args );
        }
    }
}
