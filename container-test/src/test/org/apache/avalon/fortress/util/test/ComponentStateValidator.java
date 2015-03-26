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

package org.apache.avalon.fortress.util.test;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Reconfigurable;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Recontextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.service.Serviceable;

/**
 * This class provides basic facilities for enforcing Avalon's contracts
 * within your own code.
 *
 * Based on Avalon version from Sandbox.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 */
public final class ComponentStateValidator
{
    private static final String WRITE_FAIL = "Value is already bound";

    // Interfaces
    private static final long LOG_ENABLED = 0x00000001;
    private static final long LOGGABLE = 0x00000002;
    private static final long CONTEXTUALIZABLE = 0x00000004;
    private static final long COMPOSABLE = 0x00000008;
    private static final long SERVICEABLE = 0x00000010;
    private static final long CONFIGURABLE = 0x00000020;
    private static final long PARAMETERIZABLE = 0x00000040;
    private static final long INITIALIZABLE = 0x00000080;
    private static final long STARTABLE = 0x00000100;
    private static final long SUSPENDABLE = 0x00001000;
    private static final long RECONTEXTUALIZABLE = 0x00002000;
    private static final long RECOMPOSABLE = 0x00004000;
    private static final long RECONFIGURABLE = 0x00008000;
    private static final long DISPOSABLE = 0x00100000;

    // Initialization Methods.
    private static final long ENABLE_LOGGING = 0x00000001;
    private static final long SET_LOGGER = 0x00000002;
    private static final long CONTEXTUALIZE = 0x00000004;
    private static final long SERVICE = 0x00000008;
    private static final long COMPOSE = 0x00000010;
    private static final long CONFIGURE = 0x00000020;
    private static final long PARAMETERIZE = 0x00000040;
    private static final long INITIALIZE = 0x00000080;
    private static final long START = 0x00000100;
    private static final long INIT_COMPLETE = 0x00000400;

    // Active Service Methods
    private static final long SUSPEND = 0x00001000;
    private static final long RECONTEXTUALIZE = 0x00002000;
    private static final long RECOMPOSE = 0x00004000;
    private static final long RECONFIGURE = 0x00008000;
    private static final long RESUME = 0x00010000;

    // Destruction Methods
    private static final long STOP = 0x00100000;
    private static final long DISPOSE = 0x00200000;

    // Masks
    private static final long INIT_MASK = ENABLE_LOGGING|SET_LOGGER|
        CONTEXTUALIZE|COMPOSE|SERVICE|CONFIGURE|PARAMETERIZE|INITIALIZE|
        START;

    private final long m_interfaces;
    private final long m_methods;
    private long m_state;
    private boolean m_active;
    private final Object m_object;

    /**
     * Create state validator from object (this can be used for more than just
     * components).
     */
    public ComponentStateValidator( final Object object )
    {
        m_object = object;

        long methods = 0;
        long interfaces = 0;

        if ( object instanceof LogEnabled )
        {
            interfaces |= LOG_ENABLED;
            methods |= ENABLE_LOGGING;
        }

        if ( object instanceof Loggable )
        {
            interfaces |= LOGGABLE;
            methods |= SET_LOGGER;
        }

        if ( object instanceof Contextualizable )
        {
            interfaces |= CONTEXTUALIZABLE;
            methods |= CONTEXTUALIZE;
        }

        if ( object instanceof Serviceable )
        {
            interfaces |= SERVICEABLE;
            methods |= SERVICE;
        }

        if ( object instanceof Composable )
        {
            if ( ( interfaces&SERVICEABLE ) > 0 )
            {
                throw new IllegalStateException( "Cannot implement Composable and Serviceable together" );
            }

            interfaces |= COMPOSABLE;
            methods |= COMPOSE;
        }

        if ( object instanceof Configurable )
        {
            interfaces |= CONFIGURABLE;
            methods |= CONFIGURE;
        }

        if ( object instanceof Parameterizable )
        {
            interfaces |= PARAMETERIZABLE;
            methods |= PARAMETERIZE;
        }

        if ( object instanceof Initializable )
        {
            interfaces |= INITIALIZABLE;
            methods |= INITIALIZE;
        }

        if ( object instanceof Startable )
        {
            interfaces |= STARTABLE;
            methods |= START|STOP;
        }

        if ( object instanceof Suspendable )
        {
            interfaces |= SUSPENDABLE;
            methods |= SUSPEND|RESUME;
        }

        if ( object instanceof Recontextualizable )
        {
            interfaces |= RECONTEXTUALIZABLE;
            methods |= RECONTEXTUALIZE;
        }

        if ( object instanceof Recomposable )
        {
            interfaces |= RECOMPOSABLE;
            methods |= RECOMPOSE;
        }

        if ( object instanceof Reconfigurable )
        {
            interfaces |= RECONFIGURABLE;
            methods |= RECONFIGURE;
        }

        if ( object instanceof Disposable )
        {
            interfaces |= DISPOSABLE;
            methods |= DISPOSE;
        }

        m_methods = methods;
        m_interfaces = interfaces;

        generalCheckInitComplete();
    }

    private String getInterfaceName( long interfaceId )
    {
        if ( interfaceId == LOG_ENABLED )
        {
            return LogEnabled.class.getName();
        }
        else if ( interfaceId == LOGGABLE )
        {
            return Loggable.class.getName();
        }
        else if ( interfaceId == CONTEXTUALIZABLE )
        {
            return Contextualizable.class.getName();
        }
        else if ( interfaceId == SERVICEABLE )
        {
            return Serviceable.class.getName();
        }
        else if ( interfaceId == COMPOSABLE )
        {
            return Composable.class.getName();
        }
        else if ( interfaceId == CONFIGURABLE )
        {
            return Configurable.class.getName();
        }
        else if ( interfaceId == PARAMETERIZABLE )
        {
            return Parameterizable.class.getName();
        }
        else if ( interfaceId == INITIALIZABLE )
        {
            return Initializable.class.getName();
        }
        else if ( interfaceId == STARTABLE )
        {
            return Startable.class.getName();
        }
        else if ( interfaceId == SUSPENDABLE )
        {
            return Suspendable.class.getName();
        }
        else if ( interfaceId == RECONTEXTUALIZABLE )
        {
            return Recontextualizable.class.getName();
        }
        else if ( interfaceId == RECOMPOSABLE )
        {
            return Recomposable.class.getName();
        }
        else if ( interfaceId == RECONFIGURABLE )
        {
            return Reconfigurable.class.getName();
        }
        else if ( interfaceId == DISPOSABLE )
        {
            return Disposable.class.getName();
        }
        else
        {
            throw new IllegalStateException( "Unknown Interface Id " + interfaceId );
        }
    }

    private String getMethodName( long methodId )
    {
        if ( methodId == ENABLE_LOGGING )
        {
            return "enableLogging()";
        }
        else if ( methodId == SET_LOGGER )
        {
            return "setLogger()";
        }
        else if ( methodId == CONTEXTUALIZE )
        {
            return "contextualize()";
        }
        else if ( methodId == SERVICE )
        {
            return "service()";
        }
        else if ( methodId == COMPOSE )
        {
            return "compose()";
        }
        else if ( methodId == CONFIGURE )
        {
            return "configure()";
        }
        else if ( methodId == PARAMETERIZE )
        {
            return "parameterize()";
        }
        else if ( methodId == INITIALIZE )
        {
            return "initialize()";
        }
        else if ( methodId == START )
        {
            return "start()";
        }
        else if ( methodId == SUSPEND )
        {
            return "suspend()";
        }
        else if ( methodId == RECONTEXTUALIZE )
        {
            return "recontextualize()";
        }
        else if ( methodId == RECOMPOSE )
        {
            return "recompose()";
        }
        else if ( methodId == RECONFIGURE )
        {
            return "reconfigure()";
        }
        else if ( methodId == RESUME )
        {
            return "resume()";
        }
        else if ( methodId == STOP )
        {
            return "stop()";
        }
        else if ( methodId == DISPOSE )
        {
            return "dispose()";
        }
        else
        {
            throw new IllegalStateException( "Unknown Method Id " + methodId );
        }
    }

    private String getLastMethod( long state )
    {
        for ( int i = 31; i >= 0; i-- )
        {
            long methodId = 0x1 << i;
            if ( ( state&methodId ) != 0 )
            {
                return getMethodName( methodId );
            }
        }
        throw new IllegalStateException( "No last state method found for state " + state );
    }

    /**
     * Test to see if this was the last initialization method.
     */
    private void generalCheckInitComplete()
    {
        if ( m_state == ( m_methods&INIT_MASK ) )
        {
            // All init methods called
            m_active = true;
        }
    }

    /**
     * Initialization methods must be called in order, must all be called, may
     *  not be called more than once, and may not be called once any of the
     *  Descruction methods have been called.
     */
    private void generalCheckInit( final String message, final long interfaceId, final long methodId )
    {
        if ( ( m_interfaces&interfaceId ) == 0 )
        {
            // Interface not implemented
            if ( message == null )
            {
                throw new IllegalStateException( m_object.getClass().getName() +
                    " does not implement " + getInterfaceName( interfaceId ) + "." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else if ( ( m_state&methodId ) > 0 )
        {
            // Method already called.
            if ( message == null )
            {
                throw new IllegalStateException( getMethodName( methodId ) + " already called." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else if ( m_state > methodId )
        {
            // Method called after a descruction method was called.
            if ( message == null )
            {
                throw new IllegalStateException( getMethodName( methodId ) +
                    " can not be called after " + getLastMethod( m_state ) + "." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else if ( ( m_state&( methodId - 1 ) ) != ( m_methods&( methodId - 1 ) ) )
        {
            // One or more of the methods that should have been called before
            //  this method was not.
            if ( message == null )
            {
                throw new IllegalStateException( getMethodName( methodId ) +
                    " called out of order. " + getLastMethod( m_methods&( methodId - 1 ) ) +
                    " must be called first." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }

        // Add this method to the state
        m_state |= methodId;

        // See if the initialization is complete.
        generalCheckInitComplete();
    }

    /**
     * Active Service methods may only be called after all of the
     *  Initialization methods have been called, any before any of the
     *  Descruction methods have been called.  While in the active state,
     *  the contracts of the methods allow the active state methods to be
     *  called any number of times, in any order.
     * The resume() method should do nothing if suspend() has not yet been
     *  called for example.
     */
    private void generalCheckActive( final String message, final long interfaceId, final long methodId )
    {
        if ( ( m_interfaces&interfaceId ) == 0 )
        {
            // Interface not implemented
            if ( message == null )
            {
                throw new IllegalStateException( m_object.getClass().getName() +
                    " does not implement " + getInterfaceName( interfaceId ) + "." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else if ( !m_active )
        {
            // Component not in the active state.
            if ( m_state < INIT_COMPLETE )
            {
                // Still expecting initialization methods.
                if ( message == null )
                {
                    throw new IllegalStateException( getMethodName( methodId ) +
                        " called before component was made active. " +
                        getLastMethod( m_methods&( INIT_COMPLETE - 1 ) ) +
                        " must be called first." );
                }
                else
                {
                    throw new IllegalStateException( message );
                }
            }
            else
            {
                // One or more destruction methods have been called.
                if ( message == null )
                {
                    throw new IllegalStateException( getMethodName( methodId ) +
                        " called after component was made inactive.  Cannot call after " +
                        getLastMethod( m_state ) + "." );
                }
                else
                {
                    throw new IllegalStateException( message );
                }
            }
        }
    }

    /**
     * Descruction Methods must be called in order.  They may be called before
     *  all of the Initialization methods have been called if there was an
     *  error.
     */
    private void generalCheckDest( final String message, final long interfaceId, final long methodId )
    {
        if ( ( m_interfaces&interfaceId ) == 0 )
        {
            // Interface not implemented
            if ( message == null )
            {
                throw new IllegalStateException( m_object.getClass().getName() +
                    " does not implement " + getInterfaceName( interfaceId ) + "." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else if ( m_state > methodId )
        {
            // Method called after a later descruction method was called.
            if ( message == null )
            {
                throw new IllegalStateException( getMethodName( methodId ) +
                    " can not be called after " + getLastMethod( m_state ) + "." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }

        // Add this method to the state
        m_state |= methodId;

        // Deactivate
        m_active = false;
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the ENABLE_LOGGING state has already been set, if the component implements
     * LogEnabled, and if the state has progressed beyond the Logger stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkLogEnabled()
    {
        checkLogEnabled( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the ENABLE_LOGGING state has already been set, if the component implements
     * LogEnabled, and if the state has progressed beyond the Logger stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkLogEnabled( final String message )
    {
        generalCheckInit( message, LOG_ENABLED, ENABLE_LOGGING );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SET_LOGGER state has already been set, if the component implements
     * Loggable, and if the state has progressed beyond the Logger stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkLoggable()
    {
        checkLogEnabled( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SET_LOGGER state has already been set, if the component implements
     * Loggable, and if the state has progressed beyond the Logger stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkLoggable( final String message )
    {
        generalCheckInit( message, LOGGABLE, SET_LOGGER );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the CONTEXTUALIZED state has already been set, if the component implements
     * Contextualizable, and if the state has progressed beyond the Context stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkContextualized()
    {
        checkContextualized( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the CONTEXTUALIZED state has already been set, if the component implements
     * Contextualizable, and if the state has progressed beyond the Context stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkContextualized( final String message )
    {
        generalCheckInit( message, CONTEXTUALIZABLE, CONTEXTUALIZE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SERVICE state has already been set, if the component implements
     * Composable, and if the state has progressed beyond the Configuration stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkServiced()
    {
        checkServiced( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SERVICE state has already been set, if the component implements
     * Composable, and if the state has progressed beyond the Configuration stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkServiced( final String message )
    {
        generalCheckInit( message, SERVICEABLE, SERVICE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the COMPOSED state has already been set, if the component implements
     * Composable, and if the state has progressed beyond the Configuration stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkComposed()
    {
        checkComposed( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the COMPOSED state has already been set, if the component implements
     * Composable, and if the state has progressed beyond the Configuration stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkComposed( final String message )
    {
        generalCheckInit( message, COMPOSABLE, COMPOSE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the CONFIGURED state has already been set, if the component implements
     * Configurable, and if the state has progressed beyond the Configuration stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkConfigured()
    {
        checkConfigured( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the CONFIGURED state has already been set, if the component implements
     * Configurable, and if the state has progressed beyond the Configuration stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkConfigured( final String message )
    {
        generalCheckInit( message, CONFIGURABLE, CONFIGURE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the PARAMETERIZED state has already been set, if the component implements
     * Parameterizable, and if the state has progressed beyond the Parameters stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkParameterized()
    {
        checkParameterized( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the PARAMETERIZED state has already been set, if the component implements
     * Parameterizable, and if the state has progressed beyond the Parameters stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkParameterized( final String message )
    {
        generalCheckInit( message, PARAMETERIZABLE, PARAMETERIZE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the INITIALIZED state has already been set, if the component implements
     * Initializable, and if the state has progressed beyond the <code>initialize</code> stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkInitialized()
    {
        checkInitialized( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the INITIALIZED state has already been set, if the component implements
     * Initializable, and if the state has progressed beyond the <code>initialize</code> stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkInitialized( final String message )
    {
        generalCheckInit( message, INITIALIZABLE, INITIALIZE );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the STARTED state has already been set, if the component implements
     * Startable, and if the state has progressed beyond the <code>start</code> stage.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkStarted()
    {
        checkStarted( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the STARTED state has already been set, if the component implements
     * Startable, and if the state has progressed beyond the <code>start</code> stage.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkStarted( final String message )
    {
        generalCheckInit( message, STARTABLE, START );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SUSPENDED state has already been set, if the component implements
     * Suspendable, and if the Component is active.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkSuspended()
    {
        checkSuspended( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SUSPENDED state has already been set, if the component implements
     * Suspendable, and if the Component is active.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkSuspended( final String message )
    {
        generalCheckActive( message, SUSPENDABLE, SUSPEND );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SUSPENDED state has not been set, if the component implements
     * Suspendable, and if the Component is active.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkResumed()
    {
        checkResumed( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the SUSPENDED state has not been set, if the component implements
     * Suspendable, and if the Component is active.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkResumed( final String message )
    {
        generalCheckActive( message, SUSPENDABLE, RESUME );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the STOPPED state has not been set, if the component implements
     * Startable, and if the Component is active.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkStopped()
    {
        checkStopped( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the STOPPED state has not been set, if the component implements
     * Startable, and if the Component is active.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkStopped( final String message )
    {
        generalCheckDest( message, STARTABLE, STOP );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the DISPOSED state has not been set, if the component implements
     * Disposable.
     *
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkDisposed()
    {
        checkDisposed( null );
    }

    /**
     * Throw an exception if the initialization is out of order.  It tests to see
     * if the DISPOSED state has not been set, if the component implements
     * Disposable.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkDisposed( final String message )
    {
        generalCheckDest( message, DISPOSABLE, DISPOSE );
    }

    /**
     * Checks to see if the state is active.
     *
     * @throws java.lang.IllegalStateException if the component is not active
     */
    public void checkActive()
    {
        checkActive( null );
    }

    /**
     * Checks to see if the state is active.
     *
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the component is not active
     */
    public void checkActive( final String message )
    {
        if ( isActive() )
        {
            return;
        }

        // Component not in the active state.
        if ( m_state < INIT_COMPLETE )
        {
            // Still expecting initialization methods.
            if ( message == null )
            {
                throw new IllegalStateException( "Component not in the active state. " +
                    getLastMethod( m_methods&( INIT_COMPLETE - 1 ) ) +
                    " was not called." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
        else
        {
            // One or more destruction methods have been called.
            if ( message == null )
            {
                throw new IllegalStateException( "Component not in the active state because " +
                    getLastMethod( m_state ) + " was called." );
            }
            else
            {
                throw new IllegalStateException( message );
            }
        }
    }

    /**
     * Checks to see if the state is active, and returns true or false.
     *
     * @return <code>true</code> if active, <code>false</code> if not
     */
    public boolean isActive()
    {
        return m_active;
    }

    /**
     * Make sure object has not been assigned yet.
     *
     * @param object to test
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkNotAssigned( final Object object )
    {
        checkNotAssigned( object, WRITE_FAIL );
    }

    /**
     * Make sure object has not been assigned yet.
     *
     * @param object to test
     * @param message the message to include in the thrown exception
     * @throws java.lang.IllegalStateException if the state is manage out of order
     */
    public void checkNotAssigned( final Object object, final String message )
    {
        if ( null != object )
        {
            throw new IllegalStateException( message );
        }
    }
}

