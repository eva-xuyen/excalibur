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
package org.apache.avalon.excalibur.component.test;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.excalibur.component.PoolableComponentHandler;
import org.apache.avalon.excalibur.testcase.BufferedLogger;
import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;

/**
 * Test the PoolableComponentHandler.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class PoolableComponentHandlerTestCase
    extends ExcaliburTestCase
{
    private Exception m_exception;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public PoolableComponentHandlerTestCase( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/

    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    /**
     * Test the default values and make sure that objects are reused as expected.
     */
    public void testDefaults() throws Exception
    {
        String name = "testDefaults";
        getLogger().info( "Test: " + name );

        int size = PoolableComponentHandler.DEFAULT_MAX_POOL_SIZE + 2;

        BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ size ];

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        // When objects are returned the pool, they are stored in a last in first off list.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" +
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - PoolableTestObject #5 initialized.\n" +
            "DEBUG - PoolableTestObject #6 initialized.\n" +
            "DEBUG - PoolableTestObject #7 initialized.\n" +
            "DEBUG - PoolableTestObject #8 initialized.\n" +
            "DEBUG - PoolableTestObject #9 initialized.\n" +
            "DEBUG - PoolableTestObject #10 initialized.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #1 disposed.\n" + // Still 9 outstanding
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #2 disposed.\n" + // Still 8 outstanding
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #5 recycled.\n" +
            "DEBUG - PoolableTestObject #6 recycled.\n" +
            "DEBUG - PoolableTestObject #7 recycled.\n" +
            "DEBUG - PoolableTestObject #8 recycled.\n" +
            "DEBUG - PoolableTestObject #9 recycled.\n" +
            "DEBUG - PoolableTestObject #10 recycled.\n" +
            "DEBUG - PoolableTestObject #11 initialized.\n" +
            "DEBUG - PoolableTestObject #12 initialized.\n" +
            "DEBUG - PoolableTestObject #10 recycled.\n" + // Gets are in LIFO order.
            "DEBUG - PoolableTestObject #10 disposed.\n" + // Still 9 outstanding
            "DEBUG - PoolableTestObject #9 recycled.\n" +
            "DEBUG - PoolableTestObject #9 disposed.\n" + // Still 8 outstanding
            "DEBUG - PoolableTestObject #8 recycled.\n" +
            "DEBUG - PoolableTestObject #7 recycled.\n" +
            "DEBUG - PoolableTestObject #6 recycled.\n" +
            "DEBUG - PoolableTestObject #5 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #11 recycled.\n" +
            "DEBUG - PoolableTestObject #12 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }
    }

    /**
     * Test a non-default max value.
     */
    public void testMax4() throws Exception
    {
        String name = "testMax4";
        getLogger().info( "Test: " + name );

        int size = 4 + 1;

        BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ size ];

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" +
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - PoolableTestObject #5 initialized.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #1 disposed.\n" + // Still 4 outstanding
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #5 recycled.\n" +
            "DEBUG - PoolableTestObject #6 initialized.\n" +
            "DEBUG - PoolableTestObject #5 recycled.\n" + // Gets are in LIFO order.
            "DEBUG - PoolableTestObject #5 disposed.\n" + // Still 4 outstanding
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #6 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }
    }

    /**
     * Test a non-default max value with a strict max and no blocking
     */
    public void testMax4StrictNoBlocking() throws Exception
    {
        String name = "testMax4StrictNoBlocking";
        getLogger().info( "Test: " + name );

        int size = 4;

        BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ size ];

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Try to get one more.  Should fail.
        try
        {
            lookup( PoolableTestObjectInterface.ROLE + "/" + name );
            fail( "Attempt to get more Pollables than are in the pool should have failed." );
        }
        catch( Exception e )
        {
            // Passed
        }

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" +
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }
    }

    private static class TestMax4StrictBlockingThread extends Thread
    {
        private final ComponentManager manager;
        private final BufferedLogger logger;

        public TestMax4StrictBlockingThread( ComponentManager manager, BufferedLogger logger )
        {
            this.manager = manager;
            this.logger = logger;
        }

        public void run()
        {
            final String name = "testMax4StrictBlocking";
            try
            {
                logger.debug( "Lookup in second thread." );
                PoolableTestObjectInterface poolable = (PoolableTestObjectInterface) manager.lookup( PoolableTestObjectInterface.ROLE + "/" + name );

                // Give the main thread a chance to block
                try
                {
                    Thread.sleep( 500 );
                }
                catch( InterruptedException e )
                {
                }

                logger.debug( "Release in second thread." );
                manager.release( (Component)poolable );
            }
            catch( Exception e )
            {
                e.printStackTrace ();
            }
        }
    }

    /**
     * Test a non-default max value with a strict max and blocking with no timeout
     */
    public void testMax4StrictBlocking() throws Exception
    {
        final String name = "testMax4StrictBlocking";
        getLogger().info( "Test: " + name );

        int size = 3;

        // Initialize the exception field.
        m_exception = null;

        final BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ size ];

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // In another thread, get and release another poolable to cause this one to wait.
        TestMax4StrictBlockingThread secondThread = new TestMax4StrictBlockingThread( manager, logger );
        secondThread.start();

        // Give the second thread a chance to get the 4th poolable
        try
        {
            Thread.sleep( 250 );
        }
        catch( InterruptedException e )
        {
        }

        // Try to get one more.  Should block until the other thread has put it back.
        logger.debug( "Lookup in main thread." );
        PoolableTestObjectInterface poolable =
            (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );

        logger.debug( "Release in main thread." );
        release( (Component)poolable );

        secondThread.join();

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // Make sure that the second thread did not throw an exception
        assertTrue( "Unexpected exception in second thread.", m_exception == null );

        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" +
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - Lookup in second thread.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - Lookup in main thread.\n" +
            "DEBUG - Release in second thread.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - Release in main thread.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }

    }

    /**
     * Test a non-default max value with a strict max and blocking with a timeout
     */
    public void testMax4StrictBlockingTimeout() throws Exception
    {
        String name = "testMax4StrictBlockingTimeout";
        getLogger().info( "Test: " + name );

        int size = 4;

        BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ size ];

        // Lookup the components.
        for( int i = 0; i < size; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }

        // Try to get one more.  Should fail after 500 milliseconds.
        long start = System.currentTimeMillis();
        try
        {
            lookup( PoolableTestObjectInterface.ROLE + "/" + name );
            fail( "Attempt to get more Pollables than are in the pool should have failed." );
        }
        catch( Exception e )
        {
            // Passed
        }
        long dur = System.currentTimeMillis() - start;
        assertTrue( "Block timeout was not within 50 milliseconds of the configured 500 milliseconds,",
                    dur >= 450 && dur <= 550 );

        // Release the components.
        for( int i = 0; i < size; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" +
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }
    }

    /**
     * Test the trimming features.
     */
    public void testTrimming() throws Exception
    {
        String name = "testTrimming";
        getLogger().info( "Test: " + name );

        BufferedLogger logger = new BufferedLogger();
        PoolableTestObject.setStaticLoggger( logger );
        PoolableTestObject.resetInstanceCounter();

        PoolableTestObjectInterface[] poolables = new PoolableTestObjectInterface[ 4 ];

        // Lookup and release all 4 components a couple of times.
        for( int i = 0; i < 4; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }
        for( int i = 0; i < 4; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }
        for( int i = 0; i < 4; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }
        for( int i = 0; i < 4; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // Now wait for 550 ms to trigger a trim on the next lookup.
        try
        {
            Thread.sleep( 550 );
        }
        catch( InterruptedException e )
        {
        }

        // Lookup and release 2 components to mark them as being recently used.
        for( int i = 0; i < 2; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }
        for( int i = 0; i < 2; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }

        // Now wait for 550 ms to trigger a trim on the next lookup.
        try
        {
            Thread.sleep( 550 );
        }
        catch( InterruptedException e )
        {
        }

        // This next get should cause 2 of the components to be trimmed but the 2 we just lookedup
        //  should stay around.
        // Lookup and release all 4 components to see which ones are left.
        for( int i = 0; i < 4; i++ )
        {
            poolables[ i ] =
                (PoolableTestObjectInterface)lookup( PoolableTestObjectInterface.ROLE + "/" + name );
        }
        for( int i = 0; i < 4; i++ )
        {
            release( (Component)poolables[ i ] );
            poolables[ i ] = null;
        }


        // The disposal of the objects will not show up in the log until the component manager is
        //  actually disposed.
        String resultLog = logger.toString();
        String expectedLog =
            "DEBUG - PoolableTestObject #1 initialized.\n" + // First 4 lookups
            "DEBUG - PoolableTestObject #2 initialized.\n" +
            "DEBUG - PoolableTestObject #3 initialized.\n" +
            "DEBUG - PoolableTestObject #4 initialized.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" + // First 4 releases
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" +
            "DEBUG - PoolableTestObject #4 recycled.\n" + // Second 4 releases already existed.
            "DEBUG - PoolableTestObject #3 recycled.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #1 recycled.\n" + // 2 lookups after wait.
            "DEBUG - PoolableTestObject #2 recycled.\n" +
            "DEBUG - PoolableTestObject #4 disposed.\n" + // First lookup after second wait triggers disposal of 2 old Poolables.
            "DEBUG - PoolableTestObject #3 disposed.\n" +
            "DEBUG - PoolableTestObject #5 initialized.\n" + // 4 lookups requred 2 more instances.
            "DEBUG - PoolableTestObject #6 initialized.\n" +
            "DEBUG - PoolableTestObject #2 recycled.\n" + // Final 4 releases
            "DEBUG - PoolableTestObject #1 recycled.\n" +
            "DEBUG - PoolableTestObject #5 recycled.\n" +
            "DEBUG - PoolableTestObject #6 recycled.\n";

        try
        {
            assertEquals( "Log did not contain the expected output.", resultLog, expectedLog );
        }
        catch(junit.framework.ComparisonFailure cf)
        {
            // For clarity.
            System.out.println( "Expected:\n" + expectedLog + "Was:\n" + resultLog);
            throw cf;
        }
    }
}

