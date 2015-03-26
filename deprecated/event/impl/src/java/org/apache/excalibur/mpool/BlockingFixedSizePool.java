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
package org.apache.excalibur.mpool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.commons.collections.BoundedFifoBuffer;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUnderflowException;

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 * Please note that this pool offers no resource limiting whatsoever.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 * @since 4.1
 */
public final class BlockingFixedSizePool
    implements Pool, Disposable, Initializable
{
    private boolean m_disposed = false;
    private final Buffer m_buffer;
    private final ObjectFactory m_factory;
    private final long m_timeout;
    private final int m_maxSize;

    /** The semaphor we synchronize on */
    protected final Object m_semaphore = new Object();

    public BlockingFixedSizePool( ObjectFactory factory, int size )
        throws Exception
    {
        this( factory, size, 1000 );
    }

    public BlockingFixedSizePool( ObjectFactory factory, int size, long timeout )
        throws Exception
    {
        m_timeout = ( timeout < 1 ) ? 0 : timeout;
        m_buffer = new BoundedFifoBuffer( size );
        m_maxSize = size;
        m_factory = factory;
    }

    public void initialize()
        throws Exception
    {
        for( int i = 0; i < m_maxSize; i++ )
        {
            m_buffer.add( newInstance() );
        }
    }

    public Object acquire()
    {
        if( m_disposed )
        {
            throw new IllegalStateException( "Cannot get an object from a disposed pool" );
        }

        Object object = null;

        synchronized( m_semaphore )
        {
            if( m_buffer.isEmpty() )
            {
                long blockStart = System.currentTimeMillis();

                if( m_timeout > 0 )
                {
                    long blockWait = m_timeout;

                    do
                    {
                        try
                        {
                            m_semaphore.wait( blockWait );
                        }
                        catch( InterruptedException ie )
                        {
                        }

                        if( m_disposed )
                        {
                            throw new IllegalStateException( "Pool disposed of while waiting for resources to free up" );
                        }

                        if( m_buffer.isEmpty() )
                        {
                            blockWait = m_timeout -
                                ( System.currentTimeMillis() - blockStart );
                        }
                    } while( m_buffer.isEmpty() && blockWait > 0 );
                }
                else
                {
                    do
                    {
                        try
                        {
                            m_semaphore.wait();
                        }
                        catch( InterruptedException ie )
                        {
                        }

                        if( m_disposed )
                        {
                            throw new IllegalStateException( "Pool disposed of while waiting for resources to free up" );
                        }
                    } while( m_buffer.isEmpty() );
                }
            }

            try
            {
                object = m_buffer.remove();
            }
            catch( BufferUnderflowException bufe )
            {
                // ignore exception and leave object as null
            }
        }

        if( object == null )
        {
            throw new IllegalStateException( "Timeout exceeded without acquiring resource." );
        }

        return object;
    }

    public void release( Object object )
    {
        synchronized( m_semaphore )
        {
            if( m_disposed )
            {
                try
                {
                    m_factory.dispose( object );
                }
                catch( Exception e )
                {
                    // We should never get here, but ignore the exception if it happens
                }
            }
            else
            {
                if( m_buffer.size() < m_maxSize )
                {
                    m_buffer.add( PoolUtil.recycle( object ) );
                    m_semaphore.notify();
                }
                else
                {
                    try
                    {
                        m_factory.dispose( object );
                    }
                    catch( Exception e )
                    {
                        // We should never get here, but ignore the exception if it happens
                    }
                }
            }
        }
    }

    public Object newInstance()
        throws Exception
    {
        return m_factory.newInstance();
    }

    public void dispose()
    {
        m_disposed = true;

        synchronized( m_semaphore )
        {
            while( !m_buffer.isEmpty() )
            {
                try
                {
                    m_factory.dispose( m_buffer.remove() );
                }
                catch( Exception e )
                {
                    // We should never get here, but ignore the exception if it happens
                }
            }

            m_semaphore.notifyAll();
        }
    }
}

