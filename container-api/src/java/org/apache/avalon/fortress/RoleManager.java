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

package org.apache.avalon.fortress;

/**
 * RoleManager Interface, use this to specify the Components and how they
 * correspond to easy shorthand names. The RoleManager assumes a one to one
 * relationship of shorthand names to classes, and a flat relationship of
 * classes to roles.  Any one role can have multiple classes associated with
 * it.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/02/28 15:16:24 $
 * @since 4.1
 */
public interface RoleManager
{
    /**
     * Convenience constant to make lookup of the RoleManager easer.
     */
    String ROLE = RoleManager.class.getName();

    /**
     * Get a <code>RoleEntry</code> for a short name.  The short name is an
     * alias for a component type.
     *
     * @param shortname  The shorthand name for the component type.
     *
     * @return the proper {@link org.apache.avalon.fortress.RoleEntry}
     */
    RoleEntry getRoleForShortName( String shortname );

    /**
     * Get a <code>RoleEntry</code> for a component type.  This facilitates
     * self-healing configuration files where the impl reads the
     * configuration and translates all <code>&lt;component/&gt;</code>
     * entries to use the short hand name for readability.
     *
     * @param classname  The component type name
     *
     * @return the proper {@link org.apache.avalon.fortress.RoleEntry}
     */
    RoleEntry getRoleForClassname( String classname );
}
