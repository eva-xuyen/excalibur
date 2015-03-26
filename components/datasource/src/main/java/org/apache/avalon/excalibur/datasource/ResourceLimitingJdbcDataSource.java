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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;

import org.apache.avalon.excalibur.pool.TraceableResourceLimitingPool;

/**
 * The ResourceLimiting implementation for DataSources in Avalon.
 * This uses the normal <code>java.sql.Connection</code> object and
 * <code>java.sql.DriverManager</code>.
 * <p>
 * This datasource pool implementation is designed to make as many
 * database connections as are needed available without placing
 * undo load on the database server.
 * <p>
 * If an application under normal load needs 3 database connections
 * for example, then the <code>max</code> pool size should be set
 * to a value like 10.  This will allow the pool to grow to accomodate
 * a sudden spike in load without allowing the pool to grow to such
 * a large size as to place undo load on the database server.  The
 * pool's trimming features will keep track of how many connections
 * are actually needed and close those connections which are no longer
 * required.
 * <p>
 * Configuration Example:
 * <pre>
 *   &lt;rl-jdbc&gt;
 *     &lt;pool-controller max="<i>10</i>" max-strict="<i>true</i>"
 *       blocking="<i>true</i>" timeout="<i>-1</i>"
 *       trim-interval="<i>60000</i>"
 *       connection-class="<i>my.overrided.ConnectionClass</i>"&gt;
 *       &lt;keep-alive disable="false" age="5000"&gt;select 1&lt;/keep-alive&gt;
 *     &lt;/pool-controller&gt;
 *     &lt;auto-commit&gt;<i>true</i>&lt;/auto-commit&gt;
 *     &lt;driver&gt;<i>com.database.jdbc.JdbcDriver</i>&lt;/driver&gt;
 *     &lt;dburl&gt;<i>jdbc:driver://host/mydb</i>&lt;/dburl&gt;
 *     &lt;user&gt;<i>username</i>&lt;/user&gt;
 *     &lt;password&gt;<i>password</i>&lt;/password&gt;
 *   &lt;/rl-jdbc&gt;
 * </pre>
 * <p>
 * Roles Example:
 * <pre>
 *   &lt;role name="org.apache.avalon.excalibur.datasource.DataSourceComponentSelector"
 *     shorthand="datasources"
 *     default-class="org.apache.avalon.excalibur.component.ExcaliburComponentSelector"&gt;
 *     &lt;hint shorthand="rl-jdbc"
 *       class="org.apache.avalon.excalibur.datasource.ResourceLimitingJdbcDataSource"/&gt;
 *   &lt;/role&gt;
 * </pre>
 * <p>
 * Configuration Attributes:
 * <ul>
 * <li>The <code>max</code> attribute is used to set the maximum
 * number of connections which will be opened.  See the
 * <code>blocking</code> attribute.  (Defaults to "3")</li>
 *
 * <li>The <code>max-strict</code> attribute is used to determine whether
 * or not the maximum number of connections can be exceeded.  If true,
 * then an exception will be thrown if more than max connections are
 * requested and blocking is false.  (Defaults to "true")<br>
 * <i>WARNING: In most cases, this value
 * should always be set to true.  Setting it to false means that under
 * heavy load, your application may open a very large number of
 * connections to the database.  Some database servers behave very poorly
 * under large connection loads and can even crash.</i></li>
 *
 * <li>The <code>blocking</code> attributes is used to specify the
 * behavior of the DataSource pool when an attempt is made to allocate
 * more than <code>max</code> concurrent connections.  If true, the
 * request will block until a connection is released, otherwise, a
 * NoAvailableConnectionException will be thrown.  Ignored if
 * <code>max-strict</code> is false.  (Defaults to "true")</li>
 *
 * <li>The <code>timeout</code> attribute is used to specify the
 * maximum amount of time in milliseconds that a request for a
 * connection will be allowed to block before a
 * NoAvailableConnectionException is thrown.  A value of "0" specifies
 * that the block will never timeout.  (Defaults to "0")</li>
 *
 * <li>The <code>trim-interval</code> attribute is used to specify how
 * long idle connections will be maintained in the pool before being
 * closed.  For a complete explanation on how this works, see {@link
 * org.apache.avalon.excalibur.pool.ResourceLimitingPool#trim()}
 * (Defaults to "60000", 1 minute)</li>
 *
 * <li>The <code>connection-class</code> attribute is used to override
 * the Connection class returned by the DataSource from calls to
 * getConnection().  Set this to
 * "org.apache.avalon.excalibur.datasource.Jdbc3Connection" to gain
 * access to JDBC3 features.  Jdbc3Connection does not exist if your
 * JVM does not support JDBC3.  (Defaults to
 * "org.apache.avalon.excalibur.datasource.JdbcConnection")</li>
 *
 * <li>The <code>keep-alive</code> element is used to override the
 * query used to monitor the health of connections.  If a connection
 * has not been used for 5 seconds then before returning the
 * connection from a call to getConnection(), the connection is first
 * used to ping the database to make sure that it is still alive.
 * Setting the <code>disable</code> attribute to true will disable
 * this feature.  Setting the <code>age</code> allows the 5 second age to
 * be overridden.  (Defaults to a query of "SELECT 1" and being enabled)</li>
 *
 * <li>The <code>auto-commit</code> element is used to determine the
 * default auto-commit mode for the <code>Connection</code>s returned
 * by this <code>DataSource</code>.
 *
 * <li>The <code>driver</code> element is used to specify the driver
 * to use when connecting to the database.  The specified class must
 * be in the classpath.  (Required)</li>
 *
 * <li>The <code>dburl</code> element is the JDBC connection string
 * which will be used to connect to the database.  (Required)</li>
 *
 * <li>The <code>user</code> and <code>password</code> attributes are
 * used to specify the user and password for connections to the
 * database. (Required)</li>
 * </ul>
 * 
 * @avalon.component
 * @avalon.service type=DataSourceComponent
 * @avalon.service type=TraceableDataSourceComponent
 * @x-avalon.info name=rl-jdbc
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/30 15:58:50 $
 * @since 4.1
 */
public class ResourceLimitingJdbcDataSource
    extends AbstractLogEnabledInstrumentable
    implements TraceableDataSourceComponent, Disposable
{
    private boolean m_configured;
    private boolean m_disposed;
    protected TraceableResourceLimitingPool m_pool;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public ResourceLimitingJdbcDataSource()
    {
    }

    /*---------------------------------------------------------------
     * DataSourceComponent Methods
     *-------------------------------------------------------------*/
    /**
     * Gets the Connection to the database
     *
     * @throws NoValidConnectionException when there is no valid Connection wrapper
     *         available in the classloader.
     *
     * @throws NoAvailableConnectionException when there are no more available
     *         Connections in the pool.
     */
    public Connection getConnection()
        throws SQLException
    {
        if( !m_configured ) throw new IllegalStateException( "Not Configured" );
        if( m_disposed ) throw new IllegalStateException( "Already Disposed" );

        Object connection;
        try
        {
            connection = m_pool.get();
            if (null == connection)
            {
                throw new SQLException("Could not return Connection");
            }
        }
        catch( SQLException e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Could not return Connection", e );
            }

            throw e;
        }
        catch( Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Could not return Connection", e );
            }

            throw new NoAvailableConnectionException( e.getMessage() );
        }
        
        return (Connection)connection;
    }

    /**
     * Returns a snapshot of the current state of the pool.
     *
     * @return A snapshot of the current pool state.
     */
    public TraceableResourceLimitingPool.State getState()
    {
        return m_pool.getState();
    }

    /*---------------------------------------------------------------
     * DataSourceComponent (Configurable) Methods
     *-------------------------------------------------------------*/
    /**
     * Pass the <code>Configuration</code> to the <code>Configurable</code>
     * class. This method must always be called after the constructor
     * and before any other method.
     *
     * @param configuration the class configurations.
     */
    public void configure( Configuration configuration ) throws ConfigurationException
    {
        if( m_configured ) throw new IllegalStateException( "Already Configured" );

        final String driver = configuration.getChild( "driver" ).getValue( "" );
        final String dburl = configuration.getChild( "dburl" ).getValue( null );
        final String user = configuration.getChild( "user" ).getValue( null );
        final String passwd = configuration.getChild( "password" ).getValue( null );

        final Configuration controller = configuration.getChild( "pool-controller" );
        final int keepAliveAge = controller.getChild( "keep-alive" ).getAttributeAsInteger( "age", 5000 );
        String keepAlive = controller.getChild( "keep-alive" ).getValue( "SELECT 1" );
        final boolean disableKeepAlive =
            controller.getChild( "keep-alive" ).getAttributeAsBoolean( "disable", false );

        final int max = controller.getAttributeAsInteger( "max", 3 );
        final boolean maxStrict = controller.getAttributeAsBoolean( "max-strict", true );
        final boolean blocking = controller.getAttributeAsBoolean( "blocking", true );
        final long timeout = controller.getAttributeAsLong( "timeout", 0 );
        final long trimInterval = controller.getAttributeAsLong( "trim-interval", 60000 );
        final boolean trace = controller.getAttributeAsBoolean( "trace", false );
        final boolean oradb = controller.getAttributeAsBoolean( "oradb", false );
        final boolean autoCommit = configuration.getChild( "auto-commit" ).getValueAsBoolean( true );
        // Get the JdbcConnection class.  The factory will resolve one if null.
        final String connectionClass = controller.getAttribute( "connection-class", null );

        final int l_max;

        // If driver is specified....
        if( !"".equals( driver ) )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Loading new driver: " + driver );
            }

            try
            {
                Class.forName( driver, true, Thread.currentThread().getContextClassLoader() );
            }
            catch( ClassNotFoundException cnfe )
            {
                if( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Could not load driver: " + driver, cnfe );
                }
            }
        }

        // Validate the max pool size values.
        if( max < 1 )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Maximum number of connections specified must be at least 1." );
            }

            l_max = 1;
        }
        else
        {
            l_max = max;
        }

        // If the keepAlive disable attribute was set, then set the keepAlive query to null,
        //  disabling it.
        if( disableKeepAlive )
        {
            keepAlive = null;
        }

        // If the oradb attribute was set, then override the keepAlive query.
        // This will override any specified keepalive value even if disabled.
        //  (Deprecated, but keep this for backwards-compatability)
        if( oradb )
        {
            keepAlive = "SELECT 1 FROM DUAL";

            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "The oradb attribute is deprecated, please use the" +
                                  "keep-alive element instead." );
            }
        }

        final JdbcConnectionFactory factory = new JdbcConnectionFactory
            ( dburl, user, passwd, autoCommit, keepAlive, keepAliveAge, connectionClass );

        factory.enableLogging( getLogger() );

        try
        {
            m_pool = new ResourceLimitingJdbcConnectionPool(
                factory, l_max, maxStrict, blocking, timeout, trimInterval, trace, autoCommit );

            m_pool.enableLogging( getLogger() );
            
            addChildInstrumentable( m_pool );
        }
        catch( Exception e )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Error configuring ResourceLimitingJdbcDataSource", e );
            }

            throw new ConfigurationException( "Error configuring ResourceLimitingJdbcDataSource", e );
        }

        m_configured = true;
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * The dispose operation is called at the end of a components lifecycle.
     * This method will be called after Startable.stop() method (if implemented
     * by component). Components use this method to release and destroy any
     * resources that the Component owns.
     */
    public void dispose()
    {
        m_disposed = true;
        m_pool.dispose();
        m_pool = null;
    }
}

