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
package org.apache.avalon.excalibur.logger.decorator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.logger.util.LoggerUtil;

/**
 * This class implements LoggerManager interface by
 * prepending a prefix to all requests and letting the
 * wrapped LoggerManager actually create the loggers.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class PrefixDecorator extends LoggerManagerDecorator implements LoggerManager
{
    private final String m_prefix;

    /**
     * Creates a PrefixDecorator instance.
     * @param prefix the prefix to prepend; 
     *         can be neither null nor empty. 
     *         This is done to avoid ambiguity 
     *         in the getDefaultLogger() method - what would we call
     *         in such case getDefaultLogger() or getLoggerForCategory("") ?
     *
     */
    public PrefixDecorator( final LoggerManager loggerManager, final String prefix )
    {
        super( loggerManager );
        if ( prefix == null ) throw new NullPointerException( "prefix" );
        if ( "".equals( prefix ) ) throw new IllegalArgumentException( "prefix can't be empty" );
        m_prefix = prefix;
    }

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        final String fullCategoryName = LoggerUtil.getFullCategoryName( m_prefix, categoryName );
        return m_loggerManager.getLoggerForCategory( fullCategoryName );
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category. 
     */
    public Logger getDefaultLogger()
    {
        final String fullCategoryName = LoggerUtil.getFullCategoryName( m_prefix, null );
        return m_loggerManager.getLoggerForCategory( fullCategoryName );
    }
}
