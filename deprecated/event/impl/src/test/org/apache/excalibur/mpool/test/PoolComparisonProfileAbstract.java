/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.excalibur.mpool.test;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.pool.ResourceLimitingPool;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.excalibur.mpool.BlockingFixedSizePool;
import org.apache.excalibur.mpool.FixedSizePool;
import org.apache.excalibur.mpool.ObjectFactory;
import org.apache.excalibur.mpool.Pool;
import org.apache.excalibur.mpool.VariableSizePool;

/**
 * Used as a basis for the PoolComparisonProfile Tests
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PoolComparisonProfileAbstract.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public abstract class PoolComparisonProfileAbstract
    extends TestCase
{
    /**
     * The TEST_SIZE defines the overall size of the tests.  Decreasing this will
     *  decrease the time the test takes to run, but also decrease its efficiency.
     */
    protected static final int TEST_SIZE = 50000;

    protected Logger m_logger;
    protected Logger m_poolLogger;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public PoolComparisonProfileAbstract( String name )
    {
        super( name );

        // Set to debug to see more useful information.
        org.apache.log.Logger logger =
            org.apache.log.Hierarchy.getDefaultHierarchy().getLoggerFor( "test" );
        logger.setPriority( org.apache.log.Priority.INFO );
        m_logger = new LogKitLogger( logger );

        // The output from the pools is too much data to be useful, so use a different logger.
        org.apache.log.Logger poolLogger =
            org.apache.log.Hierarchy.getDefaultHierarchy().getLoggerFor( "pool" );
        poolLogger.setPriority( org.apache.log.Priority.INFO );
        m_poolLogger = new LogKitLogger( poolLogger );
    }

    /*---------------------------------------------------------------
     * FixedSizePool vs ResourceLimitingPool TestCases
     *-------------------------------------------------------------*/
    /**
     * Compare the FixedSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_FixedSizePool_And_ResourceLimitingPool_Max100_Gets100_SmallPoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_ResourceLimitingPool_Max100_Gets100_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = true;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        FixedSizePool poolA = new FixedSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and BlockingFixedSizePool when the
     *  BlockingFixedSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_SmallPoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        long blockTimeout = 1;

        FixedSizePool poolA = new FixedSizePool( factory, max );
        BlockingFixedSizePool poolB = new BlockingFixedSizePool( factory, max, blockTimeout );
        poolB.initialize();

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and BlockingFixedSizePool when the
     *  BlockingFixedSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_MediumPoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        long blockTimeout = 1;

        FixedSizePool poolA = new FixedSizePool( factory, max );
        BlockingFixedSizePool poolB = new BlockingFixedSizePool( factory, max, blockTimeout );
        poolB.initialize();

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and BlockingFixedSizePool when the
     *  BlockingFixedSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_LargePoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_BlockingFixedSizePool_Max100_Gets100_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        long blockTimeout = 1;

        FixedSizePool poolA = new FixedSizePool( factory, max );
        BlockingFixedSizePool poolB = new BlockingFixedSizePool( factory, max, blockTimeout );
        poolB.initialize();

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_FixedSizePool_And_ResourceLimitingPool_Max100_Gets100_LargePoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_ResourceLimitingPool_Max100_Gets100_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = true;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        FixedSizePool poolA = new FixedSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /*---------------------------------------------------------------
     * FixedSizePool vs VariableSizePool TestCases
     *-------------------------------------------------------------*/
    /**
     * Compare the FixedSizePool and VariableSizePool when the
     *  VariableSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_FixedSizePool_And_VariableSizePool_Max100_Gets100_SmallPoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_VariableSizePool_Max100_Gets100_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;

        FixedSizePool poolA = new FixedSizePool( factory, max );

        VariableSizePool poolB = new VariableSizePool( factory, max );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and VariableSizePool when the
     *  VariableSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_FixedSizePool_And_VariableSizePool_Max100_Gets100_MediumPoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_VariableSizePool_Max100_Gets100_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;

        FixedSizePool poolA = new FixedSizePool( factory, max );

        VariableSizePool poolB = new VariableSizePool( factory, max );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the FixedSizePool and VariableSizePool when the
     *  VariableSizePool is configured to act like a FixedSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_FixedSizePool_And_VariableSizePool_Max100_Gets100_LargePoolables()
        throws Exception
    {
        String name = "FixedSizePool_And_VariableSizePool_Max100_Gets100_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;

        FixedSizePool poolA = new FixedSizePool( factory, max );

        VariableSizePool poolB = new VariableSizePool( factory, max );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /*---------------------------------------------------------------
     * Test Classes
     *-------------------------------------------------------------*/
    public static class SmallPoolable
        implements org.apache.avalon.excalibur.pool.Poolable
    {
        int a;
    }

    public static class MediumPoolable
        implements org.apache.avalon.excalibur.pool.Poolable
    {
        int[] a = new int[ 100 ];
    }

    public static class LargePoolable
        implements org.apache.avalon.excalibur.pool.Poolable
    {
        int[][] a = new int[ 10 ][ 100 ];
    }

    /**
     * Dummy class used for timing test cases where no pooling is done.
     */
    public static class NoPoolingPool
        implements Pool, LogEnabled
    {
        private ObjectFactory m_factory;
        private Logger m_logger;

        public NoPoolingPool( ObjectFactory factory )
        {
            m_factory = factory;
        }

        public void enableLogging( Logger logger )
        {
            m_logger = logger;
        }

        public Object acquire() throws Exception
        {
            return newInstance();
        }

        public void release( Object poolable )
        {
            try
            {
                m_factory.dispose( poolable );
            }
            catch( Exception e )
            {
                m_logger.debug( "Error decommissioning object", e );
            }
        }

        public Object newInstance() throws Exception
        {
            return m_factory.newInstance();
        }
    }

    /*---------------------------------------------------------------
     * VariableSizePool vs ResourceLimitingPool TestCases
     *-------------------------------------------------------------*/
    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets100_SmallPoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max100_Gets100_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 200 at a time,
     *  Poolables are small objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_SmallPoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_SmallPoolables";

        Class poolableClass = SmallPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 200, factory );
    }

    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets100_MediumPoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max10_Gets100_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 200 at a time,
     *  Poolables are medium objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_MediumPoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_MediumPoolables";

        Class poolableClass = MediumPoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 200, factory );
    }

    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 100 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets100_LargePoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max100_Gets100_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 100, factory );
    }

    /**
     * Compare the VariableSizePool and ResourceLimitingPool when the
     *  ResourceLimitingPool is configured to act like a VariableSizePool.
     * <p>
     * Test will use pools with a max size of 100, while getting up to 200 at a time,
     *  Poolables are large objects.
     */
    public void testCompare_VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_LargePoolables()
        throws Exception
    {
        String name = "VariableSizePool_And_ResourceLimitingPool_Max100_Gets200_LargePoolables";

        Class poolableClass = LargePoolable.class;
        ClassInstanceObjectFactory factory = new ClassInstanceObjectFactory( poolableClass, m_poolLogger );
        int max = 100;
        boolean maxStrict = false;
        boolean blocking = false;
        long blockTimeout = 0;
        long trimInterval = 0;

        VariableSizePool poolA = new VariableSizePool( factory, max );

        ResourceLimitingPool poolB = new ResourceLimitingPool( factory, max, maxStrict, blocking, blockTimeout, trimInterval );
        poolB.enableLogging( m_poolLogger );

        generalTest( name, poolA, poolB, 200, factory );
    }

    /*---------------------------------------------------------------
     * Utility Methods
     *-------------------------------------------------------------*/
    protected void resetMemory()
    {
        System.gc();
        System.gc();

        // Let the system settle down.
        try
        {
            Thread.sleep( 50 );
        }
        catch( InterruptedException e )
        {
        }
        Runtime runtime = Runtime.getRuntime();
        m_logger.debug( "Memory: " + ( runtime.totalMemory() - runtime.freeMemory() ) );
    }

    protected String getShortClassName( Object o )
    {
        String name = o.getClass().getName();
        int pos = name.lastIndexOf( '.' );
        if( pos > 0 )
        {
            name = name.substring( pos + 1 );
        }
        return name;
    }

    protected abstract long getPoolRunTime( Pool pool, int gets )
        throws Exception;

    protected abstract long getPoolRunTime( org.apache.avalon.excalibur.pool.Pool pool, int gets )
        throws Exception;

    /**
     * The guts of the various test cases.  Will dispose the pools
     */
    protected void generalTest( String name, Pool poolA, Pool poolB, int gets, ClassInstanceObjectFactory factory )
        throws Exception
    {
        m_logger.info( "Test Case: " + name );

        // Get the short class names
        final String poolAName = getShortClassName( poolA );
        final String poolBName = getShortClassName( poolB );

        // Start clean
        resetMemory();

        // Get a baseline speed for object creation
        NoPoolingPool poolBase = new NoPoolingPool( factory );
        poolBase.enableLogging( m_poolLogger );
        final long noPoolDuration = getPoolRunTime( poolBase, gets );
        m_logger.info( "     Unpooled time = " + noPoolDuration + "ms. to use " + TEST_SIZE + " objects." );
        resetMemory();


        // Get the time for poolA
        final long poolADuration = getPoolRunTime( poolA, gets );
        m_logger.info( "     " + poolAName + " time = " + poolADuration + "ms. to use " + TEST_SIZE + " objects, " + gets + " at a time." );
        resetMemory();


        // Get the time for poolB
        final long poolBDuration = getPoolRunTime( poolB, gets );
        m_logger.info( "     " + poolBName + " time = " + poolBDuration + "ms. to use " + TEST_SIZE + " objects, " + gets + " at a time." );
        resetMemory();

        // Show a summary
        if( m_logger.isInfoEnabled() )
        {
            double mult;
            mult = ( poolADuration > 0 ? ( noPoolDuration * 100 / poolADuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolAName + " is " + mult + " X as fast as not pooling." );

            mult = ( poolBDuration > 0 ? ( noPoolDuration * 100 / poolBDuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolBName + " is " + mult + " X as fast as not pooling." );

            mult = ( poolBDuration > 0 ? ( poolADuration * 100 / poolBDuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolBName + " is " + mult + " X as fast as " + poolAName + "." );

            mult = ( poolADuration > 0 ? ( poolBDuration * 100 / poolADuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolAName + " is " + mult + " X as fast as " + poolBName + "." );
        }
    }

    /**
     * The guts of the various test cases.  Will dispose the pools
     */
    protected void generalTest( String name, Pool poolA, org.apache.avalon.excalibur.pool.Pool poolB, int gets, ClassInstanceObjectFactory factory )
        throws Exception
    {
        m_logger.info( "Test Case: " + name );

        // Get the short class names
        final String poolAName = getShortClassName( poolA );
        final String poolBName = getShortClassName( poolB );

        // Start clean
        resetMemory();

        // Get a baseline speed for object creation
        NoPoolingPool poolBase = new NoPoolingPool( factory );
        poolBase.enableLogging( m_poolLogger );
        final long noPoolDuration = getPoolRunTime( poolBase, gets );
        m_logger.info( "     Unpooled time = " + noPoolDuration + "ms. to use " + TEST_SIZE + " objects." );
        resetMemory();


        // Get the time for poolA
        final long poolADuration = getPoolRunTime( poolA, gets );
        m_logger.info( "     " + poolAName + " time = " + poolADuration + "ms. to use " + TEST_SIZE + " objects, " + gets + " at a time." );
        resetMemory();


        // Get the time for poolB
        final long poolBDuration = getPoolRunTime( poolB, gets );
        m_logger.info( "     " + poolBName + " time = " + poolBDuration + "ms. to use " + TEST_SIZE + " objects, " + gets + " at a time." );
        resetMemory();

        // Show a summary
        if( m_logger.isInfoEnabled() )
        {
            double mult;
            mult = ( poolADuration > 0 ? ( noPoolDuration * 100 / poolADuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolAName + " is " + mult + " X as fast as not pooling." );

            mult = ( poolBDuration > 0 ? ( noPoolDuration * 100 / poolBDuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolBName + " is " + mult + " X as fast as not pooling." );

            mult = ( poolBDuration > 0 ? ( poolADuration * 100 / poolBDuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolBName + " is " + mult + " X as fast as " + poolAName + "." );

            mult = ( poolADuration > 0 ? ( poolBDuration * 100 / poolADuration ) / 100.0 : Float.POSITIVE_INFINITY );
            m_logger.info( "  => " + poolAName + " is " + mult + " X as fast as " + poolBName + "." );
        }
    }
}
