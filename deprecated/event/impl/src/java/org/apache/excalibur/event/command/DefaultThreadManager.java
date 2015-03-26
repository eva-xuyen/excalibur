/* 
 * Copyright 1999-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.excalibur.event.command;

import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 * A ThreadManager that will use an external ThreadPool. This will be useful
 * if you want to have several ThreadManagers talking to a commonly defined
 * set of ThreadPools, such as
 * <a href="http://jakarta.apache.org/avalon/cornerstone">Cornerstone's</a>
 * (similarly named) ThreadManager (which manages ThreadPools).
 *
 * @see org.apache.avalon.cornerstone.services.thread.ThreadManager
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultThreadManager extends AbstractThreadManager
{
    /**
     * Create a new ThreadManager with the associated ThreadPool.
     *
     * @param pool  The ThreadPool we will use.
     */
    public DefaultThreadManager( final Executor executor )
    {
        setExecutor( executor );
    }
}
