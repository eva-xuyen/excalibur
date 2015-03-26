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

package org.apache.avalon.fortress.examples.extended;

import org.apache.avalon.fortress.examples.extended.components.ExtendedComponent;
import org.apache.avalon.fortress.impl.DefaultContainer;

public final class ExtendedContainer
    extends DefaultContainer
{
    public void doLookups()
        throws Exception
    {
        getLogger().info( "Starting lookup of custom component" );

        for( int i = 0; i < 10; ++i )
        {
            ExtendedComponent comp = (ExtendedComponent)m_serviceManager.lookup( ExtendedComponent.ROLE );
            m_serviceManager.release( comp );
        }

        getLogger().info( "Finished lookups of custom component" );
    }
}

