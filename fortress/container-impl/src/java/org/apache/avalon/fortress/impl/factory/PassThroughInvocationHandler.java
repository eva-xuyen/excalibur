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

package org.apache.avalon.fortress.impl.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * InvocationHandler that just passes on all methods to target object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
final class PassThroughInvocationHandler
    implements InvocationHandler
{
    /**
     * The target object delegated to.
     */
    private final Object m_object;

    /**
     * Create an Invocation handler for specified object.
     *
     * @param object the object to delegate to
     */
    public PassThroughInvocationHandler( final Object object )
    {
        if ( null == object )
        {
            throw new NullPointerException( "object" );
        }

        m_object = object;
    }

    /**
     * Invoke the appropriate method on underlying object.
     *
     * @param proxy the proxy object
     * @param meth the method
     * @param args the arguments
     * @return the return value of object
     * @exception Throwable method throws an exception
     */
    public Object invoke( final Object proxy,
                          final Method meth,
                          final Object[] args )
        throws Throwable
    {
        try
        {
            return meth.invoke( m_object, args );
        }
        catch ( final InvocationTargetException ite )
        {
            throw ite.getTargetException();
        }
    }

    /**
     * Retrieve the underlying object delegated to.
     *
     * @return the object delegated to
     */
    Object getObject()
    {
        return m_object;
    }
}
