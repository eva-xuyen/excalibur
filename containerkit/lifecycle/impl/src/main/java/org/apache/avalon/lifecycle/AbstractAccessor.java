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
package org.apache.avalon.lifecycle;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Abstract implementation of a <code>Accessor</code>.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class AbstractAccessor extends AbstractLogEnabled implements Accessor
{

    //=======================================================================
    // Accessor
    //=======================================================================

    /**
     * Access stage handler.
     *
     * @param object the object that is being accessed
     * @param context the context instance required by the access handler
     *    implementation
     * @exception Exception if an error occurs
     */
    public void access( Object object, Context context )
        throws Exception
    {
        if( getLogger() == null )
        {
            return;
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "accessing " + object.getClass().getName()
                + "#" + System.identityHashCode( object ) );
        }
    }

    /**
     * Release stage handler.
     *
     * @param object the object that is being released
     * @param context the context instance required by the release handler
     *    implementation
     */
    public void release( Object object, Context context )
    {
        if( getLogger() == null )
        {
            return;
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "releasing " + object.getClass().getName()
                + "#" + System.identityHashCode( object ) );
        }
    }
}
