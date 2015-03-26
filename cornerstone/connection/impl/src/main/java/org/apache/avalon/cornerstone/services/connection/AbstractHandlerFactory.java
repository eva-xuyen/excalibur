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

package org.apache.avalon.cornerstone.services.connection;

import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Helper class to extend to create handler factorys.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractHandlerFactory
    extends AbstractLogEnabled
    implements Contextualizable, Serviceable, Configurable, ConnectionHandlerFactory
{
    private Context m_context;
    private ServiceManager m_serviceManager;
    private Configuration m_configuration;

    public void contextualize( final Context context )
    {
        m_context = context;
    }

    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        m_configuration = configuration;
    }

    /**
     * Construct an appropriate ConnectionHandler.
     *
     * @return the new ConnectionHandler
     * @exception Exception if an error occurs
     */
    public ConnectionHandler createConnectionHandler()
        throws Exception
    {
        final ConnectionHandler handler = newHandler();
        ContainerUtil.enableLogging( handler, getLogger() );
        ContainerUtil.contextualize( handler, m_context );
        ContainerUtil.service( handler, m_serviceManager );
        ContainerUtil.compose( handler, new WrapperComponentManager( m_serviceManager ) );
        ContainerUtil.configure( handler, m_configuration );
        ContainerUtil.initialize( handler );

        return handler;
    }

    public void releaseConnectionHandler( ConnectionHandler connectionHandler )
    {
        ContainerUtil.dispose( connectionHandler );
    }

    /**
     * Overide this method to create actual instance of connection handler.
     *
     * @return the new ConnectionHandler
     * @exception Exception if an error occurs
     */
    protected abstract ConnectionHandler newHandler()
        throws Exception;
}
