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

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 * Please note that this pool offers no resource limiting whatsoever.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public class DefaultPool
    extends AbstractPool
    implements Disposable
{
    protected int m_min;
    protected int m_max;
    protected PoolController m_controller;
    protected boolean m_disposed = false;
    protected boolean m_quickFail = false;

    public DefaultPool( final ObjectFactory factory,
                        final PoolController controller )
        throws Exception
    {
        this( factory, controller, AbstractPool.DEFAULT_POOL_SIZE, AbstractPool.DEFAULT_POOL_SIZE );
    }

    public DefaultPool( final ObjectFactory factory,
                        final PoolController controller,
                        final int initial,
                        final int maximum )
        throws Exception
    {
        super( factory );

        int t_max = maximum;
        int t_min = initial;

        if( t_min < 0 )
        {
            if( null != getLogger() && getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Minumum number of poolables specified is " +
                                  "less than 0, using 0" );
            }

            t_min = 0;
        }

        if( ( t_max < t_min ) || ( t_max < 1 ) )
        {
            if( null != getLogger() && getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Maximum number of poolables specified must be at " +
                                  "least 1 and must be greater than the minumum number " +
                                  "of connections" );
            }
            t_max = ( t_min > 1 ) ? t_min : 1;
        }

        m_max = t_max;
        m_min = t_min;

        if( null != controller )
        {
            m_controller = controller;
        }
        else
        {
            m_controller = new DefaultPoolController( t_min / 2 );
        }
    }

    public DefaultPool( final ObjectFactory factory )
        throws Exception
    {
        this( factory, null, AbstractPool.DEFAULT_POOL_SIZE, AbstractPool.DEFAULT_POOL_SIZE );
    }

    public DefaultPool( final Class clazz, final int initial, final int maximum )
        throws NoSuchMethodException, Exception
    {
        this( new DefaultObjectFactory( clazz ), null, initial, maximum );
    }

    public DefaultPool( final Class clazz, final int initial )
        throws NoSuchMethodException, Exception
    {
        this( clazz, initial, initial );
    }

    public Poolable get() throws Exception
    {
        Poolable obj = null;

        if( !m_initialized )
        {
            throw new IllegalStateException( "You cannot get a Poolable before the pool is initialized" );
        }

        if( m_disposed )
        {
            throw new IllegalStateException( "You cannot get a Poolable after the pool is disposed" );
        }

        m_mutex.acquire();

        try
        {
            if( m_ready.size() == 0 )
            {
                if( this instanceof Resizable )
                {
                    this.internalGrow( m_controller.grow() );

                    if( m_ready.size() > 0 )
                    {
                        obj = (Poolable)m_ready.remove();
                    }
                    else
                    {
                        final String message =
                            "Could not create enough components to service " +
                            "your request.";
                        throw new Exception( message );
                    }
                }
                else
                {
                    obj = newPoolable();
                }
            }
            else
            {
                obj = (Poolable)m_ready.remove();
            }

            m_active.add( obj );

            if( getLogger().isDebugEnabled() )
            {
                final String message = "Retrieving a " +
                    m_factory.getCreatedClass().getName() + " from the pool";
                getLogger().debug( message );
            }
            return obj;
        }
        finally
        {
            m_mutex.release();
        }
    }

    public void put( final Poolable obj )
    {
        if( !m_initialized )
        {
            final String message = "You cannot put a Poolable before " +
                "the pool is initialized";
            throw new IllegalStateException( message );
        }

        try
        {
            if( obj instanceof Recyclable )
            {
                ( (Recyclable)obj ).recycle();
            }

            m_mutex.acquire();
            try
            {
                if( !m_active.remove(obj) )
                {
                    final String msg = "Attempted to return object that is not in active list: "
                            + obj ;
                    getLogger().warn( msg );
                }

                if( getLogger().isDebugEnabled() )
                {
                    final String message =
                        "Returning a " + m_factory.getCreatedClass().getName() +
                        " to the pool";
                    getLogger().debug( message );
                }

                if( m_disposed == false )
                {
                    m_ready.add( obj );

                    if( ( this.size() > m_max ) && ( this instanceof Resizable ) )
                    {
                        this.internalShrink( m_controller.shrink() );
                    }
                }
                else
                {
                    this.removePoolable( obj );
                }
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Pool interrupted while waiting for lock.", e );
            }
        }
    }

    public final void dispose()
    {
        try
        {
            m_mutex.acquire();
            try
            {
                while( m_ready.size() > 0 )
                {
                    this.removePoolable( (Poolable)m_ready.remove() );
                }
            }
            finally
            {
                m_mutex.release();
            }
        }
        catch( Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Caught an exception disposing of pool", e );
            }
        }

        this.m_disposed = true;
    }
}
