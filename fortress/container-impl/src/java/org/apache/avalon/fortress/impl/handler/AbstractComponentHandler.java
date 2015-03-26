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

package org.apache.avalon.fortress.impl.handler;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.d_haven.mpool.ObjectFactory;

/**
 * AbstractComponentHandler class, ensures components are initialized
 * and destroyed correctly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.19 $ $Date: 2004/03/13 13:56:51 $
 * @since 4.0
 */
public abstract class AbstractComponentHandler
    extends AbstractLogEnabledInstrumentable
    implements Serviceable, Initializable, Disposable, ComponentHandler
{
    private CounterInstrument m_request = new CounterInstrument( "requests" );
    private CounterInstrument m_release = new CounterInstrument( "releases" );

    /**
     * The instance of the ComponentFactory that creates and disposes of the
     * Component
     */
    protected ObjectFactory m_factory;

    /**
     * State management boolean stating whether the Handler is initialized or
     * not
     */
    protected boolean m_prepared;

    /**
     * State management boolean stating whether the Handler is disposed or
     * not
     */
    protected boolean m_disposed;

    /** Logger for factory */
    protected Logger m_logger;

    /** Logger Manager */
    protected LoggerManager m_loggerManager;

    /**
     * @avalon.dependency type="LoggerManager"
     */
    public void service( final ServiceManager manager )
        throws ServiceException
    {
        m_loggerManager =
            (LoggerManager) manager.lookup( LoggerManager.ROLE );
        m_factory =
            (ObjectFactory) manager.lookup( ObjectFactory.class.getName() );
    }

    public void initialize()
        throws Exception
    {
        final String classname = getClass().getName();
        final int index = classname.lastIndexOf( '.' );
        final String name = classname.substring( index + 1 );

        String loggerName = name.toLowerCase();
        if ( name.endsWith( "ComponentHandler" ) )
        {
            final int endIndex = loggerName.length() - 16;
            loggerName = loggerName.substring( 0, endIndex );
        }

        final String categoryName = "system.handler." + loggerName;
        m_logger =
            m_loggerManager.getLoggerForCategory( categoryName );

        if ( m_factory instanceof Instrumentable )
        {
            addChildInstrumentable( (Instrumentable) m_factory );
        }

        addInstrument( m_request );
        addInstrument( m_release );

        setInstrumentableName( name );
    }

    /**
     * Return the component's class that this handler is trying to create.
     * Used for deubug information.
     *
     * @return the <code>Class</code> object for the component
     */
    public Class getComponentClass()
    {
        return m_factory.getCreatedClass();
    }

    /**
     * Actually prepare the handler and make it ready to
     * handle component access.
     *
     * @throws Exception if unable to prepare handler
     */
    public synchronized void prepareHandler()
        throws Exception
    {
        if ( m_prepared )
        {
            return;
        }

        if ( m_disposed )
        {
            final String message = "Attempted to prepare disposed ComponentHandler for : " +
                m_factory.getCreatedClass().getName();
            m_logger.warn( message );

            return;
        }

        doPrepare();

        if ( m_logger.isDebugEnabled() )
        {
            final String message = "ComponentHandler initialized for: " +
                m_factory.getCreatedClass().getName();
            m_logger.debug( message );
        }

        m_prepared = true;
    }

    /**
     * Initialize the ComponentHandler.
     * Subclasses should overide this to do their own initialization.
     *
     * @throws Exception if there is a problem
     */
    protected void doPrepare() throws Exception
    {}

    /**
     * Get a reference of the desired Component
     * @return the component
     */
    public Object get()
        throws Exception
    {
        if ( !m_prepared )
        {
            prepareHandler();
        }

        if ( m_disposed )
        {
            final String message =
                "You cannot get a component from a disposed holder";
            throw new IllegalStateException( message );
        }

        if ( m_request.isActive() )
        {
            m_request.increment();
        }

        return doGet();
    }

    /**
     * Subclasses should actually overide this to do the work
     * of retrieving a service.
     *
     * @return the service
     * @throws Exception if unable to aquire service
     */
    protected abstract Object doGet()
        throws Exception;

    /**
     * Return a reference of the desired Component
     * @param component the component
     */
    public void put( final Object component )
    {
        if ( !m_prepared )
        {
            final String message =
                "You cannot put a component in an uninitialized holder";
            throw new IllegalStateException( message );
        }

        if ( m_release.isActive() )
        {
            m_release.increment();
        }

        doPut( component );
    }

    /**
     * Subclasses should overide this to return component to handler.
     *
     * @param component the component
     */
    protected void doPut( final Object component )
    {
    }

    /**
     * Create a new component for handler.
     *
     * @return the new component
     * @throws Exception if unable to create new component
     */
    protected Object newComponent()
        throws Exception
    {
        try
        {
            final Object component = m_factory.newInstance();
            if ( component instanceof ReleasableComponent )
            {
                ((ReleasableComponent) component).initialize( this );
            }
            return component;
        }
        catch ( final Exception e )
        {
            if ( m_logger.isDebugEnabled() )
            {
                final String message = "Unable to create new instance";
                m_logger.debug( message, e );
            }

            throw e;
        }
    }

    /**
     * Dispose of the specified component.
     *
     * @param component the component
     */
    protected void disposeComponent( final Object component )
    {
        if ( null == component )
        {
            return;
        }
        try
        {
            m_factory.dispose( component );
        }
        catch ( final Exception e )
        {
            if ( m_logger.isWarnEnabled() )
            {
                m_logger.warn( "Error disposing component", e );
            }
        }
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose()
    {
        doDispose();
        try
        {
            ContainerUtil.dispose( m_factory );
        }
        catch ( RuntimeException e )
        {
            if ( m_logger.isWarnEnabled() )
            {
                final String message = "Error decommissioning component: " +
                    m_factory.getCreatedClass().getName();
                m_logger.warn( message, e );
            }
        }

        m_disposed = true;
    }

    /**
     * Dispose handler specific resources.
     * Subclasses should overide this to provide their own funcitonality.
     */
    protected void doDispose()
    {
    }

    /**
     * Represents the handler as a string.
     * @return the string representation of the handler
     */
    public String toString()
    {
        return getClass().getName() + "[for: " + m_factory.getCreatedClass().getName() + "]";
    }
}
