/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress;

/**
 * Provides constants used to access the Context object for containers.
 * A impl should allow these values to propagate down to child
 * containers, so that they may create child containers in turn.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/02/28 15:16:24 $
 */
public interface ContainerConstants
{
    /**
     * Context Directory: The location of the context.
     */
    String CONTEXT_DIRECTORY = "context-root";
    /**
     * Work Directory: The location the impl can store temperorary files.
     */
    String WORK_DIRECTORY = "impl.workDir";
    /**
     * Threads per CPU: Information about the hardware the impl is running on.
     */
    String THREADS_CPU = "impl.threadsPerCPU";
    /**
     * Thread timeout: Information about the hardware the impl is running on.
     */
    String THREAD_TIMEOUT = "impl.threadTimeout";
}
