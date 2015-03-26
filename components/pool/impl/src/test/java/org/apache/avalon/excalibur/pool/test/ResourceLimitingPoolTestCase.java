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

import junit.framework.TestCase;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.ResourceLimitingPool;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.1
 */
public final class ResourceLimitingPoolTestCase extends TestCase
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public ResourceLimitingPoolTestCase()
    {
        this( "ResourceLimitingPool Test Case" );
    }

    public ResourceLimitingPoolTestCase( final String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * TestCases
     *-------------------------------------------------------------*/
    public void testCreateDestroy()
    {
        BufferedLogger logger = new BufferedLogger();
        ClassInstanceObjectFactory factory =
            new ClassInstanceObjectFactory( PoolableTestObject.class, logger );
        ResourceLimitingPool pool = new ResourceLimitingPool( factory, 0, false, false, 0, 0 );

        pool.enableLogging( logger );
        pool.dispose();

        // Make sure the logger output check out.
        assertEquals(
            logger.toString(),
            ""
        );
    }

    public void testSingleGetPut() throws Exception
    {
        BufferedLogger logger = new BufferedLogger();
        ClassInstanceObjectFactory factory =
            new ClassInstanceObjectFactory( PoolableTestObject.class, logger );
        ResourceLimitingPool pool = new ResourceLimitingPool( factory, 0, false, false, 0, 0 );

        pool.enableLogging( logger );

        assertEquals( "1) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "1) Pool Size", 0, pool.getSize() );

        Poolable p = pool.get();

        assertEquals( "2) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "2) Pool Size", 1, pool.getSize() );

        pool.put( p );

        assertEquals( "3) Pool Ready Size", 1, pool.getReadySize() );
        assertEquals( "3) Pool Size", 1, pool.getSize() );

        pool.dispose();

        // Make sure the logger output check out.
        assertEquals( "Logger output",
                      "DEBUG - ClassInstanceObjectFactory.newInstance()  id:1\n" +
                      "DEBUG - Created a new org.apache.avalon.excalibur.pool.test.PoolableTestObject from the object factory.\n" +
                      "DEBUG - Got a org.apache.avalon.excalibur.pool.test.PoolableTestObject from the pool.\n" +
                      "DEBUG - Put a org.apache.avalon.excalibur.pool.test.PoolableTestObject back into the pool.\n" +
                      "DEBUG - ClassInstanceObjectFactory.decommission(a org.apache.avalon.excalibur.pool.test.PoolableTestObject)  id:1\n",
                      logger.toString()
        );
    }

    public void testSingleGetPutPoolCheck() throws Exception
    {
        BufferedLogger logger = new BufferedLogger();
        ClassInstanceObjectFactory factory =
            new ClassInstanceObjectFactory( PoolableTestObject.class, logger );
        ResourceLimitingPool pool = new ResourceLimitingPool( factory, 0, false, false, 0, 0 );

        pool.enableLogging( logger );

        assertEquals( "1) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "1) Pool Size", 0, pool.getSize() );

        Poolable p1 = pool.get();

        assertEquals( "2) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "2) Pool Size", 1, pool.getSize() );

        pool.put( p1 );

        assertEquals( "3) Pool Ready Size", 1, pool.getReadySize() );
        assertEquals( "3) Pool Size", 1, pool.getSize() );

        Poolable p2 = pool.get();

        assertEquals( "4) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "4) Pool Size", 1, pool.getSize() );

        assertEquals( "Pooled Object reuse check", p1, p2 );

        pool.put( p2 );

        assertEquals( "5) Pool Ready Size", 1, pool.getReadySize() );
        assertEquals( "5) Pool Size", 1, pool.getSize() );

        pool.dispose();

        // Make sure the logger output check out.
        assertEquals( "Logger output",
                      "DEBUG - ClassInstanceObjectFactory.newInstance()  id:1\n" +
                      "DEBUG - Created a new org.apache.avalon.excalibur.pool.test.PoolableTestObject from the object factory.\n" +
                      "DEBUG - Got a org.apache.avalon.excalibur.pool.test.PoolableTestObject from the pool.\n" +
                      "DEBUG - Put a org.apache.avalon.excalibur.pool.test.PoolableTestObject back into the pool.\n" +
                      "DEBUG - Got a org.apache.avalon.excalibur.pool.test.PoolableTestObject from the pool.\n" +
                      "DEBUG - Put a org.apache.avalon.excalibur.pool.test.PoolableTestObject back into the pool.\n" +
                      "DEBUG - ClassInstanceObjectFactory.decommission(a org.apache.avalon.excalibur.pool.test.PoolableTestObject)  id:1\n",
                      logger.toString()
        );
    }

    public void testMultipleGetPut() throws Exception
    {
        BufferedLogger logger = new BufferedLogger();
        ClassInstanceObjectFactory factory =
            new ClassInstanceObjectFactory( PoolableTestObject.class, logger );
        ResourceLimitingPool pool = new ResourceLimitingPool( factory, 0, false, false, 0, 0 );

        pool.enableLogging( logger );

        assertEquals( "1) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "1) Pool Size", 0, pool.getSize() );

        Poolable p1 = pool.get();

        assertEquals( "2) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "2) Pool Size", 1, pool.getSize() );

        Poolable p2 = pool.get();

        assertEquals( "3) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "3) Pool Size", 2, pool.getSize() );

        pool.put( p1 );

        assertEquals( "4) Pool Ready Size", 1, pool.getReadySize() );
        assertEquals( "4) Pool Size", 2, pool.getSize() );

        pool.put( p2 );

        assertEquals( "5) Pool Ready Size", 2, pool.getReadySize() );
        assertEquals( "5) Pool Size", 2, pool.getSize() );

        pool.dispose();

        // Make sure the logger output check out.
        assertEquals( "Logger output",
                      "DEBUG - ClassInstanceObjectFactory.newInstance()  id:1\n" +
                      "DEBUG - Created a new org.apache.avalon.excalibur.pool.test.PoolableTestObject from the object factory.\n" +
                      "DEBUG - Got a org.apache.avalon.excalibur.pool.test.PoolableTestObject from the pool.\n" +
                      "DEBUG - ClassInstanceObjectFactory.newInstance()  id:2\n" +
                      "DEBUG - Created a new org.apache.avalon.excalibur.pool.test.PoolableTestObject from the object factory.\n" +
                      "DEBUG - Got a org.apache.avalon.excalibur.pool.test.PoolableTestObject from the pool.\n" +
                      "DEBUG - Put a org.apache.avalon.excalibur.pool.test.PoolableTestObject back into the pool.\n" +
                      "DEBUG - Put a org.apache.avalon.excalibur.pool.test.PoolableTestObject back into the pool.\n" +
                      "DEBUG - ClassInstanceObjectFactory.decommission(a org.apache.avalon.excalibur.pool.test.PoolableTestObject)  id:1\n" +
                      "DEBUG - ClassInstanceObjectFactory.decommission(a org.apache.avalon.excalibur.pool.test.PoolableTestObject)  id:2\n",
                      logger.toString()
        );
    }
    
    public void testFailingGets() throws Exception
    {
        BufferedLogger logger = new BufferedLogger();
        ClassInstanceObjectFactory factory =
            new ClassInstanceObjectFactory( FailingPoolableTestObject.class, logger );
        ResourceLimitingPool pool = new ResourceLimitingPool( factory, 3, true, true, 5000, 0 );

        pool.enableLogging( logger );
        Poolable p1;
        
        assertEquals( "1) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "1) Pool Size", 0, pool.getSize() );
        
        try
        {
            p1 = pool.get();
            fail( "1) call to get should have failed." );
        }
        catch ( IllegalStateException e )
        {
            // Expected
        }
        
        assertEquals( "2) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "2) Pool Size", 0, pool.getSize() );
        
        try
        {
            p1 = pool.get();
            fail( "2) call to get should have failed." );
        }
        catch ( IllegalStateException e )
        {
            // Expected
        }
        
        assertEquals( "3) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "3) Pool Size", 0, pool.getSize() );
        
        try
        {
            p1 = pool.get();
            fail( "3) call to get should have failed." );
        }
        catch ( IllegalStateException e )
        {
            // Expected
        }
        
        assertEquals( "4) Pool Ready Size", 0, pool.getReadySize() );
        assertEquals( "4) Pool Size", 0, pool.getSize() );
        
        try
        {
            p1 = pool.get();
            fail( "4) call to get should have failed." );
        }
        catch ( IllegalStateException e )
        {
            // Expected
        }
        
        logger.debug( "OK" );
        
        pool.dispose();
        
        // Make sure the logger output check out.
        assertEquals( "Logger output",
                      "DEBUG - OK\n",
                      logger.toString()
        );
    }
}

