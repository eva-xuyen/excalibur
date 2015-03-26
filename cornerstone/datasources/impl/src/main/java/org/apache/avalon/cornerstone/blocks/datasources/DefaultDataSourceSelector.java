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

package org.apache.avalon.cornerstone.blocks.datasources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.cornerstone.services.datasources.DataSourceSelector;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceException;

/**
 * A default implementation for DataSourceSelector.
 * The Configuration is like this:
 *
 * <pre>
 * &lt;myBlock&gt;
 *   &lt;data-source name="<i>default</i>"
 *     class="<i>org.apache.avalon.excalibur.datasource.JdbcDataSource</i>"&gt;
 *     &lt;!-- configuration for JdbcDataSource --&gt;
 *     &lt;pool-controller min="<i>5</i>" max="<i>10</i>" connection-class="<i>my.overrided.ConnectionClass</i>"&gt;
 *       &lt;keep-alive&gt;select 1&lt;/keep-alive&gt;
 *     &lt;/pool-controller&gt;
 *     &lt;driver&gt;<i>com.database.jdbc.JdbcDriver</i>&lt;/driver&gt;
 *     &lt;dburl&gt;<i>jdbc:driver://host/mydb</i>&lt;/dburl&gt;
 *     &lt;user&gt;<i>username</i>&lt;/user&gt;
 *     &lt;password&gt;<i>password</i>&lt;/password&gt;
 *   &lt;/data-source&gt;
 * &lt;/myBlock&gt;
 * </pre>
 *
 * @author <a href="mailto:avalon-dev@jakarta.apache.org">Avalon Development Team</a>
 * @avalon.component name="data-source-selector" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.datasources.DataSourceSelector"
 */
public class DefaultDataSourceSelector
    extends AbstractLogEnabled
    implements DataSourceSelector, Contextualizable, Configurable, Initializable, Disposable
{
    private Configuration m_configuration;
    private Map m_dataSources;
    private String m_blockName;


   /**
    * Contextualization of the component by the container.
    * @param context the supplied context object
    * @avalon.entry key="urn:avalon:name" alias="block.name"
    */
    public void contextualize( final Context context )
        throws ContextException
    {
        try
        {
            m_blockName = (String) context.get( "urn:avalon:name" );
        }
        catch( Throwable e )
        {
            // handle legacy scenario
            try
            {
                m_blockName = (String)context.get( "block.name" );
            }
            catch( Throwable ee )
            {
                m_blockName = "DataSourceSelector/" + System.identityHashCode( this );
            }
        }
    }

    /**
     * @avalon.configuration schema="relax-ng"
     */
    public void configure( final Configuration configuration )
    {
        m_configuration = configuration;
    }

    public void initialize()
        throws Exception
    {
        m_dataSources = new HashMap();

        Configuration[] dataSourceConfs = getDataSourceConfig();

        for( int i = 0; i < dataSourceConfs.length; i++ )
        {
            final Configuration dataSourceConf = dataSourceConfs[ i ];

            final String name = dataSourceConf.getAttribute( "name" );
            final String clazz = dataSourceConf.getAttribute( "class" );
            final String driver = dataSourceConf.getChild( "driver", true ).getValue( "" );

            final ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

            DataSourceComponent component = null;
            if( null == classLoader )
            {
                if( !"".equals( driver ) )
                {
                    Class.forName( driver, true, Thread.currentThread().getContextClassLoader() );
                }

                component = (DataSourceComponent)Class.forName( clazz ).newInstance();
            }
            else
            {
                if( !"".equals( driver ) )
                {
                    classLoader.loadClass( driver );
                }

                component = (DataSourceComponent)classLoader.loadClass( clazz ).newInstance();
            }

            if( component instanceof LogEnabled )
            {
                setupLogger( component, name );
            }
            component.configure( dataSourceConf );
            m_dataSources.put( name, component );

            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "DataSource " + name + " ready" );
            }
        }
    }

    private Configuration[] getDataSourceConfig()
    {
        final Configuration head =
            m_configuration.getChild( "data-sources" );
        if( 0 != head.getChildren().length )
        {

            final String message =
                "WARNING: Child node <data-sources/> in " +
                "configuration of component named " + m_blockName +
                " has been deprecated. Please put <data-source/> elements" +
                " in root configuration element";
            getLogger().warn( message );
            System.out.println( message );
            return head.getChildren( "data-source" );
        }
        else
        {
            return m_configuration.getChildren( "data-source" );
        }
    }

    public void dispose()
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "disposal" );
        }
        final Iterator keys = m_dataSources.keySet().iterator();
        while( keys.hasNext() )
        {
            final DataSourceComponent dsc =
                (DataSourceComponent)m_dataSources.get( keys.next() );
            if( dsc instanceof Disposable )
            {
                ( (Disposable)dsc ).dispose();
            }
        }
    }

    public boolean isSelectable( final Object hint )
    {
        return m_dataSources.containsKey( hint );
    }

    public Object select( final Object hint )
        throws ServiceException
    {
        final Object component = m_dataSources.get( hint );

        if( null == component )
        {
            throw new ServiceException( hint.toString(), "Unable to provide DataSourceComponent for " + hint );
        }

        return component;
    }

    public void release( final Object component )
    {
        //do nothing
    }
}
