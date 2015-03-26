/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.excalibur.mpool;

/**
 * This is the interface for Pools that are not a fixed size.  This interface
 * exposes enough explicit state so that an external asynchronous Controller
 * can do it's job.  A secondary purpose of this interface is to supply a
 * simple authentication mechanism so that the Pool only responds to method
 * invocations by the legitimate controller.
 *
 * <p>
 *   The key is a randomly generated number greater than one assigned by the
 *   PoolManager and given to the Pool and the PoolController.  The mechanism
 *   to generate the number is up to the PoolManager's policy.  Keep in mind
 *   that should the key be made publicly available, the Pool is susceptible
 *   to a replay attack.  Therefore, it is suggested that the key be created
 *   at the same time the Pool is created.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:33 $
 * @since 4.1
 */
public interface ManagablePool extends Pool
{
    /**
     * Grow by the specified amount.  The pool should trust the Controller
     * for the Grow size.
     *
     * @param  amount  an integer amount to increase the pool size by.
     * @param  key     an integer number supplied by the PoolManager to
     *                 validate that the method is called legitimately
     *
     * @throws IllegalAccessException if the key does not match the
     *                                controller's key.
     */
    void grow( int amount, long key )
        throws IllegalAccessException;

    /**
     * Shrink the pool by the specified amount.  The pool should trust the
     * Controller, but be smart enough not to achieve a negative pool size.
     * In other words, you should clip the shrink amount so that the pool
     * does not go below 0.
     *
     * @param  amount  an integer amount to decrease the pool size by.
     * @param  key     an integer number supplied by the PoolManager to
     *                 validate that the method is called legitimately
     *
     * @throws IllegalAccessException if the key does not match the
     *                                controller's key.
     */
    void shrink( int amount, long key )
        throws IllegalAccessException;

    /**
     * Determine the pool's current size.  The size is defined as the number
     * of Poolable objects in reserve.
     *
     * @param  key     an integer number supplied by the PoolManager to
     *                 validate that the method is called legitimately
     *
     * @return  size of pool's reserve.
     *
     * @throws IllegalAccessException if the key does not match the
     *                                controller's key.
     */
    int size( long key )
        throws IllegalAccessException;
}
