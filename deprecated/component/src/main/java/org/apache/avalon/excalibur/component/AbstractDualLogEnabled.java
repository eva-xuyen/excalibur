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
package org.apache.avalon.excalibur.component;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogKit2AvalonLoggerAdapter;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Loggable;

/**
 * A base class for all objects that need to support LogEnabled/Loggable
 * for backwards compatability.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:14 $
 */
public class AbstractDualLogEnabled
    extends AbstractLogEnabled
    implements Loggable
{
    private org.apache.log.Logger m_logkitLogger;

    public void setLogger( org.apache.log.Logger logger )
    {
        m_logkitLogger = logger;
        // are we already enabled
        if ( this.getLogger() == null ) {
            enableLogging( new LogKitLogger( logger ) );
        }
    }

    protected final org.apache.log.Logger getLogkitLogger()
    {
        if( null == m_logkitLogger )
        {
            m_logkitLogger = LogKit2AvalonLoggerAdapter.createLogger( getLogger() );
        }
        return m_logkitLogger;
    }
}
