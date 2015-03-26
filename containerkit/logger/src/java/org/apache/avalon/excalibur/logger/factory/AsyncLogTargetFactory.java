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
package org.apache.avalon.excalibur.logger.factory;

import org.apache.avalon.excalibur.logger.LogTargetFactory;
import org.apache.avalon.excalibur.logger.LogTargetFactoryManageable;
import org.apache.avalon.excalibur.logger.LogTargetFactoryManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.output.AsyncLogTarget;

/**
 * AsyncLogTargetFactory class.
 *
 * This factory creates LogTargets with a wrapped AsyncLogTarget around it:
 *
 * <pre>
 *
 * &lt;async-target id="target-id" queue-size=".." priority="MIN|NORM|MAX|n"&gt;
 *  &lt;any-target-definition/&gt;
 * &lt;/async-target&gt;
 *
 * </pre>
 * <p>
 *  This factory creates a AsyncLogTarget object with a specified queue-size
 *  attribute (which defaults to what the AsyncLogTarget uses if absent).
 *  The LogTarget to wrap is described in a child element of the configuration (in
 *  the sample above named as &lt;any-target-definition/&gt;).
 *  The Thread of the created AsyncLogTarget will have a priority specified by the
 *  priotity attribute (which defaults to Thread.MIN_PRIORITY). The priority values
 *  corresponds to those defined in the Thread class which are:
 * </p>
 * <p>
 * <blockquote>
 * MIN=Thread.MIN_PRIORITY<br>
 * NORM=Thread.NORM_PRIORITY<br>
 * MAX=Thread.MAX_PRIORITY<br>
 * number=priority number (see class java.lang.Thread)<br>
 * </blockquote>
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public final class AsyncLogTargetFactory
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
        final int queuesize = configuration.getAttributeAsInteger( "queue-size", -1 );
        final Configuration config = configuration.getChildren()[ 0 ];
        final LogTargetFactory factory = m_logTargetFactoryManager.getLogTargetFactory( config.getName() );
        final LogTarget target = factory.createTarget( config );
        final AsyncLogTarget asyncTarget;
        if( queuesize == -1 )
        {
            asyncTarget = new AsyncLogTarget( target );
        }
        else
        {
            asyncTarget = new AsyncLogTarget( target, queuesize );
        }

        final String priority = configuration.getAttribute( "priority", null );
        final int threadPriority;
        if( "MIN".equalsIgnoreCase( priority ) )
        {
            threadPriority = Thread.MIN_PRIORITY;
        }
        else if( "NORM".equalsIgnoreCase( priority ) )
        {
            threadPriority = Thread.NORM_PRIORITY;
        }
        else
        {
            threadPriority = configuration.getAttributeAsInteger( "priority", 1 );
        }
        final Thread thread = new Thread( asyncTarget );
        thread.setPriority( threadPriority );
        thread.setDaemon( true );
        thread.start();
        return asyncTarget;
    }

    /**
     * get the LogTargetFactoryManager
     */
    public final void setLogTargetFactoryManager( LogTargetFactoryManager logTargetFactoryManager )
    {
        m_logTargetFactoryManager = logTargetFactoryManager;
    }

}

