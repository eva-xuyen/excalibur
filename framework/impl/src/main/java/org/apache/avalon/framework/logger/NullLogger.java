/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.logger;

/**
 * The Null Logger class.  This is useful for implementations where you need
 * to provide a logger to a utility class, but do not want any output from it.
 * It also helps when you have a utility that does not have a logger to supply.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: NullLogger.java 506231 2007-02-12 02:36:54Z crossley $
 */
public final class NullLogger implements Logger
{
    /**
     * Creates a new <code>NullLogger</code>.
     */
    public NullLogger()
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     */
    public void debug( String message )
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     * @param throwable ignored
     */
    public void debug( String message, Throwable throwable )
    {
    }

    /**
     * No-op.
     *
     * @return <code>false</code>
     */
    public boolean isDebugEnabled()
    {
        return false;
    }

    /**
     * No-op.
     *
     * @param message ignored
     */
    public void info( String message )
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     * @param throwable ignored
     */
    public void info( String message, Throwable throwable )
    {
    }

    /**
     * No-op.
     *
     * @return <code>false</code>
     */
    public boolean isInfoEnabled()
    {
        return false;
    }

    /**
     * No-op.
     *
     * @param message ignored
     */
    public void warn( String message )
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     * @param throwable ignored
     */
    public void warn( String message, Throwable throwable )
    {
    }

    /**
     * No-op.
     *
     * @return <code>false</code>
     */
    public boolean isWarnEnabled()
    {
        return false;
    }

    /**
     * No-op.
     *
     * @param message ignored
     */
    public void error( String message )
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     * @param throwable ignored
     */
    public void error( String message, Throwable throwable )
    {
    }

    /**
     * No-op.
     *
     * @return <code>false</code>
     */
    public boolean isErrorEnabled()
    {
        return false;
    }

    /**
     * No-op.
     *
     * @param message ignored
     */
    public void fatalError( String message )
    {
    }

    /**
     * No-op.
     *
     * @param message ignored
     * @param throwable ignored
     */
    public void fatalError( String message, Throwable throwable )
    {
    }

    /**
     * No-op.
     *
     * @return <code>false</code>
     */
    public boolean isFatalErrorEnabled()
    {
        return false;
    }

    /**
     * Returns this <code>NullLogger</code>.
     *
     * @param name ignored
     * @return this <code>NullLogger</code>
     */
    public Logger getChildLogger( String name )
    {
        return this;
    }
}
