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

package org.apache.avalon.fortress.impl.lookup;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * No-op ServiceManager contains no components, and is used when we don't want to allow a component to access other
 * components.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class NoopServiceManager implements ServiceManager
{
    private static final String MESSAGE = "Could not return a reference to the Component";

    public Object lookup( String role ) throws ServiceException
    {
        throw new ServiceException(role, MESSAGE);
    }

    public boolean hasService( String role )
    {
        return false;
    }

    public void release( Object component )
    {
        // Do nothing
    }
}
