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
 * The ComponentHandler interface marks the ComponentHandler implementations.
 * The desire for a ComponentHandler is to manage the instances of a
 * Component.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/28 15:16:25 $
 * @since 4.0
 */
public interface ComponentHandler
{
    /**
     * Return the component's class that this handler is trying to create.
     * Used for deubug information.
     *
     * @return the <code>Class</code> object for the component
     */
    Class getComponentClass();

    /**
     * Actually prepare the handler and make it ready to
     * handle component access.
     *
     * @exception Exception if unable to prepare handler
     */
    void prepareHandler() throws Exception;

    /**
     * Gets the current reference to a Component according to the policy of
     * the implementation.
     * @exception Exception if unable to ge tthe compoennt reference
     */
    Object get() throws Exception;

    /**
     * Puts the reference back in the ComponentHandler according to the
     * policy of the implementation.
     * @param component the component to return to the handler
     */
    void put( Object component );
}
