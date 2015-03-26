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

package org.apache.avalon.fortress.impl.lookup;

import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.impl.AbstractContainer;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.ReleasableComponent;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.WrapperServiceSelector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the Default ServiceManager for the Container.  It provides
 * a very simple abstraction, and makes it easy for the Container to manage
 * the references.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.22 $ $Date: 2004/03/13 17:57:59 $
 */
public class FortressServiceManager
    implements ServiceManager
{
    private final Container m_container;
    private final Map m_used;
    private final ServiceManager m_parent;

    /**
     * This constructor is for a ContainerComponentManager with a parent
     * ComponentLocator
     * @param container the impl
     * @param parent the parent service manager
     * @throws NullPointerException if the supplied impl is null
     */
    public FortressServiceManager( final Container container,
                                   final ServiceManager parent ) throws NullPointerException
    {
        if ( null == container )
        {
            throw new NullPointerException( "impl" );
        }

        m_parent = parent;
        m_container = container;
        m_used = Collections.synchronizedMap(new HashMap());
    }

    public Object lookup( final String role )
        throws ServiceException
    {
        final Lookup lookup = parseRole( role );

        if ( !m_container.has( lookup.m_role, lookup.m_hint ) )
        {
            return m_parent.lookup( role );
        }

        final Object result = m_container.get( lookup.m_role, lookup.m_hint );
        if ( result instanceof ServiceSelector )
        {
            return result;
        }

        if ( result instanceof ComponentSelector )
        {
            return new WrapperServiceSelector( lookup.m_role, (ComponentSelector) result );
        }

        if ( !( result instanceof ComponentHandler ) )
        {
            final String message = "Invalid entry in component manager";
            throw new ServiceException( role, message );
        }

        try
        {
            final ComponentHandler handler = (ComponentHandler) result;
            final Object component = handler.get();

            // we only have to keep track of components that don't implement
            // the ReleasableComponent interface
            if ( !(component instanceof ReleasableComponent) )
            {
                m_used.put( new ComponentKey( component ), handler );
            }
            return component;
        }
        catch ( final ServiceException ce )
        {
            throw ce; // rethrow
        }
        catch ( final Exception e )
        {
            final String message =
                "Could not return a reference to the Component";
            throw new ServiceException( role, message, e );
        }
    }

    public boolean hasService( final String role )
    {
        final Lookup lookup = parseRole( role );

        if ( m_container.has( lookup.m_role, lookup.m_hint ) )
        {
            return true;
        }
        else
        {
            return null != m_parent ? m_parent.hasService( role ) : false;
        }
    }

    public void release( final Object component )
    {
        // Is this a releasable component ?
        if ( component instanceof ReleasableComponent )
        {
            ((ReleasableComponent)component).releaseOnComponentHandler();
            return;
        }
        final ComponentHandler handler = (ComponentHandler) m_used.remove( new ComponentKey( component ) );
        if ( null == handler )
        {
            if ( null == m_parent )
            {
                /* This is a purplexing problem.  SOmetimes the m_used hash
                 * returns null for the component--usually a ThreadSafe
                 * component.  When there is no handler and no parent, that
                 * is an error condition--but if the component is usually
                 * ThreadSafe, the impact is essentially nill.
                 */
                //Pete: This occurs when objects are released more often than
                //when they are aquired
                //Pete: It also happens when a release of a ComponentSelector occurs
            }
            else
            {
                m_parent.release( component );
            }
        }
        else
        {
            handler.put( component );
        }
    }

    private Lookup parseRole( final String role )
    {
        final Lookup lookup = new Lookup();
        lookup.m_role = role;
        lookup.m_hint = AbstractContainer.DEFAULT_ENTRY;

        if ( role.endsWith( "Selector" ) )
        {
            lookup.m_role = role.substring( 0, role.length() - "Selector".length() );
            lookup.m_hint = AbstractContainer.SELECTOR_ENTRY;
        }

        final int index = role.lastIndexOf( "/" );

        // needs to be further than the first character
        if ( index > 0 )
        {
            lookup.m_role = role.substring( 0, index );
            lookup.m_hint = role.substring( index + 1 );
        }

        return lookup;
    }

    private final static class Lookup
    {
        String m_role;
        String m_hint;
    }
}
