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
import java.sql.SQLException;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.ValidatedResourceLimitingPool;

/**
 * A ResourceLimiting JdbcConnectionPool which allows for fine configuration of
 *  how the pool scales to loads.
 *
 * The pool supports; weak and strong pool size limits, optional blocking gets
 *  when connections are not available, and automatic trimming of unused
 *  connections.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.1
 */
public class ResourceLimitingJdbcConnectionPool
    extends ValidatedResourceLimitingPool
{
    private boolean m_autoCommit;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new ResourceLimitingJdbcConnectionPool
     *
     * @param factory The ObjectFactory which will be used to create new connections as needed
     *  by the pool.
     * @param max Maximum number of connections which can be stored in the pool, 0 implies
     *  no limit.
     * @param maxStrict true if the pool should never allow more than max connections to be
     *  created.  Will cause an exception to be thrown if more than max connections are
     *  requested and blocking is false.
     * @param blocking true if the pool should cause a thread calling get() to block when
     *  connections are not currently available in the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused connections will be removed
     *  from the pool.  A value of 0 will cause the pool to never trim old connections.
     * @param autoCommit true if connections created by this pool should have autoCommit enabled.
     */
    public ResourceLimitingJdbcConnectionPool( final ObjectFactory factory,
                                               int max,
                                               boolean maxStrict,
                                               boolean blocking,
                                               long blockTimeout,
                                               long trimInterval,
                                               boolean autoCommit )
    {

        super( factory, max, maxStrict, blocking, blockTimeout, trimInterval );

        m_autoCommit = autoCommit;
    }

    /*---------------------------------------------------------------
     * ValidatedResourceLimitingPool Methods
     *-------------------------------------------------------------*/
    /**
     * Create a new poolable instance by by calling the newInstance method
     *  on the pool's ObjectFactory.
     * This is the method to override when you need to enforce creational
     *  policies.
     * This method is only called by threads that have m_semaphore locked.
     */
    protected Poolable newPoolable() throws Exception
    {
        Object pooledObject = super.newPoolable();
        
        if ( null == pooledObject )
        {
            throw new SQLException("Could not create a connection.");
        }
        
        PoolSettable conn = (PoolSettable)pooledObject;

        // Store a reference to this pool in the connection
        conn.setPool( this );

        // Set the auto commit flag for new connections.
        ((Connection)conn).setAutoCommit( m_autoCommit );

        return conn;
    }

    /**
     * Validates the poolable before it is provided to the caller of get on this pool.
     *  This implementation of the validation method always returns true indicating
     *  that the Poolable is valid.
     * The pool is not locked by the current thread when this method is called.
     *
     * @param poolable The Poolable to be validated
     * @return true if the Poolable is valid, false if it should be removed from the pool.
     */
    protected boolean validatePoolable( Poolable poolable )
    {
        PoolSettable conn = (PoolSettable)poolable;
        try
        {
            // Calling isClosed() may take time if the connection has not been
            //  used for a while.  Is this a problem because the m_semaphore
            //  is currently locked?  I am thinking no because isClosed() will
            //  return immediately when connections are being used frequently.
            if( conn.isClosed() )
            {
                getLogger().debug( "JdbcConnection was closed." );
                return false;
            }
        }
        catch( SQLException e )
        {
            getLogger().debug(
                "Failed to check whether JdbcConnection was closed. " + e.getMessage() );
        }

        return true;
    }
}

