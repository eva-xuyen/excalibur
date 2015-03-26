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
package org.apache.avalon.excalibur.thread.impl;

import org.apache.excalibur.thread.Executable;

/**
 * Class to adapt a {@link org.apache.avalon.framework.activity.Executable} object in
 * an {@link Executable} object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
final class ExecutableExecuteable
    implements Executable
{
    ///The runnable instance being wrapped
    private org.apache.avalon.framework.activity.Executable m_executable;

    /**
     * Create adapter using specified executable.
     *
     * @param executable the executable to adapt to
     */
    protected ExecutableExecuteable( final org.apache.avalon.framework.activity.Executable executable )
    {
        if( null == executable )
        {
            throw new NullPointerException( "executable" );
        }
        m_executable = executable;
    }

    /**
     * Execute the underlying
     * {@link org.apache.avalon.framework.activity.Executable} object.
     *
     * @throws Exception if an error occurs
     */
    public void execute()
        throws Exception
    {
        m_executable.execute();
    }
}
