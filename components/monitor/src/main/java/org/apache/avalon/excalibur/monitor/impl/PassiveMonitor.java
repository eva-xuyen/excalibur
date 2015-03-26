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
package org.apache.avalon.excalibur.monitor.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.monitor.Resource;

/**
 * A passive monitor will check the reosurce each time it
 * is accessed.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:35 $
 */
public class PassiveMonitor
    extends AbstractMonitor
{
    private Map m_lastModified = Collections.synchronizedMap( new HashMap() );

    /**
     * Find a monitored resource.  If no resource is available, return null
     */
    public final Resource getResource( final String key )
    {
        final Resource resource = super.getResource( key );
        if( resource != null )
        {
            final Long lastModified = (Long)m_lastModified.get( key );

            if( lastModified != null )
            {
                resource.testModifiedAfter( lastModified.longValue() );
            }

            m_lastModified.put( key,
                                new Long( System.currentTimeMillis() ) );
        }

        return resource;
    }
}
