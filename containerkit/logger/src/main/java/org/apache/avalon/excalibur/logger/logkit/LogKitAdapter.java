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
package org.apache.avalon.excalibur.logger.logkit;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.log.Hierarchy;

/**
 * This class sits on top of an existing LogKit Hierarchy
 * and returns logger wrapping LogKit loggers.
 *
 * Attach PrefixDecorator and/or CachingDecorator if desired.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class LogKitAdapter extends AbstractLogEnabled implements LoggerManager
{
    /**
     * The hierarchy that really produces loggers.
     */
    protected final Hierarchy m_hierarchy;

    /**
     * Initialized <code>LogKitAdapter</code> to operate
     * of a certain LogKit <code>Hierarchy</code>.
     */
    public LogKitAdapter( final Hierarchy hierarchy )
    {
        if ( hierarchy == null ) throw new NullPointerException( "hierarchy" );
        m_hierarchy = hierarchy;
    }

    /**
     * Return the Logger for the specified category.
     * <p>
     *
     * In LogKit getRootLogger() and getLoggerFor("")
     * unless the logger for category "" has been explicitly
     * configured return identically configured but different
     * loggers.
     *
     * <p>
     * Our LogKitConfHelper configures getRootLogger(), not getLoggerFor("").
     * We think this is a reasonable behavior and expect that LogKit
     * Hierarchies configured by other means then LogKitConfHelper are
     * configured in the same way.
     * 
     * <p>
     * This justifies our decision to return getRootLogger() when given
     * "" category name.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        if ( categoryName == null || categoryName.length() == 0 )
        {
            return getDefaultLogger();
        }
        else
        {
            return new LogKitLogger( m_hierarchy.getLoggerFor( categoryName ) );
        }
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger()
    {
        return new LogKitLogger( m_hierarchy.getRootLogger() );
    }
}
