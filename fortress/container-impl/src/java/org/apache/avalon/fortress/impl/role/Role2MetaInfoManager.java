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

package org.apache.avalon.fortress.impl.role;

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.fortress.RoleEntry;
import org.apache.avalon.fortress.RoleManager;

/**
 * Role2MetaInfoManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS Revision: 1.1 $
 */
public final class Role2MetaInfoManager implements MetaInfoManager
{
    private final RoleManager m_manager;
    private final MetaInfoManager m_parent;

    public Role2MetaInfoManager( final RoleManager manager )
    {
        this( manager, null );
    }

    public Role2MetaInfoManager( final RoleManager manager, final MetaInfoManager parent )
    {
        m_manager = manager;
        m_parent = parent;
    }

    /**
     * Get a <code>MetaInfoEntry</code> for a short name.  The short name is an
     * alias for a component type.
     *
     * @param shortname  The shorthand name for the component type.
     *
     * @return the proper {@link RoleEntry}
     */
    public MetaInfoEntry getMetaInfoForShortName( final String shortname )
    {
        final RoleEntry roleEntry = m_manager.getRoleForShortName( shortname );

        if ( roleEntry != null )
        {
            return new MetaInfoEntry( roleEntry );
        }
        else
        {
            return null != m_parent ? m_parent.getMetaInfoForShortName(shortname ) : null;
        }
    }

    /**
     * Get a <code>MetaInfoEntry</code> for a component type.  This facilitates
     * self-healing configuration files where the impl reads the
     * configuration and translates all <code>&lt;component/&gt;</code>
     * entries to use the short hand name for readability.
     *
     * @param classname  The component type name
     *
     * @return the proper {@link RoleEntry}
     */
    public MetaInfoEntry getMetaInfoForClassname( final String classname )
    {
        final RoleEntry roleEntry = m_manager.getRoleForClassname( classname );

        if ( roleEntry != null )
        {
            return new MetaInfoEntry( roleEntry );
        }
        else
        {
            return null != m_parent ? m_parent.getMetaInfoForClassname( classname ) : null;
        }
    }
}
