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
package org.apache.avalon.excalibur.logger;

import org.apache.avalon.framework.logger.Logger;

/**
 * LoggerManager Interface.  This is the interface used to get instances of
 * a Logger for your system.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/10 13:54:50 $
 */
public interface LoggerManager
{
    public static final String ROLE = LoggerManager.class.getName();

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( String categoryName );

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger();
}
