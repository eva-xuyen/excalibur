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
 * The FactoryComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/28 15:16:25 $
 * @since 4.0
 */
public final class FactoryComponentHandler
    extends AbstractComponentHandler
{
    /**
     * Get a reference of the desired Component
     * @exception Exception if arros occurs
     */
    protected Object doGet()
        throws Exception
    {
        return newComponent();
    }

    /**
     * Return a reference of the desired Component
     * @param component the compoent to return to the handler
     */
    protected void doPut( final Object component )
    {
        disposeComponent( component );
    }
}
