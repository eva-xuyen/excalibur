/*
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.util;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceManager;
import org.d_haven.event.Sink;
import org.d_haven.event.command.ThreadManager;
import org.apache.excalibur.instrument.InstrumentManager;
import org.d_haven.mpool.PoolManager;

import java.io.File;
import java.net.URL;

/**
 * Helper class to create a m_context for the ContextManager.
 * @version CVS $Revision: 1.23 $ $Date: 2004/04/03 18:10:35 $
 */
public final class FortressConfig
{
    private final DefaultContext m_context;

    /**
     * Creates a m_context builder and initializes it with default values.
     * The default values are:
     *
     * <ul>
     * <li>CONTAINER_CLASS = "org.apache.avalon.fortress.impl.DefaultContainer" </li>
     * <li>THREADS_CPU =  2</li>
     * <li>THREAD_TIMEOUT = 1000</li>
     * <li>CONTEXT_DIRECTORY = "../"</li>
     * <li>WORK_DIRECTORY = "/tmp"</li>
     * <li>LOG_CATEGORY = "fortress"</li>
     * <li>CONTEXT_CLASSLOADER = the thread m_context class loader</li>
     * </ul>
     */
    public FortressConfig()
    {
        this( createDefaultConfig() );
    }

    /**
     * Creates a m_context builder and initializes it with default values.
     *
     * @param parent parent m_context with default values.
     */
    public FortressConfig( final Context parent )
    {
        m_context = new OverridableContext( parent );
    }

    /**
     * Creates a default m_context.
     */
    public static final Context createDefaultConfig()
    {
        return createDefaultConfig( Thread.currentThread().getContextClassLoader() );
    }

    /**
     * Creates a default m_context.
     */
    public static final Context createDefaultConfig( final ClassLoader classLoader )
    {
        final DefaultContext defaultContext = new DefaultContext();

        try
        {
            defaultContext.put( ContextManagerConstants.CONTAINER_CLASS,
                DefaultContainer.class );
            defaultContext.put( ContextManagerConstants.COMMAND_FAILURE_HANDLER_CLASS,
                FortressCommandFailureHandler.class );
        }
        catch ( Exception e )
        {
            // ignore
        }

        final File contextDir = new File( System.getProperty( "user.dir" ) );
        final File workDir = new File( System.getProperty( "java.io.tmpdir" ) );

        defaultContext.put( ContextManagerConstants.THREADS_CPU, new Integer( 2 ) );
        defaultContext.put( ContextManagerConstants.THREAD_TIMEOUT, new Long( 1000 ) );
        defaultContext.put( ContextManagerConstants.CONTEXT_DIRECTORY, contextDir );
        defaultContext.put( ContextManagerConstants.WORK_DIRECTORY, workDir );
        defaultContext.put( ContextManagerConstants.LOG_CATEGORY, "fortress" );
        defaultContext.put( ClassLoader.class.getName(), classLoader );
        defaultContext.put( ContextManagerConstants.CONFIGURATION_URI, "conf/system.xconf" );
        defaultContext.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI, "conf/logkit.xconf" );

        defaultContext.makeReadOnly();

        return defaultContext;
    }

    /**
     * Finalizes the m_context and returns it.
     */
    public Context getContext()
    {
        m_context.makeReadOnly();
        return m_context;
    }

    public void setCommandSink( final Sink commandSink )
    {
        m_context.put( Sink.class.getName(), commandSink );
    }

    public void setServiceManager( final ServiceManager componentManager )
    {
        m_context.put( ContextManagerConstants.SERVICE_MANAGER, componentManager );
    }

    public void setLifecycleExtensionManager( final LifecycleExtensionManager extensionManager )
    {
        m_context.put( LifecycleExtensionManager.ROLE, extensionManager );
    }

    public void setContainerClass( final String containerClass )
        throws ClassNotFoundException
    {
        ClassLoader classLoader;
        try
        {
            classLoader = (ClassLoader) m_context.get( ClassLoader.class.getName() );
        }
        catch ( ContextException ce )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        setContainerClass( classLoader.loadClass( containerClass ) );
    }

    public void setContainerClass( final Class containerClass )
    {
        m_context.put( ContextManagerConstants.CONTAINER_CLASS, containerClass );
    }

    /**
     * Sets a class whose instance will be used to override the default
     *  CommandFailureHandler used by the container.  This makes it possible
     *  for applications to decide how they wish to handle failures.
     *
     * @param commandFailureHandlerClass Name of the CommandFailureHandler class to use.
     */
    public void setCommandFailureHandlerClass( final String commandFailureHandlerClass )
        throws ClassNotFoundException
    {
        ClassLoader classLoader;
        try
        {
            classLoader = (ClassLoader) m_context.get( ClassLoader.class.getName() );
        }
        catch ( ContextException ce )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        setCommandFailureHandlerClass( classLoader.loadClass( commandFailureHandlerClass ) );
    }

    /**
     * Sets a class whose instance will be used to override the default
     *  CommandFailureHandler used by the container.  This makes it possible
     *  for applications to decide how they wish to handle failures.
     *
     * @param commandFailureHandlerClass The CommandFailureHandler class to use.
     */
    public void setCommandFailureHandlerClass( final Class commandFailureHandlerClass )
    {
        m_context.put(
            ContextManagerConstants.COMMAND_FAILURE_HANDLER_CLASS, commandFailureHandlerClass );
    }

    public void setContainerConfiguration( final Configuration config )
    {
        m_context.put( ContextManagerConstants.CONFIGURATION, config );
        m_context.put( ContextManagerConstants.CONFIGURATION_URI, null );
    }

    public void setContainerConfiguration( final String location )
    {
        m_context.put( ContextManagerConstants.CONFIGURATION_URI, location );
    }

    public void setContextClassLoader( final ClassLoader loader )
    {
        m_context.put( ClassLoader.class.getName(), loader );
    }

    public void setContextDirectory( final File file )
    {
        m_context.put( ContextManagerConstants.CONTEXT_DIRECTORY, file );
    }

    public void setContextDirectory( final String directory )
    {
        m_context.put( ContextManagerConstants.CONTEXT_DIRECTORY, new File( directory ) );
    }

    public void setContextRootURL( final URL url )
    {
        m_context.put( ContextManagerConstants.CONTEXT_DIRECTORY, url );
    }

    public void setLoggerCategory( final String category )
    {
        m_context.put( ContextManagerConstants.LOG_CATEGORY, category );
    }

    public void setLoggerManager( final LoggerManager logManager )
    {
        m_context.put( LoggerManager.ROLE, logManager );
        m_context.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION, null );
        m_context.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI, null );
    }

    public void setLoggerManagerConfiguration( final Configuration config )
    {
        m_context.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION, config );
        m_context.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI, null );
    }

    public void setLoggerManagerConfiguration( final String location )
    {
        m_context.put( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI, location );
    }

    public void setInstrumentManager( final InstrumentManager profiler )
    {
        m_context.put( InstrumentManager.ROLE, profiler );
        m_context.put( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION, null );
        m_context.put( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI, null );
    }

    public void setInstrumentManagerConfiguration( final Configuration config )
    {
        m_context.put( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION, config );
        m_context.put( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI, null );
    }

    public void setInstrumentManagerConfiguration( final String location )
    {
        m_context.put( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI, location );
    }

    public void setNumberOfThreadsPerCPU( final int numberOfThreads )
    {
        m_context.put( ContextManagerConstants.THREADS_CPU, new Integer( numberOfThreads ) );
    }

    public void setPoolManager( final PoolManager poolManager )
    {
        m_context.put( PoolManager.class.getName(), poolManager );
    }

    public void setRoleManager( final org.apache.avalon.fortress.RoleManager roleManager )
    {
        m_context.put( org.apache.avalon.fortress.RoleManager.ROLE, roleManager );
    }

    public void setRoleManagerConfiguration( final Configuration config )
    {
        m_context.put( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION, config );
        m_context.put( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION_URI, null );
    }

    public void setRoleManagerClass( final String containerClass )
    throws ClassNotFoundException
    {
        ClassLoader classLoader;
        try
        {
            classLoader = (ClassLoader) m_context.get( ClassLoader.class.getName() );
        }
        catch ( ContextException ce )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        setRoleManagerClass( classLoader.loadClass( containerClass ) );
    }

    public void setRoleManagerClass( final Class clazz )
    {
        m_context.put( ContextManagerConstants.ROLE_MANAGER_CLASS, clazz );
    }

    public void setRoleManagerConfiguration( final String location )
    {
        m_context.put( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION_URI, location );
    }

    public void setThreadTimeout( final long timeout )
    {
        m_context.put( ContextManagerConstants.THREAD_TIMEOUT, new Long( timeout ) );
    }

    public void setWorkDirectory( final File file )
    {
        m_context.put( ContextManagerConstants.WORK_DIRECTORY, file );
    }

    public void setWorkDirectory( final String directory )
    {
        setWorkDirectory( new File( directory ) );
    }

    public void setThreadManager( final ThreadManager threadManager )
    {
        m_context.put( ThreadManager.class.getName(), threadManager );
    }
}
