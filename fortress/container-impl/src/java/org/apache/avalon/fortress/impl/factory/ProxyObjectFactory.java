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

package org.apache.avalon.fortress.impl.factory;

import org.apache.avalon.framework.component.Component;
import org.d_haven.mpool.ObjectFactory;

import java.lang.reflect.Proxy;

/**
 * An ObjectFactory that delegates to another ObjectFactory
 * and proxies results of that factory.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.4 $ $Date: 2004/02/28 15:16:25 $
 */
public final class ProxyObjectFactory extends AbstractObjectFactory
{
    /**
     * Create factory that delegates to specified factory.
     *
     * @param objectFactory the factory to delegate to
     * @exception NullPointerException if the supplied object factory is null
     */
    public ProxyObjectFactory( final ObjectFactory objectFactory ) throws NullPointerException
    {
        super( objectFactory );
    }

    /**
     * Create a new instance from delegated factory and proxy it.
     *
     * @return the proxied object
     * @throws Exception if unable to create new instance
     */
    public Object newInstance()
        throws Exception
    {
        final Object object = m_delegateFactory.newInstance();
        return createProxy( object );
    }

    /**
     * Dispose of objects created by this factory.
     * Involves deproxying object and delegating to real ObjectFactory.
     *
     * @param object the proxied object
     * @throws Exception if unable to dispose of object
     */
    public void dispose( final Object object )
        throws Exception
    {
        final Object target = getObject( object );
        m_delegateFactory.dispose( target );
    }

    /**
     * Get the Component wrapped in the proxy.
     *
     * @param service the service object to proxy
     */
    public static Component createProxy( final Object service )
    {
        final Class clazz = service.getClass();
        final Class[] workInterfaces = guessWorkInterfaces( clazz );

        return (Component) Proxy.newProxyInstance( clazz.getClassLoader(),
            workInterfaces,
            new PassThroughInvocationHandler( service ) );
    }

    /**
     * Get the target object from specified proxy.
     *
     * @param proxy the proxy object
     * @return the target object
     * @throws NullPointerException if unable to aquire target object,
     *                   or specified object is not a proxy
     */
    public static Object getObject( final Object proxy )

    {
        if ( null == proxy )
        {
            throw new NullPointerException( "proxy" );
        }

        if ( !Proxy.isProxyClass( proxy.getClass() ) )
        {
            final String message = "object is not a proxy";
            throw new IllegalArgumentException( message );
        }

        final PassThroughInvocationHandler handler =
            (PassThroughInvocationHandler) Proxy.getInvocationHandler( proxy );
        return handler.getObject();
    }
}
