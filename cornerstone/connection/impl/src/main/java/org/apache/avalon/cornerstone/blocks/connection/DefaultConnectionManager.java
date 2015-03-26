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

package org.apache.avalon.cornerstone.blocks.connection;

import org.apache.avalon.cornerstone.services.connection.ConnectionManager;
import org.apache.avalon.cornerstone.services.threads.ThreadManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * This is the service through which ConnectionManagement occurs.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="connection-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.connection.ConnectionManager"
 */
public class DefaultConnectionManager
    extends AbstractConnectionManager
    implements ConnectionManager, Serviceable, Disposable, LogEnabled
{

    public void enableLogging(Logger logger) {
        AvalonLoggerConnectionMonitor avalonLoggerConnectionMonitor = new AvalonLoggerConnectionMonitor();
        avalonLoggerConnectionMonitor.enableLogging(logger);
        monitor = avalonLoggerConnectionMonitor;
    }

   /**
    * @avalon.dependency type="org.apache.avalon.cornerstone.services.threads.ThreadManager"
    */
    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_threadManager = (ThreadManager)serviceManager.lookup( ThreadManager.ROLE );
    }


}
