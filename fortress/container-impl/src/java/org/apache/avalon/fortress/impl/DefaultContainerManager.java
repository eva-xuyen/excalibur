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

package org.apache.avalon.fortress.impl;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.ContainerManagerConstants;
import org.apache.avalon.fortress.InitializationException;
import org.apache.avalon.fortress.util.ContextManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.excalibur.logger.LoggerManager;

/**
 * This is the default implementation of the
 * {@link org.apache.avalon.fortress.ContainerManager} interface.
 * See that interface for a description.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.29 $ $Date: 2004/02/28 15:16:24 $
 */
public class DefaultContainerManager
    implements Initializable, Disposable, ContainerManager, ContainerManagerConstants
{
    private final ContextManager m_contextManager;
    private final Logger m_logger;
    private Object m_containerInstance;

    public DefaultContainerManager( final ContextManager contextManager )
    {
        this( contextManager, null );
    }

    public DefaultContainerManager( final ContextManager contextManager,
                                    final Logger logger )
    {
        m_contextManager = contextManager;
        m_logger = ( logger == null ?
            createLoggerFromContext( m_contextManager.getContainerManagerContext() ) : logger );
    }

    public DefaultContainerManager( final Context initParameters ) throws Exception
    {
        this( initParameters, null );
    }

    public DefaultContainerManager( final Context initParameters,
                                    final Logger logger ) throws Exception
    {
        this( getInitializedContextManager( initParameters, logger ), logger );
    }

    /**
     * Creates and initializes a contextManager given an initialization context.
     *  This is necessary so that these operations can complete before the
     *  super constructor has been executed.
     */
    private static ContextManager getInitializedContextManager( final Context initParameters,
                                                                Logger logger ) throws Exception
    {
        // The context manager will use an internal coonsole logger if logger is null.
        final ContextManager contextManager = new ContextManager( initParameters, logger );
        contextManager.initialize();
        return contextManager;
    }

    protected Logger createLoggerFromContext( final Context initParameters )
    {
        try
        {
            final Logger logger = (Logger) initParameters.get( LOGGER );
            return logger;
        }
        catch ( ContextException ce )
        {
            final Logger consoleLogger = new ConsoleLogger();
            consoleLogger.error( "ContainerManager could not obtain logger manager from context "
                + "(this should not happen). Using console instead." );
            return consoleLogger;
        }
    }

    /**
     * Initialize the ContainerManager
     */
    public void initialize() throws Exception
    {
        initializeContainer();
    }

    protected void initializeContainer() throws InitializationException
    {
        if ( null == m_containerInstance )
        {
            createContainer();
        }
    }

    private void createContainer()
        throws InitializationException
    {
        final Context managerContext =
            m_contextManager.getContainerManagerContext();
        final Object instance;
        try
        {
            final Class clazz = (Class) managerContext.get( CONTAINER_CLASS );
            instance = clazz.newInstance();
        }
        catch ( Exception e )
        {
            final String message =
                "Cannot set up impl. Unable to create impl class";
            throw new InitializationException( message, e );
        }

        if ( instance instanceof Loggable )
        {
            throw new InitializationException( "Loggable containers are not supported" );
        }

        if ( instance instanceof Composable )
        {
            throw new InitializationException( "Composable containers are not supported" );
        }

        try
        {
            final Context implContext = m_contextManager.getChildContext();

            final ServiceManager serviceManager =
                    (ServiceManager) getContextEntry( managerContext, SERVICE_MANAGER );
            final LoggerManager loggerManager = 
                    (LoggerManager) serviceManager.lookup( LoggerManager.ROLE );

            ContainerUtil.enableLogging( instance, loggerManager.getDefaultLogger() );
            ContainerUtil.contextualize( instance, implContext );

            ContainerUtil.service( instance, serviceManager );

            final Configuration config =
                (Configuration) getContextEntry( managerContext, CONFIGURATION );
            ContainerUtil.configure( instance, config );

            final Parameters parameters =
                (Parameters) getContextEntry( managerContext, PARAMETERS );
            ContainerUtil.parameterize( instance, parameters );

            ContainerUtil.initialize( instance );
            ContainerUtil.start( instance );

            m_containerInstance = instance;
        }
        catch ( Exception e )
        {
            final String message =
                "Cannot set up Container. Startup lifecycle failure";
            throw new InitializationException( message, e );
        }
    }

    /**
     * Retrieve an entry from context if it exists, else return null.
     *
     * @param context the context
     * @param key the key
     * @return the entry
     */
    private Object getContextEntry( final Context context, final String key )
    {
        try
        {
            return context.get( key );
        }
        catch ( ContextException e )
        {
            return null;
        }
    }

    protected void disposeContainer()
    {
        if ( null != m_containerInstance )
        {
            try
            {
                ContainerUtil.stop( m_containerInstance );
            }
            catch ( Exception e )
            {
                if ( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Caught an exception when stopping the Container, "
                        + "continuing with shutdown", e );
                }
            }

            ContainerUtil.dispose( m_containerInstance );
            m_containerInstance = null;
        }
    }

    /**
     * Dispose of the ContainerManager and managed Container
     */
    public void dispose()
    {
        disposeContainer();
        m_contextManager.dispose();
    }

    /**
     * Get a reference to your Container.  Typically, you would cast this to
     * whatever interface you will use to interact with it.
     */
    public Object getContainer()
    {
        return m_containerInstance;
    }

    /**
     * Allows to get the logger and associated hierarchy for logging.
     * @return Logger
     */
    public final Logger getLogger()
    {
        // (mschier) was protected.
        // Made public to get to the logger at the impl setup level.
        return m_logger;
    }
}
