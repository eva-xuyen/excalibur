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

import org.d_haven.mpool.ObjectFactory;

import java.lang.reflect.Constructor;

/**
 * ProxyManager is used to abstract away the plumbing for the underlying
 * proxy generator.  Each proxy solution has to implement the ObjectFactory interface,
 * that way we can keep a soft dependency on things like BCEL.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public final class ProxyManager
{
    public static final int DISCOVER = 0;
    public static final int NONE = 1;
    public static final int BCEL = 2;
    public static final int PROXY = 3;
    private static final String BCEL_CLASS = "org.apache.bcel.classfile.JavaClass";
    private static final String BCEL_WRAPPER = "org.apache.avalon.fortress.impl.factory.WrapperObjectFactory";
    private static final String PROXY_WRAPPER = "org.apache.avalon.fortress.impl.factory.ProxyObjectFactory";
    private static final String NOOP_WRAPPER = "org.apache.avalon.fortress.impl.factory.NoopObjectFactory";
    private static final boolean m_isBCELPresent;
    private final String m_wrapperClassName;
    private Class m_factoryClass;

    static
    {
        boolean bcelPresent = false;
        try
        {
            Thread.currentThread().getContextClassLoader().loadClass( BCEL_CLASS );
            Thread.currentThread().getContextClassLoader().loadClass( BCEL_WRAPPER );
            bcelPresent = true;
        }
        catch ( ClassNotFoundException cfne )
        {
            //ignore because we already have the proper value
        }

        m_isBCELPresent = bcelPresent;
    }

    public ProxyManager( final int type ) throws Exception
    {
        switch (type)
        {
            case NONE:
                m_wrapperClassName = NOOP_WRAPPER;
                break;

            case BCEL:
                if ( ! m_isBCELPresent ) throw new IllegalStateException("BCEL is not available");
                m_wrapperClassName = BCEL_WRAPPER;
                break;

            case PROXY:
                m_wrapperClassName = PROXY_WRAPPER;
                break;

            default: // DISCOVER
                if ( m_isBCELPresent )
                {
                    m_wrapperClassName = BCEL_WRAPPER;
                }
                else
                {
                    m_wrapperClassName = PROXY_WRAPPER;
                }
                break;
        }
    }

    public ObjectFactory getWrappedObjectFactory( final ObjectFactory source ) throws Exception
    {
        if ( null == m_factoryClass )
        {
            final ClassLoader loader = source.getClass().getClassLoader();

            m_factoryClass = loader.loadClass( m_wrapperClassName );
        }

        final Constructor constr = m_factoryClass.getConstructor( new Class[]{ObjectFactory.class} );
        return (ObjectFactory) constr.newInstance( new Object[]{source} );
    }
}
