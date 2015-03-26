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
package org.apache.excalibur.mpool;

import java.lang.reflect.Method;

/**
 * The PoolUtil class performs the reflection magic that is necessary to work
 * with the legacy Recyclable interface in the
 * <a href="http://jakarta.apache.org/avalon/excalibur/pool">Pool</a> package.
 * It also works with the new Resettable interface in MPool.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public final class PoolUtil
{
    private final static Object[] EMPTY = new Object[]{};
    private final static Class[] EMPTY_ARGS = new Class[]{};

    private PoolUtil()
    {
    }

    /**
     * This method will either call "reset" on Resettable objects,
     * or it will call "recycle" on Recyclable objects.
     *
     * @param obj  The object you want recycled.
     * @return the same object
     */
    public static Object recycle( final Object obj )
    {
        if( obj instanceof Resettable )
        {
            ( (Resettable)obj ).reset();
        }
        else
        {
            try
            {
                Class klass = obj.getClass();
                Class recyclable = klass.getClassLoader().loadClass( "org.apache.avalon.excalibur.pool.Recyclable" );

                if( recyclable.isAssignableFrom( klass ) )
                {
                    recycleLegacy( obj );
                }
            }
            catch( Exception e )
            {
                // No recyclable interface
            }
        }

        return obj;
    }

    private static void recycleLegacy( final Object obj ) throws Exception
    {
        Class klass = obj.getClass();
        Method recycle = klass.getMethod( "recycle", EMPTY_ARGS );
        recycle.invoke( obj, EMPTY );
    }
}
