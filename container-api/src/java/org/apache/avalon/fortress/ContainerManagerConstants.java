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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.d_haven.event.command.CommandFailureHandler;

/**
 * Provides constants used to access the Context object for impl
 * managers. A impl manager can assume that all these elements are
 * present in the initial context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.11 $ $Date: 2004/02/28 15:16:24 $
 */
public interface ContainerManagerConstants extends ContainerConstants
{
    /**
     * Class: The class of the impl.
     */
    String CONTAINER_CLASS = "impl.class";

    /**
     * Class: The class of the command failure handler impl.
     */
    String COMMAND_FAILURE_HANDLER_CLASS = CommandFailureHandler.class.getName();

    /**
     * ComponentLocator: The component manager to give to the impl.
     */
    String SERVICE_MANAGER = ServiceManager.class.getName();

    /**
     * Logger where to log our own messages.
     */
    String LOGGER = Logger.class.getName();

    /**
     * Configuration: The configuration to give to the impl.
     */
    String CONFIGURATION = "impl.configuration";

    /**
     * Parameters: The Parameters object to give to the impl.
     */
    String PARAMETERS = "impl.parameters";
}
