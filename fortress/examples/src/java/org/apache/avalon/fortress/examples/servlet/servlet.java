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

package org.apache.avalon.fortress.examples.servlet;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.framework.container.ContainerUtil;

import java.io.IOException;
import java.io.File;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * Servlet based Fortress container example.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: servlet.java,v 1.9 2004/02/24 22:31:22 niclas Exp $
 */
public final class servlet extends HttpServlet
{
    private ServletContainer m_container;
    private ContainerManager m_containerManager;

    /**
     * Initializes Servlet and creates a <code>ServletContainer</code> instance
     *
     * @exception ServletException if an error occurs
     */
    public void init()
        throws ServletException
    {
        super.init();

        try
        {
            final org.apache.avalon.fortress.util.FortressConfig config = new org.apache.avalon.fortress.util.FortressConfig();
            config.setContainerClass( ServletContainer.class );
            config.setContextDirectory( getServletContext().getRealPath("/") );
            config.setWorkDirectory( (File) getServletContext().getAttribute( "javax.servlet.context.tempdir" ) );
            config.setContainerConfiguration( "resource://org/apache/avalon/fortress/examples/servlet/ServletContainer.xconf" );
            config.setLoggerManagerConfiguration( "resource://org/avalon/excalibur/fortress/examples/servlet/ServletContainer.xlog" );

            m_containerManager = new DefaultContainerManager( config.getContext() );
            ContainerUtil.initialize( m_containerManager );

            m_container = (ServletContainer)m_containerManager.getContainer();
        }
        catch( Exception e )
        {
            throw new ServletException( "Error during initialization", e );
        }
    }

    /**
     * Pass all servlet requests through to container to be handled. In a more
     * complex system, there could be multiple containers that handle different
     * requests, or a main controlling container with subcontainers for different
     * requests.
     *
     * @param request a <code>ServletRequest</code> instance
     * @param response a <code>ServletResponse</code> instance
     * @exception IOException if an IO error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void service( ServletRequest request, ServletResponse response )
        throws IOException, ServletException
    {
        m_container.handleRequest( request, response );
    }

    /**
     * Disposes of container manager and container instance.
     */
    public void destroy()
    {
        ContainerUtil.dispose( m_containerManager );
    }
}
