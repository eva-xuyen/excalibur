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
package org.apache.avalon.excalibur.logger.factory;

import org.apache.avalon.excalibur.logger.LogTargetFactory;
import org.apache.avalon.excalibur.logger.LogTargetFactoryManageable;
import org.apache.avalon.excalibur.logger.LogTargetFactoryManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.output.PriorityFilteringTarget;

/**
 * PriorityFilterTargetFactory class.
 *
 * This factory creates LogTargets with a wrapped PriorityFilteringTarget
 * around it:
 *
 * <pre>
 *
 * &lt;priority-filter id="target-id" log-level="ERROR"&gt;
 *  &lt;any-target-definition/&gt;
 *  ...
 *  &lt;any-target-definition/&gt;
 * &lt;/priority-filter&gt;
 *
 * </pre>
 * <p>
 *  This factory creates a PriorityFilteringTarget object with a logging Priority set
 *  to the value of the log-level attribute (which defaults to INFO if absent).
 *  The LogTarget to filter is described in child elements of the configuration (in
 *  the sample above named as &lt;any-target-definition/&gt;).
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/03/13 03:48:55 $
 * @since 4.0
 */
public final class PriorityFilterTargetFactory
    extends AbstractTargetFactory
    implements LogTargetFactoryManageable
{
    /** The LogTargetFactoryManager */
    protected LogTargetFactoryManager m_logTargetFactoryManager;

    /**
     * create a LogTarget based on a Configuration
     */
    public final LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        final String loglevel = configuration.getAttribute( "log-level", "INFO" );
        getLogger().debug( "loglevel is " + loglevel );
        
        final boolean closeWrappedTargets =
            configuration.getAttributeAsBoolean( "close-wrapped-targets", true );
        
        final PriorityFilteringTarget filter = new PriorityFilteringTarget(
            Priority.getPriorityForName( loglevel ), closeWrappedTargets );

        final Configuration[] configs = configuration.getChildren();
        for( int i = 0; i < configs.length; i++ )
        {
            final LogTargetFactory factory =
                m_logTargetFactoryManager.getLogTargetFactory( configs[ i ].getName() );

            if( null == factory )
            {
                throw new ConfigurationException( "Unknown target type '" + configs[ i ].getName()
                                                  + "' at " + configs[ i ].getLocation() );
            }

            getLogger().debug(
                "creating target " + configs[ i ].getName() + ": " + configs[ i ].toString() );
            final LogTarget logtarget = factory.createTarget( configs[ i ] );
            filter.addTarget( logtarget );
        }
        return filter;
    }

    /**
     * get the LogTargetFactoryManager
     */
    public final void setLogTargetFactoryManager( LogTargetFactoryManager logTargetFactoryManager )
    {
        m_logTargetFactoryManager = logTargetFactoryManager;
    }

}

