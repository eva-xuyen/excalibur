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
 
package org.apache.avalon.fortress.examples.interceptors.business.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.attributes.AttributeInfo;
import org.apache.avalon.fortress.impl.interceptor.AbstractInterceptor;

/**
 * This is a blank space with hypothetical code service as
 * a recipe for a TransactionalInterceptor. 
 * 
 * Basically pick up your favorite implementation of JTA (Java Transaction API)
 * and access the User Transaction from here. Adjust the transaction according the
 * transaction attribute (required, supported, requiresnew and so on)
 * That it, painless transaction management for your classes.
 * 
 * You can also you the same approach for OJB, Hibernate and similar frameworks. 
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class TransactionalInterceptor extends AbstractInterceptor
{
    /**
     * Document me!
     *  
     * @see org.apache.avalon.fortress.interceptor.Interceptor#intercept(java.lang.Object, org.apache.avalon.fortress.ExtendedMetaInfo, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object intercept(Object instance, ExtendedMetaInfo meta, Method method, Object[] args)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        AttributeInfo transactionMode = 
            meta.getAttributeForMethod( "transaction.required", method );
        
        // TransactionManager.getTransaction().beginTransaction();
        
        Object retValue = null;
        
        try
        {
            retValue = super.intercept(instance, meta, method, args);

            // TransactionManager.getTransaction().commit();
        }
        catch(Exception ex)
        {
            // TransactionManager.getTransaction().rollback();
        }
        
        return retValue;
    }
}
