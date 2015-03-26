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
package org.apache.avalon.fortress.testcase;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.PrepareHandlerCommand;
import org.apache.avalon.fortress.util.FortressConfig;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * JUnit TestCase for Components run under Fortress.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExcaliburTestCase.java,v 1.6 2004/02/28 11:47:27 cziegeler Exp $
 */
public class FortressTestCase
    extends TestCase
{
    /* Name of the user test case class minus its package. */
    private final String m_className;
    
    /** Reference to the container manager for use in the shutdown hook. */
    private ContainerManager m_containerManager;
    
    /** The Fortress Container instance */
    private DefaultContainer m_container;
    
    /** Reference to the Service Manager. */
    private ServiceManager m_serviceManager;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public FortressTestCase( String name )
    {
        super( name );
        
        String className = getClass().getName();
        int pos = className.lastIndexOf( '.' );
        if ( pos >= 0 )
        {
            className = className.substring( pos + 1 );
        }
        m_className = className;
    }
    
    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    public void setUp()
        throws Exception
    {
        //System.out.println( "setUp()" );
        
        // Create and initialize a fortress container
        FortressConfig config = new FortressConfig();
        config.setContainerClass( DefaultContainer.class.getName() );
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        
        // Set the container configuration.  User is required to create it.
        InputStream is = locateConfigurationResource( getClass(), m_className + ".xconf" );
        if ( is == null )
        {
            fail( "Unable to locate the '" + m_className + ".xconf' resource in the same package "
                + "as " + getClass().getName() );
        }
        try
        {
            config.setContainerConfiguration( loadConfiguration( is ) );
        }
        finally
        {
            is.close();
        }
        
        // Set the logger manager configuration.  The user may create it, but will fall back to
        //  using a default configuration.
        is = locateConfigurationResource( getClass(), m_className + ".xlog" );
        if ( is == null )
        {
            // Get the default
            is = locateConfigurationResource(
                FortressTestCase.class, "DefaultFortressTestCase.xlog" );
            if ( is == null )
            {
                fail( "Unable to locate the 'DefaultFortressTestCase.xlog' default resource in "
                    + "the same package as " + FortressTestCase.class.getName() );
            }
        }
        try
        {
            config.setLoggerManagerConfiguration( loadConfiguration( is ) );
        }
        finally
        {
            is.close();
        }
        
        // Set up the instrument manager configuration if it exists, no problem it does not.
        is = locateConfigurationResource( getClass(), m_className + ".instruments" );
        if ( is != null )
        {
            try
            {
                config.setInstrumentManagerConfiguration( loadConfiguration( is ) );
            }
            finally
            {
                is.close();
            }
        }
        
        Context context = initializeContext( config.getContext() );
        
        m_containerManager = new DefaultContainerManager( context );
        ContainerUtil.initialize( m_containerManager );
        
        m_container = (DefaultContainer)m_containerManager.getContainer();
        
        m_serviceManager = m_container.getServiceManager();
    }
    
    public void tearDown()
    {
        //System.out.println( "tearDown()" );
        
        // Tear down the Container
        m_serviceManager = null;
        if ( m_containerManager != null )
        {
            m_container = null;
            
            ContainerUtil.dispose( m_containerManager );
            m_containerManager = null;
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private InputStream locateConfigurationResource( Class clazz, String name )
    {
        return clazz.getResourceAsStream( name );
    }
    
    private Configuration loadConfiguration( InputStream is )
        throws Exception
    {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        return builder.build( is );
    }
    
    /**
     * Gives sublasses the oportunity to modify the Context before it
     *  is used to create the Container.
     * <p>
     * The context provided will have been marked read-only.  To make
     *  modifications to the context, it must first be wrapped in a
     *  new context instance as follows:
     * <pre>
     *   DefaultContext newContext = new DefaultContext( context );
     *   newContext.put( "key", "value" );
     *   newContext.makeReadOnly();
     *   return newContext;
     * </pre>
     * This version of the method simply returns the context instance
     *  unmodified.
     *
     * @param context The base Context object.
     *
     * @return The context which will be used to create the Container.
     */
    protected Context initializeContext( Context context )
    {
        return context;
    }
    
    /**
     * Returns a reference to the Fortress ServiceManager.
     *
     * @return The ServiceManager.
     */
    protected ServiceManager getServiceManager()
    {
        return m_serviceManager;
    }
}
