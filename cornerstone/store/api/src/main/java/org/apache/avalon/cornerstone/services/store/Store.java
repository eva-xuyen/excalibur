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

package org.apache.avalon.cornerstone.services.store;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * Allows selection from a number of configured Repositories.
 * Selection criterion is passed in as a <tt>Configuration</tt>
 * object.
 *
 * @see Repository
 * @see ObjectRepository
 * @see StreamRepository
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 */
public interface Store
    extends ServiceSelector
{
    String ROLE = Store.class.getName();

    /**
     * Selects a Repository configured for the given <tt>policy</tt>.
     * The <tt>policy</tt> must be an instance of
     * {@link org.apache.avalon.framework.configuration.Configuration}.
     * The following attributes are used by the Store and thus are mandatory
     * in the <tt>policy</tt> parameter:
     * <pre>
     * &lt;repository destinationURL="[URL of this repository]"
     *             type="[repository type e.g. OBJECT, STREAM or MAIL]"
     *             model="[repository model e.g. PERSISTENT, CACHE]"&gt;
     *   [additional configuration]
     * &lt;/repository&gt;
     * </pre>
     * <p>
     * The <tt>policy</tt> is used both to select the appropriate
     * Repository and to configure it.
     * </p>
     *
     * @param policy a {@link org.apache.avalon.framework.configuration.Configuration} object identifying the sought Repository
     * @return requested {@link Repository}
     * @throws ServiceException if no repository matches <tt>policy</tt>
     */
    Object select( Object policy )
        throws ServiceException;
}
