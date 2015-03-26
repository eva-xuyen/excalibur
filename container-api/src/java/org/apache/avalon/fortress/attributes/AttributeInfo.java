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

import java.util.Collections;
import java.util.Map;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class AttributeInfo
{
    public static final AttributeInfo EMPTY = new AttributeInfo();
    
    private final String m_name;
    private final Map m_properties;
    private final AttributeLevel m_level;

    private AttributeInfo()
    {
        m_name = "";
        m_properties = Collections.EMPTY_MAP;
        m_level = AttributeLevel.Undefined;
    }

    public AttributeInfo( final String name, 
                          final Map properties, 
                          final AttributeLevel level )
    {
        m_name = name;
        m_properties = properties;
        m_level = level;
    }
    
    public String getName()
    {
        return m_name;
    }

    public Map getProperties()
    {
        return m_properties;
    }
    
    /**
     * Pending
     * 
     * @return
     */
    public AttributeLevel getAttributeLevel()
    {
        return m_level;
    }
}
