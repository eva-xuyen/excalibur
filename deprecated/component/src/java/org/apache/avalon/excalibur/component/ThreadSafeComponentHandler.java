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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;

/**
 * The ThreadSafeComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public class ThreadSafeComponentHandler
    extends ComponentHandler
{
    private Component m_instance;
    private final DefaultComponentFactory m_factory;
    private boolean m_initialized = false;
    private boolean m_disposed = false;

    /**
     * Create a ComponentHandler that takes care of hiding the details of
     * whether a Component is ThreadSafe, Poolable, or SingleThreaded.
     * It falls back to SingleThreaded if not specified.
     */
    protected ThreadSafeComponentHandler( final String role,
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
     * Create a ThreadSafeComponentHandler which manages a pool of Components
     *  created by the specified factory object.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the ComponentHandler.
     * @param config The configuration to use to configure the pool.
     */
    public ThreadSafeComponentHandler( final DefaultComponentFactory factory,
                                       final Configuration config )
        throws Exception
    {
        m_factory = factory;
    }

    /**
     * Create a ComponentHandler that takes care of hiding the details of
     * whether a Component is ThreadSafe, Poolable, or SingleThreaded.
     * It falls back to SingleThreaded if not specified.
     */
    protected ThreadSafeComponentHandler( final Component component )
        throws Exception
    {
        m_instance = component;
        m_factory = null;
    }

    /**
     * Initialize the ComponentHandler.
     */
    public void initialize()
        throws Exception
    {
        if( m_initialized )
        {
            return;
        }
        if( null != m_factory )
        {
            m_factory.setLogger( getLogkitLogger() );
            m_factory.enableLogging( getLogger() );
        }

        if( m_instance == null )
        {
            m_instance = (Component)m_factory.newInstance();
        }

        if( getLogger().isDebugEnabled() )
        {
            if( m_factory != null )
            {
                getLogger().debug( "ComponentHandler initialized for: " + m_factory.getCreatedClass().getName() );
            }
            else
            {
                getLogger().debug( "ComponentHandler initialized for: " + m_instance.getClass().getName() );
            }
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
            throw new IllegalStateException( "You cannot get a component from an uninitialized holder." );
        }

        if( m_disposed )
        {
            throw new IllegalStateException( "You cannot get a component from a disposed holder" );
        }

        return m_instance;
    }

    /**
     * Return a reference of the desired Component
     */
    protected void doPut( final Component component )
    {
        if( !m_initialized )
        {
            throw new IllegalStateException( "You cannot put a component in an uninitialized holder." );
        }
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose()
    {
        try
        {
            if( null != m_factory )
            {
                m_factory.decommission( m_instance );
            }
            else
            {
                if( m_instance instanceof Startable )
                {
                    ( (Startable)m_instance ).stop();
                }

                if( m_instance instanceof Disposable )
                {
                    ( (Disposable)m_instance ).dispose();
                }
            }

            m_instance = null;
        }
        catch( final Exception e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Error decommissioning component: " +
                                  m_factory.getCreatedClass().getName(), e );
            }
        }

        m_disposed = true;
    }
}
