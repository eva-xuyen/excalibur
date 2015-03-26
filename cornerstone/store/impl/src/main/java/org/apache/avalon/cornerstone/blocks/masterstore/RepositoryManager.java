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

package org.apache.avalon.cornerstone.blocks.masterstore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.avalon.cornerstone.services.store.Repository;
import org.apache.avalon.cornerstone.services.store.Store;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 * @avalon.component name="repository-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.store.Store"
 */
public class RepositoryManager
    extends AbstractLogEnabled
    implements Store, Contextualizable, Serviceable, Configurable
{
    private static final String REPOSITORY_NAME = "Repository";
    private static long id = 0;

    protected HashMap m_repositories = new HashMap();
    protected HashMap m_models = new HashMap();
    protected HashMap m_classes = new HashMap();
    protected ServiceManager m_serviceManager;
    protected Context m_context;

   /**
    * @avalon.entry key="urn:avalon:name" alias="block.name"
    * @avalon.entry key="urn:avalon:partition" alias="app.name"
    * @avalon.entry key="urn:avalon:home" type="java.io.File" alias="app.home"
    */
    public void contextualize( final Context context )
    {
        m_context = context;
    }

    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration[] registeredClasses =
            configuration.getChild( "repositories" ).getChildren( "repository" );

        for( int i = 0; i < registeredClasses.length; i++ )
        {
            registerRepository( registeredClasses[ i ] );
        }
    }

    public void registerRepository( final Configuration repConf )
        throws ConfigurationException
    {
        final String className = repConf.getAttribute( "class" );
        getLogger().info( "Registering Repository " + className );

        final Configuration[] protocols =
            repConf.getChild( "protocols" ).getChildren( "protocol" );
        final Configuration[] types = repConf.getChild( "types" ).getChildren( "type" );
        final Configuration[] modelIterator =
            repConf.getChild( "models" ).getChildren( "model" );

        for( int i = 0; i < protocols.length; i++ )
        {
            final String protocol = protocols[ i ].getValue();

            for( int j = 0; j < types.length; j++ )
            {
                final String type = types[ j ].getValue();

                for( int k = 0; k < modelIterator.length; k++ )
                {
                    final String model = modelIterator[ k ].getValue();
                    m_classes.put( protocol + type + model, className );
                    getLogger().info( "   for " + protocol + "," + type + "," + model );
                }
            }
        }
    }

    public void release( final Object service )
    {
    }

    public boolean isSelectable( final Object policy )
    {
        return ( policy instanceof Configuration );
    }

    public Object select( final Object policy )
        throws ServiceException
    {
        Configuration repConf = null;
        try
        {
            repConf = (Configuration)policy;
        }
        catch( final ClassCastException cce )
        {
            throw new ServiceException( policy.toString(), "Hint is of the wrong type. " +
                                                           "Must be a Configuration", cce );
        }

        URL destination = null;
        try
        {
            destination = new URL( repConf.getAttribute( "destinationURL" ) );
        }
        catch( final ConfigurationException ce )
        {
            throw new ServiceException( policy.toString(), "Malformed configuration has no " +
                                                           "destinationURL attribute", ce );
        }
        catch( final MalformedURLException mue )
        {
            throw new ServiceException( policy.toString(), "destination is malformed. " +
                                                           "Must be a valid URL", mue );
        }

        try
        {
            final String type = repConf.getAttribute( "type" );
            final String repID = destination + type;
            Repository reply = (Repository)m_repositories.get( repID );
            final String model = (String)repConf.getAttribute( "model" );

            if( null != reply )
            {
                if( m_models.get( repID ).equals( model ) )
                {
                    return reply;
                }
                else
                {
                    final String message = "There is already another repository with the " +
                        "same destination and type but with different model";
                    throw new ServiceException( policy.toString(), message );
                }
            }
            else
            {
                final String protocol = destination.getProtocol();
                final String repClass = (String)m_classes.get( protocol + type + model );

                getLogger().debug( "Need instance of " + repClass + " to handle: " +
                                   protocol + type + model );

                try
                {
                    reply = (Repository)Class.forName( repClass ).newInstance();

                    setupLogger( reply, "repository" );

                    ContainerUtil.contextualize( reply, m_context );
                    ContainerUtil.service( reply, m_serviceManager );
                    ContainerUtil.configure( reply, repConf );
                    ContainerUtil.initialize( reply );

                    m_repositories.put( repID, reply );
                    m_models.put( repID, model );
                    getLogger().info( "New instance of " + repClass + " created for " +
                                      destination );
                    return reply;
                }
                catch( final Exception e )
                {
                    final String message = "Cannot find or init repository: " + e.getMessage();
                    getLogger().warn( message, e );

                    throw new ServiceException( policy.toString(), message, e );
                }
            }
        }
        catch( final ConfigurationException ce )
        {
            throw new ServiceException( policy.toString(), "Malformed configuration", ce );
        }
    }

    public static final String getName()
    {
        return REPOSITORY_NAME + id++;
    }
}
