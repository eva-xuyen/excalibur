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

package org.apache.avalon.cornerstone.threads.tutorial;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * A demonstration runnable object that simply logs a countdown sequence.
 *
 * @author Stephen McConnell
 * @avalon.component name="counter" 
 */
public class Counter extends Thread implements LogEnabled
{
   /**
    * The supplied logging channel.
    */
    private Logger m_logger;

    private int m_count = 10;

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

    public void run()
    {
        while( m_count > 0 )
        {
            getLogger().info( "count: " + m_count );
            m_count--;
            try
            {
                sleep( 1000 );
            }
            catch( Throwable e )
            {
                getLogger().info( "I've been interrupted." );
                m_count = -1;
            }
        }
        getLogger().info( "Time to die." );
        m_logger = null;
    }
}

