/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl;

import org.apache.avalon.fortress.impl.factory.ProxyManager;
import org.apache.avalon.fortress.impl.interceptor.DefaultInterceptorManager;
import org.apache.avalon.fortress.interceptor.InterceptorManager;
import org.apache.avalon.fortress.util.CompositeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;

/**
 * InterceptorEnabledContainer extends the default container behavior
 * adding interceptor capabilities to it.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class InterceptorEnabledContainer extends DefaultContainer
{
    ///
    /// Static fields
    /// 
    
    private static final String INTERCEPTOR_CONFIG_NAME = "interceptorManager";

    ///
    /// Instance fields
    /// 
    
    /**
     * InterceptorManager configuration node.
     */
    private Configuration m_managerConfiguration;
    
    /**
     * InterceptorManager instance per container.
     */
    private InterceptorManager m_manager;
    
    ///
    /// Public implementation
    /// 
    
    public InterceptorManager getInterceptorManager()
    {
        return m_manager;
    }
    
    ///
    /// DefaultContainer's overrides
    /// 

    /**
     * Turn off proxy (interceptable components already has proxy)
     *
     * @param proxyType
     * @throws ConfigurationException
     */
    protected void interpretProxy( final String proxyType ) throws ConfigurationException
    {
        try
        {
            setProxyManager( new ProxyManager( ProxyManager.NONE ) );
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Could not create ProxyManager", e);
        }
    }
    
    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.impl.AbstractContainer#provideComponentContext(org.apache.avalon.framework.context.Context)
     */
    protected Context provideComponentContext(Context parent) throws Exception
    {
        DefaultContext context = new DefaultContext( parent );
        context.put( "container", this );
        context.put( "metamanager", m_metaManager );
        context.makeReadOnly();
        return context;
    }
    
    /**
     * Avoid the registration of interceptorManager configuration node. 
     * 
     * @see org.apache.avalon.fortress.impl.DefaultContainer#isValidComponent(org.apache.avalon.framework.configuration.Configuration, java.lang.String)
     */
    protected boolean isValidComponent( final Configuration config, final String id )
        throws ConfigurationException
    {
        String name = config.getName();
            
        if (INTERCEPTOR_CONFIG_NAME.equalsIgnoreCase( name ))
        {
            return false;
        }
        
        return super.isValidComponent( config, id );
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.impl.DefaultContainer#addHookComponents(org.apache.avalon.framework.configuration.Configuration)
     */
    protected void addHookComponents(Configuration config) throws ConfigurationException
    {
        super.addHookComponents(config);
        
        m_managerConfiguration = config.getChild( INTERCEPTOR_CONFIG_NAME, true );
        
        try
        {
            createAndConfigureInterceptorManager();
        }
        catch(ContextException ex)
        {
            throw new ConfigurationException( "Error trying to configure interceptor manager.", ex );
        }
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws CompositeException, Exception
    {
        ContainerUtil.initialize( m_manager );
        
        super.initialize();
    }
    
    /**
     * Pending
     * 
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose()
    {
        super.dispose();
        ContainerUtil.dispose( m_manager );
    }
    
    ///
    /// Virtual implementation
    ///
    
    protected void createAndConfigureInterceptorManager() throws ContextException, ConfigurationException
    {
        InterceptorManager manager = createInterceptorManager();
        ContainerUtil.configure( manager, m_managerConfiguration );
        m_manager = manager;
    }
    
    protected InterceptorManager createInterceptorManager() throws ContextException
    {
        try
        {
            return new DefaultInterceptorManager( provideComponentContext( m_context ) );
        }
        catch(Exception ex)
        {
            throw new ContextException( "Error creating context for InterceptorManager", ex );
        }
    }

}
