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
package org.apache.avalon.excalibur.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.output.io.FileTarget;
import org.apache.log.util.Closeable;

/**
 * A {@link LoggerManager} that supports the old &lt;logs version="1.0"/&gt;
 * style logging configuration from
 * <a href="http://jakarta.apache.org/avalon/phoenix">Phoenix</a>.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class SimpleLogKitManager
    extends AbstractLogEnabled
    implements LoggerManager, Contextualizable, Configurable, Disposable
{
    private static final String DEFAULT_FORMAT =
        "%7.7{priority} %23.23{time:yyyy-MM-dd' 'HH:mm:ss.SSS} [%8.8{category}] (%{context}): "
        + "%{message}\n%{throwable}";

    ///Base directory of applications working directory
    private File m_baseDirectory;

    /**
     *  Hierarchy of Application logging
     */
    private final Hierarchy m_hierarchy = new Hierarchy();

    /**
     * The root logger in hierarchy.
     */
    private final Logger m_logkitLogger = m_hierarchy.getLoggerFor( "" );

    /**
     * The root logger wrapped using AValons Logging Facade.
     */
    private org.apache.avalon.framework.logger.Logger m_logger =
        new LogKitLogger( m_logkitLogger );
    private Collection m_targets;

    /**
     * Contextualize the manager. Requires that the "app.home" entry
     * be set to a File object that points at the base directory for logs.
     *
     * @param context the context
     * @throws ContextException if missing context entry
     */
    public void contextualize( final Context context )
        throws ContextException
    {
        try
        {
            m_baseDirectory = (File)context.get( "app.home" );
        }
        catch( ContextException e )
        {
            m_baseDirectory = new File( "." );
        }
    }

    /**
     * Interpret configuration to build loggers.
     *
     * @param configuration the configuration
     * @throws ConfigurationException if malformed configuration
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration[] targets = configuration.getChildren( "log-target" );
        final HashMap targetSet = configureTargets( targets );
        m_targets = targetSet.values();
        final Configuration[] categories = configuration.getChildren( "category" );
        configureCategories( categories, targetSet );
    }

    /**
     * Close any closable log targets opened by LoggerManager.
     */
    public void dispose()
    {
        final Iterator iterator = m_targets.iterator();
        while( iterator.hasNext() )
        {
            final LogTarget logTarget = (LogTarget)iterator.next();
            if( logTarget instanceof Closeable )
            {
                ( (Closeable)logTarget ).close();
            }
        }
    }

    /**
     * Retrieve a logger by name.
     *
     * @param name the name of logger
     * @return the specified Logger
     */
    public org.apache.avalon.framework.logger.Logger
        getLoggerForCategory( final String name )
    {
        return m_logger.getChildLogger( name );
    }

    /**
     * Retrieve the root logger.
     *
     * @return the root Logger
     */
    public org.apache.avalon.framework.logger.Logger getDefaultLogger()
    {
        return m_logger;
    }

    /**
     * Configure a set of logtargets based on config data.
     *
     * @param targets the target configuration data
     * @return a Map of target-name to target
     * @throws ConfigurationException if an error occurs
     */
    private HashMap configureTargets( final Configuration[] targets )
        throws ConfigurationException
    {
        final HashMap targetSet = new HashMap();

        for( int i = 0; i < targets.length; i++ )
        {
            final Configuration target = targets[ i ];
            final String name = target.getAttribute( "name" );
            String location = target.getAttribute( "location" ).trim();
            final String format = target.getAttribute( "format", DEFAULT_FORMAT );
            final boolean append = target.getAttributeAsBoolean( "append", true );

            if( '/' == location.charAt( 0 ) )
            {
                location = location.substring( 1 );
            }

            final AvalonFormatter formatter = new AvalonFormatter( format );

            //Specify output location for logging
            final File file = new File( m_baseDirectory, location );

            //Setup logtarget
            FileTarget logTarget = null;
            try
            {
                logTarget = new FileTarget( file.getAbsoluteFile(), append, formatter );
            }
            catch( final IOException ioe )
            {
                final String message =
                    "Error creating LogTarget named \"" + name + "\" for file " +
                    file + ". (Reason: " + ioe.getMessage() + ").";
                throw new ConfigurationException( message, ioe );
            }

            targetSet.put( name, logTarget );
        }

        return targetSet;
    }

    /**
     * Configure Logging categories.
     *
     * @param categories configuration data for categories
     * @param targets a hashmap containing the already existing taregt
     * @throws ConfigurationException if an error occurs
     */
    private void configureCategories( final Configuration[] categories, final HashMap targets )
        throws ConfigurationException
    {
        for( int i = 0; i < categories.length; i++ )
        {
            final Configuration category = categories[ i ];
            final String name = category.getAttribute( "name", "" );
            final String target = category.getAttribute( "target" );
            final String priorityName = category.getAttribute( "priority" );

            final Logger logger =
                m_logkitLogger.getChildLogger( name );

            final LogTarget logTarget = (LogTarget)targets.get( target );
            if( null == target )
            {
                final String message =
                    "Unable to locate LogTarget named \"" + target +
                    "\" for Logger named \"" + name + "\".";
                throw new ConfigurationException( message );
            }

            final Priority priority = Priority.getPriorityForName( priorityName );
            if( !priority.getName().equals( priorityName ) )
            {
                final String message =
                    "Unknown priority \"" + priorityName + "\" for Logger named \"" +
                    name + "\".";
                throw new ConfigurationException( message );
            }

            if( getLogger().isDebugEnabled() )
            {
                final String message =
                    "Creating a log category named \"" + name +
                    "\" that writes to \"" + target + "\" target at priority \"" +
                    priorityName + "\".";
                getLogger().debug( message );
            }

            if( name.equals( "" ) )
            {
                m_logkitLogger.setPriority( priority );
                m_logkitLogger.setLogTargets( new LogTarget[] {logTarget} );
            }
            else
            {
                logger.setPriority( priority );
                logger.setLogTargets( new LogTarget[]{logTarget} );
            }
        }
    }
}
