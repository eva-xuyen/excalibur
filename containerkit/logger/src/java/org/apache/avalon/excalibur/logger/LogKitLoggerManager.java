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
package org.apache.avalon.excalibur.logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.excalibur.logger.util.LoggerUtil;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.ErrorHandler;
import org.apache.log.Hierarchy;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.util.Closeable;

/**
 * LogKitLoggerManager implementation.  It populates the LoggerManager
 * from a configuration file.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.21 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class LogKitLoggerManager extends AbstractLoggerManager
    implements LoggerManager, Contextualizable, Configurable, Disposable
{
    /** Set of log targets */
    final private Set m_targets = new HashSet();

    /** The context object */
    private Context m_context;

    /** The hierarchy private to LogKitManager */
    private final Hierarchy m_hierarchy;

    /**
     * Creates a new <code>LogKitLoggerManager</code>;
     * one of the preferred constructors.
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     */
    public LogKitLoggerManager()
    {
        this( (String)null, (Hierarchy)null, (String)null, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code>;
     * one of the preferred constructors.
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     */
    public LogKitLoggerManager( final String prefix )
    {
        this( prefix, (Hierarchy)null, (String)null, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code>;
     * one of the preferred constructors,
     * intended for the widest usage.
     *
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     * <p>
     * Example:
     * <pre>
     * LogKitLoggerManager l = new LogKitLoggerManager( "fortress", "system.logkit" );
     * l.enableLogging( bootstrapLogger );
     * l.configure( loggerManagerConfiguration );
     * </pre>
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     * @param switchToCategory if this parameter is not null
     *         after <code>start()</code>
     *         <code>LogKitLoggerManager</code> will start
     *         to log its own debug and error messages to
     *         a logger obtained via
     *         <code>this.getLoggerForCategory( switchToCategory )</code>.
     *         Note that prefix will be prepended to
     *         the value of <code>switchToCategory</code> also.
     */
    public LogKitLoggerManager( final String prefix, final String switchToCategory )
    {
        this( prefix, (Hierarchy)null, switchToCategory, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code> 
     * with an existing <code>Hierarchy</code>;
     * use with caution.
     *
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     * See <a href="#h-warning">comments on the root constructor</a> 
     * for details on why constructors supplying an existing hierarchy 
     * should be used with caution.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     * @param switchToCategory if this parameter is not null
     *         after <code>start()</code>
     *         <code>LogKitLoggerManager</code> will start
     *         to log its own debug and error messages to
     *         a logger obtained via
     *         <code>this.getLoggerForCategory( switchToCategory )</code>.
     *         Note that prefix will be prepended to
     *         the value of <code>switchToCategory</code> also.
     */
    public LogKitLoggerManager( final String prefix, final Hierarchy hierarchy,
            final String switchToCategory )
    {
        this( prefix, hierarchy, switchToCategory, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code> 
     * with an existing <code>Hierarchy</code>;
     * use with caution.
     *
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     * See <a href="#h-warning">comments on the root constructor</a> 
     * for details on why constructors supplying an existing hierarchy 
     * should be used with caution.
     */
    public LogKitLoggerManager( final Hierarchy hierarchy )
    {
        this( (String)null, hierarchy, (String)null, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code> 
     * with an existing <code>Hierarchy</code>;
     * use with caution.
     *
     * Please also invoke <code>enableLogging()</code>
     * to supply a fallback logger.
     * See <a href="#h-warning">comments on the root constructor</a> 
     * for details on why constructors supplying an existing hierarchy 
     * should be used with caution.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     */
    public LogKitLoggerManager( final String prefix, final Hierarchy hierarchy )
    {
        this( prefix, hierarchy, (String)null, (Logger)null, (Logger)null );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code>
     * with an existing <code>Hierarchy</code> using
     * specified logger name as a fallback logger and to
     * <strong>forcibly override</strong> the root logger;
     * compatibility constructor.
     *
     * The configuration for the <code>""</code>
     * category in the configuration will supply the defaults for
     * all other categories, but <code>getDefaultLogger()</code>
     * and <code>getLoggerForCategory()</code> will still
     * use logger supplied by this constructor.
     *
     * <p>
     * See <a href="#h-warning">comments on the root constructor</a> 
     * for details on why constructors supplying an existing hierarchy 
     * should be used with caution.
     *
     * <p>
     * As this constructor provides a logger to be used as a fallback
     * a subsequent <code>enableLogging()</code> stage is unnecessary.
     * Moreover, it will fail.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     * @param defaultOverrideAndFallback the logger used to 
     *         a) <strong>forcibly</strong>
     *         override the root logger that will further be obtained from
     *         the configuration and b) as the fallback logger. Note that
     *         specifying a logger as this parameter crucially differs from
     *         supplying it via <code>enableLogging()</code>. The logger
     *         supplied via <code>enableLogging</code> will only be used
     *         as the fallback logger and to log messages during initialization
     *         while this constructor argument also has the described
     *         <strong>override</strong> semantics.
     */
    public LogKitLoggerManager( final String prefix, final Hierarchy hierarchy,
                                final Logger defaultOverrideAndFallback )
    {
        this( prefix, hierarchy, (String)null, 
                defaultOverrideAndFallback, defaultOverrideAndFallback );
    }

    /**
     * Creates a new <code>LogKitLoggerManager</code>
     * with an existing <code>Hierarchy</code> using
     * specified loggers to <strong>forcibly override</strong>
     * the default logger and to provide a fallback logger;
     * compatibility constructor.
     *
     * See <a href="#h-warning">comments on the root constructor</a> 
     * for details on why constructors supplying an existing hierarchy 
     * should be used with caution.
     * <p>
     * As this constructor provides a logger to be used as a fallback
     * a subsequent <code>enableLogging()</code> stage is unnecessary.
     * Moreover, it will fail.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     * @param defaultLoggerOverride the logger to be used to <strong>forcibly</strong>
     *         override the root logger that will further be obtained from
     *         the configuration
     * @param fallbackLogger the logger to as a fallback logger
     *         (passing non-null as this argument eliminates the need
     *         to invoke <code>enableLogging()</code>)
     */
    public LogKitLoggerManager( final String prefix, final Hierarchy hierarchy,
                                final Logger defaultLoggerOverride, 
                                final Logger fallbackLogger )
    {
        this( prefix, hierarchy, (String)null, defaultLoggerOverride, fallbackLogger );
    }

    /**
     * <a name="h-warning" id="h-warning"/>
     *
     * Creates a new <code>LogKitLoggerManager</code>;
     * "root" constructor invoked by all other constructors.
     * <p>
     *
     * If the <code>hierarchy</code> parameter is not <code>null</code>
     * this instructs this constructor to use an existing hierarchy
     * instead of creating a new one. This also disables removing
     * the default log target configured by the <code>Hierarchy()</code>
     * constructor (this target logs to <code>System.out</code>) and
     * installing our own <code>ErrorHandler</code> for our
     * <code>Hierarchy</code> (the default <code>ErrorHandler</code>
     * writes to <code>System.err</code>).
     * <p>
     * The configuration of the resulting <code>Hierarchy</code>
     * is a combination of the original configuraiton that
     * existed when this <code>Hierarchy</code> was handled to us
     * and the configuration supplied to use via <code>configure()</code>.
     * <code>LogTarget</code>s for those categories for which a
     * configuration node has been supplied in the configuration
     * supplied via <code>configure()</code> are replaced during
     * the <code>configure()</code> process. <code>LogTargets</code>
     * for those categories for which configuration nodes have
     * not been supplied are left as they were. A special case
     * is when a node in configuration for a category exists but
     * it does not enlist log targets. In this case the original
     * targets if any are left as they were.
     *
     * <p>
     * Generally it is preferrable to
     * <ul>
     * <li>have the <code>Hierarchy</code> be configured
     * from top to bottom via the configuration</li>
     * <li>have our custom <code>ErrorHandler</code> reporting
     * errors via the fallback logger (supplied either as
     * the <code>fallbackLogger</code> to this constructor or
     * via the <code>enableLogging()</code> method) installed</li>
     * </ul>
     * That's why it is preferrable to pass <code>null</code>
     * for the <code>hierarchy</code> parameter of this constructor
     * or, which is easier to read but has the same effect, to
     * invoke a constructor which does not accept a <code>Hierarchy</code>
     * argument.
     * </p>
     *
     * <p>The <code>defaultLoggerOverride</code> and <code>fallbackLogger</code>
     * are a special case too. <code>defaultLoggerOverride</code> 
     * <strong>forcibly overrides</strong>
     * the root logger configured via <code>configure()</code>.
     * As there is little reason to take away users's freedom to configure
     * whatever he likes as the default logger, it is preferrable to pass
     * <code>null</code> for this parameter or better still to invoke
     * a constructor without <code>Logger</code> parameters at all.</p>
     *
     * <p>There is nothing wrong with passing <code>fallbackLogger</code>
     * via this constructor, but as this constructor is not convinient to
     * be invoked (too many arguments, some of them likely to be null) and the 
     * {@link #LogKitLoggerManager(String,Hierarchy,Logger)}
     * constructor is broken
     * in using its <code>Logger</code> argument both as 
     * <code>fallbackLogger</code> (which is okay) and as 
     * a <code>defaultLoggerOverride</code> (which is probably not 
     * desired for the reasons given above) it is preferrable not to 
     * specify a logger
     * as a constructor argument but rather supply it via
     * <code>enableLogging()</code> call, like this happens with all
     * other normal Avalon components after all.
     *
     * @param prefix to prepended to category name on each 
     *         invocation of <code>getLoggerForCategory()</code>.
     * @param switchToCategory if this parameter is not null
     *         after <code>start()</code>
     *         <code>LogKitLoggerManager</code> will start
     *         to log its own debug and error messages to
     *         a logger obtained via
     *         <code>this.getLoggerForCategory( switchToCategory )</code>.
     *         Note that prefix will be prepended to
     *         the value of <code>switchToCategory</code> also.
     * @param defaultLoggerOverride the logger to be used to 
     *         <strong>forcibly override</strong>
     *         the root logger that would further be obtained from
     *         the configuration
     * @param fallbackLogger the logger to as a fallback logger
     *         (passing non-null as this argument eliminates the need
     *         to invoke <code>enableLogging()</code>)
     */
    public LogKitLoggerManager( final String prefix, final Hierarchy hierarchy,
                                final String switchToCategory,
                                final Logger defaultLoggerOverride, 
                                final Logger fallbackLogger )
    {
        super( prefix, switchToCategory, defaultLoggerOverride );
        m_prefix = prefix;

        if ( hierarchy == null )
        {
            m_hierarchy = new Hierarchy();
            m_hierarchy.getRootLogger().unsetLogTargets( true );
            final ErrorHandler errorHandler = new OurErrorHandler( getLogger() );
            m_hierarchy.setErrorHandler( errorHandler );
        }
        else
        {
            m_hierarchy = hierarchy;
        }

        if ( fallbackLogger != null )
        {
            this.enableLogging( fallbackLogger );
        }
    }

    /**
     * Reads a context object that will be supplied to the log target factory manager.
     *
     * @param context The context object.
     * @throws ContextException if the context is malformed
     */
    public final void contextualize( final Context context )
            throws ContextException
    {
        m_context = context;
    }

    /**
     * Actually create a logger for the given category.
     * The result will be cached by 
     * <code>AbstractLoggerManager.getLoggerForCategory()</code>.
     */
    protected Logger doGetLoggerForCategory( final String fullCategoryName )
    {
        return new LogKitLogger( m_hierarchy.getLoggerFor( fullCategoryName ) );
    }

    /**
     * Reads a configuration object and creates the category mapping.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    public final void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration factories = configuration.getChild( "factories" );
        final LogTargetFactoryManager targetFactoryManager = setupTargetFactoryManager( factories );

        final Configuration targets = configuration.getChild( "targets" );
        final LogTargetManager targetManager = setupTargetManager( targets, targetFactoryManager );

        final Configuration categories = configuration.getChild( "categories" );
        final Configuration[] category = categories.getChildren( "category" );
        setupLoggers( targetManager,
                      m_prefix,
                      category,
                      true,
                      categories.getAttributeAsBoolean( "additive", false ) );
    }

    /**
     * Setup a LogTargetFactoryManager
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    private final LogTargetFactoryManager setupTargetFactoryManager( final Configuration configuration )
        throws ConfigurationException
    {
        final DefaultLogTargetFactoryManager targetFactoryManager = new DefaultLogTargetFactoryManager();

        ContainerUtil.enableLogging( targetFactoryManager, getLogger() );

        try
        {
            ContainerUtil.contextualize( targetFactoryManager, m_context );
        }
        catch( final ContextException ce )
        {
            throw new ConfigurationException( "cannot contextualize default factory manager", ce );
        }

        ContainerUtil.configure( targetFactoryManager, configuration );

        return targetFactoryManager;
    }

    /**
     * Setup a LogTargetManager
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    private final LogTargetManager setupTargetManager( final Configuration configuration,
                                                       final LogTargetFactoryManager targetFactoryManager )
        throws ConfigurationException
    {
        final DefaultLogTargetManager targetManager = new DefaultLogTargetManager();

        ContainerUtil.enableLogging( targetManager, getLogger() );

        targetManager.setLogTargetFactoryManager( targetFactoryManager );

        ContainerUtil.configure( targetManager, configuration );

        return targetManager;
    }

    /**
     * Setup Loggers
     *
     * @param categories []  The array object of configurations for categories.
     * @param root shows if we're processing the root of the configuration
     * @throws ConfigurationException if the configuration is malformed
     */
    private final void setupLoggers( final LogTargetManager targetManager,
                                     final String parentCategory,
                                     final Configuration[] categories,
                                     boolean root,
                                     final boolean defaultAdditive )
        throws ConfigurationException
    {
        boolean rootLoggerAlive = false;

        for( int i = 0; i < categories.length; i++ )
        {
            final String category = categories[ i ].getAttribute( "name" );
            final String loglevel = categories[ i ].getAttribute( "log-level" ).toUpperCase();
            final boolean additive = categories[ i ].
                getAttributeAsBoolean( "additive", defaultAdditive );

            final Configuration[] targets = categories[ i ].getChildren( "log-target" );
            final LogTarget[] logTargets = new LogTarget[ targets.length ];
            for( int j = 0; j < targets.length; j++ )
            {
                final String id = targets[ j ].getAttribute( "id-ref" );
                logTargets[ j ] = targetManager.getLogTarget( id );
                if( !m_targets.contains( logTargets[ j ] ) )
                {
                    m_targets.add( logTargets[ j ] );
                }
            }

            if( root && "".equals( category ) && logTargets.length > 0 )
            {
                m_hierarchy.setDefaultPriority( Priority.getPriorityForName( loglevel ) );
                m_hierarchy.setDefaultLogTargets( logTargets );
                rootLoggerAlive = true;
            }

            final String fullCategory = 
                    LoggerUtil.getFullCategoryName( parentCategory, category );

            final org.apache.log.Logger logger = m_hierarchy.getLoggerFor( fullCategory );
            m_loggers.put( fullCategory, new LogKitLogger( logger ) );
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "added logger for category " + fullCategory );
            }
            logger.setPriority( Priority.getPriorityForName( loglevel ) );
            logger.setLogTargets( logTargets );
            logger.setAdditivity( additive );

            final Configuration[] subCategories = categories[ i ].getChildren( "category" );
            if( null != subCategories )
            {
                setupLoggers( targetManager, fullCategory, subCategories, false, defaultAdditive );
            }
        }

        if ( root && !rootLoggerAlive )
        {
            final String message = "No log targets configured for the root logger.";

            throw new ConfigurationException( message );
        }
    }

    /**
     * Closes all our LogTargets.
     */
    public void dispose()
    {
        final Iterator iterator = m_targets.iterator();
        while( iterator.hasNext() )
        {
            final LogTarget target = (LogTarget)iterator.next();
            if( target instanceof Closeable )
            {
                ( (Closeable)target ).close();
            }
        }
    }

    private static class OurErrorHandler implements ErrorHandler
    {
        /** 
         * This will be initialized to an instance of LoggerSwitch.SwitchingLogger;
         * that is really reliable.
         */
        private Logger m_reliableLogger;

        OurErrorHandler( final Logger reliableLogger )
        {
           if ( reliableLogger == null )
           {
               throw new NullPointerException( "reliableLogger" );
           }
           m_reliableLogger = reliableLogger;
        }

        public void error( final String message, final Throwable throwable, final LogEvent event )
        {
            // let them know we're not OK
            m_reliableLogger.fatalError( message, throwable );

            // transmit the original error
            final Priority p = event.getPriority();
            final String nestedMessage = "nested log event: " + event.getMessage();

            if ( p == Priority.DEBUG )
            {
                m_reliableLogger.debug( nestedMessage, event.getThrowable() );
            }
            else if ( p == Priority.INFO )
            {
                m_reliableLogger.info( nestedMessage, event.getThrowable() );
            }
            else if ( p == Priority.WARN )
            {
                m_reliableLogger.warn( nestedMessage, event.getThrowable() );
            }
            else if ( p == Priority.ERROR )
            {
                m_reliableLogger.error( nestedMessage, event.getThrowable() );
            }
            else if ( p == Priority.FATAL_ERROR)
            {
                m_reliableLogger.fatalError( nestedMessage, event.getThrowable() );
            }
            else
            {
                /** This just plainly can't happen :-)*/
                m_reliableLogger.error( "unrecognized priority " + nestedMessage, 
                    event.getThrowable() );
            }
        }
    }
}
