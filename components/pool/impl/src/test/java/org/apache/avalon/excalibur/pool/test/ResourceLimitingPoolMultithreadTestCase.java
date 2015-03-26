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
 * @version CVS $Revision: 1.8 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.1
 */
public final class ResourceLimitingPoolMultithreadTestCase
    extends TestCase
{
    private static BufferedLogger m_logger;
    private static ClassInstanceObjectFactory m_factory;
    private static ResourceLimitingPool m_pool;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public ResourceLimitingPoolMultithreadTestCase()
    {
        this( "ResourceLimitingPool Multithreaded Test Case" );
    }

    public ResourceLimitingPoolMultithreadTestCase( final String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * Suite
     *-------------------------------------------------------------*/
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Timer timer = new ConstantTimer( 10 );
        int maxUsers = 20;
        int iterations = 50;
        long maxElapsedTime = 20000;

        Test testCase = new ResourceLimitingPoolMultithreadTestCase( "testGetPut" );
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
        m_pool = new ResourceLimitingPool( m_factory, 0, false, false, 0, 0 );

        m_pool.enableLogging( m_logger );
    }

    public static void oneTimeTearDown() throws Exception
    {
        // Dump the logger.
        System.out.println( "Debug output of the logger.  "
            + "This is useful for debugging problems if the test fails." );
        System.out.println( m_logger.toString() );
        System.out.println();
        
        // The current pool does not have a maximum pool size set so the size to which it has
        //  grown will depend greatly on the speed of the machine on which the test is run.
        // The size is well tested in other tests when the max size is fixed.
        int size = m_pool.getSize();
        
        System.out.println( "Final pool size is: " + size );
        System.out.println();
        
        // The ready size should be equal to the pool size at this point.
        assertEquals( "1) Pool Ready Size", size, m_pool.getReadySize() );
        // Any actual pool size is legal
        
        // Get all objects from the pool.
        Poolable[] ps = new Poolable[size];
        for ( int i = 0; i < ps.length; i++ )
        {
            ps[i] = m_pool.get();
        }

        // Make sure that all of the elements were checked out.
        assertEquals( "2) Pool Ready Size", 0, m_pool.getReadySize() );
        assertEquals( "2) Pool Size", size, m_pool.getSize() );
        
        // Iterate over the elements and make sure that they are all unique.  This
        //  is to make sure the pool has not been corrupted.
        for ( int i = 0; i < ps.length; i++ )
        {
            for ( int j = i + 1; j < ps.length; j++ )
            {
                assertTrue( "ps[" + i + "] != ps[" + j + "]", ps[i] != ps[j] );
            }
        }
        
        // Put all the elements back into the pool
        for ( int i = 0; i < ps.length; i++ )
        {
            m_pool.put( ps[i] );
        }

        assertEquals( "3) Pool Ready Size", size, m_pool.getReadySize() );
        assertEquals( "3) Pool Size", size, m_pool.getSize() );

        m_pool.dispose();

        assertEquals( "4) Pool Ready Size", 0, m_pool.getReadySize() );
        assertEquals( "4) Pool Size", 0, m_pool.getSize() );
    }

    /*---------------------------------------------------------------
     * TestCases
     *-------------------------------------------------------------*/
    public void testGetPut() throws Exception
    {
        Poolable p = m_pool.get();
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

