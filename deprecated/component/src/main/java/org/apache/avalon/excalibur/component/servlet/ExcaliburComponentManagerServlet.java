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
package org.apache.avalon.excalibur.component.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.apache.avalon.excalibur.component.ExcaliburComponentManagerCreator;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.instrument.InstrumentManager;

/**
 * Makes it possible for servlets to work with Avalon components
 *  without having to do any coding to setup and manage the
 *  lifecycle of the ServiceManager (ComponentManager).
 * <p>
 * To make use of the ExcaliburComponentManagerServet.  You will
 *  need to define the servlet in your web.xml file as follows:
 * <pre>
 *  &lt;!-- ExcaliburComponentManagerServlet (for initializing ComponentManager).   --&gt;
 *  &lt;servlet&gt;
 *      &lt;servlet-name&gt;ExcaliburComponentManagerServlet&lt;/servlet-name&gt;
 *      &lt;display-name&gt;ExcaliburComponentManagerServlet&lt;/display-name&gt;
 *      &lt;description&gt;Creates component manager, does not service requests.&lt;/description&gt;
 *      &lt;servlet-class&gt;
 *          org.apache.avalon.excalibur.component.servlet.ExcaliburComponentManagerServlet
 *      &lt;/servlet-class&gt;
 *
 *      &lt;!-- This parameter points to the logkit configuration file.             --&gt;
 *      &lt;!-- Note that the path is specified in absolute notation but it will be --&gt;
 *      &lt;!-- resolved relative to the servlets webapp context path               --&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;logkit&lt;/param-name&gt;
 *          &lt;param-value&gt;/WEB-INF/logkit.xml&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *
 *      &lt;!-- This parameter points to the components configuration file.         --&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;components&lt;/param-name&gt;
 *          &lt;param-value&gt;/WEB-INF/components.xml&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *
 *      &lt;!-- Roles file supplements configuration file to make the latter        --&gt;
 *      &lt;!-- more readable. Most likely you don't want to change the roles       --&gt;
 *      &lt;!-- file --&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;roles&lt;/param-name&gt;
 *          &lt;param-value&gt;/WEB-INF/roles.xml&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *
 *      &lt;!-- This parameter points to the instrument manager configuration file. --&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;instrument&lt;/param-name&gt;
 *          &lt;param-value&gt;/WEB-INF/instrument.xml&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *
 *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *  &lt;/servlet&gt;
 * </pre>
 * Please pay particular attention to the load-on-startup element.  It is used
 * to control the order in which servlets are started by the servlet engine.
 * It must have a value which is less than any other servlets making use of
 * the ServiceManager.  This is to ensure that the ServiceManager is
 * initialized before any other servlets attempt to start using it.
 * <p>
 * All of the configuration files are located in the WEB-INF directory by
 * default.  The instrument configuration file is optional.  Please see the
 * {@link org.apache.avalon.excalibur.component.ExcaliburComponentManagerCreator}
 * class for details on what goes into these configuration files.  Note that
 * the lifecycle of the ExcaliburComponentManagerCreator is managed automatically
 * by this servlet, so there is no need to access the class directly.
 * <p>
 * Once the servlet has been configured, other servlets may gain access to
 * the ServiceManager (ComponentManager), InstrumentManager and LoggerManager
 * via the ServletContext using the following code within a servlet:
 * <pre>
 *  // Get a reference to the ServletContext
 *  ServletContext servletContext = getServletContext();
 *
 *  // Acquire the LoggerManager
 *  LoggerManager loggerManager =
 *      (LoggerManager)m_servletContext.getAttribute( LoggerManager.class.getName() );
 *
 *  // Acquire the InstrumentManager
 *  InstrumentManager instrumentManager =
 *      (InstrumentManager)m_servletContext.getAttribute( InstrumentManager.class.getName() );
 *
 *  // Acquire the ServiceManager
 *  ServiceManager serviceManager =
 *      (ServiceManager)m_servletContext.getAttribute( ServiceManager.class.getName() );
 *
 *  // Acquire the ComponentManager ( Deprecated )
 *  ComponentManager componentManager =
 *      (ComponentManager)m_servletContext.getAttribute( ComponentManager.class.getName() );
 * </pre>
 * The ExcaliburComponentManagerServlet makes use of a proxy system to manage
 * reference to the above managers, so it is not necessary to release them
 * when a servlet is done using them.
 * <p>
 * It may be necessary to add the following code to the end of the dispose method of any
 *  servlets referencing any of the above proxies.  This is because on some containers,
 *  like Tomcat, the classloader is immediately invalidated after the last servlet is
 *  disposed.  If this happens before the managers have all been disposed, then you may
 *  see errors in the console like: <code>WebappClassLoader: Lifecycle error : CL stopped</code>
 * <pre>
 *  System.gc();
 *  try
 *  {
 *      Thread.sleep(250);
 *  }
 *  catch ( InterruptedException e ) {}
 * </pre>
 * Note that servlets which extend the AbstractComponentManagerServlet will behave correctly.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
public class ExcaliburComponentManagerServlet
    extends GenericServlet
{
    private ExcaliburComponentManagerCreator m_componentManagerCreator;

    /** Latch used to shutdown the ExcaliburComponentManagerCreator cleanly. */
    private Latch m_latch;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/

    /*---------------------------------------------------------------
     * GenericServlet Methods
     *-------------------------------------------------------------*/
    /**
     * Builds the component manager and stores references to the
     *  ComponentManager, LoggerManager, and InstrumentManager into
     *  the ServletContext.
     *
     * @param servletConfig Servlet configuration
     *
     * @throws ServletException If there are any problems initializing the
     *                          servlet.
     */
    public void init( ServletConfig servletConfig ) throws ServletException
    {
        super.init( servletConfig );

        //System.out.println( "ExcaliburComponentManagerServlet.init() BEGIN" );
        ServletContext servletContext = getServletContext();

        InputStream loggerManagerConfigStream = null;
        InputStream roleManagerConfigStream = null;
        InputStream componentManagerConfigStream = null;
        InputStream instrumentManagerConfigStream = null;
        try
        {
            loggerManagerConfigStream =
                getStreamFromParameter( servletConfig, "logkit", true );
            roleManagerConfigStream =
                getStreamFromParameter( servletConfig, "roles", true );
            componentManagerConfigStream =
                getStreamFromParameter( servletConfig, "components", true );
            instrumentManagerConfigStream =
                getStreamFromParameter( servletConfig, "instrument", false );

            // Create the ComponentManagerCreator
            try
            {
                m_componentManagerCreator = new ExcaliburComponentManagerCreator(
                    null,
                    loggerManagerConfigStream,
                    roleManagerConfigStream,
                    componentManagerConfigStream,
                    instrumentManagerConfigStream );
            }
            catch( Exception e )
            {
                String msg = "Unable to create the ComponentManagerCreator.  "
                    + "Most likely a comfiguration problem.";
                throw new ServletException( msg, e );
            }
        }
        finally
        {
            // Close the resource streams
            try
            {
                if( loggerManagerConfigStream != null )
                {
                    loggerManagerConfigStream.close();
                }
                if( roleManagerConfigStream != null )
                {
                    roleManagerConfigStream.close();
                }
                if( componentManagerConfigStream != null )
                {
                    componentManagerConfigStream.close();
                }
                if( instrumentManagerConfigStream != null )
                {
                    instrumentManagerConfigStream.close();
                }
            }
            catch( IOException e )
            {
                throw new ServletException( "Encountered an error closing resource streams.", e );
            }
        }

        LoggerManager loggerManager = m_componentManagerCreator.getLoggerManager();

        // A series of ReferenceProxies which will be used to access the ComponentManager
        //  and other managers created by the ComponentManagerCreator must be created.
        //  This is necessary because the order in which servlets are shut down by a
        //  ServletContainer can not be controlled.  If a manager is disposed before all
        //  servlets have released their references to it, then errors can result.

        // Create the latch which will manager the ReferenceProxies.
        m_latch = new Latch( m_componentManagerCreator );
        m_latch.enableLogging( loggerManager.getLoggerForCategory( "system.ecmservlet" ) );

        // Create the actual ReferenceProxies.
        ReferenceProxy loggerManagerProxy = m_latch.createProxy(
            loggerManager, "LoggerManager" );
        ReferenceProxy serviceManagerProxy = m_latch.createProxy(
            m_componentManagerCreator.getServiceManager(), "ServiceManager" );
        ReferenceProxy componentManagerProxy = m_latch.createProxy(
            m_componentManagerCreator.getComponentManager(), "ComponentManager" );
        ReferenceProxy instrumentManagerProxy = m_latch.createProxy(
            m_componentManagerCreator.getInstrumentManager(), "InstrumentManager" );

        // Store references to the proxies in the ServletContext so that other servlets can gain
        //  access to them
        servletContext.setAttribute( LoggerManager.class.getName(),     loggerManagerProxy );
        servletContext.setAttribute( ServiceManager.class.getName(),    serviceManagerProxy );
        servletContext.setAttribute( ComponentManager.class.getName(),  componentManagerProxy );
        servletContext.setAttribute( InstrumentManager.class.getName(), instrumentManagerProxy );

        //System.out.println( "ExcaliburComponentManagerServlet.init() END" );
    }

    /**
     * Called by the servlet container to destroy the servlet.
     */
    public void destroy()
    {
        //System.out.println( "ExcaliburComponentManagerServlet.destroy() BEGIN" );

        ServletContext servletContext = getServletContext();

        // Remove the references to the managers from the servlet context.
        servletContext.removeAttribute( LoggerManager.class.getName() );
        servletContext.removeAttribute( ServiceManager.class.getName() );
        servletContext.removeAttribute( ComponentManager.class.getName() );
        servletContext.removeAttribute( InstrumentManager.class.getName() );

        // Tell the latch that we are ready for it do dispose of the ECMC
        m_latch.requestTrigger();

        //System.out.println( "ExcaliburComponentManagerServlet.destroy() END" );
    }

    /**
     * This servlet does not accept requests.  It will complain if called.
     *
     * @param req servlet request
     * @param res servlet response
     *
     * @throws UnavailableException always
     */
    public void service( ServletRequest servletRequest, ServletResponse servletResponse )
        throws UnavailableException
    {
        throw new UnavailableException( getClass().getName() + " does not except service requests." );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Looks up a specified resource name and returns it as an InputStream.
     *  It is the responsibility of the caller to close the stream.
     *
     * @param servletConfig ServletConfig.
     * @param resourceName Name of the resource to be loaded.
     * @param required True if an error should be thrown if the property is missing.
     *
     * @return InputStream used to read the contents of the resource.
     *
     * @throws ServletException If the specified resource does not exist,
     *                          or could not be opened.
     */
    private InputStream getStreamFromParameter( ServletConfig servletConfig,
                                                String resourceName,
                                                boolean required )
        throws ServletException
    {

        String configFileName = servletConfig.getInitParameter( resourceName );

        if( configFileName == null )
        {
            if( required )
            {
                throw new ServletException( resourceName
                                            + " parameter must be provided in servlet configuration." );
            }
            else
            {
                return null;
            }
        }

        ServletContext servletContext = servletConfig.getServletContext();

        log( "Attempting to access resource: " + configFileName );

        InputStream is = servletContext.getResourceAsStream( configFileName );

        if( is == null )
        {
            throw new ServletException( "Resource '" + configFileName + "' is not available." );
        }

        return is;
    }

    /*---------------------------------------------------------------
     * Private Classes
     *-------------------------------------------------------------*/
    private static class Latch
        extends AbstractReferenceProxyLatch
    {
        ExcaliburComponentManagerCreator m_componentManagerCreator;

        /*---------------------------------------------------------------
         * Constructors
         *-------------------------------------------------------------*/
        /**
         * Create a new Latch.
         *
         * @param ecmc The ExcaliburComponentManagerCreator to be disposed
         *             when all proxies are done.
         */
        Latch( ExcaliburComponentManagerCreator componentManagerCreator )
        {
            m_componentManagerCreator = componentManagerCreator;
        }

        /*---------------------------------------------------------------
         * AbstractReferenceProxyLatch Methods
         *-------------------------------------------------------------*/
        /**
         * Called when all of the proxies have notified that they are done.
         */
        public void triggered()
            throws Exception
        {
            //System.out.println( "ExcaliburComponentManagerServlet.Latch.triggered() BEGIN" );
            ContainerUtil.shutdown( m_componentManagerCreator );
            //System.out.println( "ExcaliburComponentManagerServlet.Latch.triggered() END" );
        }
    }
}
