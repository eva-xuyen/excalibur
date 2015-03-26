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

package org.apache.avalon.cornerstone.blocks.sockets;

import java.util.HashMap;
import org.apache.avalon.cornerstone.services.sockets.ServerSocketFactory;
import org.apache.avalon.cornerstone.services.sockets.SocketFactory;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Implementation of SocketManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="socket-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.sockets.SocketManager"
 */
public class DefaultSocketManager
    extends AbstractLogEnabled
    implements SocketManager, Contextualizable, Configurable, Initializable
{
    protected final HashMap m_serverSockets = new HashMap();
    protected final HashMap m_sockets = new HashMap();

    protected Context m_context;
    protected Configuration m_configuration;

   /**
    * @avalon.entry key="urn:avalon:name" alias="block.name"
    * @avalon.entry key="urn:avalon:partition" alias="app.name"
    * @avalon.entry key="urn:avalon:home" type="java.io.File" alias="app.home"
    */
    public void contextualize( final Context context )
    {
        m_context = context;
    }

    /**
     * Configure the SocketManager.
     *
     * @param configuration the Configuration
     * @exception ConfigurationException if an error occurs
     * @avalon.configuration schema="http://relaxng.org/ns/structure/1.0"
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        m_configuration = configuration;
    }

    public void initialize()
        throws Exception
    {
        final Configuration[] serverSockets =
            m_configuration.getChild( "server-sockets" ).getChildren( "factory" );

        for( int i = 0; i < serverSockets.length; i++ )
        {
            final Configuration element = serverSockets[ i ];
            final String name = element.getAttribute( "name" );
            final String className = element.getAttribute( "class" );

            setupServerSocketFactory( name, className, element );
        }

        final Configuration[] clientSockets =
            m_configuration.getChild( "client-sockets" ).getChildren( "factory" );

        for( int i = 0; i < clientSockets.length; i++ )
        {
            final Configuration element = clientSockets[ i ];
            final String name = element.getAttribute( "name" );
            final String className = element.getAttribute( "class" );

            setupClientSocketFactory( name, className, element );
        }
    }

    protected void setupServerSocketFactory( final String name,
                                             final String className,
                                             final Configuration configuration )
        throws Exception
    {
        final Object object = createFactory( name, className, configuration );

        if( !( object instanceof ServerSocketFactory ) )
        {
            throw new Exception( "Error creating factory " + name +
                                 " with class " + className + " as " +
                                 "it does not implement the correct " +
                                 "interface (ServerSocketFactory)" );
        }

        m_serverSockets.put( name, object );
    }

    protected void setupClientSocketFactory( final String name,
                                             final String className,
                                             final Configuration configuration )
        throws Exception
    {
        final Object object = createFactory( name, className, configuration );

        if( !( object instanceof SocketFactory ) )
        {
            throw new Exception( "Error creating factory " + name +
                                 " with class " + className + " as " +
                                 "it does not implement the correct " +
                                 "interface (SocketFactory)" );
        }

        m_sockets.put( name, object );
    }

    protected Object createFactory( final String name,
                                    final String className,
                                    final Configuration configuration )
        throws Exception
    {
        Object factory = null;

        try
        {
            final ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
            factory = classLoader.loadClass( className ).newInstance();
        }
        catch( final Throwable e )
        {
            final String error = 
              "Error creating factory with class " + className;
            getLogger().error( "## CLASSLOADER: " + Thread.currentThread().getContextClassLoader() );
            throw new CascadingException( error, e );
        }

        ContainerUtil.enableLogging( factory, getLogger() );
        ContainerUtil.contextualize( factory, m_context );
        ContainerUtil.configure( factory, configuration );
        ContainerUtil.initialize( factory );

        return factory;
    }

    /**
     * Retrieve a server socket factory by name.
     *
     * @param name the name of server socket factory
     * @return the ServerSocketFactory
     * @exception Exception if server socket factory is not available
     */
    public ServerSocketFactory getServerSocketFactory( String name )
        throws Exception
    {
        final ServerSocketFactory factory = (ServerSocketFactory)m_serverSockets.get( name );

        if( null != factory )
        {
            return factory;
        }
        else
        {
            throw new Exception( "Unable to locate server socket factory " +
                                 "named " + name );
        }
    }

    /**
     * Retrieve a client socket factory by name.
     *
     * @param name the name of client socket factory
     * @return the SocketFactory
     * @exception Exception if socket factory is not available
     */
    public SocketFactory getSocketFactory( final String name )
        throws Exception
    {
        final SocketFactory factory = (SocketFactory)m_sockets.get( name );

        if( null != factory )
        {
            return factory;
        }
        else
        {
            throw new Exception( "Unable to locate client socket factory " +
                                 "named " + name );
        }
    }
}
