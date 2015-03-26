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

package org.apache.avalon.excalibur.datasource.cluster;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.excalibur.datasource.NoValidConnectionException;

/**
 * The DefaultIndexedDataSourceCluster requires that the user implement their own method of
 *  selecting which DataSource in the cluster to use for each connection request.  Calls to
 *  getConnection() will throw an exception.  Components which make use of this class must call
 *  the getConnectionForIndex(int index) method instead.
 * <p>
 * The Configuration for a 2 database cluster is like this:
 *
 * <pre>
 *   &lt;datasources&gt;
 *     &lt;indexed-cluster name="mydb-cluster" size="2"&gt;
 *       &lt;dbpool index="0"&gt;mydb-0&lt;/dbpool&gt;
 *       &lt;dbpool index="1"&gt;mydb-1&lt;/dbpool&gt;
 *     &lt;/indexed-cluster&gt;
 *   &lt;/datasources&gt;
 *   &lt;cluster-datasources&gt;
 *     &lt;jdbc name="mydb-0"&gt;
 *       &lt;pool-controller min="1" max="10"/&gt;
 *       &lt;auto-commit&gt;true&lt;/auto-commit&gt;
 *       &lt;driver&gt;com.database.jdbc.JdbcDriver&lt;/driver&gt;
 *       &lt;dburl&gt;jdbc:driver://host0/mydb&lt;/dburl&gt;
 *       &lt;user&gt;username&lt;/user&gt;
 *       &lt;password&gt;password&lt;/password&gt;
 *     &lt;/jdbc&gt;
 *     &lt;jdbc name="mydb-1"&gt;
 *       &lt;pool-controller min="1" max="10"/&gt;
 *       &lt;auto-commit&gt;true&lt;/auto-commit&gt;
 *       &lt;driver&gt;com.database.jdbc.JdbcDriver&lt;/driver&gt;
 *       &lt;dburl&gt;jdbc:driver://host1/mydb&lt;/dburl&gt;
 *       &lt;user&gt;username&lt;/user&gt;
 *       &lt;password&gt;password&lt;/password&gt;
 *     &lt;/jdbc&gt;
 *   &lt;/cluster-datasources&gt;
 * </pre>
 *
 * With the following roles declaration:
 *
 * <pre>
 *   &lt;role name="org.apache.avalon.excalibur.datasource.DataSourceComponentSelector"
 *     shorthand="datasources"
 *     default-class="org.apache.avalon.excalibur.component.ExcaliburComponentSelector"&gt;
 *     &lt;hint shorthand="jdbc" class="org.apache.avalon.excalibur.datasource.JdbcDataSource"/&gt;
 *     &lt;hint shorthand="j2ee" class="org.apache.avalon.excalibur.datasource.J2eeDataSource"/&gt;
 *     &lt;hint shorthand="indexed-cluster"
 *       class="org.apache.avalon.excalibur.datasource.cluster.DefaultIndexedDataSourceCluster"/&gt;
 *   &lt;/role&gt;
 *   &lt;role name="org.apache.avalon.excalibur.datasource.DataSourceComponentClusterSelector"
 *       shorthand="cluster-datasources"
 *       default-class="org.apache.avalon.excalibur.component.ExcaliburComponentSelector"&gt;
 *     &lt;hint shorthand="jdbc" class="org.apache.avalon.excalibur.datasource.JdbcDataSource"/&gt;
 *     &lt;hint shorthand="j2ee" class="org.apache.avalon.excalibur.datasource.J2eeDataSource"/&gt;
 *   &lt;/role&gt;
 * </pre>
 *
 * An indexed-cluster definition enforces that the configuration specify a size.  This size must
 *  equal the number of datasources referenced as being members of the cluster.  Any datasource can
 *  be a member of the cluster.
 * <p>
 * The indexed-cluster can be obtained in the same manner as a non-clustered datasource.  The only
 *  difference is in how it is used.  The IndexedDataSourceCluster requires that the caller specify
 *  the index of the cluster member to use when requesting a connection.
 * <p>
 * The following code demonstrates a change that can be made to database enabled components so that
 *  they will be able to work with both IndexedDataSourceCluster DataSources and regular
 *  DataSources.
 * <p>
 * old:
 * <pre>
 *   Connection connection = m_dataSource.getConnection();
 * </pre>
 *
 * new:
 * <pre>
 *   Connection connection;
 *   if ( m_dataSource instanceof IndexedDataSourceCluster )
 *   {
 *     connection = ((IndexedDataSourceCluster)m_dataSource).getConnectionForIndex( index );
 *   }
 *   else
 *   {
 *     connection = m_dataSource.getConnection();
 *   }
 * </pre>
 * 
 * @avalon.component
 * @avalon.service type=org.apache.avalon.excalibur.datasource.DataSourceComponent
 * @avalon.service type=IndexedDataSourceCluster
 * @x-avalon.info name=index-db-cluster
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 * @since 4.1
 */
public class DefaultIndexedDataSourceCluster
    extends AbstractDataSourceCluster
    implements IndexedDataSourceCluster
{

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public DefaultIndexedDataSourceCluster()
    {
    }

    /*---------------------------------------------------------------
     * DataSourceComponent Methods
     *-------------------------------------------------------------*/
    /**
     * Not supported in this component.  Will throw a NoValidConnectionException.
     */
    public Connection getConnection() throws SQLException
    {
        throw new NoValidConnectionException(
            "getConnection() should not be called for a " + getClass().getName() + ".  " +
            "Please verify your configuration." );
    }

    /*---------------------------------------------------------------
     * IndexedDataSourceCluster Methods
     *-------------------------------------------------------------*/
    // public int getClusterSize()
    //   declared in AbstractDataSourceCluster

    // public Connection getConnectionForIndex( int index ) throws SQLException
    //   declared in AbstractDataSourceCluster
}

