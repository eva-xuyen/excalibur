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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * The J2EE implementation for DataSources in Cocoon.  This uses the
 * <code>javax.sql.DataSource</code> object and assumes that the
 * J2EE container pools the datasources properly.
 *
 * @avalon.component
 * @avalon.service type=DataSourceComponent
 * @x-avalon.info name=j2ee-datasource
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class J2eeDataSource
    extends AbstractLogEnabled
    implements DataSourceComponent
{
    public static final String JDBC_NAME = "java:comp/env/jdbc/";
    protected DataSource m_dataSource = null;
    protected String m_user;
    protected String m_password;

    /**
     *  Configure and set up DB connection.  Here we set the connection
     *  information needed to create the Connection objects.  It must
     *  be called only once.
     *
     * @param conf The Configuration object needed to describe the
     *             connection.
     *
     * @throws ConfigurationException
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        if( null == m_dataSource )
        {
            final String contextFactory =
                configuration.getChild( "initial-context-factory" ).getValue( null );
            final String providerUrl =
                configuration.getChild( "provider-url" ).getValue( null );
            String lookupName =
                configuration.getChild( "lookup-name" ).getValue( null );

            if ( null == lookupName )
            {
                lookupName = JDBC_NAME +
                    configuration.getChild( "dbname" ).getValue();
            }

            try
            {
                Context initialContext;
                if ( null == contextFactory && null == providerUrl )
                {
                    initialContext = new InitialContext();
                }
                else
                {
                    final Hashtable props = new Hashtable();
                    if ( null != contextFactory )
                    {
                        props.put( Context.INITIAL_CONTEXT_FACTORY, contextFactory );
                    }
                    if ( null != providerUrl )
                    {
                        props.put( Context.PROVIDER_URL, providerUrl );
                    }
                    initialContext = new InitialContext( props );
                }

                if ( null == lookupName )
                {
                    m_dataSource =
                        (DataSource)initialContext.lookup( lookupName );
                }
                else
                {
                    m_dataSource = (DataSource)initialContext.lookup( lookupName );
                }
            }
            catch( final NamingException ne )
            {
                if( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Problem with JNDI lookup of datasource", ne );
                }

                throw new ConfigurationException( "Could not use JNDI to find datasource", ne );
            }
        }

        m_user = configuration.getChild( "user" ).getValue( null );
        m_password = configuration.getChild( "password" ).getValue( null );
    }

    /** Get the database connection */
    public Connection getConnection()
        throws SQLException
    {
        if( null == m_dataSource )
        {
            throw new SQLException( "Can not access DataSource object" );
        }

        if ( null == m_user || null == m_password )
        {
            return m_dataSource.getConnection();
        }
        else
        {
            return m_dataSource.getConnection( m_user, m_password );
        }
    }
}

