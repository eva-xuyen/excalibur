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

package org.apache.avalon.fortress.test.data;

/**
 * @avalon.component
 * @avalon.service type=org.apache.avalon.fortress.test.data.Role2
 * @x-avalon.lifestyle type=pooled
 * @x-avalon.info name=component2
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 */
public class Component2
    implements Role2
{
    public long getID()
    {
        return hashCode();
    }
}
