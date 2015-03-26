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

package org.apache.avalon.fortress.impl.role.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.RoleEntry;
import org.apache.avalon.fortress.RoleManager;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 */
public class AbstractRoleManagerTestCase extends TestCase
{
    private boolean m_informixClassExists = false;

    public AbstractRoleManagerTestCase( final String key )
    {
        super( key );

        try
        {
            Class.forName( "org.apache.avalon.excalibur.datasource.InformixDataSource" );
            m_informixClassExists = true;
        }
        catch ( Exception e )
        {
            m_informixClassExists = false;
        }
    }

    protected boolean isInformixClassExists()
    {
        return m_informixClassExists;
    }

    protected void checkRole( final RoleManager roles,
                              final String shortname,
                              final String role,
                              final String className,
                              final String handlerClassname )
        throws ClassNotFoundException
    {
        final RoleEntry roleEntry = roles.getRoleForShortName( shortname );
        assertNotNull( "RoleEntry for '" + shortname + "' is null", roleEntry );

        assertEquals( "componentClass:",
            roleEntry.getComponentClass(), Class.forName( className ) );
        assertEquals( "Role:", roleEntry.getRole(), role );
        assertEquals( "Handler:",
            roleEntry.getHandlerClass(), Class.forName( handlerClassname ) );
    }
}
