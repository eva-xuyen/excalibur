/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * MetaInfoManager Interface, use this to specify the Components and how they
 * correspond to easy shorthand names. The MetaInfoManager assumes a one to one
 * relationship of shorthand names to classes.  A component can have any number
 * of roles associated with it, so it is more flexible and robust than the
 * {@link RoleManager) alternative.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/02/28 15:16:24 $
 */
public interface MetaInfoManager
{
    /**
     * Convenience constant to make lookup of the MetaInfoManager easer.
     */
    String ROLE = MetaInfoManager.class.getName();

    /**
     * Get a <code>MetaInfoEntry</code> for a short name.  The short name is an
     * alias for a component type.
     *
     * @param shortname  The shorthand name for the component type.
     *
     * @return the proper {@link MetaInfoEntry}
     */
    MetaInfoEntry getMetaInfoForShortName( String shortname );

    /**
     * Get a <code>MetaInfoEntry</code> for a component type.  This facilitates
     * self-healing configuration files where the impl reads the
     * configuration and translates all <code>&lt;component/&gt;</code>
     * entries to use the short hand name for readability.
     *
     * @param classname  The component type name
     *
     * @return the proper {@link MetaInfoEntry}
     */
    MetaInfoEntry getMetaInfoForClassname( String classname );
}
