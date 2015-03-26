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
 * This interface is for a PoolManager that creates pools that are managed
 * asynchronously.  The contract is that the controller type is specified in
 * the constructor.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:33 $
 * @since 4.1
 */
public interface PoolManager
{
    String ROLE = PoolManager.class.getName();

    /**
     * Return a managed pool that has a controller.
     */
    Pool getManagedPool( ObjectFactory factory, int initialEntries )
        throws Exception;
}
