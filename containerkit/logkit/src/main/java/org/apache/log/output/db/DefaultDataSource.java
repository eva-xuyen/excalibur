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
package org.apache.log.output.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * A basic datasource that doesn't do any pooling but just wraps
 * around default mechanisms.
 *
 * @author Peter Donald
 */
public class DefaultDataSource
    implements DataSource
{
    private final String m_username;
    private final String m_password;
    private final String m_url;

    private PrintWriter m_logWriter;
    private int m_loginTimeout;

    public DefaultDataSource( final String url,
                              final String username,
                              final String password )
    {
        m_url = url;
        m_username = username;
        m_password = password;

        m_logWriter = new PrintWriter( System.err, true );
    }

    /**
     * Attempt to establish a database connection.
     *
     * @return the Connection
     */
    public Connection getConnection()
        throws SQLException
    {
        return getConnection( m_username, m_password );
    }

    /**
     * Attempt to establish a database connection.
     *
     * @return the Connection
     */
    public Connection getConnection( final String username, final String password )
        throws SQLException
    {
        return DriverManager.getConnection( m_url, username, password );
    }

    /**
     * Gets the maximum time in seconds that this data source can wait while
     * attempting to connect to a database.
     *
     * @return the login time
     */
    public int getLoginTimeout()
        throws SQLException
    {
        return m_loginTimeout;
    }

    /**
     * Get the log writer for this data source.
     *
     * @return the LogWriter
     */
    public PrintWriter getLogWriter()
        throws SQLException
    {
        return m_logWriter;
    }

    /**
     * Sets the maximum time in seconds that this data source will wait
     * while attempting to connect to a database.
     *
     * @param loginTimeout the loging timeout in seconds
     */
    public void setLoginTimeout( final int loginTimeout )
        throws SQLException
    {
        m_loginTimeout = loginTimeout;
    }

    public void setLogWriter( final PrintWriter logWriter )
        throws SQLException
    {
        m_logWriter = logWriter;
    }

    /**
     * This method is not supported. This method is defined only to support
     * compilation with Java 6.
     *
     * @param iface class defining an interface that the result must implement
     * @return nothing
     * @throws java.sql.SQLException always
     * @since 4-SNAPSHOT
     * @see javax.sql.DataSource#unwrap
     */
    public Object unwrap(Class iface) throws SQLException {
        throw new SQLException("DefaultDataSource does not implement wrapping support");
    }

    /**
     * This method always returns false. This method is defined only to support
     * compilation with Java 6.
     * 
     * @param iface class defining an interface
     * @return falsse
     * @throws SQLException never
     * @since 4-SNAPSHOT
     * @see javax.sql.DataSource#isWrapperFor
     */
    public boolean isWrapperFor(Class iface) throws SQLException {
        return false;
    }
}
