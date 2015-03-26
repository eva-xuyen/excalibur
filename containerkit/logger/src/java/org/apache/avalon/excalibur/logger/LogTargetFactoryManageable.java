/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.logger;

/**
 * LogTargetFactoryManageable Interface, use this to set the LogTargetFactoryManager
 * for child Components.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public interface LogTargetFactoryManageable
{
    /**
     * Sets the LogTargetFactoryManager for child components.  Can be for special
     * purpose components, however it is used mostly internally.
     */
    void setLogTargetFactoryManager( LogTargetFactoryManager logTargetFactoryManager );
}
