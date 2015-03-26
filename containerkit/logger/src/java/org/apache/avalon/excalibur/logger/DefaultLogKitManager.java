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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;

/**
 * Default LogManager implementation.  It populates the LogManager
 * from a configuration file.
 *
 * @deprecated we should use the new LogKitLoggerManager interface that directly
 *             supports the new framework Logger interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.12 $ $Date: 2004/03/10 13:54:49 $
 * @since 4.0
 */
public class DefaultLogKitManager
    extends AbstractLogEnabled
    implements LogKitManager, Loggable, Contextualizable, Configurable
{
    /** Map for name to logger mapping */
    final private Map m_loggers = new HashMap();

    /** The context object */
    private Context m_context;

    /** The hierarchy private to LogKitManager */
    private Hierarchy m_hierarchy;

    /** The root logger to configure */
    private String m_prefix;

    /**
     * Creates a new <code>DefaultLogKitManager</code>. It will use a new <code>Hierarchy</code>.
     */
    public DefaultLogKitManager()
    {
        this( new Hierarchy() );
    }

    /**
     * Creates a new <code>DefaultLogKitManager</code> with an existing <code>Hierarchy</code>.
     */
    public DefaultLogKitManager( final Hierarchy hierarchy )
    {
        this( null, hierarchy );
    }

    /**
     * Creates a new <code>DefaultLogKitManager</code> using
     * specified logger name as root logger.
     */
    public DefaultLogKitManager( final String prefix )
    {
        this( prefix, new Hierarchy() );
    }

    /**
     * Creates a new <code>DefaultLogKitManager</code> with an existing <code>Hierarchy</code> using
     * specified logger name as root logger.
     */
    public DefaultLogKitManager( final String prefix, final Hierarchy hierarchy )
    {
        m_prefix = prefix;
        m_hierarchy = hierarchy;
    }

    public void setLogger( final Logger logger )
    {
        enableLogging( new LogKitLogger( logger ) );
    }

    /**
     * Retrieves a Logger from a category name. Usually
     * the category name refers to a configuration attribute name.  If
     * this LogKitManager does not have the match the default Logger will
     * be returned and a warning is issued.
     *
     * @param categoryName  The category name of a configured Logger.
     * @return the Logger.
     */
    public final Logger getLogger( final String categoryName )
    {
        final Logger logger = (Logger)m_loggers.get( categoryName );

        if( null != logger )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Logger for category " + categoryName + " returned" );
            }
            return logger;
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Logger for category " + categoryName
                               + " not defined in configuration. New Logger created and returned" );
        }

        return m_hierarchy.getLoggerFor( categoryName );
    }

    /**
     * Retrieve Hierarchy for Loggers configured by the system.
     *
     * @return the Hierarchy
     */
    public Hierarchy getHierarchy()
    {
        return m_hierarchy;
    }

    /**
     * Reads a context object.
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
     * Reads a configuration object and creates the category mapping.
     * If the <code>&lt;categories/&gt;</code> element has an attribute
     * named <code>debug</code>, it will try to load a configuration file
     * specified by that attribute.  The contents of that configuration
     * file will be the same as the <code>&lt;categories/&gt;</code> element.
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
        final String debugURL = configuration.getAttribute( "debug", null );
        final Configuration[] category = categories.getChildren( "category" );
        setupLoggers( targetManager, m_prefix, category );

        if( null != debugURL )
        {
            try
            {
                final File rootContext = (File)m_context.get( "context-root" );
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                Configuration debugCategories = builder.buildFromFile( new File( rootContext, debugURL ) );
                final Configuration[] debugCat = debugCategories.getChildren( "category" );
                setupLoggers( targetManager, m_prefix, debugCat );
            }
            catch( ConfigurationException ce )
            {
                throw ce;
            }
            catch( Exception e )
            {
                getLogger().info( "Either there was no \"debug.xlog\" file, or there was a problem reading it." );
                getLogger().debug( e.getMessage(), e );
                // swallow exception because it is not critical.
            }
        }
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
            final String message = "cannot contextualize default factory manager";
            throw new ConfigurationException( message, ce );
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
        try
        {
            ContainerUtil.contextualize( targetManager, m_context );
        }
        catch( final ContextException ce )
        {
            final String message = "cannot contextualize factory manager";
            throw new ConfigurationException( message, ce );
        }

        targetManager.setLogTargetFactoryManager( targetFactoryManager );

        ContainerUtil.configure( targetManager, configuration );
        return targetManager;
    }

    /**
     * Setup Loggers
     *
     * @param categories The array object of configurations for categories.
     * @throws ConfigurationException if the configuration is malformed
     */
    private final void setupLoggers( final LogTargetManager targetManager,
                                     final String parentCategory,
                                     final Configuration[] categories )
        throws ConfigurationException
    {
        for( int i = 0; i < categories.length; i++ )
        {
            final String category = categories[ i ].getAttribute( "name" );
            final String loglevel = categories[ i ].getAttribute( "log-level" ).toUpperCase();

            final Configuration[] targets = categories[ i ].getChildren( "log-target" );
            final LogTarget[] logTargets = new LogTarget[ targets.length ];
            for( int j = 0; j < targets.length; j++ )
            {
                final String id = targets[ j ].getAttribute( "id-ref" );
                logTargets[ j ] = targetManager.getLogTarget( id );
            }

            if( "".equals( category ) && logTargets.length > 0 )
            {
                m_hierarchy.setDefaultPriority( Priority.getPriorityForName( loglevel ) );
                m_hierarchy.setDefaultLogTargets( logTargets );
            }

            final String fullCategory;
            if( null == parentCategory )
            {
                fullCategory = category;
            }
            else
            {
                fullCategory = parentCategory + Logger.CATEGORY_SEPARATOR + category;
            }

            final Logger logger =
                m_hierarchy.getLoggerFor( fullCategory );
            m_loggers.put( fullCategory, logger );
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "added logger for category " + fullCategory );
            }
            logger.setPriority( Priority.getPriorityForName( loglevel ) );
            logger.setLogTargets( logTargets );

            final Configuration[] subCategories = categories[ i ].getChildren( "category" );
            if( null != subCategories )
            {
                setupLoggers( targetManager, fullCategory, subCategories );
            }
        }
    }
}
