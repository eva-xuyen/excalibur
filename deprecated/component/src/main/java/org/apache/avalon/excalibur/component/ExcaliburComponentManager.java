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
package org.apache.avalon.excalibur.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LoggerManageable;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.logger.LogKitManageable;
import org.apache.avalon.excalibur.logger.LogKitManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * Default component manager for Avalon's components.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class ExcaliburComponentManager
    extends AbstractDualLogEnabled
    implements ComponentManager,
    Configurable,
    Contextualizable,
    Initializable,
    Disposable,
    RoleManageable,
    LogKitManageable,
    LoggerManageable,
    InstrumentManageable,
    Instrumentable
{
    /** Instrumentable name used to represent the component-manager.
     *  Handlers reference this name to register themselves at the correct
     *  location under the ECM. */
    public static final String INSTRUMENTABLE_NAME = "component-manager";

    /** The parent ComponentLocator */
    private final ComponentManager m_parentManager;

    /** The classloader used for this system. */
    private final ClassLoader m_loader;

    /** The application context for components */
    private Context m_context;

    /** Static component mapping handlers. */
    private final Map m_componentMapping = Collections.synchronizedMap(new HashMap());

    /** Used to map roles to ComponentHandlers. */
    private final Map m_componentHandlers = Collections.synchronizedMap(new HashMap());

    /** added component handlers before initialization to maintain
     *  the order of initialization
     */
    private final List m_newComponentHandlers = new ArrayList();

    /** RoleInfos. */
    private RoleManager m_roles;

    /** LogKitManager. */
    private LogkitLoggerManager m_logkit;

    /** Is the Manager disposed or not? */
    private boolean m_disposed;

    /** Is the Manager initialized? */
    private boolean m_initialized;

    /** Instrument Manager being used by the Component Manager. */
    private InstrumentManager m_instrumentManager;

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName = INSTRUMENTABLE_NAME;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /** Create a new ExcaliburComponentManager. */
    public ExcaliburComponentManager()
    {
        this( null, Thread.currentThread().getContextClassLoader() );
    }

    /** Create a new ExcaliburComponentManager with uses a specific Classloader. */
    public ExcaliburComponentManager( final ClassLoader loader )
    {
        this( null, loader );
    }

    /** Create the ComponentLocator with a Classloader and parent ComponentLocator */
    public ExcaliburComponentManager( final ComponentManager manager, final ClassLoader loader )
    {
        if( null == loader )
        {
            m_loader = Thread.currentThread().getContextClassLoader();
        }
        else
        {
            m_loader = loader;
        }

        m_parentManager = manager;
    }

    /** Create the ComponentLocator with a parent ComponentLocator */
    public ExcaliburComponentManager( final ComponentManager manager )
    {
        this( manager, Thread.currentThread().getContextClassLoader() );
    }

    /*---------------------------------------------------------------
     * ComponentManager Methods
     *-------------------------------------------------------------*/
    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Component lookup( final String role )
        throws ComponentException
    {
        if( !m_initialized )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn(
                    "Looking up component on an uninitialized ComponentLocator [" + role + "]" );
            }
        }

        if( m_disposed )
        {
            throw new IllegalStateException(
                "You cannot lookup components on a disposed ComponentLocator" );
        }

        if( null == role )
        {
            final String message =
                "ComponentLocator Attempted to retrieve component with null role.";

            if( getLogger().isErrorEnabled() )
            {
                getLogger().error( message );
            }
            throw new ComponentException( role, message );
        }

        ComponentHandler handler = (ComponentHandler)m_componentHandlers.get( role );

        // Retrieve the instance of the requested component
        if( null == handler )
        {
            if( m_parentManager != null )
            {
                try
                {
                    return m_parentManager.lookup( role );
                }
                catch( Exception e )
                {
                    if( getLogger().isWarnEnabled() )
                    {
                        final String message =
                            "ComponentLocator exception from parent CM during lookup.";
                        getLogger().warn( message, e );
                    }
                    // ignore.  If the exception is thrown, we try to
                    // create the component next
                }
            }

            if( null != m_roles )
            {
                final String className = m_roles.getDefaultClassNameForRole( role );

                if( null != className )
                {
                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Could not find ComponentHandler, attempting to create "
                            + "one for role [" + role + "]" );
                    }

                    try
                    {
                        final Class componentClass = m_loader.loadClass( className );

                        final Configuration configuration = new DefaultConfiguration( "", "-" );

                        handler = getComponentHandler( role,
                                                       componentClass,
                                                       configuration,
                                                       m_context,
                                                       m_roles,
                                                       m_logkit );

                        handler.setLogger( getLogkitLogger() );
                        handler.enableLogging( getLogger() );
                        handler.initialize();
                    }
                    catch( final Exception e )
                    {
                        final String message = "Could not find component";
                        if( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( message + " for role: " + role, e );
                        }
                        throw new ComponentException( role, message, e );
                    }

                    m_componentHandlers.put( role, handler );
                }
            }
            else
            {
                getLogger().debug( "Component requested without a RoleManager set.\n"
                    + "That means setRoleManager() was not called during initialization." );
            }
        }

        if( null == handler )
        {
            final String message = "Could not find component";
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( message + " for role: " + role );
            }
            throw new ComponentException( role, message );
        }

        Component component = null;

        try
        {
            component = handler.get();
        }
        catch( final IllegalStateException ise )
        {
            try
            {
                handler.initialize();
                component = handler.get();
            }
            catch( final ComponentException ce )
            {
                // Rethrow instead of wrapping a ComponentException with another one
                throw ce;
            }
            catch( final Exception e )
            {
                final String message = "Could not access the Component";
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( message + " for role [" + role + "]", e );
                }

                throw new ComponentException( role, message, e );
            }
        }
        catch( final Exception e )
        {
            final String message = "Could not access the Component";
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( message + " for role [" + role + "]", e );
            }

            throw new ComponentException( role, message, e );
        }

        // Add a mapping between the component and its handler.
        //  In the case of a ThreadSafeComponentHandler, the same component will be mapped
        //  multiple times but because each put will overwrite the last, this is not a
        //  problem.  Checking to see if the put has already been done would be slower.
        m_componentMapping.put( component, handler );

        return component;
    }

    /**
     * Tests for existence of a component.  Please note that this test is for
     * <strong>existing</strong> components, and a component will not be created
     * to satisfy the request.
     */
    public boolean hasComponent( final String role )
    {
        if( !m_initialized ) return false;
        if( m_disposed ) return false;

        boolean exists = m_componentHandlers.containsKey( role );

        if( !exists && null != m_parentManager )
        {
            exists = m_parentManager.hasComponent( role );
        }

        return exists;
    }

    /**
     * Release a Component.  This implementation makes sure it has a handle on the propper
     * ComponentHandler, and let's the ComponentHandler take care of the actual work.
     */
    public void release( final Component component )
    {
        if( null == component )
        {
            return;
        }

        // The m_componentMapping StaticBucketMap itself is threadsafe, and because the same component
        //  will never be released by more than one thread, this method does not need any
        //  synchronization around the access to the map.

        final ComponentHandler handler =
            (ComponentHandler)m_componentMapping.get( component );

        if( null != handler )
        {
            // ThreadSafe components will always be using a ThreadSafeComponentHandler,
            //  they will only have a single entry in the m_componentMapping map which
            //  should not be removed until the ComponentLocator is disposed.  All
            //  other components have an entry for each instance which should be
            //  removed.
            if( !( handler instanceof ThreadSafeComponentHandler ) )
            {
                // Remove the component before calling put.  This is critical to avoid the
                //  problem where another thread calls put on the same component before
                //  remove can be called.
                m_componentMapping.remove( component );
            }

            try
            {
                handler.put( component );
            }
            catch( Exception e )
            {
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Error trying to release component.", e );
                }
            }
        }
        else if( null != m_parentManager )
        {
            m_parentManager.release( component );
        }
        else
        {
            getLogger().warn( "Attempted to release a " + component.getClass().getName() +
                              " but its handler could not be located." );
        }
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Configure the ComponentLocator.
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        if( null == m_roles )
        {
            final DefaultRoleManager roleInfo = new DefaultRoleManager();
            roleInfo.enableLogging( getLogger() );
            roleInfo.configure( configuration );
            m_roles = roleInfo;
            getLogger().debug( "No RoleManager given, deriving one from configuration" );
        }

        // Set components

        final Configuration[] configurations = configuration.getChildren();

        for( int i = 0; i < configurations.length; i++ )
        {
            String type = configurations[ i ].getName();

            if( !type.equals( "role" ) )
            {
                String role = configurations[ i ].getAttribute( "role", "" );
                String className = configurations[ i ].getAttribute( "class", "" );

                if( role.equals( "" ) )
                {
                    role = m_roles.getRoleForName( type );
                }

                if( null != role && !role.equals( "" ) )
                {
                    if( className.equals( "" ) )
                    {
                        className = m_roles.getDefaultClassNameForRole( role );
                    }

                    try
                    {
                        if( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Adding component (" + role + " = "
                                + className + ")" );
                        }

                        final Class clazz = m_loader.loadClass( className );
                        addComponent( role, clazz, configurations[ i ] );
                    }
                    catch( final ClassNotFoundException cnfe )
                    {
                        final String message = "Could not get class ";

                        if( getLogger().isErrorEnabled() )
                        {
                            getLogger().error( message + className + " for role " + role
                                + " on configuration element " + configurations[ i ].getName(),
                                cnfe );
                        }

                        throw new ConfigurationException( message, cnfe );
                    }
                    catch( final ComponentException ce )
                    {
                        final String message = "Bad component ";

                        if( getLogger().isErrorEnabled() )
                        {
                            getLogger().error( message + className + " for role " + role
                                + " on configuration element " + configurations[ i ].getName(),
                                ce );
                        }

                        throw new ConfigurationException( message, ce );
                    }
                    catch( final Exception e )
                    {
                        if( getLogger().isErrorEnabled() )
                        {
                            getLogger().error( "Unexpected exception for hint: " + role, e );
                        }
                        throw new ConfigurationException( "Unexpected exception", e );
                    }
                }
            }
        }
    }

    /*---------------------------------------------------------------
     * Contextual
    /*---------------------------------------------------------------
     * Contextualizable Methods
     *-------------------------------------------------------------*/
    /** Set up the Component's Context.
     */
    public void contextualize( final Context context )
    {
        if( null == m_context )
        {
            m_context = context;
        }
    }

    /*---------------------------------------------------------------
     * Initializable Methods
     *-------------------------------------------------------------*/
    /** Properly initialize of the Child handlers.
     */
    public void initialize()
        throws Exception
    {
        if( m_instrumentManager != null )
        {
            m_instrumentManager.registerInstrumentable( this, m_instrumentableName );
        }

        synchronized( this )
        {
            m_initialized = true;

            for( int i = 0; i < m_newComponentHandlers.size(); i++ )
            {
                final ComponentHandler handler =
                    (ComponentHandler)m_newComponentHandlers.get( i );
                try
                {
                    handler.initialize();

                    // Manually register the handler so that it will be located under the
                    //  instrument manager, seperate from the actual instrumentable data of the
                    //  components
                    if( ( m_instrumentManager != null ) &&
                        ( handler instanceof Instrumentable ) )
                    {
                        String handleInstName = ( (Instrumentable)handler ).getInstrumentableName();
                        m_instrumentManager.registerInstrumentable( handler, handleInstName );
                    }
                }
                catch( Exception e )
                {
                    if( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "Caught an exception trying to initialize "
                                           + "the component handler.", e );
                    }

                    // Rethrow the exception
                    throw e;
                }
            }

            List keys = new ArrayList( m_componentHandlers.keySet() );

            for( int i = 0; i < keys.size(); i++ )
            {
                final Object key = keys.get( i );
                final ComponentHandler handler =
                    (ComponentHandler)m_componentHandlers.get( key );

                if( !m_newComponentHandlers.contains( handler ) )
                {
                    try
                    {
                        handler.initialize();

                        // Manually register the handler so that it will be located under the
                        //  instrument manager, seperate from the actual instrumentable data of the
                        //  components
                        if( ( m_instrumentManager != null ) &&
                            ( handler instanceof Instrumentable ) )
                        {
                            String handleInstName =
                                ( (Instrumentable)handler ).getInstrumentableName();
                            m_instrumentManager.registerInstrumentable( handler, handleInstName );
                        }
                    }
                    catch( Exception e )
                    {
                        if( getLogger().isErrorEnabled() )
                        {
                            getLogger().error( "Caught an exception trying to initialize "
                                               + "the component handler.", e );

                            // Rethrow the exception
                            throw e;
                        }
                    }
                }
            }
            m_newComponentHandlers.clear();
        }
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /** Properly dispose of the Child handlers.
     */
    public void dispose()
    {
        synchronized( this )
        {
            boolean forceDisposal = false;

            final List disposed = new ArrayList();

            while( m_componentHandlers.size() > 0 )
            {
                for( Iterator iterator = m_componentHandlers.keySet().iterator();
                     iterator.hasNext(); )
                {
                    final Object role = iterator.next();

                    final ComponentHandler handler =
                        (ComponentHandler)m_componentHandlers.get( role );

                    if( forceDisposal || handler.canBeDisposed() )
                    {
                        if( forceDisposal && getLogger().isWarnEnabled() )
                        {
                            getLogger().warn
                                ( "disposing of handler for unreleased component."
                                  + " role [" + role + "]" );
                        }

                        handler.dispose();
                        disposed.add( role );
                    }
                }

                if( disposed.size() > 0 )
                {
                    removeDisposedHandlers( disposed );
                }
                else
                {   // no more disposable handlers!
                    forceDisposal = true;
                }
            }

            m_disposed = true;
        }
    }

    /*---------------------------------------------------------------
     * RoleManageable Methods
     *-------------------------------------------------------------*/
    /**
     * Configure the RoleManager
     */
    public void setRoleManager( final RoleManager roles )
    {
        if( null == m_roles )
        {
            m_roles = roles;
        }
    }

    /*---------------------------------------------------------------
     * LogKitManageable Methods
     *-------------------------------------------------------------*/
    /**
     * Configure the LogKitManager
     */
    public void setLogKitManager( final LogKitManager logkit )
    {
        if( null == m_logkit )
        {
            m_logkit = new LogkitLoggerManager( null, logkit );
        }
    }

    /*---------------------------------------------------------------
     * InstrumentManageable Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the InstrumentManager for child components.  Can be for special
     * purpose components, however it is used mostly internally.
     *
     * @param instrumentManager The InstrumentManager for the component to use.
     */
    public void setInstrumentManager( InstrumentManager instrumentManager )
    {
        m_instrumentManager = instrumentManager;
    }

    /*---------------------------------------------------------------
     * Instrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the name for the Instrumentable.  The Instrumentable Name is used
     *  to uniquely identify the Instrumentable during the configuration of
     *  the InstrumentManager and to gain access to an InstrumentableDescriptor
     *  through the InstrumentManager.  The value should be a string which does
     *  not contain spaces or periods.
     * <p>
     * This value may be set by a parent Instrumentable, or by the
     *  InstrumentManager using the value of the 'instrumentable' attribute in
     *  the configuration of the component.
     *
     * @param name The name used to identify a Instrumentable.
     */
    public void setInstrumentableName( String name )
    {
        // Ignored.  The ECM name is fixed.
    }

    /**
     * Gets the name of the Instrumentable.
     *
     * @return The name used to identify a Instrumentable.
     */
    public String getInstrumentableName()
    {
        return m_instrumentableName;
    }

    /**
     * Obtain a reference to all the Instruments that the Instrumentable object
     *  wishes to expose.  All sampling is done directly through the
     *  Instruments as opposed to the Instrumentable interface.
     *
     * @return An array of the Instruments available for profiling.  Should
     *         never be null.  If there are no Instruments, then
     *         EMPTY_INSTRUMENT_ARRAY can be returned.  This should never be
     *         the case though unless there are child Instrumentables with
     *         Instruments.
     */
    public Instrument[] getInstruments()
    {
        return Instrumentable.EMPTY_INSTRUMENT_ARRAY;
    }

    /**
     * Any Object which implements Instrumentable can also make use of other
     *  Instrumentable child objects.  This method is used to tell the
     *  InstrumentManager about them.
     *
     * @return An array of child Instrumentables.  This method should never
     *         return null.  If there are no child Instrumentables, then
     *         EMPTY_INSTRUMENTABLE_ARRAY can be returned.
     */
    public Instrumentable[] getChildInstrumentables()
    {
        // Child instrumentables register themselves as they are discovered.
        return EMPTY_INSTRUMENTABLE_ARRAY;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private void removeDisposedHandlers( List disposed )
    {

        for( Iterator iterator = disposed.iterator(); iterator.hasNext(); )
        {
            m_componentHandlers.remove( iterator.next() );
        }

        disposed.clear();
    }

    /**
     * Configure the LoggerManager.
     */
    public void setLoggerManager( final LoggerManager logkit )
    {
        if( null == m_logkit )
        {
            m_logkit = new LogkitLoggerManager( logkit, null );
        }
    }

    /**
     * Obtain a new ComponentHandler for the specified component.  This method
     *  allows classes which extend the ExcaliburComponentManager to use their
     *  own ComponentHandlers.
     *
     * @param componentClass Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     * @param context The current context object.
     * @param roleManager The current RoleManager.
     * @param logkitManager The current LogKitManager.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    protected ComponentHandler getComponentHandler( final String role,
                                                    final Class componentClass,
                                                    final Configuration configuration,
                                                    final Context context,
                                                    final RoleManager roleManager,
                                                    final LogkitLoggerManager logkitManager )
        throws Exception
    {
        // The instrumentable name will be set by first looking for a name set using
        //  the instrumentable attribute.  If missing, the name of the configuration
        //  element is used.
        String instrumentableName =
            configuration.getAttribute( "instrumentable", configuration.getName() );

        return ComponentHandler.getComponentHandler( role,
                                                     componentClass,
                                                     configuration,
                                                     this,
                                                     context,
                                                     roleManager,
                                                     logkitManager,
                                                     m_instrumentManager,
                                                     instrumentableName );
    }

    /**
     * Add a new component to the manager.
     *
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final String role,
                              final Class component,
                              final Configuration configuration )
        throws ComponentException
    {
        if( m_initialized )
        {
            throw new ComponentException( role,
                "Cannot add components to an initialized ComponentLocator" );
        }

        try
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Attempting to get Handler for role [" + role + "]" );
            }

            final ComponentHandler handler = getComponentHandler( role,
                                                                  component,
                                                                  configuration,
                                                                  m_context,
                                                                  m_roles,
                                                                  m_logkit );

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Handler type = " + handler.getClass().getName() );
            }

            handler.setLogger( getLogkitLogger() );
            handler.enableLogging( getLogger() );
            m_componentHandlers.put( role, handler );
            m_newComponentHandlers.add( handler );
        }
        catch( final Exception e )
        {
            throw new ComponentException( role, "Could not set up Component.", e );
        }
    }

    /** Add a static instance of a component to the manager.
     * @param role the role name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance( final String role, final Component instance )
    {
        if( m_initialized )
        {
            throw new IllegalStateException(
                "Cannot add components to an initialized ComponentLocator" );
        }

        try
        {
            ComponentHandler handler =
                ComponentHandler.getComponentHandler( instance );
            handler.setLogger( getLogkitLogger() );
            handler.enableLogging( getLogger() );
            m_componentHandlers.put( role, handler );
        }
        catch( final Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Could not set up Component for role [" + role + "]", e );
            }
        }
    }
}
