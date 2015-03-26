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

/**
 * An object factory that delegates all calls to another object factory and
 * wraps the returned object into another object that exposes only the wrapped
 * object's work interface(s).
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public final class WrapperObjectFactory extends AbstractObjectFactory
{

    /**
     * The {@link BCELWrapperGenerator} to use for creating the wrapper.
     */
    private final BCELWrapperGenerator m_wrapperGenerator;

    /**
     * Creates a {@link WrapperObjectFactory} with the specified
     * {@link org.d_haven.mpool.ObjectFactory ObjectFactory} as the
     * object factory to delegate all calls for new instances to.
     *
     * @param  objectFactory The {@link org.d_haven.mpool.ObjectFactory
     *                     ObjectFactory} to sue when creating new instances
     * @throws IllegalArgumentException If <code>objFactory</code> is
     *                                   <code>null</code>
     */
    public WrapperObjectFactory( final ObjectFactory objectFactory )
        throws IllegalArgumentException
    {
        super( objectFactory );
        m_wrapperGenerator = new BCELWrapperGenerator();
    }

    /**
     * @see org.d_haven.mpool.ObjectFactory#newInstance()
     */
    public Object newInstance() throws Exception
    {
        final Object obj = m_delegateFactory.newInstance();

        final Class wrappedClass =
            m_wrapperGenerator.createWrapper( obj.getClass() );

        return wrappedClass.getConstructor(
            new Class[]{obj.getClass()} ).newInstance(
                new Object[]{obj} );
    }

    /**
     * @see org.d_haven.mpool.ObjectFactory#dispose(java.lang.Object)
     */
    public void dispose( final Object object ) throws Exception
    {
        if ( object == null )
        {
            final String error = "Supplied argument is <null>";
            throw new IllegalArgumentException( error );
        }
        if ( !( object instanceof WrapperClass ) )
        {
            final String error =
                "Supplied argument is no instance of \""
                + WrapperClass.class.getName()
                + "\"";
            throw new IllegalArgumentException( error );
        }

        final Object target = ( (WrapperClass) object ).getWrappedObject();

        m_delegateFactory.dispose( target );
    }
}
