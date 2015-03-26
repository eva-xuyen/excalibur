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

package org.apache.avalon.fortress.tools;

/**
 * TestInterface implementation for testing FortressBean with FortressBeanTestCase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/02/28 15:16:27 $
 */
public class TestImpl implements TestInterface
{
    private boolean m_isRunning = false;

    /**
     * @see org.apache.avalon.fortress.tools.TestInterface#isRunning()
     */
    public boolean isRunning()
    {
        return m_isRunning;
    }

    /**
     * @see org.apache.avalon.fortress.tools.TestInterface#run()
     */
    public void run()
    {
		m_isRunning = true;
    }
}
