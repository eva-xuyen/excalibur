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

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.InstrumentedResourceLimitingPool;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;

/**
 * The PoolableComponentHandler to make sure that poolable components are initialized
 * destroyed and pooled correctly.
 * <p>
 * Components which implement Poolable may be configured to be pooled using the following
 *  example configuration.  This example assumes that the user component class MyComp
 *  implements Poolable.
 * <p>
 * Configuration Example:
 * <pre>
 *   &lt;my-comp pool-max="8" pool-max-strict="false" pool-blocking="true" pool-timeout="0"
 *            pool-trim-interval="0"/&gt;
 * </pre>
 * <p>
 * Roles Example:
 * <pre>
 *   &lt;role name="com.mypkg.MyComponent"
 *         shorthand="my-comp"
 *         default-class="com.mypkg.DefaultMyComponent"/&gt;
 * </pre>
 * <p>
 * Configuration Attributes:
 * <ul>
 * <li>The <code>pool-max</code> attribute is used to set the maximum number of components which
 *  will be pooled.  See the <code>pool-max-strict</code> and <code>pool-blocking</code>
 *  attributes.  (Defaults to "8")</li>
 *
 * <li>The <code>pool-max-strict</code> attribute is used to configure whether the Component
 *  Manager should allow more than <code>pool-max</code> Poolables to be looked up at the same
 *  time.  Setting this to true will throw an exception if the <code>pool-blocking</code> attribute
 *  is false.  A value of false will allow additional instances of the Component to be created
 *  to serve requests, but the additional instances will not be pooled.  (Defaults to "false")
 *
 * <li>The <code>pool-blocking</code> attributes is used to configure whether the Component Manager
 *  should block or throw an Exception when more than <code>pool-max</code> Poolables are looked
 *  up at the same time.  Setting this to true will cause requests to block until another thread
 *  releases a Poolable back to the Component Manager.  False will cause an exception to be thrown.
 *  This attribute is ignored if <code>pool-max-strict</code> is false.  (Defaults to "true")</li>
 *
 * <li>The <code>pool-timeout</code> attribute is used to specify the maximum amount of time in
 *  milliseconds that a lookup will block for if Poolables are unavailable.  If the timeout expires
 *  before another thread releases a Poolable back to the Component Managaer then an Exception
 *  will be thrown.  A value of "0" specifies that the block will never timeout.
 *  (Defaults to "0")</li>
 *
 * <li>The <code>pool-trim-interval</code> attribute is used to
 * specify, in milliseconds, how long idle Poolables will be
 * maintained in the pool before being closed.  For a complete
 * explanation on how this works, see {@link
 * org.apache.avalon.excalibur.pool.InstrumentedResourceLimitingPool#trim()}
 * (Defaults to "0", trimming disabled)</li>
 *
 * <li>The <code>pool-min</code> and <code>pool-grow</code> attributes
 * were deprecated as the underlying Pool ({@link
 * org.apache.avalon.excalibur.pool.InstrumentedResourceLimitingPool}) does not
 * make use of them.  Configurations which still use these attributes
 * will continue to function however, a minimum pool size is no longer
 * applicable.
 *
 * </ul>
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/30 14:15:23 $
 * @since 4.0
 */
public class PoolableComponentHandler
    extends ComponentHandler
{
    /** The default max size of the pool */
    public static final int DEFAULT_MAX_POOL_SIZE = 8;

    /** The instance of the ComponentFactory that creates and disposes of the Component */
    private final DefaultComponentFactory m_factory;

    /** The pool of components for <code>Poolable</code> Components */
    private final InstrumentedResourceLimitingPool m_pool;

    /** State management boolean stating whether the Handler is initialized or not */
    private boolean m_initialized = false;

    /** State management boolean stating whether the Handler is disposed or not */
    private boolean m_disposed = false;

    /**
     * Create a ComponentHandler that takes care of hiding the details of
     * whether a Component is ThreadSafe, Poolable, or SingleThreaded.
     * It falls back to SingleThreaded if not specified.
     */
    protected PoolableComponentHandler( final String role,
                                        final Class componentClass,
                                        final Configuration config,
                                        final ComponentManager manager,
                                        final Context context,
                                        final RoleManager roles,
                                        final LogkitLoggerManager logkit )
        throws Exception
    {
        this(
            new DefaultComponentFactory( role, componentClass, config, manager, context, roles, logkit ),
            config );
    }

    /**
     * Create a PoolableComponentHandler which manages a pool of Components
     *  created by the specified factory object.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the ComponentHandler.
     * @param config The configuration to use to configure the pool.
     */
    public PoolableComponentHandler( final DefaultComponentFactory factory,
                                     final Configuration config )
        throws Exception
    {
        m_factory = factory;

        int poolMax = config.getAttributeAsInteger( "pool-max", DEFAULT_MAX_POOL_SIZE );
        boolean poolMaxStrict = config.getAttributeAsBoolean( "pool-max-strict", false );
        boolean poolBlocking = config.getAttributeAsBoolean( "pool-blocking", true );
        long poolTimeout = config.getAttributeAsLong( "pool-timeout", 0 );
        long poolTrimInterval = config.getAttributeAsLong( "pool-trim-interval", 0 );

        m_pool = new InstrumentedResourceLimitingPool( m_factory, poolMax, poolMaxStrict, poolBlocking,
                                           poolTimeout, poolTrimInterval );
        // Initialize the Instrumentable elements.
        addChildInstrumentable( m_pool );
    }

    /**
     * Initialize the ComponentHandler.
     */
    public void initialize()
    {
        if( m_initialized )
        {
            return;
        }

        m_factory.setLogger( getLogkitLogger() );
        m_factory.enableLogging( getLogger() );
        m_pool.enableLogging( getLogger() );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "ComponentHandler initialized for: " +
                               m_factory.getCreatedClass().getName() );
        }

        m_initialized = true;
    }

    /**
     * Get a reference of the desired Component
     */
    protected Component doGet()
        throws Exception
    {
        if( !m_initialized )
        {
            throw new IllegalStateException( "You cannot get a component from an " +
                                             "uninitialized holder." );
        }

        if( m_disposed )
        {
            throw new IllegalStateException( "You cannot get a component from " +
                                             "a disposed holder" );
        }

        return (Component)m_pool.get();
    }

    /**
     * Return a reference of the desired Component
     */
    protected void doPut( final Component component )
    {
        if( !m_initialized )
        {
            throw new IllegalStateException( "You cannot put a component in " +
                                             "an uninitialized holder." );
        }

        m_pool.put( (Poolable)component );
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose()
    {
        m_pool.dispose();

        if( m_factory instanceof Disposable )
        {
            ( (Disposable)m_factory ).dispose();
        }

        m_disposed = true;
    }
}
