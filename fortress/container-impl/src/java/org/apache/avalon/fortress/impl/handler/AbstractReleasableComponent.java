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

package org.apache.avalon.fortress.impl.handler;

import org.apache.avalon.framework.service.ServiceException;

/**
 * Base Implementation for a releasable component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/13 17:57:59 $
 * @since 1.2
 */
public class AbstractReleasableComponent implements ReleasableComponent
{
    private boolean initialized = false;
    
    private ComponentHandler handler;
    
    public void initialize(ComponentHandler handler)
    throws Exception
    {
        if ( this.initialized ) 
        {
            throw new ServiceException(this.toString(), "Handable component is already initialized.");
        }
        if ( handler == null )
        {
            throw new ServiceException(this.toString(), "Handler is required.");
        }
        this.handler = handler;
        this.initialized = true;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.fortress.impl.handler.HandableComponent#put(java.lang.Object)
     */
    public void releaseOnComponentHandler( ) 
    {
        if ( this.initialized ) 
        {
            this.handler.put( this );
        }
    }
}
