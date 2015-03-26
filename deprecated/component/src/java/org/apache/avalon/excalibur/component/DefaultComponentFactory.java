/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LogKitManageable;
import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * Factory for Avalon components.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class DefaultComponentFactory
    extends AbstractDualLogEnabled
    implements ObjectFactory, Disposable, ThreadSafe
{
    /** The class which this <code>ComponentFactory</code>
     * should create.
     */
    private Class m_componentClass;

    /** The Context for the component
     */
    private Context m_context;

    /** The component manager for this component.
     */
    private ComponentManager m_componentManager;

    /** The service manager for this component
     */
    private WrapperServiceManager m_serviceManager;
    
    /** The configuration for this component.
     */
    private Configuration m_configuration;

    /** The RoleManager for child ComponentSelectors
     */
    private RoleManager m_roles;

    /** The LogkitLoggerManager for child ComponentSelectors
     */
    private LogkitLoggerManager m_loggerManager;

    /** Components created by this factory, and their associated ComponentLocator
     *  proxies, if they are Composables.  These must be seperate maps in case
     *  a component falls into more than one category, which they often do.
     */
    private final Map m_componentProxies = Collections.synchronizedMap(new HashMap());

    /** Instrument Manager to register objects created by this factory with (May be null). */
    private InstrumentManager m_instrumentManager;

    /** Instrumentable Name assigned to objects created by this factory. */
    private String m_instrumentableName;

    private ComponentProxyGenerator m_proxyGenerator;
    private String m_role;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Construct a new component factory for the specified component.
     *
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     * @param componentManager the component manager to pass to <code>Composable</code>s.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     * @param roles the <code>RoleManager</code> to pass to <code>DefaultComponentSelector</code>s.
     *
     * @deprecated This constructor has been deprecated in favor of the version below which
     *             handles instrumentation.
     */
    public DefaultComponentFactory( final String role,
                                    final Class componentClass,
                                    final Configuration configuration,
                                    final ComponentManager componentManager,
                                    final Context context,
                                    final RoleManager roles,
                                    final LogkitLoggerManager loggerManager )
    {
        this( role,
              componentClass,
              configuration,
              componentManager,
              context,
              roles,
              loggerManager,
              null,
              "N/A" );
    }

    /**
     * Construct a new component factory for the specified component.
     *
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     * @param componentManager the component manager to pass to <code>Composable</code>s.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     * @param roles the <code>RoleManager</code> to pass to
     *              <code>DefaultComponentSelector</code>s.
     * @param instrumentManager the <code>InstrumentManager</code> to register the component
     *                          with if it is a Instrumentable (May be null).
     * @param instrumentableName The instrument name to assign the component if
     *                           it is Instrumentable.
     */
    public DefaultComponentFactory( final String role,
                                    final Class componentClass,
                                    final Configuration configuration,
                                    final ComponentManager componentManager,
                                    final Context context,
                                    final RoleManager roles,
                                    final LogkitLoggerManager loggerManager,
                                    final InstrumentManager instrumentManager,
                                    final String instrumentableName )

    {
        m_role = role;
        m_componentClass = componentClass;
        m_configuration = configuration;
        m_componentManager = componentManager;
        m_context = context;
        m_roles = roles;
        m_loggerManager = loggerManager;
        m_instrumentManager = instrumentManager;
        m_instrumentableName = instrumentableName;
        m_proxyGenerator = new ComponentProxyGenerator( m_componentClass.getClassLoader() );
        m_serviceManager = new WrapperServiceManager( m_componentManager );
    }

    /*---------------------------------------------------------------
     * ObjectFactory Methods
     *-------------------------------------------------------------*/
    public Object newInstance()
        throws Exception
    {
        final Object component = m_componentClass.newInstance();

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "ComponentFactory creating new instance of " +
                               m_componentClass.getName() + "." );
        }

        if( component instanceof LogEnabled )
        {
            if( null == m_loggerManager || null == m_configuration )
            {
                ContainerUtil.enableLogging( component, getLogger() );
            }
            else
            {
                final String logger = m_configuration.getAttribute( "logger", null );
                if( null == logger )
                {
                    getLogger().debug( "no logger attribute available, using standard logger" );
                    ContainerUtil.enableLogging( component, getLogger() );
                }
                else
                {
                    getLogger().debug( "logger attribute is " + logger );
                    ContainerUtil.enableLogging( component, m_loggerManager.getLoggerForCategory( logger ) );
                }
            }
        }

        else if( component instanceof Loggable )
        {
            if( null == m_loggerManager || null == m_configuration )
            {
                ( (Loggable)component ).setLogger( getLogkitLogger() );
            }
            else
            {
                final String logger = m_configuration.getAttribute( "logger", null );
                if( null == logger )
                {
                    getLogger().debug( "no logger attribute available, using standard logger" );
                    ( (Loggable)component ).setLogger( getLogkitLogger() );
                }
                else
                {
                    getLogger().debug( "logger attribute is " + logger );
                    ( (Loggable)component ).setLogger( m_loggerManager.getLogKitLoggerForCategory( logger ) );
                }
            }
        }

        // Set the name of the instrumentable before initialization.
        if( component instanceof Instrumentable )
        {
            Instrumentable instrumentable = (Instrumentable)component;
            instrumentable.setInstrumentableName( m_instrumentableName );
        }

        if( ( component instanceof InstrumentManageable ) && ( m_instrumentManager != null ) )
        {
            ( (InstrumentManageable)component ).setInstrumentManager( m_instrumentManager );
        }

        if( component instanceof Contextualizable )
        {
            ContainerUtil.contextualize( component, m_context );
        }

        if( component instanceof Composable )
        {
            ContainerUtil.compose( component, m_componentManager );
        }

        if( component instanceof Serviceable )
        {
            ContainerUtil.service( component, m_serviceManager );

        }

        if( component instanceof RoleManageable )
        {
            ( (RoleManageable)component ).setRoleManager( m_roles );
        }

        if( component instanceof LogKitManageable )
        {
            ( (LogKitManageable)component ).setLogKitManager( m_loggerManager.getLogKitManager() );
        }

        ContainerUtil.configure( component, m_configuration );

        if( component instanceof Parameterizable )
        {
            final Parameters parameters = Parameters.fromConfiguration( m_configuration );
            ContainerUtil.parameterize( component, parameters );
        }

        ContainerUtil.initialize( component );

        // Register the component as an instrumentable now that it has been initialized.
        if( component instanceof Instrumentable )
        {
            // Instrumentable Name is set above.
            if( m_instrumentManager != null )
            {
                m_instrumentManager.registerInstrumentable(
                    (Instrumentable)component, m_instrumentableName );
            }
        }
        ContainerUtil.start( component );

        // If the component is not an instance of Component then wrap it in a proxy.
        //  This makes it possible to use components which are not real Components
        //  with the ECM.  We need to remember to unwrap this when the component is
        //  decommissioned.
    //
    // note that ComponentHandler depends on this specific
    // component instanceof Component check to be made
        Component returnableComponent;
        if( !( component instanceof Component ) )
        {
            returnableComponent = m_proxyGenerator.getCompatibleProxy( component );
            m_componentProxies.put( returnableComponent, component );
        }
        else
        {
            returnableComponent = (Component)component;
        }

        return returnableComponent;
    }

    public Class getCreatedClass()
    {
        return m_componentClass;
    }

    public void decommission( final Object component )
        throws Exception
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "ComponentFactory decommissioning instance of " +
                               m_componentClass.getName() + "." );
        }

        // See if we need to unwrap this component.  It may have been wrapped in a proxy
        //  by the ProxyGenerator.
        Object decommissionComponent = m_componentProxies.remove( component );
        if ( null == decommissionComponent )
        {
            // It was not wrapped.
            decommissionComponent = component;
        }

        ContainerUtil.stop( decommissionComponent );
        ContainerUtil.dispose( decommissionComponent );

        /*if ( decommissionComponent instanceof Composable )
        {
            // A proxy will have been created.  Ensure that components created by it
            //  are also released.
            ((ComponentManagerProxy)m_composableProxies.remove( decommissionComponent )).
                releaseAll();
        }

        if ( decommissionComponent instanceof Serviceable )
        {
            // A proxy will have been created.  Ensure that components created by it
            //  are also released.
            ((ServiceManagerProxy)m_serviceableProxies.remove( decommissionComponent )).
                releaseAll();
        }*/
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    public void dispose()
    {
    }

}
