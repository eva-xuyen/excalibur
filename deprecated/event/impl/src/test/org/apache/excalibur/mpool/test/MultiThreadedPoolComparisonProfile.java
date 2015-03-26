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

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.excalibur.mpool.Pool;

/**
 * This is used to profile and compare various pool implementations
 *  given a single access thread.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MultiThreadedPoolComparisonProfile.java,v 1.5 2004/04/02 05:53:14 mcconnell Exp $
 */
public class MultiThreadedPoolComparisonProfile
    extends PoolComparisonProfileAbstract
{
    protected static final int THREADS = 100;

    private int m_getCount;
    private Throwable m_throwable;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public MultiThreadedPoolComparisonProfile( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * PoolComparisonProfileAbstract Methods
     *-------------------------------------------------------------*/
    protected long getPoolRunTime( final Pool pool, final int gets )
        throws Exception
    {
        if( gets % THREADS != 0 )
        {
            m_logger.info( "Invalid: " + gets % THREADS + " gets(" + gets + ") threads(" + THREADS + ")" );
            fail( "gets must be evenly divisible by THREADS" );
        }

        m_getCount = 0;
        m_throwable = null;

        // Create the runnable
        MPoolRunner runnable = new MPoolRunner( pool, gets, m_logger );

        LatchedThreadGroup group = new LatchedThreadGroup( runnable, THREADS );
        group.enableLogging( m_logger );

        long duration;
        try
        {
            duration = group.go();
        }
        catch( Throwable t )
        {
            // Throwable could have been thrown by one of the tests.
            if( m_throwable == null )
            {
                m_throwable = t;
            }
            duration = 0;
        }

        if( m_throwable != null )
        {
            throw new CascadingAssertionFailedError( "Exception in test thread.", m_throwable );
        }

        assertTrue( "m_getCount == 0 (" + m_getCount + ")", m_getCount == 0 );

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
    protected long getPoolRunTime( final org.apache.avalon.excalibur.pool.Pool pool, final int gets )
        throws Exception
    {
        if( gets % THREADS != 0 )
        {
            m_logger.info( "Invalid: " + gets % THREADS + " gets(" + gets + ") threads(" + THREADS + ")" );
            fail( "gets must be evenly divisible by THREADS" );
        }

        m_getCount = 0;
        m_throwable = null;

        // Create the runnable
        PoolRunner runnable = new PoolRunner( pool, gets, m_logger );

        LatchedThreadGroup group = new LatchedThreadGroup( runnable, THREADS );
        group.enableLogging( m_logger );

        long duration;
        try
        {
            duration = group.go();
        }
        catch( Throwable t )
        {
            // Throwable could have been thrown by one of the tests.
            if( m_throwable == null )
            {
                m_throwable = t;
            }
            duration = 0;
        }

        if( m_throwable != null )
        {
            throw new CascadingAssertionFailedError( "Exception in test thread.", m_throwable );
        }

        assertTrue( "m_getCount == 0 (" + m_getCount + ")", m_getCount == 0 );

        // Dispose if necessary
        if( pool instanceof Disposable )
        {
            ( (Disposable)pool ).dispose();
        }

        return duration;
    }

    private static class PoolRunner implements Runnable
    {
        private Logger m_logger;
        private org.apache.avalon.excalibur.pool.Pool m_pool;
        private int m_getCount = 0;
        private Throwable m_throwable = null;
        private int m_gets;

        public PoolRunner( org.apache.avalon.excalibur.pool.Pool pool, int gets, Logger logger )
        {
            m_pool = pool;
            m_logger = logger;
            m_gets = gets;
        }

        public int getCount()
        {
            return m_getCount;
        }

        public Throwable getThrowable()
        {
            return m_throwable;
        }

        public void run()
        {
            // Perform this threads part of the test.
            final int cnt = m_gets / THREADS;
            final Poolable[] poolTmp = new Poolable[ cnt ];
            final int loops = ( TEST_SIZE / THREADS ) / cnt;
            for( int i = 0; i < loops; i++ )
            {
                // Get some Poolables
                for( int j = 0; j < cnt; j++ )
                {
                    try
                    {
                        poolTmp[ j ] = m_pool.get();
                        m_getCount++;
                    }
                    catch( Throwable t )
                    {
                        m_logger.error( "Unexpected error", t );

                        if( m_throwable == null )
                        {
                            m_throwable = t;
                        }

                        return;
                    }
                }

                // Make the loops hold the poolables longer than they are released, but only slightly.
                Thread.yield();

                // Put the Poolables back
                for( int j = 0; j < cnt; j++ )
                {
                    m_pool.put( poolTmp[ j ] );
                    m_getCount--;
                    poolTmp[ j ] = null;
                }
            }
        }
    }

    private static class MPoolRunner implements Runnable
    {
        private Logger m_logger;
        private Pool m_pool;
        private int m_getCount = 0;
        private Throwable m_throwable = null;
        private final int m_gets;

        public MPoolRunner( Pool pool, int gets, Logger logger )
        {
            m_pool = pool;
            m_logger = logger;
            m_gets = gets;
        }

        public int getCount()
        {
            return m_getCount;
        }

        public Throwable getThrowable()
        {
            return m_throwable;
        }

        public void run()
        {
            // Perform this threads part of the test.
            final int cnt = m_gets / THREADS;
            final Object[] poolTmp = new Poolable[ cnt ];
            final int loops = ( TEST_SIZE / THREADS ) / cnt;
            for( int i = 0; i < loops; i++ )
            {
                // Get some Poolables
                for( int j = 0; j < cnt; j++ )
                {
                    try
                    {
                        poolTmp[ j ] = m_pool.acquire();
                        m_getCount++;
                    }
                    catch( Throwable t )
                    {
                        m_logger.error( "Unexpected error after " + m_getCount +
                                        " items retrieved and " + m_gets + " requested", t );

                        if( m_throwable == null )
                        {
                            m_throwable = t;
                        }
                        return;
                    }
                }

                // Make the loops hold the poolables longer than they are released, but only slightly.
                Thread.yield();

                // Put the Poolables back
                for( int j = 0; j < cnt; j++ )
                {
                    m_pool.release( poolTmp[ j ] );
                    m_getCount--;
                    poolTmp[ j ] = null;
                }
            }
        }
    }
}
