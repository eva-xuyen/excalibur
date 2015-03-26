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

import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.log.LogTarget;

/**
 * Default LogTargetManager implementation.  It populates the LogTargetManager
 * from a configuration file.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.14 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class DefaultLogTargetManager
    extends AbstractLogEnabled
    implements LogTargetManager, LogTargetFactoryManageable, Configurable
{
    /** Map for ID to LogTarget mapping */
    final private Map m_targets = new HashMap();

    /** The LogTargetFactoryManager object */
    private LogTargetFactoryManager m_factoryManager;

    /**
     * Retrieves a LogTarget for an ID. If this LogTargetManager
     * does not have the match a null will be returned.
     *
     * @param id The LogTarget ID
     * @return the LogTarget or null if none is found.
     */
    public final LogTarget getLogTarget( final String id )
    {
        return (LogTarget)m_targets.get( id );
    }

    /**
     * Gets the LogTargetFactoryManager.
     */
    public final void setLogTargetFactoryManager( final LogTargetFactoryManager logTargetFactoryManager )
    {
        m_factoryManager = logTargetFactoryManager;
    }

    /**
     * Reads a configuration object and creates the log targets.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    public final void configure( final Configuration configuration )
        throws ConfigurationException
    {
        if( null == m_factoryManager )
        {
            final String message = "LogTargetFactory not received.";
            throw new ConfigurationException( message );
        }

        final Configuration[] confs = configuration.getChildren();
        for( int i = 0; i < confs.length; i++ )
        {
            final String targetName = confs[ i ].getName();
            final LogTargetFactory logTargetFactory = m_factoryManager.getLogTargetFactory( targetName );
            if( logTargetFactory == null )
            {
                final String message =
                    "Factory definition for '" + targetName +
                    "' missing from logger configuration.";
                throw new ConfigurationException( message );
            }
            final LogTarget logTarget = logTargetFactory.createTarget( confs[ i ] );
            final String targetId = confs[ i ].getAttribute( "id" );
            if( getLogger().isDebugEnabled() )
            {
                final String message = "Added new LogTarget of id " + targetId;
                getLogger().debug( message );
            }
            m_targets.put( targetId, logTarget );
        }
    }
}
