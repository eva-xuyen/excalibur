/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress;

import java.lang.reflect.Method;

import org.apache.avalon.fortress.attributes.AttributeInfo;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public interface ExtendedMetaInfo
{
    AttributeInfo[] getClassAttributes();
    
    AttributeInfo[] getAttributesForMethod( final Method method );

    AttributeInfo getAttributeForMethod( final String name, final Method method );
}
