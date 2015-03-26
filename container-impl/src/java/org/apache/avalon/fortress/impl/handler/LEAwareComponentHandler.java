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

import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;

/**
 * A ComponentHandler that delegates to underlying handler but also
 * calls relevent Lifecycle Extension handlers at the right time.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.18 $ $Date: 2004/02/28 15:16:25 $
 */
public final class LEAwareComponentHandler
    implements ComponentHandler, Disposable
{
    private final ComponentHandler m_componentHandler;
    private final LifecycleExtensionManager m_extManager;
    private final Context m_context;

    /**
     * Creation of a new handler.
     * @param componentHandler the handler
     * @param extManager the extension manager
     * @param context the context
     */
    public LEAwareComponentHandler( final ComponentHandler componentHandler,
                                    final LifecycleExtensionManager extManager,
                                    final Context context )
    {
        if ( null == componentHandler )
        {
            throw new NullPointerException( "componentHandler" );
        }
        if ( null == extManager )
        {
            throw new NullPointerException( "extManager" );
        }
        if ( null == context )
        {
            throw new NullPointerException( "context" );
        }

        m_componentHandler = componentHandler;
        m_extManager = extManager;
        m_context = context;
    }

    /**
     * Return the component's class that this handler is trying to create.
     * Used for deubug information.
     *
     * @return the <code>Class</code> object for the component
     */
    public Class getComponentClass()
    {
        return m_componentHandler.getComponentClass();
    }

    /**
     * Prepare the handler.
     * @exception Exception if a handler preparation error occurs
     */
    public void prepareHandler()
        throws Exception
    {
        m_componentHandler.prepareHandler();
    }

    /**
     * Retrieve the object and execute access extensions.
     *
     * @return the object
     * @throws Exception if unable to aquire object
     */
    public Object get() throws Exception
    {
        final Object object = m_componentHandler.get();
        m_extManager.executeAccessExtensions( object, m_context );
        return object;
    }

    /**
     * Return component and execute Release extensions.
     *
     * @param component the component
     */
    public void put( final Object component )
    {
        try
        {
            m_extManager.executeReleaseExtensions( component, m_context );
        }
        catch ( Exception e )
        {
            // REVISIT(MC): we need to log this somewhere
        }
        m_componentHandler.put( component );
    }

    /**
     * Disposal of the handler.
     */
    public void dispose()
    {
        ContainerUtil.dispose( m_componentHandler );
    }
}
