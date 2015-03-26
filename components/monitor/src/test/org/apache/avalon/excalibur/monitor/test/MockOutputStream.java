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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class MockOutputStream extends ByteArrayOutputStream
{
    private final Mock m_mock;
    
    public MockOutputStream(Mock mock)
    {
        super();
        m_mock = mock;
    }
    
    public void close() throws IOException
    {
        m_mock.setContent(new String(super.toByteArray()));
        super.close();
    }
}
