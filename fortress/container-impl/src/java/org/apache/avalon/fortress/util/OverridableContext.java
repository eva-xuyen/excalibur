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

package org.apache.avalon.fortress.util;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;

/**
 * The OverridableContext allows you to "null" out entries, even if they are
 * in a parent context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/02/28 15:16:26 $
 */
public final class OverridableContext
    extends DefaultContext
{
    /**
     * Creation of a new overridable context.
     * @param parent the parent context
     */
    public OverridableContext( final Context parent )
    {
        super( parent );
    }

    /**
     * Add a context entry to the context.
     * @param key the context key
     * @param value the context value
     */
    public void put( final Object key, final Object value )
    {
        if ( null == value )
        {
            hide( key );
        }
        else
        {
            super.put( key, value );
        }
    }
}
