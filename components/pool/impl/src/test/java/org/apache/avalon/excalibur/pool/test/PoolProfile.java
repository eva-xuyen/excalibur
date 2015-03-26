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

import org.apache.avalon.excalibur.pool.DefaultPool;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.SingleThreadedPool;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * This is used to profile the Pool implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class PoolProfile
    extends TestCase
{
    Logger logger;
    Logger poolLogger;


    public PoolProfile( String name )
    {
        super( name );

        // Set to debug to see more useful information.
        logger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );

        // The output from the pools is too much data to be useful, so use a different logger.
        poolLogger = logger.getChildLogger( "pool" );
    }


    public static class A
        implements Poolable
    {
        int a;
        int b;
        int c;
        float x;
        float y;
        float z;
    }

    public static class B
        implements Poolable
    {
        int a;
        int b;
        int c;
        float x;
        float y;
        float z;
        Object[] o1 = new Object[ 10 ];
        Object[] o2 = new Object[ 10 ];
        Object[] o3 = new Object[ 10 ];

        public void recycle()
        {
            o1 = o2 = o3 = null;
        }
    }

    public static class C
        implements Poolable
    {
        int a;
        int b;
        int c;
        float x;
        float y;
        float z;
        Object[] o1 = new Object[ 100 ];
        Object[] o2 = new Object[ 100 ];
        Object[] o3 = new Object[ 100 ];
        Object[] o4 = new Object[ 100 ];
        Object[] o5 = new Object[ 100 ];
        Object[] o6 = new Object[ 100 ];

        public void recycle()
        {
            o1 = o2 = o3 = o4 = o5 = o6 = null;
        }
    }

    protected static final int TEST_SIZE = 1000000;

    public void testSmallObjects()
        throws Exception
    {
        logger.info( "SMALL Sized Objects" );

        final SingleThreadedPool pool1 = new SingleThreadedPool( A.class, 5, 10 );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool2 = new SingleThreadedPool( A.class, 5, 10 );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;
        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a0 = pool2.get();
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a0 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool3 = new SingleThreadedPool( A.class, 5, 10 );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;
        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool4 = new SingleThreadedPool( A.class, 5, 10 );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;
        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 4: " + Runtime.getRuntime().freeMemory() );

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }

    public void testMediumObjects()
        throws Exception
    {
        logger.info( "MEDIUM Sized Objects" );

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        final SingleThreadedPool pool1 = new SingleThreadedPool( B.class, 5, 10 );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool2 = new SingleThreadedPool( B.class, 5, 10 );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;

        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a10 = pool2.get();
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a10 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool3 = new SingleThreadedPool( B.class, 5, 10 );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;

        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool4 = new SingleThreadedPool( B.class, 5, 10 );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;

        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 4: " + Runtime.getRuntime().freeMemory() );

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "Create Duration: " + createDuration + "ms " );
        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }

    public void testLargeObjects()
        throws Exception
    {
        logger.info( "LARGE Sized Objects" );

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        final SingleThreadedPool pool1 = new SingleThreadedPool( C.class, 5, 10 );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool2 = new SingleThreadedPool( C.class, 5, 10 );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;
        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a10 = pool2.get();
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a10 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool3 = new SingleThreadedPool( C.class, 5, 10 );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;
        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final SingleThreadedPool pool4 = new SingleThreadedPool( C.class, 5, 10 );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;

        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 4: " + Runtime.getRuntime().freeMemory() );

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }

    public void testThreadedSmallObjects()
        throws Exception
    {
        logger.info( "SMALL Sized Objects with thread safe pools" );

        final DefaultPool pool1 = new DefaultPool( A.class, 5, 10 );
        pool1.enableLogging( poolLogger );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool2 = new DefaultPool( A.class, 5, 10 );
        pool2.enableLogging( poolLogger );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;

        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a0 = pool2.get();
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a0 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool3 = new DefaultPool( A.class, 5, 10 );
        pool3.enableLogging( poolLogger );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;

        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool4 = new DefaultPool( A.class, 5, 10 );
        pool4.enableLogging( poolLogger );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;

        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 4: " + Runtime.getRuntime().freeMemory() );

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "Create Duration: " + createDuration + "ms " );
        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }

    public void testThreadedMediumObjects()
        throws Exception
    {
        logger.info( "MEDIUM Sized Objects with thread safe pools" );

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        final DefaultPool pool1 = new DefaultPool( B.class, 5, 10 );
        pool1.enableLogging( poolLogger );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool2 = new DefaultPool( B.class, 5, 10 );
        pool2.enableLogging( poolLogger );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;

        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a10 = pool2.get();
            /*
              a1.build();
              a2.build();
              a3.build();
              a4.build();
              a5.build();
              a6.build();
              a7.build();
              a8.build();
              a9.build();
              a10.build();
            */
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a10 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool3 = new DefaultPool( B.class, 5, 10 );
        pool3.enableLogging( poolLogger );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;

        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            /*
              a1.build();
              a2.build();
              a3.build();
              a4.build();
              a5.build();
              a6.build();
              a7.build();
              a8.build();
              a9.build();
              a10.build();
              a11.build();
              a12.build();
              a13.build();
              a14.build();
              a15.build();
            */
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool4 = new DefaultPool( B.class, 5, 10 );
        pool4.enableLogging( poolLogger );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;

        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            /*
              a1.build();
              a2.build();
              a3.build();
              a4.build();
              a5.build();
              a6.build();
              a7.build();
              a8.build();
              a9.build();
              a10.build();
              a11.build();
              a12.build();
              a13.build();
              a14.build();
              a15.build();
              a16.build();
              a17.build();
              a18.build();
              a19.build();
              a20.build();
            */
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "Create Duration: " + createDuration + "ms " );
        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }

    public void testThreadedLargeObjects()
        throws Exception
    {
        logger.info( "LARGE Sized Objects with thread safe pools" );

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        final DefaultPool pool1 = new DefaultPool( C.class, 5, 10 );
        pool1.enableLogging( poolLogger );
        final long pool1Start = System.currentTimeMillis();

        for( int i = 0; i < TEST_SIZE; i++ )
        {
            final Poolable a1 = pool1.get();
            //a1.build();
            pool1.put( a1 );
        }
        final long pool1End = System.currentTimeMillis();
        final long pool1Duration = pool1End - pool1Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 1: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool2 = new DefaultPool( C.class, 5, 10 );
        pool2.enableLogging( poolLogger );
        final long pool2Start = System.currentTimeMillis();
        final int pool2Factor = 10;
        final int pool2Loops = TEST_SIZE / pool2Factor;

        for( int i = 0; i < pool2Loops; i++ )
        {
            final Poolable a1 = pool2.get();
            final Poolable a2 = pool2.get();
            final Poolable a3 = pool2.get();
            final Poolable a4 = pool2.get();
            final Poolable a5 = pool2.get();
            final Poolable a6 = pool2.get();
            final Poolable a7 = pool2.get();
            final Poolable a8 = pool2.get();
            final Poolable a9 = pool2.get();
            final Poolable a10 = pool2.get();
            /*
              a1.build();
              a2.build();
              a3.build();
              a4.build();
              a5.build();
              a6.build();
              a7.build();
              a8.build();
              a9.build();
              a10.build();
            */
            pool2.put( a1 );
            pool2.put( a2 );
            pool2.put( a3 );
            pool2.put( a4 );
            pool2.put( a5 );
            pool2.put( a6 );
            pool2.put( a7 );
            pool2.put( a8 );
            pool2.put( a9 );
            pool2.put( a10 );
        }
        final long pool2End = System.currentTimeMillis();
        final long pool2Duration = pool2End - pool2Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 2: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool3 = new DefaultPool( C.class, 5, 10 );
        pool3.enableLogging( poolLogger );
        final long pool3Start = System.currentTimeMillis();
        final int pool3Factor = 15;
        final int pool3Loops = TEST_SIZE / pool3Factor;

        for( int i = 0; i < pool3Loops; i++ )
        {
            final Poolable a1 = pool3.get();
            final Poolable a2 = pool3.get();
            final Poolable a3 = pool3.get();
            final Poolable a4 = pool3.get();
            final Poolable a5 = pool3.get();
            final Poolable a6 = pool3.get();
            final Poolable a7 = pool3.get();
            final Poolable a8 = pool3.get();
            final Poolable a9 = pool3.get();
            final Poolable a10 = pool3.get();
            final Poolable a11 = pool3.get();
            final Poolable a12 = pool3.get();
            final Poolable a13 = pool3.get();
            final Poolable a14 = pool3.get();
            final Poolable a15 = pool3.get();
            /*
              a1.build();
              a2.build();
              a3.build();
              a4.build();
              a5.build();
              a6.build();
              a7.build();
              a8.build();
              a9.build();
              a10.build();
              a11.build();
              a12.build();
              a13.build();
              a14.build();
              a15.build();
            */
            pool3.put( a1 );
            pool3.put( a2 );
            pool3.put( a3 );
            pool3.put( a4 );
            pool3.put( a5 );
            pool3.put( a6 );
            pool3.put( a7 );
            pool3.put( a8 );
            pool3.put( a9 );
            pool3.put( a10 );
            pool3.put( a11 );
            pool3.put( a12 );
            pool3.put( a13 );
            pool3.put( a14 );
            pool3.put( a15 );
        }
        final long pool3End = System.currentTimeMillis();
        final long pool3Duration = pool3End - pool3Start;

        System.gc();
        System.gc();
        Thread.sleep( 2 );

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 3: " + Runtime.getRuntime().freeMemory() );

        final DefaultPool pool4 = new DefaultPool( C.class, 5, 10 );
        pool4.enableLogging( poolLogger );
        final long pool4Start = System.currentTimeMillis();
        final int pool4Factor = 20;
        final int pool4Loops = TEST_SIZE / pool4Factor;

        for( int i = 0; i < pool4Loops; i++ )
        {
            final Poolable a1 = pool4.get();
            final Poolable a2 = pool4.get();
            final Poolable a3 = pool4.get();
            final Poolable a4 = pool4.get();
            final Poolable a5 = pool4.get();
            final Poolable a6 = pool4.get();
            final Poolable a7 = pool4.get();
            final Poolable a8 = pool4.get();
            final Poolable a9 = pool4.get();
            final Poolable a10 = pool4.get();
            final Poolable a11 = pool4.get();
            final Poolable a12 = pool4.get();
            final Poolable a13 = pool4.get();
            final Poolable a14 = pool4.get();
            final Poolable a15 = pool4.get();
            final Poolable a16 = pool4.get();
            final Poolable a17 = pool4.get();
            final Poolable a18 = pool4.get();
            final Poolable a19 = pool4.get();
            final Poolable a20 = pool4.get();
            pool4.put( a1 );
            pool4.put( a2 );
            pool4.put( a3 );
            pool4.put( a4 );
            pool4.put( a5 );
            pool4.put( a6 );
            pool4.put( a7 );
            pool4.put( a8 );
            pool4.put( a9 );
            pool4.put( a10 );
            pool4.put( a11 );
            pool4.put( a12 );
            pool4.put( a13 );
            pool4.put( a14 );
            pool4.put( a15 );
            pool4.put( a16 );
            pool4.put( a17 );
            pool4.put( a18 );
            pool4.put( a19 );
            pool4.put( a20 );
        }
        final long pool4End = System.currentTimeMillis();
        final long pool4Duration = pool4End - pool4Start;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post 4: " + Runtime.getRuntime().freeMemory() );

        final long createStart = System.currentTimeMillis();
        for( int i = 0; i < TEST_SIZE; i++ )
        {
            new C();
        }
        final long createEnd = System.currentTimeMillis();
        final long createDuration = createEnd - createStart;

        if( logger.isDebugEnabled() ) logger.debug( "FreeMem post create: " + Runtime.getRuntime().freeMemory() );

        final double pool1Efficiency = (double)createDuration / (double)pool1Duration * 100.0;
        final double pool2Efficiency = (double)createDuration / (double)pool2Duration * 100.0;
        final double pool3Efficiency = (double)createDuration / (double)pool3Duration * 100.0;
        final double pool4Efficiency = (double)createDuration / (double)pool4Duration * 100.0;

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits: " + pool1Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool1Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 100% hits and saturated: " + pool2Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 100% hits: " + ( createDuration - pool2Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 60% hits: " + pool3Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 60% hits: " + ( createDuration - pool3Duration ) + "ms " );

        if( logger.isDebugEnabled() ) logger.debug( "Pool Efficiency for 50% hits: " + pool4Efficiency + "% " );
        if( logger.isInfoEnabled() ) logger.info( "Time Saved for 50% hits: " + ( createDuration - pool4Duration ) + "ms " );
    }
}
