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
import java.util.StringTokenizer;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.attributes.AttributeInfo;
import org.apache.avalon.fortress.examples.interceptors.WhoAmI;
import org.apache.avalon.fortress.impl.interceptor.AbstractInterceptor;

/**
 * Sample security interceptor. Checks if the current user have 
 * the necessary role to execute the method.
 * This is just a sample and for the sake of readability
 * it hasn't been optimized in any way.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class SecurityInterceptor extends AbstractInterceptor
{
    /**
     * Checks the required roles and the current user role.
     */
    public Object intercept(Object instance, ExtendedMetaInfo meta, Method method, Object[] args)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        AttributeInfo attribute = meta.getAttributeForMethod( "security.enabled", method );
        
        if (attribute != null)
        {
            boolean canAccess = false;

            final String roles = (String) attribute.getProperties().get( "roles" );
            
            // First lets see if the current user can access
            
            final String currentRole = WhoAmI.instance().getRole();
            
            StringTokenizer tokenizer = new StringTokenizer(roles, ",");
            while( tokenizer.hasMoreTokens() )
            {
                final String token = tokenizer.nextToken();
                
                if (token.equalsIgnoreCase( currentRole ))
                {
                    canAccess = true;
                }
            }
            
            if (!canAccess)
            {
                throw new SecurityException("You don't have ne necessary roles to access this method.");
            }
        }
        
        // Allows the chain to proceed.
        
        return super.intercept(instance, meta, method, args);
    }
}
