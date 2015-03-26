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

package org.apache.avalon.fortress.util.test;

import junit.framework.Assert;

/**
 * TestComponent does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class TestComponent extends Assert
{
    private boolean m_isCreated;
    private boolean m_isDestroyed;
    private boolean m_isAccessed;
    private boolean m_isReleased;

    public void create()
    {
        assertTrue( !m_isCreated);
        assertTrue( !m_isAccessed );
        assertTrue( !m_isReleased );
        assertTrue( !m_isDestroyed );
        m_isCreated = true;
        assertTrue(m_isCreated);
    }

    public void access()
    {
        assertTrue( m_isCreated );
        assertTrue( !m_isAccessed );
        assertTrue( !m_isReleased );
        assertTrue( !m_isDestroyed );
        m_isAccessed = true;
        assertTrue( m_isAccessed );
    }

    public void release()
    {
        assertTrue( m_isCreated );
        assertTrue( m_isAccessed );
        assertTrue( !m_isReleased );
        assertTrue( !m_isDestroyed );
        m_isReleased = true;
        assertTrue( m_isReleased );
    }

    public void destroy()
    {
        assertTrue( m_isCreated );
        assertTrue( m_isAccessed );
        assertTrue( m_isReleased );
        assertTrue( !m_isDestroyed );
        m_isDestroyed = true;
        assertTrue(m_isDestroyed);
    }
}
