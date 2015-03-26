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
package org.apache.avalon.excalibur.component;

/**
 * RoleManager Interface, use this to specify the Roles and how they
 * correspond easy shorthand names.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.0
 */
public interface RoleManager
{
    /**
     * Find Role name based on shorthand name.  Please note that if
     * this returns <code>null</code> or an empty string, then the
     * shorthand name is assumed to be a "reserved word".  In other
     * words, you should not try to instantiate a class from an empty
     * role.
     */
    String getRoleForName( String shorthandName );

    /**
     * Get the default classname for a given role.
     */
    String getDefaultClassNameForRole( String role );

    /**
     * Get the default classname for a given hint type.  This is only
     * used by ComponentSelectors.
     */
    String getDefaultClassNameForHint( String hint, String shorthand );
}
