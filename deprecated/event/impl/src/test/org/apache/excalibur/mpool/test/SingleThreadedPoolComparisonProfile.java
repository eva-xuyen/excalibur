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
package org.apache.excalibur.mpool.test;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.excalibur.mpool.Pool;

/**
 * This is used to profile and compare various pool implementations
 *  given a single access thread.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SingleThreadedPoolComparisonProfile.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public class SingleThreadedPoolComparisonProfile
    extends PoolComparisonProfileAbstract
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public SingleThreadedPoolComparisonProfile( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * PoolComparisonProfileAbstract Methods
     *-------------------------------------------------------------*/
    protected long getPoolRunTime( Pool pool, int gets )
        throws Exception
    {
        // Start clean
        resetMemory();

        final long startTime = System.currentTimeMillis();
        final Object[] poolTmp = new Object[ gets ];
        final int loops = TEST_SIZE / gets;
        for( int i = 0; i < loops; i++ )
        {
            // Get some Poolables
            for( int j = 0; j < gets; j++ )
            {
                poolTmp[ j ] = pool.acquire();
            }

            // Put the Poolables back
            for( int j = 0; j < gets; j++ )
            {
                pool.release( poolTmp[ j ] );
                poolTmp[ j ] = null;
            }
        }
        final long duration = System.currentTimeMillis() - startTime;

        // Dispose if necessary
        if( pool instanceof Disposable )
        {
            ( (Disposable)pool ).dispose();
        }

        return duration;
    }

    /*---------------------------------------------------------------
     * PoolComparisonProfileAbstract Methods
     *-------------------------------------------------------------*/
    protected long getPoolRunTime( org.apache.avalon.excalibur.pool.Pool pool, int gets )
        throws Exception
    {
        // Start clean
        resetMemory();

        final long startTime = System.currentTimeMillis();
        final Poolable[] poolTmp = new Poolable[ gets ];
        final int loops = TEST_SIZE / gets;
        for( int i = 0; i < loops; i++ )
        {
            // Get some Poolables
            for( int j = 0; j < gets; j++ )
            {
                poolTmp[ j ] = pool.get();
            }

            // Put the Poolables back
            for( int j = 0; j < gets; j++ )
            {
                pool.put( poolTmp[ j ] );
                poolTmp[ j ] = null;
            }
        }
        final long duration = System.currentTimeMillis() - startTime;

        // Dispose if necessary
        if( pool instanceof Disposable )
        {
            ( (Disposable)pool ).dispose();
        }

        return duration;
    }
}
