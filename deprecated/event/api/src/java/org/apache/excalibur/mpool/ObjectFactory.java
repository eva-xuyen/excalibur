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
 * This interface is to define how an ObjectFactory is defined.  While this
 * class is not strictly necessary, the implementation of the Pool can differ
 * object creation to and instance of this interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:33 $
 * @since 4.1.2
 */
public interface ObjectFactory
{
    String ROLE = ObjectFactory.class.getName();

    /**
     * Create a new instance of the object being pooled.
     *
     * @return the pooled Object instance
     * @throws Exception if the object cannot be instantiated
     */
    Object newInstance() throws Exception;

    /**
     * Get the class of the object you are creating.
     *
     * @return Class object of the factory's class
     */
    Class getCreatedClass();

    /**
     * Performs any deconstruction that is necessary for the
     * object.
     *
     * @param object to destroy
     * @throws IllegalArgumentException if the object is not of
     *         the same class that the factory creates.
     * @throws Exception if there is any other reason that the
     *         factory has problems disposing of the object.
     */
    void dispose( Object object ) throws Exception;
}
