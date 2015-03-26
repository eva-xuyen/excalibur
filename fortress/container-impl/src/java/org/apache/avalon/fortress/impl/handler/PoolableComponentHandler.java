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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.d_haven.mpool.Pool;
import org.d_haven.mpool.PoolManager;

/**
 * The PoolableComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/02/28 15:16:25 $
 * @since 4.0
 */
public final class PoolableComponentHandler
    extends AbstractComponentHandler
    implements Configurable
{
    /** The instance of the PoolManager to create the Pool for the Handler */
    private PoolManager m_poolManager;

    /** The pool of components for <code>Poolable</code> Components */
    private Pool m_pool;

    /** The Config element for the poolable */
    private int m_poolMin;

    /**
     * Application of suporting services to the handler.
     * @param serviceManager the service manager
     * @exception ServiceException if a service related error occurs
     * @avalon.dependency type="PoolManager"
     */
    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        super.service( serviceManager );
        m_poolManager =
            (PoolManager) serviceManager.lookup( PoolManager.class.getName() );
    }

    /**
     * Configuration of the handler under which the minimum pool size
     * is established.
     * @param configuration the configuration fragment
     */
    public void configure( final Configuration configuration )
    {
        m_poolMin = configuration.getAttributeAsInteger( "pool-min", 10 );
    }

    /**
     * Initialize the ComponentHandler.
     * @exception Exception if an error occurs
     */
    protected void doPrepare()
        throws Exception
    {
        m_pool = m_poolManager.getManagedPool( m_factory, m_poolMin );
    }

    /**
     * Get a reference of the desired Component
     * @exception Exception if an error occurs
     */
    protected Object doGet()
        throws Exception
    {
        return m_pool.acquire();
    }

    /**
     * Return a reference of the desired Component
     * @param component the component to return to the handler
     */
    protected void doPut( final Object component )
    {
        m_pool.release( component );
    }
}
