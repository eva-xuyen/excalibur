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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.ResourceLimitingPool;

import com.clarkware.junitperf.ConstantTimer;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TimedTest;
import com.clarkware.junitperf.Timer;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.1
 */
public final class ResourceLimitingPoolMultithreadMaxStrictTestCase extends TestCase
{
    private static BufferedLogger m_logger;
    private static ClassInstanceObjectFactory m_factory;
    private static ResourceLimitingPool m_pool;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public ResourceLimitingPoolMultithreadMaxStrictTestCase()
    {
        this( "ResourceLimitingPool Multithreaded Max Size Strict Test Case" );
    }

    public ResourceLimitingPoolMultithreadMaxStrictTestCase( final String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * Suite
     *-------------------------------------------------------------*/
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Timer timer = new ConstantTimer( 100 );
        int maxUsers = 50;
        int iterations = 10;
        long maxElapsedTime = 20000;

        Test testCase = new ResourceLimitingPoolMultithreadMaxStrictTestCase( "testGetPut" );
        Test loadTest = new LoadTest( testCase, maxUsers, iterations, timer );
        Test timedTest = new TimedTest( loadTest, maxElapsedTime );
        suite.addTest( timedTest );

        TestSetup wrapper = new TestSetup( suite )
        {
            public void setUp()
            {
                oneTimeSetUp();
            }

            public void tearDown() throws Exception
            {
                oneTimeTearDown();
            }
        };

        return wrapper;
    }

    public static void oneTimeSetUp()
    {
        m_logger = new BufferedLogger();
        m_factory = new ClassInstanceObjectFactory( PoolableTestObject.class, m_logger );
        m_pool = new ResourceLimitingPool( m_factory, 3, true, false, 0, 0 );

        m_pool.enableLogging( m_logger );
    }

    public static void oneTimeTearDown() throws Exception
    {
        // The timing of this test makes it so the pool should grow to 4 elements
        assertEquals( "1) Pool Ready Size", 3, m_pool.getReadySize() );
        assertEquals( "1) Pool Size", 3, m_pool.getSize() );

        // Make sure that each of the objects are uniqe by checking them all back out.
        Poolable p1 = m_pool.get();
        Poolable p2 = m_pool.get();
        Poolable p3 = m_pool.get();

        assertEquals( "2) Pool Ready Size", 0, m_pool.getReadySize() );
        assertEquals( "2) Pool Size", 3, m_pool.getSize() );

        assertTrue( "p1 != p2", p1 != p2 );
        assertTrue( "p1 != p3", p1 != p3 );
        assertTrue( "p2 != p3", p2 != p3 );

        m_pool.put( p1 );
        m_pool.put( p2 );
        m_pool.put( p3 );

        assertEquals( "3) Pool Ready Size", 3, m_pool.getReadySize() );
        assertEquals( "3) Pool Size", 3, m_pool.getSize() );

        m_pool.dispose();

        assertEquals( "4) Pool Ready Size", 0, m_pool.getReadySize() );
        assertEquals( "4) Pool Size", 0, m_pool.getSize() );
    }

    /*---------------------------------------------------------------
     * TestCases
     *-------------------------------------------------------------*/
    public void testGetPut() throws Exception
    {
        Poolable p;
        try
        {
            p = m_pool.get();
        }
        catch( Exception e )
        {
            // We will sometimes fail to get an object
            assertEquals( "Exception calling get()",
                          "Could not create enough Components to service your request.",
                          e.getMessage() );
            return;
        }

        // We can't check the exact pool size, but make sure that it never gets over 3
        assertTrue( "Pool size <= 3", m_pool.getSize() <= 3 );

        try
        {
            Thread.sleep( 33 );
        }
        catch( InterruptedException e )
        {
        }
        m_pool.put( p );
    }
}

