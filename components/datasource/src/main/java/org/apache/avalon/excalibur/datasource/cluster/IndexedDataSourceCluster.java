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

import org.apache.avalon.excalibur.datasource.DataSourceComponent;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 * @since 4.1
 */
public interface IndexedDataSourceCluster
    extends DataSourceComponent
{
    /**
     * The name of the role for convenience
     */
    String ROLE = IndexedDataSourceCluster.class.getName();

    /**
     * Returns the number of DataSources in the cluster.
     *
     * @return size of the cluster.
     */
    int getClusterSize();

    /**
     * Gets a Connection to a database given an index.
     *
     * @param index Index of the DataSource for which a connection is to be returned.
     *
     * @throws org.apache.avalon.excalibur.datasource.NoValidConnectionException when there is no valid Connection wrapper
     *         available in the classloader or when the index is not valid.
     *
     * @throws org.apache.avalon.excalibur.datasource.NoAvailableConnectionException when there are no more available
     *         Connections in the pool.
     */
    Connection getConnectionForIndex( int index ) throws SQLException;
}

