/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.fortress.examples.interceptors;

import org.apache.avalon.fortress.examples.interceptors.business.PersistenceManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.impl.InterceptorEnabledContainer;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Fortress container example allowing you to perform lookups on components
 * via a simple swing gui.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public final class Main
{
    /**
     * @param args a <code>String[]</code> array of command line arguments
     * @exception java.lang.Exception if an error occurs
     */
    public static final void main( String[] args )
        throws Exception
    {
        final FortressConfig config = new FortressConfig();
        config.setContainerClass( InterceptorEnabledContainer.class );
        config.setContainerConfiguration( "resource://org/apache/avalon/fortress/examples/interceptors/Interceptors.xconf" );
        config.setLoggerManagerConfiguration( "resource://org/apache/avalon/fortress/examples/interceptors/Interceptors.xlog" );

        ContainerManager cm = new DefaultContainerManager( config.getContext() );
        ContainerUtil.initialize( cm );
        
        InterceptorEnabledContainer container = (InterceptorEnabledContainer) cm.getContainer();
        
        ServiceManager manager = container.getServiceManager();
        
        PersistenceManager persistenceManager = (PersistenceManager) manager.lookup( PersistenceManager.ROLE );
        
        // Within an worker role, we should access the method

        WhoAmI.instance().checkIn( "Homer", "Worker" );
        persistenceManager.persist( "my profile info" );
        
        // Student role is not accepted

        try
        {
            WhoAmI.instance().checkIn( "Bart", "Student" );
            persistenceManager.persist( "my profile info" );
        }
        catch( SecurityException ex )
        {
            ex.printStackTrace();
        }
        
        manager.release( persistenceManager );
        
        ContainerUtil.dispose( cm );
    }
}

