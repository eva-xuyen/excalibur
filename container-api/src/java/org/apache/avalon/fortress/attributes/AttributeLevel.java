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

package org.apache.avalon.fortress.attributes;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public final class AttributeLevel
{
    public static final AttributeLevel Undefined = new AttributeLevel(0);
    
    public static final AttributeLevel MethodLevel = new AttributeLevel(1);
    
    public static final AttributeLevel ClassLevel = new AttributeLevel(2);
    
    private final int m_level;
    
    private AttributeLevel(int level)
    {
        m_level = level;
    }
    
    public int getLevel()
    {
        return m_level;
    }
    
    /**
     * Pending
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if ( obj instanceof AttributeLevel == false )
        {
            return false;
        }
        
        return obj.hashCode() == obj.hashCode();
    }

    /**
     * Pending
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return m_level;
    }
}
