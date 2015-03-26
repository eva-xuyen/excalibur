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

package org.apache.avalon.fortress.impl.factory;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Reconfigurable;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Recontextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Reparameterizable;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.d_haven.mpool.ObjectFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * AbstractObjectFactory does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public abstract class AbstractObjectFactory implements ObjectFactory, Instrumentable
{
    /**
     * The list interfaces that will not be proxied.
     */
    private static final Class[] INVALID_INTERFACES = new Class[]
    {
        Loggable.class,
        LogEnabled.class,
        Contextualizable.class,
        Recontextualizable.class,
        Composable.class,
        Recomposable.class,
        Serviceable.class,
        Configurable.class,
        Reconfigurable.class,
        Parameterizable.class,
        Reparameterizable.class,
        Initializable.class,
        Startable.class,
        Suspendable.class,
        Disposable.class,
        Serializable.class
    };

    /**
     * The {@link ObjectFactory ObjectFactory} proper
     * we delegate all calls to.
     */
    protected final ObjectFactory m_delegateFactory;

    public AbstractObjectFactory( final ObjectFactory objectFactory )
    {
        if ( null == objectFactory )
        {
            throw new NullPointerException( "objectFactory" );
        }

        m_delegateFactory = objectFactory;
    }

    /**
     * @see ObjectFactory#newInstance()
     */
    public abstract Object newInstance() throws Exception;

    /**
     * @see ObjectFactory#getCreatedClass()
     */
    public final Class getCreatedClass()
    {
        return m_delegateFactory.getCreatedClass();
    }

    /**
     * @see ObjectFactory#dispose(Object)
     */
    public abstract void dispose( Object object ) throws Exception;

    /* (non-Javadoc)
     * @see org.apache.excalibur.instrument.Instrumentable#setInstrumentableName(java.lang.String)
     */
    public final void setInstrumentableName( final String name )
    {
        if ( m_delegateFactory instanceof Instrumentable )
        {
            ( (Instrumentable) m_delegateFactory ).setInstrumentableName( name );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.instrument.Instrumentable#getInstrumentableName()
     */
    public final String getInstrumentableName()
    {
        if ( m_delegateFactory instanceof Instrumentable )
        {
            return ( (Instrumentable) m_delegateFactory ).getInstrumentableName();
        }

        return "";
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.instrument.Instrumentable#getInstruments()
     */
    public final Instrument[] getInstruments()
    {
        if ( m_delegateFactory instanceof Instrumentable )
        {
            return ( (Instrumentable) m_delegateFactory ).getInstruments();
        }

        return new Instrument[]{};
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.instrument.Instrumentable#getChildInstrumentables()
     */
    public final Instrumentable[] getChildInstrumentables()
    {
        if ( m_delegateFactory instanceof Instrumentable )
        {
            return ( (Instrumentable) m_delegateFactory ).getChildInstrumentables();
        }

        return new Instrumentable[]{};
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements and skipping invalid interfaces
     * (as defined in {@link #INVALID_INTERFACES}).
     *
     * @param clazz the class
     * @return the list of interfaces to proxy
     */
    protected static Class[] guessWorkInterfaces( final Class clazz )
    {
        final HashSet workInterfaces = new HashSet();

        // Get *all* interfaces
        guessWorkInterfaces( clazz, workInterfaces );

        // Make sure we have Component in there.
        workInterfaces.add( Component.class );

        // Remove the invalid ones.
        for ( int j = 0; j < INVALID_INTERFACES.length; j++ )
        {
            workInterfaces.remove(INVALID_INTERFACES[j]);
        }

        return (Class[]) workInterfaces.toArray( new Class[workInterfaces.size()] );
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements.
     *
     * @param clazz           the class
     * @param workInterfaces  the set of current work interfaces
     */
    private static void guessWorkInterfaces( final Class clazz,
                                             final Set workInterfaces )
    {
        if ( null != clazz )
        {
            addInterfaces( clazz.getInterfaces(), workInterfaces );

            guessWorkInterfaces( clazz.getSuperclass(), workInterfaces );
        }
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements.
     *
     * @param interfaces      the array of interfaces
     * @param workInterfaces  the set of current work interfaces
     */
    private static void addInterfaces( final Class[] interfaces,
                                             final Set workInterfaces )
    {
        for ( int i = 0; i < interfaces.length; i++ )
        {
            workInterfaces.add( interfaces[i] );
            addInterfaces(interfaces[i].getInterfaces(), workInterfaces);
        }
    }
}
