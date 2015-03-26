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
package org.apache.avalon.excalibur.pool;

/**
 * This is the interface you implement if you want to control how Pools capacity
 * changes overtime.
 *
 * It gets called everytime that a Pool tries to go below or above it's minimum or maximum.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public interface PoolController
{
    /**
     * Called when a Pool reaches it's minimum.
     *
     * Return the number of elements to increase minimum and maximum by.
     *
     * @return the element increase
     */
    int grow();

    /**
     * Called when a pool reaches it's maximum.
     *
     * Returns the number of elements to decrease mi and max by.
     *
     * @return the element decrease
     */
    int shrink();
}
