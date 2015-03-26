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

package org.apache.avalon.excalibur.datasource;

import java.sql.Connection;

import org.apache.avalon.excalibur.pool.DefaultPoolController;
import org.apache.avalon.excalibur.pool.HardResourceLimitingPool;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;

/**
 * The Pool implementation for JdbcConnections.  It uses a background
 * thread to manage the number of SQL Connections.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/04/02 11:08:12 $
 * @since 4.0
 */
public class JdbcConnectionPool
    extends HardResourceLimitingPool
    implements Runnable, Disposable, Initializable
{
    private Exception m_cause = null;
    private Thread m_initThread;
    private final boolean m_autoCommit;
    private boolean m_noConnections;
    private long m_wait = -1;
    private Object m_spinLock = new Object();

    public JdbcConnectionPool( final JdbcConnectionFactory factory, 
                               final DefaultPoolController controller, 
                               final int min, 
                               final int max, 
                               final boolean autoCommit )
        throws Exception
    {
        super( factory, controller, max );
        m_min = min;
        m_initialized = false;
        m_autoCommit = autoCommit;
    }

    /**
     * Set the timeout in milliseconds for blocking when waiting for a
     * new connection.  It defaults to -1.  Any number below 1 means that there
     * is no blocking, and the Pool fails hard.  Any number above 0 means we
     * will wait for that length of time before failing.
     */
    public void setTimeout( long timeout )
    {
        if( this.m_initialized )
        {
            throw new IllegalStateException( "You cannot change the timeout after the pool is initialized" );
        }

        m_wait = timeout;
    }

    public void initialize()
    {
        m_initThread = new Thread( this );
        m_initThread.start();
    }

    protected final Poolable newPoolable() throws Exception
    {
        PoolSettable conn = null;

        if( m_wait < 1 )
        {
            conn = (PoolSettable)super.newPoolable();
        }
        else
        {
            long curMillis = System.currentTimeMillis();
            long endTime = curMillis + m_wait;
            while( ( null == conn ) && ( curMillis < endTime ) )
            {
                try
                {
                    unlock();
                    curMillis = System.currentTimeMillis();

                    synchronized(m_spinLock)
                    {
                        m_spinLock.wait( endTime - curMillis );
                    }
                }
                finally
                {
                    lock();
                }

                try
                {
                    conn = (PoolSettable)super.newPoolable();
                }
                finally
                {
                    // Do nothing except keep waiting
                }
            }
        }

        if( null == conn )
        {
            throw new NoAvailableConnectionException( "All available connections are in use" );
        }

        conn.setPool( this );
        return conn;
    }

    public Poolable get()
        throws Exception
    {
        if( !m_initialized )
        {
            if( m_noConnections )
            {
                if (m_cause != null) throw m_cause;
                
                throw new IllegalStateException( "There are no connections in the pool, check your settings." );
            }
            else if( m_initThread == null )
            {
                throw new IllegalStateException( "You cannot get a Connection before the pool is initialized." );
            }
            else
            {
                m_initThread.join();
            }
        }

        PoolSettable obj = (PoolSettable)super.get();

        if( obj.isClosed() )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "JdbcConnection was closed, creating one to take its place" );
            }

            try
            {
                lock();
                if( m_active.contains( obj ) )
                {
                    m_active.remove( obj );
                }

                this.removePoolable( obj );

                obj = (PoolSettable)this.newPoolable();

                m_active.add( obj );
            }
            catch( Exception e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Could not get an open connection", e );
                }
                throw e;
            }
            finally
            {
                unlock();
            }
        }

        if( ((Connection)obj).getAutoCommit() != m_autoCommit )
        {
            ((Connection)obj).setAutoCommit( m_autoCommit );
        }

        return obj;
    }

    public void put( Poolable obj )
    {
        super.put( obj );
        synchronized(m_spinLock)
        {
            m_spinLock.notifyAll();
        }
    }

    public void run()
    {
        try
        {
            this.grow( this.m_min );

            if( this.size() > 0 )
            {
                m_initialized = true;
            }
            else
            {
                this.m_noConnections = true;

                if( getLogger().isFatalErrorEnabled() )
                {
                    getLogger().fatalError( "Excalibur could not create any connections.  " +
                                            "Examine your settings to make sure they are correct.  " +
                                            "Make sure you can connect with the same settings on your machine." );
                }
            }
        }
        catch( Exception e )
        {
            m_cause = e;
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Caught an exception during initialization", e );
            }
        }
    }
}
