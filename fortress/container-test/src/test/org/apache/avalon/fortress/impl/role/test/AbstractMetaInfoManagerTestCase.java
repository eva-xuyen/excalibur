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
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;

/**
 * AbstractMetaInfoManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public abstract class AbstractMetaInfoManagerTestCase extends TestCase
{
    private boolean m_informixClassExists = false;
    protected MetaInfoManager m_manager;

    public AbstractMetaInfoManagerTestCase( String name )
    {
        super( name );

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

    protected void checkRole( final String shortname,
                              final String[] roles,
                              final String className,
                              final String handlerClassname )
        throws ClassNotFoundException
    {
        final MetaInfoEntry metaEntry = m_manager.getMetaInfoForShortName( shortname );
        assertNotNull( "MetaInfoEntry", metaEntry );

        assertEquals( "componentClass:",
            metaEntry.getComponentClass(), Class.forName( className ) );

        for ( int i = 0; i < roles.length; i++ )
        {
            assertTrue( "Role:", metaEntry.containsRole( roles[i] ) );
        }
        assertEquals( "Handler:",
            metaEntry.getHandlerClass(), Class.forName( handlerClassname ) );
    }
}
