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

import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Default LogTargetFactoryManager implementation.  It populates the LogTargetFactoryManager
 * from a configuration file.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.15 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class DefaultLogTargetFactoryManager
    extends AbstractLogEnabled
    implements LogTargetFactoryManager, Contextualizable, Configurable
{
    /** Map for name to logger mapping */
    private final Map m_factories = new HashMap();

    /** The context object */
    private Context m_context;

    /**
     * The classloader to use to load target factorys.
     */
    private ClassLoader m_classLoader;

    /**
     * Retrieves a LogTargetFactory from a name. Usually
     * the factory name refers to a element name. If
     * this LogTargetFactoryManager does not have the match a null
     * will be returned.
     *
     * @param factoryName The name of a configured LogTargetFactory.
     * @return the LogTargetFactory or null if none is found.
     */
    public final LogTargetFactory getLogTargetFactory( final String factoryName )
    {
        return (LogTargetFactory)m_factories.get( factoryName );
    }

    /**
     * Reads a context object.
     *
     * @param context The context object.
     */
    public final void contextualize( final Context context )
    {
        m_context = context;
        try
        {
            m_classLoader = (ClassLoader)m_context.get( ClassLoader.class.getName() );
        }
        catch( ContextException ce )
        {
            try
            {
                m_classLoader = (ClassLoader)m_context.get( "classloader" );
            }
            catch( ContextException e )
            {
                m_classLoader = Thread.currentThread().getContextClassLoader();
            }
        }
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
        final Configuration[] confs = configuration.getChildren( "factory" );
        for( int i = 0; i < confs.length; i++ )
        {
            final String factoryClass = confs[ i ].getAttribute( "class" );
            final String factoryType = confs[ i ].getAttribute( "type" );

            final LogTargetFactory logTargetFactory;
            try
            {
                Class clazz = null;
                try
                {
                    clazz = m_classLoader.loadClass( factoryClass );
                }
                catch( final ClassNotFoundException cnfe )
                {
                    clazz = getClass().getClassLoader().loadClass( factoryClass );
                }

                logTargetFactory = (LogTargetFactory)clazz.newInstance();
            }
            catch( final ClassNotFoundException cnfe )
            {
                final String message =
                    "Cannot instantiate LogTargetFactory class " + factoryClass;
                throw new ConfigurationException( message, cnfe );
            }
            catch( final InstantiationException ie )
            {
                final String message =
                    "Cannot instantiate LogTargetFactory class " + factoryClass;
                throw new ConfigurationException( message, ie );
            }
            catch( final IllegalAccessException iae )
            {
                final String message =
                    "Cannot access LogTargetFactory class " + factoryClass;
                throw new ConfigurationException( message, iae );
            }

            ContainerUtil.enableLogging( logTargetFactory, getLogger() );
            try
            {
                ContainerUtil.contextualize( logTargetFactory, m_context );
            }
            catch( final ContextException ce )
            {
                final String message =
                    "Cannot contextualize LogTargetFactory " + factoryClass;
                throw new ConfigurationException( message, ce );
            }
            ContainerUtil.configure( logTargetFactory, confs[ i ] );

            if( logTargetFactory instanceof LogTargetFactoryManageable )
            {
                ( (LogTargetFactoryManageable)logTargetFactory ).setLogTargetFactoryManager( this );
            }

            if( getLogger().isDebugEnabled() )
            {
                final String message =
                    "Added new LogTargetFactory of type " + factoryType;
                getLogger().debug( message );
            }
            m_factories.put( factoryType, logTargetFactory );
        }
    }
}
