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

package org.apache.avalon.fortress.impl;

import org.apache.avalon.fortress.impl.handler.ComponentHandler;

/**
 * This is the impl of runtime information about a
 * ComponentHandler.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.11 $ $Date: 2004/02/28 15:16:24 $
 */
public final class ComponentHandlerEntry
{
    private final ComponentHandler m_handler;
    private final ComponentHandlerMetaData m_metaData;

    /**
     * Create an entry for a particular handler.
     *
     * @param handler the handler
     * @param metaData the metadata for handler
     */
    public ComponentHandlerEntry( final ComponentHandler handler,
                                  final ComponentHandlerMetaData metaData )
    {
        if ( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        if ( null == metaData )
        {
            throw new NullPointerException( "metaData" );
        }

        m_handler = handler;
        m_metaData = metaData;
    }

    /**
     * Return the handler that entry manages.
     *
     * @return the handler that entry manages.
     */
    public ComponentHandler getHandler()
    {
        return m_handler;
    }

    /**
     * Return the meta data for handler.
     *
     * @return the meta data for handler.
     */
    public ComponentHandlerMetaData getMetaData()
    {
        return m_metaData;
    }
}
