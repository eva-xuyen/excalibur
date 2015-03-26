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

/**
 * Overrides the value passed from getDefaultLogger().
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class OverrideDefaultDecorator extends LoggerManagerDecorator
{
    /**
     * The override value for getDefaultLogger() and for 
     * getLoggerForCategory(""), getLoggerForCategory( null );
     */
    private final Logger m_defaultLogger;

    /**
     * Creates an <code>OverrideDecorator</code> instance.
     * @param defaultLogger <code>OverrideDecorator</code> is unique in that
     *        it won't tolerate a null extra argument: if this
     *        argument is <code>null</code> a NullPointerException will
     *        be thrown. This ensures that no logging surprises will occur.
     */
    public OverrideDefaultDecorator( 
            final LoggerManager loggerManager, final Logger defaultLogger )
    {
        super( loggerManager );
        if ( defaultLogger == null ) throw new NullPointerException( "defaultLogger" );
        m_defaultLogger = defaultLogger;
    }

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        if ( categoryName == null || categoryName.length() == 0 )
        {
            return m_defaultLogger;
        }
        else
        {
            return m_loggerManager.getLoggerForCategory( categoryName );
        }
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger()
    {
        return m_defaultLogger;
    }
}
