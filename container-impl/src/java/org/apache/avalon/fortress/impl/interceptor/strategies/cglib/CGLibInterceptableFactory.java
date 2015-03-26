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
 
package org.apache.avalon.fortress.impl.interceptor.strategies.cglib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.impl.interceptor.AbstractInterceptor;
import org.apache.avalon.fortress.impl.interceptor.InterceptableFactory;
import org.apache.avalon.fortress.impl.interceptor.TailInterceptor;
import org.apache.avalon.fortress.interceptor.Interceptor;

/**
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class CGLibInterceptableFactory implements InterceptableFactory
{
    /**
     * Document me!
     *  
     * @see org.apache.avalon.fortress.impl.interceptor.InterceptableFactory#createInterceptableInstance(java.lang.Object, org.apache.avalon.fortress.ExtendedMetaInfo, java.lang.Class[], org.apache.avalon.fortress.interceptor.Interceptor)
     */
    public Object createInterceptableInstance(Object realInstance, ExtendedMetaInfo meta, Class[] interfaces, Interceptor chain)
    {
        InternalMethodInterceptor methodInterceptor = new InternalMethodInterceptor( realInstance, meta, chain );
        
        return Enhancer.create( realInstance.getClass(), interfaces, methodInterceptor );
    }

    public static class InternalMethodInterceptor implements MethodInterceptor
    {
        private final Object m_instance;
        private final ExtendedMetaInfo m_meta;
        private final Interceptor m_chain;
        private MethodProxy m_proxy;
        
        /**
         * @param instance
         * @param meta
         * @param chain
         */
        public InternalMethodInterceptor(final Object instance, 
                                         final ExtendedMetaInfo meta, 
                                         final Interceptor chain)
        {
            m_instance = instance;
            m_meta = meta;
            m_chain = chain;

            // Now we need to find the tailInterceptor and replace it
            Interceptor previous = chain;
            Interceptor next = chain;
            
            while( next != null )
            {
                if (next.getClass() == TailInterceptor.class)
                {
                    previous.init( new AbstractInterceptor()
                    {
                        public Object intercept(Object instance, ExtendedMetaInfo meta, Method method, Object[] args)
                            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                        {
                            try
                            {
                                return m_proxy.invoke( instance, args );
                            }
                            catch(IllegalAccessException ex)
                            {
                                throw ex;
                            }
                            catch(IllegalArgumentException ex)
                            {
                                throw ex;
                            }
                            catch(InvocationTargetException ex)
                            {
                                throw ex;
                            }
                            catch(Throwable ex)
                            {
                                throw new InvocationTargetException(ex);
                            }
                        }
                    } );
                    
                    break;
                }
                
                previous = next;
                next = next.getNext();
            }
        }

        public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable
        {
            m_proxy = proxy;
            
            return m_chain.intercept( m_instance, m_meta, method, args );
        }
    }
}
