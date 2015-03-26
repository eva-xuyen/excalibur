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
package org.apache.avalon.excalibur.pool;

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public class SoftResourceLimitingPool
    extends DefaultPool
    implements Resizable
{
    /**
     * Create an SoftResourceLimitingPool.  The pool requires a factory.
     */
    public SoftResourceLimitingPool( final ObjectFactory factory )
        throws Exception
    {
        this( factory, AbstractPool.DEFAULT_POOL_SIZE / 2 );
    }

    /**
     * Create an SoftResourceLimitingPool.  The pool requires a factory,
     * and can optionally have a controller.
     */
    public SoftResourceLimitingPool( final ObjectFactory factory,
                                     final int min )
        throws Exception
    {
        this( factory, null, min, min * 2 );
    }

    /**
     * Create an SoftResourceLimitingPool.  The pool requires a factory,
     * and can optionally have a controller.
     */
    public SoftResourceLimitingPool( final ObjectFactory factory,
                                     final int min,
                                     final int max ) throws Exception
    {
        this( factory, null, min, max );
    }

    /**
     * Create an SoftResourceLimitingPool.  The pool requires a factory,
     * and can optionally have a controller.
     */
    public SoftResourceLimitingPool( final ObjectFactory factory,
                                     final PoolController controller,
                                     final int min,
                                     final int max )
        throws Exception
    {
        super( factory, controller, min, max );
    }

    public SoftResourceLimitingPool( final Class clazz, final int initial, final int maximum )
        throws NoSuchMethodException, Exception
    {
        this( new DefaultObjectFactory( clazz ), initial, maximum );
    }

    public SoftResourceLimitingPool( final Class clazz, final int initial )
        throws NoSuchMethodException, Exception
    {
        this( clazz, initial, initial );
    }

    public void initialize()
        throws Exception
    {
        this.grow( this.m_min );

        this.m_initialized = true;
    }

    public void grow( final int amount )
    {
        try
        {
            m_mutex.acquire();

            this.internalGrow( amount );
        }
        catch( final InterruptedException ie )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Interrupted while waiting on lock", ie );
            }
        }
        catch( final Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Could not grow the pool properly, an exception was caught", e );
            }
        }
        finally
        {
            m_mutex.release();
        }
    }

    public void shrink( final int amount )
    {
        try
        {
            m_mutex.acquire();

            this.internalShrink( amount );
        }
        catch( final InterruptedException ie )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Interrupted while waiting on lock", ie );
            }
        }
        catch( final Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Could not shrink the pool properly, an exception was caught", e );
            }
        }
        finally
        {
            m_mutex.release();
        }
    }
}
