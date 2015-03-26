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

package org.apache.avalon.excalibur.datasource.ids;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 * @since 4.1
 */
public abstract class AbstractDataSourceIdGenerator
    extends AbstractIdGenerator
    implements Serviceable, Configurable, Initializable, Disposable
{
    private String m_dataSourceName;
    private ServiceSelector m_dbSelector;
    protected DataSourceComponent m_dataSource;

    /**
     * Number of allocated Ids remaining before another block must be allocated.
     */
    protected int m_allocated;
    protected long m_nextId;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public AbstractDataSourceIdGenerator()
    {
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Allocates a connection for the caller.  The connection must be closed by the caller
     *  when no longer needed.
     *
     * @return an open DB connection.
     *
     * @throws SQLException if the connection can not be obtained for any reason.
     */
    protected Connection getConnection()
        throws SQLException
    {
        return m_dataSource.getConnection();
    }

    /*---------------------------------------------------------------
     * Composable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to tell the component which ComponentLocator
     *  is controlling it.
     *
     * @param manager which curently owns the component.
     * @avalon.dependency type="org.apache.avalon.excalibur.datasource.DataSourceComponent"
     */
    public void service( final ServiceManager manager )
        throws ServiceException
    {
        m_dbSelector = (ServiceSelector)manager.lookup( DataSourceComponent.ROLE + "Selector" );
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to configure the component.
     *
     * @param configuration configuration info used to setup the component.
     *
     * @throws ConfigurationException if there are any problems with the configuration.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        // Obtain the big-decimals flag.
        setUseBigDecimals( configuration.getAttributeAsBoolean( "big-decimals", false ) );

        // Obtain a reference to the configured DataSource
        m_dataSourceName = configuration.getChild( "dbpool" ).getValue();
    }

    /*---------------------------------------------------------------
     * Initializable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to initialize the component.
     *
     * @throws Exception if there were any problems durring initialization.
     */
    public void initialize()
        throws Exception
    {
        // Get a reference to a data source
        m_dataSource = (DataSourceComponent)m_dbSelector.select( m_dataSourceName );
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to dispose the component.
     */
    public void dispose()
    {
        // Free up the data source
        if( m_dbSelector != null )
        {
            if( m_dataSource != null )
            {
                m_dbSelector.release( m_dataSource );

                m_dataSource = null;
            }

            m_dbSelector = null;
        }
    }
}

