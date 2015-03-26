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

package org.apache.avalon.cornerstone.blocks.sockets;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Contains the code common for both TLS socket factories. They both
 * need to use an SSLFactoryBuilder which is configured using
 * configuration and context given by the container. Then, they both
 * set timeouts on the manufactured sockets.
 *
 * @author <a href="mailto:greg-avalon-apps at nest.cx">Greg Steuck</a>
 */
public abstract class AbstractTLSSocketFactory
    extends AbstractLogEnabled
    implements Contextualizable, Configurable, Initializable
{
    private final static int WAIT_FOREVER = 0;
    protected int m_socketTimeOut;

    private Context m_context;
    private Configuration m_childConfig;

    public void contextualize( final Context context )
    {
        m_context = context;
    }

    /**
     * Configures the factory.
     *
     * @param configuration the Configuration
     * @exception ConfigurationException if an error occurs
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        m_socketTimeOut = configuration.getChild( "timeout" ).getValueAsInteger( WAIT_FOREVER );
        m_childConfig = configuration.getChild( "ssl-factory", false );
        if( m_childConfig == null )
        {
            final String message = "ssl-factory child not found, please" +
                " update your configuration according to" +
                " the documentation. Reverting to the" +
                " old configuration format.";
            getLogger().warn( message );
            // not completely compatible though
            m_childConfig = configuration;
        }
    }

    /**
     * Creates an SSL factory using the confuration values.
     */
    public void initialize() throws Exception
    {
        final SSLFactoryBuilder builder = new SSLFactoryBuilder();
        setupLogger( builder );
        ContainerUtil.contextualize( builder, m_context );
        ContainerUtil.configure( builder, m_childConfig );
        ContainerUtil.initialize( builder );

        visitBuilder( builder );

        ContainerUtil.shutdown( builder );
        m_context = null;
        m_childConfig = null;
    }

    /**
     * The child factories have to use an instance of
     * <tt>SSLFactoryBuilder</tt> to obtain their factories.  So they
     * are given an instance when it's ready. Another alternative was
     * to have the SSLFactoryBuilder export buildContext method, but
     * that would mean SSLContext which is deep in Sun guts will be
     * aired in 3-4 classes instead of 1.
     */
    protected abstract void visitBuilder( SSLFactoryBuilder builder );
}
