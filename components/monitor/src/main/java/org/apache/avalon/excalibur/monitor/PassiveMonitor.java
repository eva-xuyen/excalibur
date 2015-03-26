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
package org.apache.avalon.excalibur.monitor;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * The PassiveMonitor is used to passively check a set of resources to see if they have
 * changed.  It will be implemented as a Component, that can be retrieved from
 * the ComponentLocator.  It defaults to checking every 1 minute.  The configuration
 * looks like this:
 *
 * <pre>
 *   &lt;passive-monitor&gt;
 *     &lt;init-resources&gt;
 *       &lt;-- This entry can be repeated for every resource you want to register immediately --&gt;
 *
 *       &lt;resource key="<i>file:./myfile.html</i>" class="<i>org.apache.avalon.excalibur.monitor.FileMonitor</i>"/&gt;
 *     &lt;/init-resources&gt;
 *   &lt;/pasive-monitor&gt;
 * </pre>
 *
 * @avalon.component name="passive-monitor" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.excalibur.monitor.Monitor" version="1.0"
 * @x-avalon.info name=passive-monitor
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PassiveMonitor.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public class PassiveMonitor
    extends org.apache.avalon.excalibur.monitor.impl.PassiveMonitor
    implements LogEnabled, Configurable, ThreadSafe
{
    private Logger m_logger;

    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
    }

    public final void configure( final Configuration config )
        throws ConfigurationException
    {
        final Configuration[] initialResources =
            config.getChild( "init-resources" ).getChildren( "resource" );
        final Resource[] resources =
            MonitorUtil.configureResources( initialResources, m_logger );
        addResources( resources );
    }
}
