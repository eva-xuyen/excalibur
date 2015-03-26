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

package org.apache.avalon.fortress.impl.role;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;

/**
 * ECMMetaInfoManager
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS Revision: 1.1 $
 */
public final class ECMMetaInfoManager extends AbstractMetaInfoManager
{

    /**
     * Create a ECMMetaInfoManager.
     */
    public ECMMetaInfoManager()
    {
        super( (MetaInfoManager) null );
    }

    /**
     * Create a ServiceMetaManager with a parent.
     *
     * @param parent
     */
    public ECMMetaInfoManager( final MetaInfoManager parent )
    {
        super( parent );
    }

    /**
     * Create a ECMMetaInfoManager with the supplied classloader and
     * parent Rr.
     *
     * @param parent
     * @param loader
     */
    public ECMMetaInfoManager( final MetaInfoManager parent, final ClassLoader loader )
    {
        super( parent, loader );
    }

    /**
     * Add a component defined inside a selector
     */
    public final void addSelectorComponent( final String role,
                                            final String hint,
                                            final String className,
                                            final String handlerClassName )
    {
        Properties props = new Properties();
        final String lifestyle;
        if (MetaInfoEntry.THREADSAFE_HANDLER.equals(handlerClassName) )
        {
            lifestyle = "singleton";
        }
        else if ( MetaInfoEntry.PER_THREAD_HANDLER.equals(handlerClassName) )
        {
            lifestyle = "thread";
        }   
        else if ( MetaInfoEntry.POOLABLE_HANDLER.equals(handlerClassName) )
        {
            lifestyle = "pooled";
        }   
        else
        {
            lifestyle = "transient";
        }
        
        props.setProperty("x-avalon.lifestyle", lifestyle); 
        props.setProperty("x-avalon.name", role + '/' + hint);
        super.addComponent(role, className, props, new ArrayList());
        
    }
}
