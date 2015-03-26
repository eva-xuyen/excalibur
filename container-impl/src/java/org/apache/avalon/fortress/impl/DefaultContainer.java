/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.impl.factory.ProxyManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * This is the default implementation of {@link org.apache.avalon.fortress.Container},
 * adding configuration markup semantics to the {@link AbstractContainer}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.17 $ $Date: 2004/02/28 15:16:24 $
 */
public class DefaultContainer
    extends AbstractContainer
    implements Configurable
{
    /**
     * <p>Process the configuration and set up the components and their
     * mappings. At this point, all components are prepared and all mappings
     * are made. However, nothing is initialized.</p>
     *
     * <p>The configuration format follows a specific convention:</p>
     * <ul>
     *   <li>root configuration element may have any name.</li>
     *   <li>
     *     child configuration elements are named after short names
     *     managed by the <code>Container</code>'s
     *     <code>MetaInfoManager</code> or <code>RoleManager</code>
     *   </li>
     *   <li>
     *     alternatively <code>&lt;component&gt;</code> configuration elements
     *     may be used. These elements specify implementation class directly
     *     via the <code>class</code> attribute.
     *   </li>
     * </ul>
     *
     * <p>Please note: each configuration element <strong>must</strong>
     * have a unique id. If a configuration element does <strong>not</strong> 
     * have a unique id, it will not be treated as a component. 
     * This id is used later as a hint when there is more than one implementation
     * of a role.</p>
     *
     * <p>The first component encourted which implements a specific role becomes
     * the default implemenation of that role. This can be changed by the
     * <code>default</code> attribute in this configuration.</p>
     *
     * <pre>
     *   &lt;my-config&gt;
     *     &lt;component id="default-connection"
     *                   class="org.apache.avalon.excalibur.datasource.JdbcDataSourceComponent"&gt;
     *
     *       &lt;!-- Component specific configuration --&gt;
     *
     *     &lt;/component&gt;
     * 
     *     &lt;jdbc-data-source id="another-connection"&gt;
     *
     *         &lt;!-- Component specific configuration --&gt;
     *
     *     &lt;/jdbc-data-source&gt;
     *   &lt;/my-config&gt;
     * </pre>
     *
     * @param config  The configuration element to translate into the
     *                       list of components this impl managers.
     *
     * @throws ConfigurationException if the configuration is not valid
     */
    public void configure( final Configuration config )
        throws ConfigurationException
    {
        interpretProxy( config.getAttribute("proxy-type", "discover") );
        addHookComponents( config );
        addComponents( config );
    }

    /**
     * Interpret the ProxyManager type from the configuration element.
     *
     * @param proxyType
     * @throws ConfigurationException
     */
    protected void interpretProxy( String proxyType ) throws ConfigurationException
    {
        int type = ProxyManager.DISCOVER;

        if ( proxyType.equals("none") ) type = ProxyManager.NONE;
        if ( proxyType.equals("bcel") ) type = ProxyManager.BCEL;
        if ( proxyType.equals("java") || proxyType.equals("proxy") ) type = ProxyManager.PROXY;

        if ( type == ProxyManager.DISCOVER && ! proxyType.equals("discover") )
            throw new ConfigurationException("Proxy type '" + proxyType + "' not supported");

        try
        {
            setProxyManager( new ProxyManager( type ) );
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Could not create ProxyManager", e);
        }
    }

    /**
     * Allows implementations to register components before any 
     * user component.
     * 
     * @param config Configuration representing the contents in xconf file
     * @throws ConfigurationException
     */
    protected void addHookComponents(final Configuration config) throws ConfigurationException
    {
    }

	/**
	 * Iterates throught nodes, which represent components, adding the 
	 * component/configuration data to container.
	 * 
	 * @param config Configuration representing the contents in xconf file
	 * @throws ConfigurationException
	 */
	protected void addComponents(final Configuration config) throws ConfigurationException
	{
		final Configuration[] elements = config.getChildren();
		
		for ( int i = 0; i < elements.length; i++ )
		{
			final Configuration element = elements[i];
			final String hint = element.getAttribute( "id", null );
			
            if (isValidComponent( element, hint ))
            {
				final String classname = getClassname( element );
				final int activation = getActivation( element );
				final ComponentHandlerMetaData metaData =
					new ComponentHandlerMetaData( hint, classname, element, activation );
        
				try
				{
					addComponent( metaData );
				}
				catch ( Exception e )
				{
					throw new ConfigurationException( "Could not add component", e );
				}
			}
		}
	}

    /**
     * Checks if the necessary information was included in the component configuration.
     * In other words, whether it has a valid 'id' or not.
     * 
     * @param config Component configuration node
     * @param id name
     * @return true if the registration can proceed.
     * @throws ConfigurationException
     */
    protected boolean isValidComponent( final Configuration config, final String id )
        throws ConfigurationException
    {
        boolean isValid = true;
        
        if ( null == id )
        {
            // Only components with an id attribute are treated as components.
            getLogger().debug( "Ignoring configuration for component, " + config.getName()
                + ", because the id attribute is missing." );
            
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Retrieve the classname for component configuration.
     *
     * @param config the component configuration
     * @return the class name
     */
    protected String getClassname( final Configuration config )
        throws ConfigurationException
    {
        final String className;

        if ( "component".equals( config.getName() ) )
        {
            className = config.getAttribute( "class" );
        }
        else
        {
            final MetaInfoEntry roleEntry = m_metaManager.getMetaInfoForShortName( config.getName() );
            if ( null == roleEntry )
            {
                final String message = "No class found matching configuration name " +
                    "[name: " + config.getName() + ", location: " + config.getLocation() + "]";
                throw new ConfigurationException( message );
            }

            className = roleEntry.getComponentClass().getName();
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Configuration processed for: " + className );
        }

        return className;
    }

    /**
     * Helper method to determine the activation policy for a given component
     *  handler configuration. Supported values for the "activation" attribute
     *  of the component are "background", "inline", or "lazy".  The default
     *  activation is "background".  For compatibility, "startup" is an alias
     *  for "background" and "request" is an alias for "lazy".
     *
     * @param component <code>Configuration</code>
     *
     * @return the activation policy, one of
     *         ComponentHandlerMetaData.ACTIVATION_BACKGROUND,
     *         ComponentHandlerMetaData.ACTIVATION_INLINE,
     *         ComponentHandlerMetaData.ACTIVATION_LAZY.
     *
     * @throws ConfigurationException if the handler specifies an unknown
     *                                activation policy
     */
	protected int getActivation( final Configuration component )
        throws ConfigurationException
    {
        final String activation = component.getAttribute( "activation", "background" );
        
        if ( "background".equalsIgnoreCase( activation )
            || "startup".equalsIgnoreCase( activation ) )
        {
            return ComponentHandlerMetaData.ACTIVATION_BACKGROUND;
        }
        else if ( "inline".equalsIgnoreCase( activation ) )
        {
            return ComponentHandlerMetaData.ACTIVATION_INLINE;
        }
        else if ( "lazy".equalsIgnoreCase( activation )
            || "request".equalsIgnoreCase( activation ) )
        {
            return ComponentHandlerMetaData.ACTIVATION_LAZY;
        }
        else
        {
            // activation policy was unknown.
            final String classname = component.getAttribute( "class", null );

            final String message =
                "Unknown activation policy for class " + classname + ", \"" + activation + "\" "
                + "at " + component.getLocation();
            throw new ConfigurationException( message );
        }
    }

    /**
     * Return the ServiceManager that exposes all the services in impl.
     *
     * @return the ServiceManager that exposes all the services in impl.
     */
    public ServiceManager getServiceManager()
    {
        return super.getServiceManager();
    }
}
