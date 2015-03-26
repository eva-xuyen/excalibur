/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.monitor.test;


/**
 * Simple Mock object to test active notification.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class Mock
{
    private long m_lastModified = System.currentTimeMillis();
    private String m_content = "";
    private final String m_name;
    
    public Mock(String name)
    {
        m_name = name;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getContent()
    {
        return m_content;
    }
    
    public void setContent(String content)
    {
        m_content = (null == content) ? "" : content;
        touch();
    }
    
    public long lastModified()
    {
        return m_lastModified;
    }
    
    public void touch()
    {
        m_lastModified = System.currentTimeMillis();
    }
}
