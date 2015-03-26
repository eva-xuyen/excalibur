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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.avalon.cornerstone.services.sockets.ServerSocketFactory;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.avalon.cornerstone.services.threads.ThreadManager;

import org.apache.excalibur.thread.ThreadPool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Helper class to create protocol services.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractService
    extends AbstractLogEnabled
    implements Contextualizable, Serviceable, Configurable, Initializable, Disposable
{
    protected ConnectionManager m_connectionManager;
    protected SocketManager m_socketManager;
    protected ConnectionHandlerFactory m_factory;
    protected ThreadManager m_threadManager;
    protected ThreadPool m_threadPool;
    protected String m_serverSocketType;
    protected int m_port;
    protected InetAddress m_bindTo; //network interface to bind to
    protected ServerSocket m_serverSocket;
    protected String m_connectionName;

    public AbstractService()
    {
        m_factory = createFactory();
        m_serverSocketType = "plain";
    }

    protected String getThreadPoolName()
    {
        return null;
    }

    protected abstract ConnectionHandlerFactory createFactory();

    public void enableLogging( final Logger logger )
    {
        super.enableLogging( logger );
        ContainerUtil.enableLogging( m_factory, logger );
    }

    public void contextualize( final Context context )
        throws ContextException
    {
        ContainerUtil.contextualize( m_factory, context );
    }

    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_connectionManager = (ConnectionManager)serviceManager.lookup( ConnectionManager.ROLE );
        m_socketManager = (SocketManager)serviceManager.lookup( SocketManager.ROLE );
        if( null != getThreadPoolName() )
        {
            m_threadManager =
                (ThreadManager)serviceManager.lookup( ThreadManager.ROLE );
            m_threadPool = m_threadManager.getThreadPool( getThreadPoolName() );
        }
        ContainerUtil.service( m_factory, serviceManager );
        try
        {
            ContainerUtil.compose( m_factory, new WrapperComponentManager( serviceManager ) );
        }
        catch( final ComponentException ce )
        {
            throw new ServiceException( ConnectionHandlerFactory.class.getName(), ce.getMessage(), ce );
        }
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        ContainerUtil.configure( m_factory, configuration );
    }

    public void initialize()
        throws Exception
    {
        ContainerUtil.initialize( m_factory );

        if( null == m_connectionName )
        {
            final StringBuffer sb = new StringBuffer();
            sb.append( m_serverSocketType );
            sb.append( ':' );
            sb.append( m_port );

            if( null != m_bindTo )
            {
                sb.append( '/' );
                sb.append( m_bindTo );
            }

            m_connectionName = sb.toString();
        }

        final ServerSocketFactory factory =
            m_socketManager.getServerSocketFactory( m_serverSocketType );

        if( null == m_bindTo )
        {
            m_serverSocket = factory.createServerSocket( m_port );
        }
        else
        {
            m_serverSocket = factory.createServerSocket( m_port, 5, m_bindTo );
        }

        if( null == m_threadPool )
        {
            m_connectionManager.connect( m_connectionName, m_serverSocket,
                                         m_factory );
        }
        else
        {
            m_connectionManager.connect( m_connectionName, m_serverSocket,
                                         m_factory, m_threadPool );
        }
    }

    public void dispose()
    {
        try
        {
            m_connectionManager.disconnect( m_connectionName );
        }
        catch( final Exception e )
        {
            final String message = "Error disconnecting";
            getLogger().warn( message, e );
        }

        try
        {
            m_serverSocket.close();
        }
        catch( final IOException ioe )
        {
            final String message = "Error closing server socket";
            getLogger().warn( message, ioe );
        }
    }
}
