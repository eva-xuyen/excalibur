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

package org.apache.avalon.excalibur.datasource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;

/**
 * The Factory implementation for JdbcConnections.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class JdbcConnectionFactory extends AbstractLogEnabled implements ObjectFactory
{
    private final String m_dburl;
    private final String m_username;
    private final String m_password;
    private final boolean m_autoCommit;
    private final String m_keepAlive;
    private final int m_keepAliveAge;
    private final String m_connectionClass;
    private Class m_class;
    private static final String DEFAULT_KEEPALIVE = "SELECT 1";
    private static final String ORACLE_KEEPALIVE = JdbcConnectionFactory.DEFAULT_KEEPALIVE + " FROM DUAL";
    private Connection m_firstConnection;

    /**
     * @deprecated  Use the new constructor with the keepalive and connectionClass
     *              specified.
     */
    public JdbcConnectionFactory( final String url,
                                  final String username,
                                  final String password,
                                  final boolean autoCommit,
                                  final boolean oradb )
    {
        this( url, username, password, autoCommit, oradb, null );
    }

    /**
     * @deprecated Use the new constructor with the keepalive and connectionClass
     *             specified.
     */
    public JdbcConnectionFactory( final String url,
                                  final String username,
                                  final String password,
                                  final boolean autoCommit,
                                  final boolean oradb,
                                  final String connectionClass )
    {
        this( url, username, password, autoCommit, ( oradb ) ? JdbcConnectionFactory.ORACLE_KEEPALIVE : JdbcConnectionFactory.DEFAULT_KEEPALIVE, connectionClass );
    }

    /**
     * Creates and configures a new JdbcConnectionFactory.
     *
     * @param url full JDBC database url.
     * @param username username to use when connecting to the database.
     * @param password password to use when connecting to the database.
     * @param autoCommit true if connections to the database should operate with auto commit
     *                   enabled.
     * @param keepAlive a query which will be used to check the statis of a connection after it
     *                  has been idle.  A null value will cause the keep alive feature to
     *                  be disabled.
     * @param connectionClass class of connections created by the factory.
     */
    public JdbcConnectionFactory( final String url,
                                  final String username,
                                  final String password,
                                  final boolean autoCommit,
                                  final String keepAlive,
                                  final String connectionClass )
    {
        this( url, username, password, autoCommit, keepAlive, 5000, connectionClass );
    }

    /**
     * Creates and configures a new JdbcConnectionFactory.
     *
     * @param url full JDBC database url.
     * @param username username to use when connecting to the database.
     * @param password password to use when connecting to the database.
     * @param autoCommit true if connections to the database should operate with auto commit
     *                   enabled.
     * @param keepAlive a query which will be used to check the statis of a connection after it
     *                  has been idle.  A null value will cause the keep alive feature to
     *                  be disabled.
     * @param keepAliveAge the maximum age in milliseconds since a connection was last
     *                     used before it must be pinged using the keepAlive query.  Ignored
     *                     if keepAlive is null.
     * @param connectionClass class of connections created by the factory.
     */
    public JdbcConnectionFactory( final String url,
                                  final String username,
                                  final String password,
                                  final boolean autoCommit,
                                  final String keepAlive,
                                  final int keepAliveAge,
                                  final String connectionClass )
    {
        this.m_dburl = url;
        this.m_username = username;
        this.m_password = password;
        this.m_autoCommit = autoCommit;
        this.m_keepAlive = keepAlive;
        this.m_keepAliveAge = keepAliveAge;
        this.m_connectionClass = connectionClass;

        try
        {
            if( null == m_username )
            {
                m_firstConnection = DriverManager.getConnection( m_dburl );
            }
            else
            {
                m_firstConnection = DriverManager.getConnection( m_dburl, m_username, m_password );
            }

            init();
        }
        catch( Exception e )
        {
            // ignore for now
            // No logger here, so we can't log this.  Really should output something here though
            //  as it can be a real pain to track down the cause when this happens.
            //System.out.println( "Unable to get specified connection class: " + e );
        }
    }

    private void init() throws Exception
    {
        String className = m_connectionClass;
        if( null == className )
        {
            m_class = AbstractJdbcConnection.class;
        }
        else
        {
            m_class = Thread.currentThread().getContextClassLoader().loadClass( className );
        }
    }

    public Object newInstance() throws Exception
    {
        Connection jdbcConnection = null;
        Connection connection = m_firstConnection;

        if( null == connection )
        {
            if( null == m_username )
            {
                connection = DriverManager.getConnection( m_dburl );
            }
            else
            {
                connection = DriverManager.getConnection( m_dburl, m_username, m_password );
            }
        }
        else
        {
            m_firstConnection = null;
        }

        if( null == this.m_class )
        {
            try
            {
                init();
            }
            catch( Exception e )
            {
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Exception in JdbcConnectionFactory.newInstance:", e );
                }
                throw new NoValidConnectionException( "No valid JdbcConnection class available" );
            }
        }

        try
        {
            jdbcConnection = getProxy( connection, this.m_keepAlive, this.m_keepAliveAge );
        }
        catch( Exception e )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Exception in JdbcConnectionFactory.newInstance:", e );
            }

            throw new NoValidConnectionException( e.getMessage() );
        }

        ContainerUtil.enableLogging( jdbcConnection, getLogger().getChildLogger( "conn" ) );

        // Not all drivers are friendly to explicitly setting autocommit
        if( jdbcConnection.getAutoCommit() != m_autoCommit )
        {
            jdbcConnection.setAutoCommit( m_autoCommit );
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "JdbcConnection object created" );
        }

        return jdbcConnection;
    }

    public Class getCreatedClass()
    {
        return m_class;
    }

    public void decommission( Object object ) throws Exception
    {
        if( object instanceof Disposable )
        {
            ( (Disposable)object ).dispose();

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "JdbcConnection object disposed" );
            }
        }
    }

    private Connection getProxy( Connection conn, String keepAlive, int keepAliveAge )
    {
        ProxiedJdbcConnection handler = null;

        try
        {
            Constructor builder = m_class.getConstructor( new Class[]{Connection.class,
                                                                      String.class,
                                                                      Integer.TYPE } );
            handler = (ProxiedJdbcConnection)builder.newInstance(
                new Object[]{conn, keepAlive, new Integer( keepAliveAge ) } );
        }
        catch( Exception e )
        {
            final String msg = "Could not create the proper invocation handler, "
                    + "defaulting to AbstractJdbcConnection";
            getLogger().error( msg, e );
            handler = new AbstractJdbcConnection( conn, keepAlive, keepAliveAge );
        }

        final Connection connection = (Connection)Proxy.newProxyInstance(
                m_class.getClassLoader(),
                new Class[]{Connection.class,
                            LogEnabled.class,
                            PoolSettable.class,
                            Disposable.class},
                handler );

        handler.setProxiedConnection( connection );

        return connection;
    }
}
