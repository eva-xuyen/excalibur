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
package org.apache.avalon.excalibur.pool.test;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.excalibur.pool.Pool;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.ResourceLimitingPool;
import org.apache.avalon.excalibur.pool.SingleThreadedPool;
import org.apache.avalon.framework.activity.Disposable;

/**
 * This is used to profile and compare various pool implementations
 *  given a single access thread.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SingleThreadedPoolComparisonProfile.java,v 1.5 2004/03/29 16:50:37 mcconnell Exp $
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
     * SingleThreadedPool vs ResourceLimitingPool TestCases
     *-------------------------------------------------------------*/
    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 10 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_SmallPoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 10, factory );
    }

    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 20 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_SmallPoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 20, factory );
    }

    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 10 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_MediumPoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 10, factory );
    }

    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 20 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_MediumPoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 20, factory );
    }

    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 10 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_LargePoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets10_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 10, factory );
    }

    /**
     * Compare the SingleThreadedPool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a SingleThreadedPool.
     * <p>
     * Test will use pools with a max size of 10, while getting up to 20 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_LargePoolables()
        throws Exception
    {
        String name = "SingleThreadedPool_And_ResourceLimitingPool_Max10_Gets20_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int min = 0;
        int max = 10;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        SingleThreadedPool poolA = new SingleThreadedPool( factory, min, max );
        poolA.enableLogging( m_poolLogger );
        poolA.initialize();

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 20, factory );
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
