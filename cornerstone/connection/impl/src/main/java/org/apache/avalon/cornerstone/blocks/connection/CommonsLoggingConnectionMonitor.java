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

package org.apache.avalon.cornerstone.blocks.connection;

import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * A Commons Logging implementation of the ConnectionMonitor. Not very IoC, but then, nor is CommonsLogging.
 *
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public class CommonsLoggingConnectionMonitor implements ConnectionMonitor
 {

    public void acceptingConnectionException(Class clazz, String message, IOException ioe)
    {
        LogFactory.getLog(clazz).error(message, ioe);
    }

    public void unexpectedException(Class clazz, String message, Exception e)
    {
        LogFactory.getLog(clazz).error(message, e);
    }

    public void shutdownSocketWarning(Class clazz, String message, IOException ioe)
    {
        LogFactory.getLog(clazz).error(message, ioe);
    }

    public void debugMessage(Class clazz, String message)
    {
        LogFactory.getLog(clazz).error(message);
    }

    public boolean isDebugEnabled(Class clazz)
    {
        return LogFactory.getLog(clazz).isDebugEnabled();
    }
}
