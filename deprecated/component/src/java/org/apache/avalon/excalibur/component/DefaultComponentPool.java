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

import org.apache.avalon.excalibur.pool.AbstractPool;
import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.excalibur.pool.PoolController;
import org.apache.avalon.excalibur.pool.SoftResourceLimitingPool;
import org.apache.avalon.framework.activity.Initializable;

/**
 * This is the implementation of <code>Pool</code> for Avalon
 * Components that is thread safe.  For Component Management, we need
 * soft resource limiting due to the possibility of spikes in demand.
 * This pool will destroy all unnecessary Components when they are
 * no longer needed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 *
 * @deprecated DefaultComponentPool is no longer used by the PoolableComponentHandler.
 */
public class DefaultComponentPool
    extends SoftResourceLimitingPool
    implements Initializable
{
    /**
     * Initialize the <code>Pool</code> with an
     * <code>ObjectFactory</code>.
     */
    public DefaultComponentPool( ObjectFactory factory ) throws Exception
    {
        this( factory,
              new DefaultComponentPoolController(
                  AbstractPool.DEFAULT_POOL_SIZE / 4 ),
              AbstractPool.DEFAULT_POOL_SIZE / 4,
              AbstractPool.DEFAULT_POOL_SIZE );
    }

    /**
     * Initialized the <code>Pool</code> with an alternative management
     * infrastructure.
     */
    public DefaultComponentPool( ObjectFactory factory,
                                 PoolController controller,
                                 int minimumPoolSize,
                                 int maximumPoolSIze )
        throws Exception
    {
        super( factory, controller, minimumPoolSize, maximumPoolSIze );
    }
}
