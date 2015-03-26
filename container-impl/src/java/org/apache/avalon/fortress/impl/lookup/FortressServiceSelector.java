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
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.ReleasableComponent;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the Default ServiceSelector for the Container.  It provides
 * a very simple abstraction, and makes it easy for the Container to manage
 * the references.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.19 $ $Date: 2004/03/13 17:57:59 $
 */
public class FortressServiceSelector
    implements ServiceSelector
{
    private final String m_key;
    private final Container m_container;
    private final Map m_used;

    /**
     * Creation of  new service selector.
     * @param container the impl
     * @param key a key
     */
    public FortressServiceSelector( final Container container,
                                    final String key )
    {
        if ( null == container )
        {
            throw new NullPointerException( "impl" );
        }
        if ( null == key )
        {
            throw new NullPointerException( "key" );
        }

        m_key = key;
        m_container = container;
        m_used = Collections.synchronizedMap(new HashMap());
    }

    public Object select( final Object hint )
        throws ServiceException
    {
        try
        {
            final ComponentHandler handler = getHandler( hint );
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
            final String name = m_key + "/" + hint.toString();
            final String message = "Could not return a reference to the Component";
            throw new ServiceException( name, message, e );
        }
    }

    public boolean isSelectable( final Object hint )
    {
        return m_container.has( m_key, hint );
    }

    public void release( final Object component )
    {
        // Is this a releasable component ?
        if ( component instanceof ReleasableComponent )
        {
            ((ReleasableComponent)component).releaseOnComponentHandler();
            return;
        }
        final ComponentHandler handler =
            (ComponentHandler) m_used.remove( new ComponentKey( component ) );
        if ( null != handler )
        {
            handler.put( component );
        }
    }

    private ComponentHandler getHandler( final Object hint )
        throws ServiceException
    {
        if ( null == hint )
        {
            final String message = "hint cannot be null";
            throw new IllegalArgumentException( message );
        }

        final ComponentHandler handler =
            (ComponentHandler) m_container.get( m_key, hint );
        if ( null == handler )
        {
            final String message =
                "The hint does not exist in the ComponentSelector";
            throw new ServiceException( m_key + "/" + hint.toString(),
                message );
        }
        return handler;
    }
    
    public String getKey()
    {
        return m_key;
    }
}
