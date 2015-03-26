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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.UnboundedFifoBuffer;

import EDU.oswego.cs.dl.util.concurrent.Mutex;

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public abstract class AbstractPool
    extends AbstractLogEnabled
    implements Pool, ThreadSafe
{
    public static final int DEFAULT_POOL_SIZE = 8;
    protected final ObjectFactory m_factory;
    protected List m_active = new ArrayList();
    protected Buffer m_ready = new UnboundedFifoBuffer();
    protected Mutex m_mutex = new Mutex();
    protected boolean m_initialized = false;
    protected int m_min;

    /**
     * Create an AbstractPool.  The pool requires a factory, and can
     * optionally have a controller.
     */
    public AbstractPool( final ObjectFactory factory ) throws Exception
    {
        m_factory = factory;

        if( !( this instanceof Initializable ) )
        {
            initialize();
        }
    }

    protected void initialize()
        throws Exception
    {
        lock();

        for( int i = 0; i < AbstractPool.DEFAULT_POOL_SIZE; i++ )
        {
            this.m_ready.add( this.newPoolable() );
        }

        m_initialized = true;

        unlock();
    }

    protected final void lock()
        throws InterruptedException
    {
        m_mutex.acquire();
    }

    protected final void unlock()
        throws InterruptedException
    {
        m_mutex.release();
    }

    /**
     * This is the method to override when you need to enforce creational
     * policies.
     */
    protected Poolable newPoolable() throws Exception
    {
        Object obj = m_factory.newInstance();
        return (Poolable)obj;
    }

    /**
     * This is the method to override when you need to enforce destructional
     * policies.
     */
    protected void removePoolable( Poolable poolable )
    {
        try
        {
            m_factory.decommission( poolable );
        }
        catch( Exception e )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Error decommissioning object", e );
            }
        }
    }

    public final int size()
    {
        synchronized( this )
        {
            // this is actually not 100% correct as the pool should always
            // reflect the current size (i.e. m_ready.size()) and not the
            // total size.
            return this.m_active.size() + this.m_ready.size();
        }
    }

    public abstract Poolable get() throws Exception;

    public abstract void put( Poolable object );

    protected void internalGrow( final int amount )
        throws Exception
    {
        for( int i = 0; i < amount; i++ )
        {
            try
            {
                m_ready.add( newPoolable() );
            }
            catch( final Exception e )
            {
                if( null != getLogger() && getLogger().isDebugEnabled() )
                {
                    Class createdClass = m_factory.getCreatedClass();
                    if( createdClass == null )
                    {
                        getLogger().debug( "factory created class was null so a new "
                                           + "instance could not be created.", e );
                    }
                    else
                    {
                        getLogger().debug( createdClass.getName() +
                                           ": could not be instantiated.", e );
                    }
                }
                
                throw e;
            }
        }
    }

    protected void internalShrink( final int amount )
        throws Exception
    {
        for( int i = 0; i < amount; i++ )
        {
            if( m_ready.size() > m_min )
            {
                try
                {
                    this.removePoolable( (Poolable)m_ready.remove() );
                }
                catch( final Exception e )
                {
                    if( null != getLogger() && getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( m_factory.getCreatedClass().getName() +
                                           ": improperly decommissioned.", e );
                    }
                }
            }
        }
    }
}
