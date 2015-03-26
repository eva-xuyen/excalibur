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
import org.apache.commons.collections.BoundedFifoBuffer;
import org.apache.commons.collections.Buffer;

/**
 * This is an <code>Pool</code> that caches Poolable objects for reuse.
 * Please note that this pool offers no resource limiting whatsoever.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 * @since 4.1
 */
public final class FixedSizePool
    implements Pool, Disposable
{
    private boolean m_disposed = false;
    private final Buffer m_buffer;
    private final ObjectFactory m_factory;

    public FixedSizePool( ObjectFactory factory, int size )
        throws Exception
    {
        m_buffer = new BoundedFifoBuffer( size );
        m_factory = factory;

        for( int i = 0; i < size; i++ )
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

        synchronized( m_buffer )
        {
            object = m_buffer.remove();
        }

        return object;
    }

    public void release( Object object )
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
            synchronized( m_buffer )
            {
                m_buffer.add( PoolUtil.recycle( object ) );
                m_buffer.notifyAll();
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

        synchronized( m_buffer )
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
        }
    }
}

