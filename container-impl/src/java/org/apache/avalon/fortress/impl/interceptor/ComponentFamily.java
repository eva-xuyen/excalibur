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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.fortress.interceptor.Interceptor;
import org.apache.avalon.fortress.interceptor.InterceptorManagerException;

/**
 * Holds a interceptor chain for a specific component family.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class ComponentFamily
{
    /**
     * Family name (debug purpose)
     */
    private final String m_familyName;
    
    /**
     * Maps interceptor name to InterceptorHolder instance 
     */
    private final Map m_name2Interceptor;

    /**
     * Used to determine the order of new interception definitions.
     */
    private int m_interceptorsCount;
    
    ///
    /// Constructors
    /// 
    
    /**
     * Constructs a ComponentFamily instance.
     */
    public ComponentFamily( final String family )
    {
        m_name2Interceptor = new TreeMap( String.CASE_INSENSITIVE_ORDER );
        m_interceptorsCount = 0;
        
        m_familyName = family;
    }

    ///
    /// Public implementation
    /// 
    
    /**
     * Adds an interceptor definition. 
     * The inclusion order is very import as it
     * will determine the order of the interceptor chain.
     * 
     * @param interceptorName interceptor's id
     * @param interceptorClassName full class name (must implement the Interceptor interface)
     * @throws InterceptorManagerException if doesn't implements the Interceptor interface
     * @throws ClassNotFoundException if class could not be found (using the getContextClassLoader)
     */
    public void add( final String interceptorName, final String interceptorClassName )
        throws InterceptorManagerException, ClassNotFoundException
    {
        Class interceptorClass = obtainClass( interceptorClassName );
        
        if (!Interceptor.class.isAssignableFrom( interceptorClass ))
        {
            throw new InterceptorManagerException( 
                "Interceptor class specified doesn't implements the Interceptor interface." );
        }
        
        InterceptorHolder holder = null;
        
        if (!m_name2Interceptor.containsKey(interceptorName))
        {
            holder = new InterceptorHolder( interceptorClass, ++m_interceptorsCount );
        }
        else
        {
            InterceptorHolder oldHolder = (InterceptorHolder) m_name2Interceptor.remove( interceptorName );
            holder = new InterceptorHolder( interceptorClass, oldHolder.getOrder() );
        }

        m_name2Interceptor.put( interceptorName, holder );
    }

    /**
     * Removes an interception definition.
     * 
     * @param interceptorName interceptor's id
     */
    public void remove( final String interceptorName )
    {
        // We should not decrement the m_interceptorsCount as it is used 
        // only as a hint to compose the interceptor chain's order.

        m_name2Interceptor.remove( interceptorName );
    }
    
    /**
     * Returns the quantity of valid interceptors 
     * registered at the moment.
     */
    public int interceptorsCount()
    {
        return m_name2Interceptor.size();
    }
    
    /**
     * Returns the family name.
     */
    public String getFamilyName()
    {
        return m_familyName;
    }
    
    /**
     * Returns an array of Interceptors. The instances are not 
     * ready to use, they need to be connected throught the 'init(next)'. 
     * 
     * @return
     */
    public Interceptor[] buildOrderedChain() throws IllegalAccessException, InstantiationException
    {
        InterceptorHolder[] holders = (InterceptorHolder[])
            m_name2Interceptor.values().toArray( new InterceptorHolder[0] );

        Arrays.sort( holders, new Comparator()
        {
            public boolean equals(Object obj)
            {
                return false;
            }

            public int compare(Object o1, Object o2)
            {
                InterceptorHolder h1 = (InterceptorHolder) o1;
                InterceptorHolder h2 = (InterceptorHolder) o2; 
                return h2.getOrder() - h1.getOrder();
            }
        } );
        
        Interceptor[] interceptors = new Interceptor[ holders.length ];
        
        for (int i = 0; i < holders.length; i++)
        {
            InterceptorHolder holder = holders[i];
            interceptors[ i ] = (Interceptor) holder.getInterceptor().newInstance();
        }
        
        return interceptors;
    }

    /**
     * Returns a friendly description of this object.
     */
    public String toString()
    {
        return "ComponentFamily [ " + m_familyName + " ] ";
    }

    /**
     * Use the Context ClassLoader to load the specified class.
     * 
     * @param className
     * @return
     * @throws ClassNotFoundException
     */    
    protected Class obtainClass( final String className ) throws ClassNotFoundException
    {
        return Thread.currentThread().getContextClassLoader().loadClass( className );
    }

    /**
     * Helper class to keep hold an interceptor class and the 
     * order it should fill in the chain.
     */
    private static class InterceptorHolder
    { 
        private final Class m_interceptor;
        private final int m_order;
        
        public InterceptorHolder( Class interceptor, int order )
        {
            m_order = order;
            m_interceptor = interceptor;
        }
        
        public Class getInterceptor()
        {
            return m_interceptor;
        }

        public int getOrder()
        {
            return m_order;
        }
    }
}
