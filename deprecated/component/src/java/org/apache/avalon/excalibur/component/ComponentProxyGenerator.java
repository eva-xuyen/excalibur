/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.component.Component;

/**
 * Create a Component proxy.  Requires JDK 1.3+
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class ComponentProxyGenerator
{
    private final ClassLoader m_classLoader;

    /**
     * Initialize the ComponentProxyGenerator with the default classloader.
     * The default classloader is the Thread context classloader.
     */
    public ComponentProxyGenerator()
    {
        this( Thread.currentThread().getContextClassLoader() );
    }

    /**
     * Initialize the ComponentProxyGenerator with the supplied classloader.
     * If the supplied class loader is null, we use the Thread context class
     * loader.  If that is null, we use this class's classloader.
     */
    public ComponentProxyGenerator( final ClassLoader parentClassLoader )
    {
        m_classLoader = ( null == parentClassLoader ) ?
            ( ( null == Thread.currentThread().getContextClassLoader() ) ?
            getClass().getClassLoader()
            : Thread.currentThread().getContextClassLoader() )
            : parentClassLoader;
    }

    /**
     * Get the Component wrapped in the proxy.  The role must be the service
     * interface's fully qualified classname to work.
     */
    public Component getProxy( String role, Object service ) throws Exception
    {
        // Trim any trailing "/type" from the role name.
        if( role.indexOf( '/' ) != -1 )
        {
            role = role.substring( 0, role.indexOf( '/' ) );
        }

        Class serviceInterface = m_classLoader.loadClass( role );

        return (Component)Proxy.newProxyInstance( m_classLoader,
                                                  new Class[]{Component.class, serviceInterface},
                                                  new ComponentInvocationHandler( service ) );
    }

    /**
     * Get the component wrapped in a proxy. The proxy will implement all the
     * object's interfaces, for compatibility with, for example, the pool code.
     * The proxy is guaranteed to implement Component.
     */
    public Component getCompatibleProxy( Object service ) throws Exception
    {
        Set interfaces = new HashSet();
        getAllInterfaces( service.getClass(), interfaces );

        interfaces.add( Component.class );

        Class[] proxyInterfaces = (Class[]) interfaces.toArray( new Class[0] );

        return (Component)Proxy.newProxyInstance( m_classLoader,
            proxyInterfaces,
            new ComponentInvocationHandler( service ) );
    }

    private void getAllInterfaces( Class clazz, Set interfaces ) throws Exception
    {
        if (clazz == null)
        {
            return;
        }

        Class[] objectInterfaces = clazz.getInterfaces();
        for( int i = 0; i < objectInterfaces.length; i++ )
        {
            interfaces.add( objectInterfaces[i] );
        }

        getAllInterfaces( clazz.getSuperclass(), interfaces );
    }


    /**
     * Internal class to handle the wrapping with Component
     */
    private final static class ComponentInvocationHandler
        implements InvocationHandler
    {
        private final Object m_delagate;

        public ComponentInvocationHandler( final Object delegate )
        {
            if( null == delegate )
            {
                throw new NullPointerException( "delegate" );
            }

            m_delagate = delegate;
        }

        public Object invoke( final Object proxy,
                              final Method meth,
                              final Object[] args )
            throws Throwable
        {
            try
            {
                return meth.invoke( m_delagate, args );
            }
            catch( final InvocationTargetException ite )
            {
                throw ite.getTargetException();
            }
        }
    }
}
