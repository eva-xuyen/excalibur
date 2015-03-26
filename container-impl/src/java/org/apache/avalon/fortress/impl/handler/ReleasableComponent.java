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

/**
 * This interfaces marks a component that can be released without
 * using the corresponding ComponentHandler.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/13 17:57:59 $
 * @since 1.2
 */
public interface ReleasableComponent
{
    /**
     * Initialize this component with the corresponding component handler.
     * @param handler The component handler used to put this component back again
     * @throws ServiceException If the component is already initialized or the
     *                          handler is missing
     */
    void initialize(ComponentHandler handler)
    throws Exception;

    /**
     * Puts the reference back in the ComponentHandler according to the
     * policy of the implementation.
     * In fact this calls the put method on the ComponentHandler
     */
    void releaseOnComponentHandler( );
}
