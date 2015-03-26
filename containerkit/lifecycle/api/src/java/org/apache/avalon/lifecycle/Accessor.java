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
package org.apache.avalon.lifecycle;

import org.apache.avalon.framework.context.Context;

/**
 * The <code>Accessor</code> interface describes the access and release
 * stages that occur between a service or component manager and a container
 * during service deployment.  Lifecycle extensions supporting access
 * and release stages must implement this interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/02/28 11:47:19 $
 */
public interface Accessor
{
    /**
     * Access stage handler.
     *
     * @param object the object that is being accessed
     * @param context the context instance required by the access handler
     *    implementation
     * @exception Exception if an error occurs
     */
    void access( Object object, Context context )
        throws Exception;

    /**
     * Release stage handler.
     *
     * @param object the object that is being released
     * @param context the context instance required by the release handler
     *    implementation
     */
    void release( Object object, Context context );

}
