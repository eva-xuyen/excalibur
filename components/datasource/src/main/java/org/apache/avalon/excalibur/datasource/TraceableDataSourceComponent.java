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

import org.apache.avalon.excalibur.pool.TraceableResourceLimitingPool;

/**
 * The standard interface for DataSources in Avalon.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public interface TraceableDataSourceComponent
    extends DataSourceComponent
{
    /**
     * The name of the role for convenience
     */
    String ROLE = TraceableDataSourceComponent.class.getName();

    /**
     * Returns a snapshot of the current state of the pool.
     *
     * @return A snapshot of the current pool state.
     */
    TraceableResourceLimitingPool.State getState();
}

