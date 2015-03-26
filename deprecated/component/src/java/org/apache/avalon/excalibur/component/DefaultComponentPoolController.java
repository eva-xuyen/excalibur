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

import org.apache.avalon.excalibur.pool.PoolController;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * This is the <code>PoolController</code> for the Avalon Excalibur
 * Component Management Framework.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 *
 * @deprecated DefaultComponentPool is no longer used by the PoolableComponentHandler.
 */
public class DefaultComponentPoolController
    implements PoolController, ThreadSafe
{
    /** Default increase/decrease amount */
    public static final int DEFAULT_AMOUNT = 8;

    /** Used increase/decrease amount */
    protected final int m_amount;

    /**
     * The default constructor.  It initializes the used increase/
     * decrease amount to the default.
     */
    public DefaultComponentPoolController()
    {
        m_amount = DefaultComponentPoolController.DEFAULT_AMOUNT;
    }

    /**
     * The alternate constructor.  It initializes the used increase/
     * decrease amount to the specified number only if it is greater
     * than 0.  Otherwise it uses the default amount.
     *
     * @param amount   The amount to grow and shrink a pool by.
     */
    public DefaultComponentPoolController( final int amount )
    {
        if( amount > 0 )
        {
            m_amount = amount;
        }
        else
        {
            m_amount = DefaultComponentPoolController.DEFAULT_AMOUNT;
        }
    }

    /**
     * Called when a Pool reaches it's minimum.
     * Return the number of elements to increase pool by.
     *
     * @return the element increase
     */
    public int grow()
    {
        return m_amount;
    }

    /**
     * Called when a pool reaches it's maximum.
     * Returns the number of elements to decrease pool by.
     *
     * @return the element decrease
     */
    public int shrink()
    {
        return m_amount;
    }
}
