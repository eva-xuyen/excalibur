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
package org.apache.avalon.excalibur.logger.logkit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.excalibur.logger.DefaultLogTargetFactoryManager;
import org.apache.avalon.excalibur.logger.DefaultLogTargetManager;
import org.apache.avalon.excalibur.logger.LogTargetFactoryManager;
import org.apache.avalon.excalibur.logger.LogTargetManager;
import org.apache.avalon.excalibur.logger.util.LoggerUtil;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.util.Closeable;

/**
 * Tie this object to a LoggerManagerTee, give it the Hierachy
 * that LogKitAdapter operates upon and it will populate it
 * from the Configuration object passed via configure().
 * Note: this class assumes that this is a new Hierarchy,
 * freshly created with new Hierarchy() not populated before.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class LogKitConfHelper extends AbstractLogEnabled implements
        Contextualizable,
        Configurable,
        Disposable
{
    /* The hierarchy to operate upon */
    private final Hierarchy m_hierarchy;

    /* Creates an instance of LogKitLoggerHelper. */
    public LogKitConfHelper( final Hierarchy hierarchy )
    {
        if ( hierarchy == null ) throw new NullPointerException( "hierarchy" );
        m_hierarchy = hierarchy;
    }

    /** Set of log targets */
    final private Set m_targets = new HashSet();

    /** The context object */
    private Context m_context;

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
     * Populates the underlying <code>Hierarchy</code>.
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
        setupLoggers( targetManager,
                      null,
                      categories,
                      true,
                      categories.getAttributeAsBoolean( "additive", false ) );
    }

    /**
     * Setup a LogTargetFactoryManager
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    private final LogTargetFactoryManager setupTargetFactoryManager(
            final Configuration configuration )
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
     * @param parentElement  The array object of configurations for categories.
     * @param root shows if we're processing the root of the configuration
     * @throws ConfigurationException if the configuration is malformed
     */
    private final void setupLoggers( final LogTargetManager targetManager,
                                     final String parentCategory,
                                     final Configuration parentElement,
                                     boolean root,
                                     final boolean defaultAdditive )
        throws ConfigurationException
    {
        boolean rootLoggerConfigured = false;

        final Configuration[] categories = parentElement.getChildren( "category" );

        if( null != categories )
        {
            for( int i = 0; i < categories.length; i++ )
            {
                final Configuration category = categories[ i ];
                final String name = category.getAttribute( "name" );
                final String loglevel = category.getAttribute( "log-level" ).toUpperCase();
                final boolean additive = category.
                    getAttributeAsBoolean( "additive", defaultAdditive );

                final Configuration[] targets = category.getChildren( "log-target" );
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

                final String fullCategory;
                final org.apache.log.Logger logger;

                if ( "".equals( name ) )
                {
                    if ( !root )
                    {
                        final String message = "'category' element with empty name not " +
                                "at the root level: " + category.getLocation();
                        throw new ConfigurationException( message );
                    }

                    if ( logTargets.length == 0 )
                    {
                        final String message = "At least one log-target should be " +
                                "specified for the root category " + category.getLocation();
                        throw new ConfigurationException( message );
                    }

                    fullCategory = null;
                    logger = m_hierarchy.getRootLogger();
                    rootLoggerConfigured = true;
                }
                else
                {
                    fullCategory = LoggerUtil.getFullCategoryName( parentCategory, name );
                    logger = m_hierarchy.getLoggerFor( fullCategory );
                }

                if( getLogger().isDebugEnabled() )
                {
                    /**
                     * We have to identify ourselves now via 'LogKitConfHelper:'
                     * because we are likely to be logging to a shared bootstrap
                     * logger, not to a dedicated category Logger.
                     */
                    final String message = "LogKitConfHelper: adding logger for category '" +
                            ( fullCategory != null ? fullCategory : "" ) + "'";
                    getLogger().debug( message );
                }

                logger.setPriority( Priority.getPriorityForName( loglevel ) );
                logger.setLogTargets( logTargets );
                logger.setAdditivity( additive );

                setupLoggers( targetManager, fullCategory, category, false, defaultAdditive );
            }
        }

        if ( root && !rootLoggerConfigured )
        {
            final String message =
                    "No configuration for root category (<category name=''/>) found in "+
                            parentElement.getLocation();
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
}
