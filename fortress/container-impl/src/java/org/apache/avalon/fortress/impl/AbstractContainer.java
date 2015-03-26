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

package org.apache.avalon.fortress.impl;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.fortress.impl.extensions.InstrumentableCreator;
import org.apache.avalon.fortress.impl.factory.ProxyManager;
import org.apache.avalon.fortress.impl.handler.ComponentFactory;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.LEAwareComponentHandler;
import org.apache.avalon.fortress.impl.handler.PrepareHandlerCommand;
import org.apache.avalon.fortress.impl.lookup.FortressServiceManager;
import org.apache.avalon.fortress.impl.lookup.FortressServiceSelector;
import org.apache.avalon.fortress.util.CompositeException;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.fortress.util.dag.CyclicDependencyException;
import org.apache.avalon.fortress.util.dag.DirectedAcyclicGraphVerifier;
import org.apache.avalon.fortress.util.dag.Vertex;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.collections.BoundedFifoBuffer;
import org.apache.commons.collections.StaticBucketMap;
import org.d_haven.event.Sink;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;
import org.d_haven.mpool.ObjectFactory;
import org.d_haven.mpool.PoolManager;

import java.util.*;

/**
 * This abstract implementation provides basic functionality for building
 * an implementation of the {@link Container} interface.
 * It exposes a protected getServiceManager() method so that the
 * Container's Manager can expose that to the instantiating class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.41 $ $Date: 2004/02/28 15:16:24 $
 */
public abstract class AbstractContainer
        extends AbstractLogEnabled
        implements Contextualizable, Serviceable, Initializable, Disposable, Container
{
    /** The hint map's entry to get the default component type. */
    public static final String DEFAULT_ENTRY = "*";
    /** The component map's entry to get a ServiceSelector. */
    public static final String SELECTOR_ENTRY = "$";

    /** contains the impl's context passed in through contextualize(). */
    protected Context m_context;
    /** contains the ServiceManager the impl will use, based on the one passed in through service(). */
    protected ServiceManager m_serviceManager;
    /** contains the impl's LoggerManager, which is extracted from m_serviceManager. */
    protected LoggerManager m_loggerManager;
    /** contains the impl's PoolManager, which is extracted from m_serviceManager. */
    protected PoolManager m_poolManager;
    /** contains the impl's Sink, which is extracted from m_serviceManager. */
    protected Sink m_commandSink;
    /** contains the impl's root ClassLoader, which is extracted from m_serviceManager. */
    protected ClassLoader m_classLoader;
    /** contains the impl's RoleManager, which is extracted from m_serviceManager. */
    protected MetaInfoManager m_metaManager;
    /** contains the impl's InstrumentManager, which is extracted from m_serviceManager. */
    protected InstrumentManager m_instrumentManager;
    /** contains the impl's LifecycleExtensionManager, which is extracted from m_serviceManager. */
    protected LifecycleExtensionManager m_extManager;
    /**
     * contains the context that will be passed to the components we will create.
     * initialized the first time a component handler is created by a call to
     * provideComponentContext -- override this method to affect the value of this object.
     */
    protected Context m_componentContext;
    /**
     * Contains entries mapping roles to hint maps, where the hint map contains
     * mappings from hints to ComponentHandlers.
     */
    protected Map m_mapper = new StaticBucketMap();
    /** Contains an entry for each ComponentHandler */
    protected List m_components = new ArrayList( 10 );

    protected List m_shutDownOrder;

    private ProxyManager m_proxyManager;

    /**
     * Allows you to override the ProxyManager used in the container.  In order for your proxymanager
     * to be used, it <b>must</b> be set prior to adding any components.
     * @param proxyManager
     */
    protected void setProxyManager( ProxyManager proxyManager )
    {
        if ( null == proxyManager ) throw new NullPointerException("proxyManager");
        if ( null != m_proxyManager ) throw new IllegalStateException("Can not double-assign the ProxyManager");
        m_proxyManager = proxyManager;
    }

    /**
     * Guarantees that the ProxyManager will be assigned before use.  If you do not set the proxy
     * manager, the AbstractContainer will use the ProxyManager.DISCOVER algorithm.
     *
     * @return the ProxyManager.
     * @throws Exception if there is a problem
     */
    protected ProxyManager getProxyManager() throws Exception
    {
        if ( null == m_proxyManager )
        {
            m_proxyManager = new ProxyManager( ProxyManager.DISCOVER );
        }

        return m_proxyManager;
    }

    /**
     * Pull the manager items from the context so we can use them to set up
     * the system.
     *
     * @avalon.context type="ClassLoader" optional="true"
     *
     * @param context the impl context
     * @throws ContextException if a contexaulization error occurs
     */
    public void contextualize( final Context context )
            throws ContextException
    {
        m_context = context;
        try
        {
            m_classLoader = (ClassLoader) context.get( ClassLoader.class.getName() );
        }
        catch ( ContextException ce )
        {
            m_classLoader = Thread.currentThread().getContextClassLoader();
        }
    }

    /**
     * Root ServiceManager.  The Container may choose to have it's
     * ServiceManager delegate to the root manager, or it may choose to be
     * entirely self contained.
     *
     * @param serviceManager the service manager to apply to the impl
     * @throws ServiceException is a servicing related error occurs
     *
     * @avalon.dependency type="LoggerManager"
     * @avalon.dependency type="PoolManager"
     * @avalon.dependency type="InstrumentManager"
     * @avalon.dependency type="MetaInfoManager"
     * @avalon.dependency type="LifecycleExtensionManager" optional="true"
     * @avalon.dependency type="Sink" optional="true"
     */
    public void service( final ServiceManager serviceManager )
            throws ServiceException
    {
        // get non-optional services

        m_loggerManager = (LoggerManager) serviceManager.lookup( LoggerManager.ROLE );
        m_poolManager = (PoolManager) serviceManager.lookup( PoolManager.class.getName() );
        m_instrumentManager = (InstrumentManager) serviceManager.lookup( InstrumentManager.ROLE );

        // get optional services, or a default if the service isn't provided

        setupExtensionManager( serviceManager );

        if ( serviceManager.hasService( Sink.class.getName() ) )
        {
            m_commandSink = (Sink) serviceManager.lookup( Sink.class.getName() );
        }
        else
        {
            final String message =
                    "No " + Sink.class.getName() + " is given, all " +
                    "management will be performed synchronously";
            getLogger().warn( message );
        }

        m_metaManager = (MetaInfoManager) serviceManager.lookup( MetaInfoManager.ROLE );

        // set up our ServiceManager
        m_serviceManager = provideServiceManager( serviceManager );
    }

    /**
     * Set up the Lifecycle Extension Manager.
     *
     * @param serviceManager  The serviceManager we are using to determine if the extension manager is being passed.
     * @throws ServiceException  if the ServiceManager does not live up to its contract.
     */
    private void setupExtensionManager( final ServiceManager serviceManager ) throws ServiceException
    {
        final Logger extLogger = m_loggerManager.getLoggerForCategory( "system.extensions" );

        if ( serviceManager.hasService( LifecycleExtensionManager.ROLE ) )
        {
            final LifecycleExtensionManager parent = (LifecycleExtensionManager)
                    serviceManager.lookup( LifecycleExtensionManager.ROLE );

            if ( extLogger.isDebugEnabled() )
            {
                final String message = "Found the LifecycleExtensionManager, creating a copy.";
                extLogger.debug( message );
            }

            m_extManager = parent.writeableCopy();
        }
        else
        {
            if ( extLogger.isDebugEnabled() )
            {
                final String message = "No LifecycleExtensionManager found, creating a new one.";
                extLogger.debug( message );
            }

            m_extManager = new LifecycleExtensionManager();
        }

        /** LifecycleExtensionManager.writeableCopy() does not copy the logger. */
        m_extManager.enableLogging( extLogger );

        if ( extLogger.isDebugEnabled() )
        {
            final String message =
                    "Adding an InstrumentableCreator to support our InstrumentManager";
            extLogger.debug( message );
        }

        /**
         * We do need a new InstrumentableCreator, as we want strictly our
         * m_instrumentManager to be engaged. We assume there is no
         * InstrumentableCreator in the LifecycleExtensionManager passed to us
         * already. If there is one this is probably a bug. Not testing this currently,
         * although might test this in the future in order to
         * throw something like an IllegalArgumentException.
         */
        m_extManager.addCreatorExtension( new InstrumentableCreator( m_instrumentManager ) );
    }

    /**
     * Add a Component into the impl. This sets the component up for management
     * by the impl by creating an appropriate {@link ComponentHandler}.
     *
     * @param metaData the information needed to construct a ComponentHandler for the component
     * @throws IllegalArgumentException if the classname defined by the meta data
     *         argument is undefined within the scope of the role manager
     * @throws Exception if unable to create a Handler for the component
     */
    protected void addComponent( final ComponentHandlerMetaData metaData )
            throws IllegalArgumentException, Exception
    {
        // figure out Role
        final String classname = metaData.getClassname();
        final MetaInfoEntry metaEntry = m_metaManager.getMetaInfoForClassname( classname );
        if ( null == metaEntry )
        {
            final String message = "No role defined for " + classname;
            throw new IllegalArgumentException( message );
        }

        if ( DEFAULT_ENTRY.equals( metaData.getName() ) ||
                SELECTOR_ENTRY.equals( metaData.getName() ) )
        {
            throw new IllegalArgumentException( "Using a reserved id name" + metaData.getName() );
        }

        Iterator it = metaEntry.getRoles();
        // create a handler for the combo of Role+MetaData
        final ComponentHandler handler =
                getComponentHandler( metaEntry, metaData );

        while ( it.hasNext() )
        {
            final String role = (String) it.next();

            // put the role into our role mapper. If the role doesn't exist
            // yet, just stuff it in as DEFAULT_ENTRY. If it does, we create a
            // ServiceSelector and put that in as SELECTOR_ENTRY.
            if ( null != role && null != classname && null != handler )
            {
                Map hintMap = (Map) m_mapper.get( role );

                // Initialize the hintMap if it doesn't exist yet.
                if ( null == hintMap )
                {
                    hintMap = createHintMap();
                    hintMap.put( DEFAULT_ENTRY, handler );
                    hintMap.put( SELECTOR_ENTRY,
                            new FortressServiceSelector( this, role ) );
                    m_mapper.put( role, hintMap );
                }

                hintMap.put( metaData.getName(), handler );

                if ( metaData.getConfiguration().getAttributeAsBoolean( "default", false ) )
                {
                    hintMap.put( DEFAULT_ENTRY, handler );
                }
            }
        }
    }

    /**
     * Get a ComponentHandler with the default constructor for the component class passed in.
     *
     * @param metaEntry the description of the Role this handler will be for
     * @param metaData the information needed to construct a ComponentHandler for the component
     * @return the component handler
     * @throws Exception if unable to provide a componenthandler
     */
    private ComponentHandler getComponentHandler( final MetaInfoEntry metaEntry,
                                                  final ComponentHandlerMetaData metaData )
            throws Exception
    {
        // get info from params
        final ComponentHandler handler;
        final String classname = metaEntry.getComponentClass().getName();
        final Configuration configuration = metaData.getConfiguration();

        try
        {
            final ObjectFactory factory =
                    createObjectFactory( classname, configuration );

            // create the appropriate handler instance
            final ComponentHandler targetHandler =
                    (ComponentHandler) metaEntry.getHandlerClass().newInstance();

            // do the handler lifecycle
            ContainerUtil.enableLogging( targetHandler, getLogger() );
            ContainerUtil.contextualize( targetHandler, m_context );
            final DefaultServiceManager serviceManager =
                    new DefaultServiceManager( getServiceManager() );
            serviceManager.put( ObjectFactory.class.getName(), factory );
            serviceManager.makeReadOnly();

            ContainerUtil.service( targetHandler, serviceManager );
            ContainerUtil.configure( targetHandler, configuration );
            ContainerUtil.initialize( targetHandler );

            if ( targetHandler instanceof Instrumentable )
            {
                final Instrumentable instrumentable = (Instrumentable) targetHandler;
                final String name = instrumentable.getInstrumentableName();
                m_instrumentManager.registerInstrumentable( instrumentable, name );
            }

            // no other lifecycle stages supported for ComponentHandler;
            // ComponentHandler is not a "true" avalon component

            handler =
                    new LEAwareComponentHandler( targetHandler, m_extManager, m_context );
        }
        catch ( final Exception e )
        {
            // if anything went wrong, the component cannot be worked with
            // and it cannot be added into the impl, so don't provide
            // a handler
            if ( getLogger().isDebugEnabled() )
            {
                final String message =
                        "Could not create the handler for the '" +
                        classname + "' component.";
                getLogger().debug( message, e );
            }
            throw e;
        }

        if ( getLogger().isDebugEnabled() )
        {
            final String message =
                    "Component " + classname +
                    " uses handler " + metaEntry.getHandlerClass().getName();
            getLogger().debug( message );
        }

        // we're still here, so everything went smooth. Register the handler
        // and return it
        final ComponentHandlerEntry entry =
                new ComponentHandlerEntry( handler, metaData );
        m_components.add( entry );

        return handler;
    }

    /**
     * Create an objectFactory for specified Object configuration.
     *
     * @param classname the classname of object
     * @param configuration the objests configuration
     * @return the ObjectFactory
     * @throws ClassNotFoundException if the specified class does not exist
     */
    protected ObjectFactory createObjectFactory( final String classname,
                                                 final Configuration configuration )
            throws Exception
    {
        if ( m_componentContext == null )
        {
            m_componentContext = provideComponentContext( m_context );
            if ( m_componentContext == null )
            {
                throw new IllegalStateException( "provideComponentContext() has returned null" );
            }
        }

        final Class clazz = m_classLoader.loadClass( classname );
        final ComponentFactory componentFactory =
                new ComponentFactory( clazz, configuration,
                        m_serviceManager, m_componentContext,
                        m_loggerManager, m_extManager );
        return getProxyManager().getWrappedObjectFactory( componentFactory );
    }

    /**
     * This is the method that the ContainerComponentManager and Selector use
     * to gain access to the ComponentHandlers and ComponentSelectors.  The
     * actual access of the ComponentHandler is delegated to the Container.
     *
     * @param  role  The role we intend to access a Component for.
     * @param  hint  The hint that we use as a qualifier
     *         (note: if null, the default implementation is returned).
     *
     * @return Object  a reference to the ComponentHandler or
     *                 ComponentSelector for the role/hint combo.
     */
    public Object get( final String role, final Object hint )
            throws ServiceException
    {
        final Map hintMap = (Map) m_mapper.get( role );
        Object value;

        if ( null == hintMap )
        {
            final String key = getRoleKey( role, hint );
            final String message = "Component does not exist";
            throw new ServiceException( key, message );
        }

        if ( null == hint )
        {
            // no hint -> try selector
            value = hintMap.get( SELECTOR_ENTRY );

            if ( null == value )
            {
                // no selector -> use default
                value = hintMap.get( DEFAULT_ENTRY );
            }

            return value;
        }

        // got a hint -> use it
        value = hintMap.get( hint );

        if ( null == value )
        {
            final String key = getRoleKey( role, hint );
            final String message = "Component does not exist";
            throw new ServiceException( key, message );
        }

        return value;
    }

    /**
     * Create the hint map for a role.  The map may have to take care for thread-safety.
     * By default a StaticBucketMap is created, but you may change the implementation
     * or increment the number of buckets according your needs.
     *
     * <div>
     *   <span style="font-weight:strong;text-color: red;">WARNING:</span>
     *   This Map must be threadsafe, so either use the
     *   <code>StaticBucketMap</code> or a synchronized <code>Map</code>.
     *   Otherwise you will experience erratic behavior due to the nature
     *   of the asyncronous component management.
     * </div>
     *
     * @return the hint map implementation
     */
    protected Map createHintMap()
    {
        return new StaticBucketMap();
    }

    /**
     * Get the composite role name based on the specified role and hint.
     * The default implementation puts a "/" on the end of the rolename
     * and then adds the string representation of the hint.
     * This is used <i>for informational display purposes only</i>.
     *
     * @param role
     * @param hint
     * @return
     */
    protected static String getRoleKey( final String role, final Object hint )
    {
        return role + "/" + hint;
    }

    /**
     * This is the method that the ContainerComponentManager and Selector use
     * to gain access to the ComponentHandlers and ComponentSelectors.  The
     * actual access of the ComponentHandler is delegated to the Container.
     *
     * @param  role  The role we intend to access a Component for.
     * @param  hint  The hint that we use as a qualifier
     *         (note: if null, the default implementation is returned).
     *
     * @return true  if a reference to the role exists.
     */
    public boolean has( final String role, final Object hint )
    {
        final Map hintMap = (Map) m_mapper.get( role );
        boolean hasComponent = false;

        if ( null != hintMap )
        {
            hasComponent = true;
        }

        if ( hasComponent )
        {
            if ( null == hint )
            {
                // no hint -> try selector
                hasComponent = hintMap.containsKey( SELECTOR_ENTRY );

                if ( !hasComponent )
                {
                    // no hint -> try DEFAULT_ENTRY
                    hasComponent = hintMap.containsKey( DEFAULT_ENTRY );
                }
            }
            else
            {
                // hint -> find it
                hasComponent = hintMap.containsKey( hint );
            }
        }

        return hasComponent;
    }

    /**
     * Initializes the impl and all the components it hosts so that they are ready to be used.
     * Unless components ask for lazy activation, this is where they are activated.
     *
     * @throws CompositeException if one or more components could not be initialized.
     *                   The system <i>is</i> running properly so if the missing components are
     *                   not vital to operation, it should be possible to recover gracefully
     */
    public void initialize()
            throws CompositeException, Exception
    {
        // go over all components
        final Iterator i = m_components.iterator();
        final BoundedFifoBuffer buffer = new BoundedFifoBuffer( Math.max( m_components.size(), 1 ) );

        // just to be on the safe side
        m_extManager.makeReadOnly();

        verifyComponents();

        ComponentHandlerEntry entry;
        while ( i.hasNext() )
        {
            entry = (ComponentHandlerEntry) i.next();
            try
            {
                final ComponentHandler handler = entry.getHandler();

                // Depending on the activation policy of the component decide
                //  how to initialize the component.  Make sure that we can
                //  perform the specified activation policy, if not modify it.
                int activation = entry.getMetaData().getActivation();
                if ( activation == ComponentHandlerMetaData.ACTIVATION_BACKGROUND )
                {
                    // If a sink is not set then we must change to inline.
                    if ( null == m_commandSink )
                    {
                        activation = ComponentHandlerMetaData.ACTIVATION_INLINE;
                    }
                }

                // We now have an activation policy we can handle.
                switch ( activation )
                {
                    case ComponentHandlerMetaData.ACTIVATION_BACKGROUND:
                        // Add a command to initialize the component to the command
                        //  sink so it will be initialized asynchronously in the
                        //  background.
                        final PrepareHandlerCommand element =
                                new PrepareHandlerCommand( handler, getLogger() );
                        m_commandSink.enqueue( element );
                        break;

                    case ComponentHandlerMetaData.ACTIVATION_INLINE:
                        // Initialize the component now.
                        handler.prepareHandler();
                        break;

                    default: // ComponentHandlerMetaData.ACTIVATION_LAZY
                        if ( getLogger().isDebugEnabled() )
                        {
                            final String message = "ComponentHandler (" + handler +
                                    ") has specified a lazy activation policy, " +
                                    "initialization deferred until first use";
                            getLogger().debug( message );
                        }
                        break;
                }
            }
            catch ( final CascadingException e )
            {
                final String cName = entry.getMetaData().getName();

                if ( getLogger().isWarnEnabled() )
                {
                    final String message = "Could not initialize component " + cName;
                    getLogger().warn( message, e );

                    final String cause = "Cause for exception";
                    getLogger().warn( cause, e.getCause() );
                }
                buffer.add( e );
            }
            catch ( final Exception e )
            {
                final String cName = entry.getMetaData().getName();

                if ( getLogger().isWarnEnabled() )
                {
                    final String message = "Could not initialize component " + cName;
                    getLogger().warn( message, e );
                }
                buffer.add( e );
            }
            catch ( final LinkageError le )
            {
                final String cName = entry.getMetaData().getName();

                if ( getLogger().isWarnEnabled() )
                {
                    final String message = "Could not initialize component " + cName;
                    getLogger().warn( message, le );
                }
                buffer.add( le );
            }
        }

        // if we were unable to activate one or more components,
        // throw an exception
        if ( buffer.size() > 0 )
        {
            throw new CompositeException( (Exception[]) buffer.toArray( new Exception[0] ),
                    "unable to instantiate one or more components" );
        }
    }

    private void verifyComponents() throws CyclicDependencyException
    {
        Map vertexMap = new HashMap();
        List vertices = new ArrayList( m_components.size() );
        Iterator it = m_components.iterator();

        while ( it.hasNext() )
        {
            ComponentHandlerEntry entry = (ComponentHandlerEntry) it.next();
            ComponentHandlerMetaData metaData = entry.getMetaData();

            String name = metaData.getName();
            Vertex v = (Vertex) vertexMap.get( name );
            if ( v == null )
            {
                v = new Vertex( name, entry.getHandler() );
                vertexMap.put( name, v );
                vertices.add( v );
            }

            MetaInfoEntry meta = m_metaManager.getMetaInfoForClassname( metaData.getClassname() );

            Iterator dit = meta.getDependencies().iterator();
            while ( dit.hasNext() )
            {
                Map deps = (Map) m_mapper.get( dit.next() );

                /* Ignore for now...  It is probably due to a component requiring a Container
                 * component....  This happens when a required Service is not _directly_ handled
                 * by this container.
                 */
                if ( null == deps ) continue;

                Iterator mdit = deps.entrySet().iterator();
                while ( mdit.hasNext() )
                {
                    Map.Entry depEntry = (Map.Entry) mdit.next();

                    // If this key is neither the DEFAULT_ENTRY or the SELECTOR_ENTRY then we
                    //  want to add a dependency vertex.
                    if ( !( depEntry.getKey().equals( DEFAULT_ENTRY ) ||
                            depEntry.getKey().equals( SELECTOR_ENTRY ) ) )
                    {
                        String dName = depEntry.getKey().toString();
                        Vertex dv = (Vertex) vertexMap.get( dName );
                        if ( dv == null )
                        {
                            dv = new Vertex( dName, depEntry.getValue() );
                            vertexMap.put( dName, dv );
                            vertices.add( dv );
                        }
                        v.addDependency( dv );
                    }
                }
            }
        }

        DirectedAcyclicGraphVerifier.topologicalSort( vertices );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Component initialization order:" );
            int i = 1;
            for ( Iterator iter = vertices.iterator(); iter.hasNext(); i++ )
            {
                final Vertex v = (Vertex) iter.next();
                final int o = v.getOrder();
                
                getLogger().debug(
                    "  #" + i + " (" + o + ") : " + v.getName() 
                    + ( o > 0 ? ( " [ " + getVertexDeps(v) + " ]" ) : "" )
                );
            }
        }

        Collections.reverse( vertices );

        m_shutDownOrder = vertices;
    }

    /**
     * Disposes of all components and frees resources that they consume.
     */
    public void dispose()
    {

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Component shutdown order:" );
            int i = 1;
            for ( Iterator iter = m_shutDownOrder.iterator(); iter.hasNext(); i++ )
            {
                final Vertex v = (Vertex) iter.next();
                final int o = v.getOrder();
                
                getLogger().debug(
                    "  #" + i + " (" + o + ") : " + v.getName() 
                    + ( o > 0 ? ( " [ " + getVertexDeps(v) + " ]" ) : "" )
                );
            }
        }

        final Iterator i = m_shutDownOrder.iterator();
        while ( i.hasNext() )
        {
            final Vertex entry = (Vertex) i.next();
            final ComponentHandler handler = (ComponentHandler) entry.getNode();

            if ( getLogger().isDebugEnabled() ) getLogger().debug( "Shutting down: " + handler );
            ContainerUtil.dispose( handler );
            if ( getLogger().isDebugEnabled() ) getLogger().debug( "Done." );
        }
    }
    
    /**
     * Obtain the names of the dependencies for a given vertex.
     * 
     * @param vertex vertex to examine
     * @return comma separated String listing the dependencies for this vertex
     */
    private String getVertexDeps( final Vertex vertex )
    {
        final List deps = vertex.getDependencies();
        final StringBuffer buf = new StringBuffer( "" );
        
        for ( Iterator i = deps.iterator(); i.hasNext(); )
        {
            final Vertex dep = ( Vertex ) i.next();
            buf.append( dep.getName() );

            if ( i.hasNext() ) {
                buf.append( ", " );
            }
        }
        
        return buf.toString();
    }

    /**
     * Exposes to subclasses the service manager which this impl
     * uses to manage its child components.
     * The returned ServiceManager <i>is</i> aware of the services passed
     * in to <i>this</i> impl, and services that were passed in through
     * service() are hence available to subclasses.
     *
     * @return the service manager that contains the child components.
     */
    protected ServiceManager getServiceManager()
    {
        return m_serviceManager;
    }

    /**
     * Override this method to control creation of the serviceManager
     * belonging to this container. This serviceManager is passed to
     * child components as they are being created and is exposed via
     * the getServiceManager() method.
     * Invoked from the service() method.
     *
     * However even a self-contained container should be careful about
     * cutting access to parent serviceManager completely, as important
     * (and required) system services including Sink, LoggerManager,
     * InstrumentManager, PoolManager and LifecycleExtensionManager
     * are passed via ServiceManager also. SourceResolver hangs somewhere
     * in between system and "user space" services.
     *
     * It's more or less okay to cut access to them if our child
     * components do not need them and are not containers themselves,
     * but if we have containers as our children they will require these
     * services.
     */
    protected ServiceManager provideServiceManager( final ServiceManager parent )
            throws ServiceException
    {
        return new FortressServiceManager( this, parent );
    }

    /**
     * Override this method to control what context will be passed to
     * the components created by this container. Called the first time
     * a component being created - withing this implementation it is
     * a part of the configure() stage.
     * You may derive your context from m_context or create a new one.
     */
    protected Context provideComponentContext( final Context parent )
            throws Exception
    {
        /* the default implementation: just use the same as for container itself */
        return parent;
    }
}
