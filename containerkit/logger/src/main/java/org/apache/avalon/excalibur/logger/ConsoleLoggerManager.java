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

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * This is a very simple logger manager for debugging purpose
 * that uses always the ConsoleLogger
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 13:54:49 $
 */
public class ConsoleLoggerManager implements LoggerManager
{

    protected Logger m_logger;
    
    /**
     * Constructor
     */
    public ConsoleLoggerManager() 
    {
        m_logger = new ConsoleLogger();
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.logger.LoggerManager#getDefaultLogger()
     */
    public Logger getDefaultLogger() 
    {
        return m_logger;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.logger.LoggerManager#getLoggerForCategory(java.lang.String)
     */
    public Logger getLoggerForCategory(String categoryName) 
    {
        return m_logger;
    }
}
