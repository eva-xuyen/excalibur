/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.manager.impl.DefaultInstrumentManagerImpl;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;

/**
 * Utility class which can be used to manage the life cycle of a
 *  ComponentManager and its RoleManager, LoggerManager, and optional
 *  InstrumentManager.
 * <p>
 * The code necessary to manage all of the above can be reduced to the
 *  following:
 * <pre>
 *     m_componentManagerCreator = new ExcaliburComponentManagerCreator(
 *                                          null,  // Optional Context
 *                                          new File( "../conf/logkit.xml" ),
 *                                          new File( "../conf/roles.xml" ),
 *                                          new File( "../conf/components.xml"),
 *                                          new File( "../conf/instrument.xml" ) );
 * </pre>
 *
 * Then simply remember to dispose of the creator when the application
 *  shuts down.
 * <pre>
 *     m_componentManagerCreator.dispose();
 *     m_componentManagerCreator = null;
 * </pre>
 *
 * The ServiceManager (ComponentManager) or any of the other managers can be accessed
 *  using their getter methods.  getServiceManager() for example.  Note that
 *  while the ComponentManager is still available, it has been deprecated in favor
 *  of the ServiceManager interface.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.2
 */
public class ExcaliburComponentManagerCreator
    implements Disposable
{
    /** Internal logger set once the LoggerManager has been initialized.
     * Always call getLogger() to get the best available logger. */
    private Logger m_logger;

    /** Simple logger which can be used until the LoggerManager has been setup.
     * Always call getLogger() to get the best available logger. */
    private final Logger m_primordialLogger;

    /** Context to create the ComponentManager with. */
    private Context m_context;

    /** Internal logger manager. */
    private LoggerManager m_loggerManager;

    /** Internal role manager. */
    private RoleManager m_roleManager;

    /** Internal component manager. */
    private ComponentManager m_componentManager;

    /** Internal service manager. */
    private ServiceManager m_serviceManager;

    /** Internal instrument manager. */
    private InstrumentManager m_instrumentManager;
    /*---------------------------------------------------------------
     * Static Methods
     *-------------------------------------------------------------*/
    /**
     * Creates and initializes a default context.
     */
    private static Context createDefaultContext()
    {
        DefaultContext context = new DefaultContext();
        context.makeReadOnly();
        return context;
    }

    /**
     * Creates a Configuration object from data read from an InputStream.
     *
     * @param is InputStream from which the Configuration is created.
     *
     * @return Configuration created from the InputStream
     *
     * @throws Exception If the configuration could not be processed.
     */
    private static Configuration readConfigurationFromStream( InputStream is )
        throws Exception
    {
        if( is == null )
        {
            return null;
        }
        else
        {
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            Configuration config = builder.build( is );
            return config;
        }
    }

    /**
     * Creates a Configuration object from data read from an InputStream.
     *
     * @param file InputStream from which the Configuration is created.
     *
     * @return Configuration created from the InputStream
     *
     * @throws Exception If the configuration could not be read or processed.
     */
    private static Configuration readConfigurationFromFile( File file )
        throws Exception
    {
        if( file == null )
        {
            return null;
        }
        else
        {
            InputStream is = new FileInputStream( file );
            try
            {
                return readConfigurationFromStream( is );
            }
            finally
            {
                is.close();
            }
        }
    }

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new ExcaliburComponentManagerCreator using Configuration
     *  objects.
     *
     * @param context Context to use when creating the ComponentManager. May
     *                be null.
     * @param loggerManagerConfig Configuration object to use to create a
     *                            LoggerManager.
     * @param roleManagerConfig Configuration object to use to create a
     *                          RoleManager.
     * @param componentManagerConfig Configuration object to use to create a
     *                               ComponentManager.
     * @param instrumentManagerConfig Configuration object to use to create an
     *                                InstrumentManager.  May be null.
     *
     * @throws Exception If there were any problems initializing the
     *                   ComponentManager.
     */
    public ExcaliburComponentManagerCreator( Context context,
                                             Configuration loggerManagerConfig,
                                             Configuration roleManagerConfig,
                                             Configuration componentManagerConfig,
                                             Configuration instrumentManagerConfig )
        throws Exception
    {
        if( context == null )
        {
            m_context = createDefaultContext();
        }
        else
        {
            m_context = context;
        }

        // The primordial logger is used for all output up until the point
        //  where the Logger manager has been initialized.  However it is set
        //  into objects used to load the configuration resource files.  Any
        //  problems loading these files will result in warning or error
        //  messages.  However in most cases, the debug information should not
        //  be displayed, so turn it off by default.
        //  Unfortunately, there is not a very good place to make this settable.
        m_primordialLogger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );

        try
        {
            initializeLoggerManager( loggerManagerConfig );
            initializeRoleManager( roleManagerConfig );
            initializeInstrumentManager( instrumentManagerConfig );
            initializeComponentManager( componentManagerConfig );
        }
        catch( Exception e )
        {
            // Clean up after the managers which were set up.
            dispose();
            throw e;
        }
    }

    /**
     * Create a new ExcaliburComponentManagerCreator using Input Streams.
     *
     * @param context Context to use when creating the ComponentManager. May
     *                be null.
     * @param loggerManagerConfigStream InputStream from which to read the
     *                                  Configuration object to use to create
     *                                  a LoggerManager.
     * @param roleManagerConfigStream InputStream from which to read the
     *                                Configuration object to use to create
     *                                a RoleManager.
     * @param componentManagerConfigStream InputStream from which to read the
     *                                     Configuration object to use to
     *                                     create a ComponentManager.
     * @param instrumentManagerConfigStream InputStream from which to read the
     *                                      Configuration object to use to
     *                                      create a InstrumentManager.  May
     *                                      be null.
     *
     * @throws Exception If there were any problems initializing the
     *                   ComponentManager.
     */
    public ExcaliburComponentManagerCreator( Context context,
                                             InputStream loggerManagerConfigStream,
                                             InputStream roleManagerConfigStream,
                                             InputStream componentManagerConfigStream,
                                             InputStream instrumentManagerConfigStream )
        throws Exception
    {
        this( context,
              readConfigurationFromStream( loggerManagerConfigStream ),
              readConfigurationFromStream( roleManagerConfigStream ),
              readConfigurationFromStream( componentManagerConfigStream ),
              readConfigurationFromStream( instrumentManagerConfigStream ) );
    }

    /**
     * Create a new ExcaliburComponentManagerCreator using Files.
     *
     * @param context Context to use when creating the ComponentManager. May
     *                be null.
     * @param loggerManagerConfigFile File from which to read the
     *                                Configuration object to use to create
     *                                a LoggerManager.
     * @param roleManagerConfigFile File from which to read the Configuration
     *                              object to use to create a RoleManager.
     * @param componentManagerConfigFile File from which to read the
     *                                   Configuration object to use to
     *                                   create a ComponentManager.
     * @param instrumentManagerConfigFile File from which to read the
     *                                    Configuration object to use to
     *                                    create a InstrumentManager.  May
     *                                    be null.
     *
     * @throws Exception If there were any problems initializing the
     *                   ComponentManager.
     */
    public ExcaliburComponentManagerCreator( Context context,
                                             File loggerManagerConfigFile,
                                             File roleManagerConfigFile,
                                             File componentManagerConfigFile,
                                             File instrumentManagerConfigFile )
        throws Exception
    {
        this( context,
              readConfigurationFromFile( loggerManagerConfigFile ),
              readConfigurationFromFile( roleManagerConfigFile ),
              readConfigurationFromFile( componentManagerConfigFile ),
              readConfigurationFromFile( instrumentManagerConfigFile ) );
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * Disposes the component manager creator along with the CompoentManager
     *  and other managers which it was responsible for creating.
     */
    public void dispose()
    {
        // Clean up all of the objects that we created in the propper order.
        try
        {
            if( m_componentManager != null )
            {
                ContainerUtil.shutdown( m_componentManager );
            }

            if( m_instrumentManager != null )
            {
                ContainerUtil.shutdown( m_instrumentManager );
            }

            if( m_roleManager != null )
            {
                ContainerUtil.shutdown( m_roleManager );
            }

            if( m_loggerManager != null )
            {
                ContainerUtil.shutdown( m_loggerManager );
            }
        }
        catch( Exception e )
        {
            getLogger().error( "Unexpected error disposing managers.", e );
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the configured LoggerManager.
     *
     * @return The configured LoggerManager.
     */
    public LoggerManager getLoggerManager()
    {
        return m_loggerManager;
    }

    /**
     * Returns the configured InstrumentManager.  May be null if an instrument
     *  configuration was not specified in the constructor.
     *
     * @return The configured InstrumentManager.
     */
    public InstrumentManager getInstrumentManager()
    {
        return m_instrumentManager;
    }

    /**
     * Returns the configured ComponentManager.
     *
     * @return The configured ComponentManager.
     *
     * @deprecated The ComponentManager interface has been deprecated.
     *             Please use the getServiceManager method.
     */
    public ComponentManager getComponentManager()
    {
        return m_componentManager;
    }

    /**
     * Returns the configured ServiceManager.
     *
     * @return The configured ServiceManager.
     */
    public ServiceManager getServiceManager()
    {
        return m_serviceManager;
    }

    /**
     * Returns the logger for internal use.
     */
    private Logger getLogger()
    {
        if( m_logger != null )
        {
            return m_logger;
        }
        return m_primordialLogger;
    }

    private void initializeLoggerManager( Configuration loggerManagerConfig )
        throws Exception
    {
        // Do we want to allow a log prefix to be set?
        String logPrefix = null;

        // Resolve a name for the logger, taking the logPrefix into account
        String lmDefaultLoggerName;
        String lmLoggerName;
        if( logPrefix == null )
        {
            lmDefaultLoggerName = "";
            lmLoggerName = loggerManagerConfig.getAttribute( "logger", "system.logkit" );
        }
        else
        {
            lmDefaultLoggerName = logPrefix;
            lmLoggerName = logPrefix + org.apache.log.Logger.CATEGORY_SEPARATOR
                + loggerManagerConfig.getAttribute( "logger", "system.logkit" );
        }

        // Create the default logger for the Logger Manager.
        org.apache.log.Logger lmDefaultLogger =
            Hierarchy.getDefaultHierarchy().getLoggerFor( lmDefaultLoggerName );
        // The default logger is not used until after the logger conf has been loaded
        //  so it is possible to configure the priority there.
        lmDefaultLogger.setPriority( Priority.DEBUG );

        // Create the logger for use internally by the Logger Manager.
        org.apache.log.Logger lmLogger =
            Hierarchy.getDefaultHierarchy().getLoggerFor( lmLoggerName );
        lmLogger.setPriority( Priority.getPriorityForName(
            loggerManagerConfig.getAttribute( "log-level", "DEBUG" ) ) );

        // Setup the Logger Manager
        LogKitLoggerManager loggerManager = new LogKitLoggerManager(
            logPrefix, Hierarchy.getDefaultHierarchy(),
            new LogKitLogger( lmDefaultLogger ), new LogKitLogger( lmLogger ) );
        loggerManager.contextualize( m_context );
        loggerManager.configure( loggerManagerConfig );
        m_loggerManager = loggerManager;

        // Since we now have a LoggerManager, we can update the m_logger field
        //  if it is null and start logging to the "right" logger.
        if( m_logger == null )
        {
            getLogger().debug( "Switching to default Logger provided by LoggerManager." );
            m_logger = m_loggerManager.getDefaultLogger();
        }
    }

    private void initializeRoleManager( Configuration roleManagerConfig )
        throws Exception
    {
        // Get the logger for the role manager
        Logger rmLogger = m_loggerManager.getLoggerForCategory(
            roleManagerConfig.getAttribute( "logger", "system.roles" ) );

        // Setup the RoleManager
        DefaultRoleManager roleManager = new DefaultRoleManager();
        roleManager.enableLogging( rmLogger );
        roleManager.configure( roleManagerConfig );
        m_roleManager = roleManager;
    }

    private void initializeInstrumentManager( Configuration instrumentManagerConfig )
        throws Exception
    {
        if( instrumentManagerConfig != null )
        {
            // Get the logger for the instrument manager
            Logger imLogger = m_loggerManager.getLoggerForCategory(
                instrumentManagerConfig.getAttribute( "logger", "system.instrument" ) );

            // Set up the Instrument Manager
            DefaultInstrumentManagerImpl instrumentManager = new DefaultInstrumentManagerImpl();
            instrumentManager.enableLogging( imLogger );
            instrumentManager.configure( instrumentManagerConfig );
            instrumentManager.initialize();
            m_instrumentManager = instrumentManager;
        }
    }

    private void initializeComponentManager( Configuration componentManagerConfig )
        throws Exception
    {
        // Get the logger for the component manager
        Logger cmLogger = m_loggerManager.getLoggerForCategory(
            componentManagerConfig.getAttribute( "logger", "system.components" ) );

        // Set up the ComponentManager
        ExcaliburComponentManager componentManager = new ExcaliburComponentManager();
        componentManager.enableLogging( cmLogger );
        componentManager.setLoggerManager( m_loggerManager );
        componentManager.contextualize( m_context );
        if ( m_instrumentManager != null )
        {
            componentManager.setInstrumentManager( m_instrumentManager );
        }
        componentManager.setRoleManager( m_roleManager );
        componentManager.configure( componentManagerConfig );
        componentManager.initialize();
        m_componentManager = componentManager;

        // Now wrap the ComponentManager so that we can provide access to it as
        //  a ServiceManager.
        m_serviceManager = new WrapperServiceManager( m_componentManager );
    }
}

