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
package org.apache.avalon.excalibur.pool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.SingleThreaded;

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public class SingleThreadedPool
    extends AbstractLogEnabled
    implements Pool, Initializable, SingleThreaded, Resizable, Disposable
{
    protected boolean m_initialized;
    protected int m_count;
    protected Poolable[] m_pool;
    protected ObjectFactory m_factory;
    protected PoolController m_controller;
    protected int m_maximum;
    protected int m_initial;

    public SingleThreadedPool( final Class clazz,
                               final int initial,
                               final int maximum ) throws Exception
    {
        this( new DefaultObjectFactory( clazz ), initial, maximum );
    }

    public SingleThreadedPool( final ObjectFactory factory,
                               final int initial,
                               final int maximum ) throws Exception
    {
        this( factory, null, initial, maximum );
    }

    public SingleThreadedPool( final ObjectFactory factory,
                               final PoolController controller,
                               final int initial,
                               final int maximum ) throws Exception
    {
        m_count = 0;
        m_factory = factory;
        m_controller = controller;
        m_maximum = maximum;
        m_initial = initial;
    }

    public void initialize()
        throws Exception
    {
        m_initialized = true;

        grow( m_maximum );
        fill( m_initial );
    }

    /**
     * Retrieve an object from pool.
     *
     * @return an object from Pool
     */
    public Poolable get() throws Exception
    {
        // To make this class backwards compatible, it has to auto initialize if necessary
        if( !m_initialized )
        {
            initialize();
        }

        if( null == m_pool && null != m_controller )
        {
            final int increase = m_controller.grow();
            if( increase > 0 )
            {
                grow( increase );
            }
        }

        if( 0 > m_count )
        {
            m_count = -1;
            return (Poolable)m_factory.newInstance();
        }
        else if( 0 == m_count )
        {
            m_count--;
            return m_pool[ 0 ];
        }

        final Poolable poolable = m_pool[ m_count ];
        m_pool[ m_count ] = null;
        m_count--;
        return poolable;
    }

    /**
     * Place an object in pool.
     *
     * @param poolable the object to be placed in pool
     */
    public void put( final Poolable poolable )
    {
        if( poolable instanceof Recyclable )
        {
            ( (Recyclable)poolable ).recycle();
        }

        if( m_pool.length == ( m_count + 1 ) && null != m_controller )
        {
            final int decrease = m_controller.shrink();
            if( decrease > 0 )
            {
                shrink( decrease );
            }
        }

        if( m_pool.length > m_count + 1 )
        {
            m_count++;
            m_pool[ m_count ] = poolable;
        }
        else
        {
            try
            {
                m_factory.decommission( poolable );
            }
            catch( Exception e )
            {
                // To be backwards compatible, we have to support the logger having not been set.
                if( ( getLogger() != null ) && ( getLogger().isDebugEnabled() ) )
                {
                    getLogger().debug( "Error decommissioning object", e );
                }
            }
        }
    }

    /**
     * Return the total number of slots in Pool
     *
     * @return the total number of slots
     */
    public final int getCapacity()
    {
        return m_pool.length;
    }

    /**
     * Get the number of used slots in Pool
     *
     * @return the number of used slots
     */
    public final int size()
    {
        return m_count;
    }

    /**
     * This fills the pool to the size specified in parameter.
     */
    public final void fill( final int fillSize ) throws Exception
    {
        final int size = Math.min( m_pool.length, fillSize );

        for( int i = m_count; i < size; i++ )
        {
            m_pool[ i ] = (Poolable)m_factory.newInstance();
        }

        m_count = size - 1;
    }

    /**
     * This fills the pool by the size specified in parameter.
     */
    public final void grow( final int increase )
    {
        if( null == m_pool )
        {
            m_pool = new Poolable[ increase ];
            return;
        }

        final Poolable[] poolables = new Poolable[ increase + m_pool.length ];
        System.arraycopy( m_pool, 0, poolables, 0, m_pool.length );
        m_pool = poolables;
    }

    /**
     * This shrinks the pool by parameter size.
     */
    public final void shrink( final int decrease )
    {
        final Poolable[] poolables = new Poolable[ m_pool.length - decrease ];
        System.arraycopy( m_pool, 0, poolables, 0, poolables.length );
        m_pool = poolables;
    }

    /**
     * Dispose the pool and decommission any Poolables.
     */
    public void dispose()
    {
        while( m_count > 0 )
        {
            int i = m_count - 1;
            try
            {
                m_factory.decommission( m_pool[ i ] );
            }
            catch( Exception e )
            {
                // To be backwards compatible, we have to support the logger having not been set.
                if( ( getLogger() != null ) && ( getLogger().isDebugEnabled() ) )
                {
                    getLogger().debug( "Error decommissioning object", e );
                }
            }
            m_pool[ i ] = null;
            m_count--;
        }
    }
}
