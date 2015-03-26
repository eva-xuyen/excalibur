/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.util.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.ContainerManagerConstants;
import org.apache.avalon.fortress.util.ContextManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 * ContextManagerTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class ContextManagerTestCase extends TestCase implements ContainerManagerConstants
{
    private ContextManager m_manager;
    private InstrumentManager m_instrManager;

    public ContextManagerTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        FortressConfig config = new FortressConfig( FortressConfig.createDefaultConfig() );
        config.setContainerConfiguration( "resource://org/apache/avalon/fortress/test/data/test1.xconf" );
        config.setLoggerManagerConfiguration( "resource://org/apache/avalon/fortress/test/data/test1.xlog" );

        m_instrManager = new DefaultInstrumentManager();
        ContainerUtil.enableLogging(m_instrManager, new ConsoleLogger());
        ContainerUtil.initialize(m_instrManager);
        config.setInstrumentManager(m_instrManager);

        m_manager = new ContextManager( config.getContext(), new ConsoleLogger() );
        m_manager.initialize();
    }

    public void testContextManager() throws Exception
    {
        final Context managerContext = m_manager.getContainerManagerContext();
        assertNotNull( managerContext );

        final ServiceManager serviceManager = (ServiceManager) managerContext.get( SERVICE_MANAGER );
        assertNotNull( serviceManager );

        final InstrumentManager instrumentManager =
                (InstrumentManager) serviceManager.lookup( InstrumentManager.ROLE );
        assertNotNull( instrumentManager );
        assertSame( m_instrManager, instrumentManager );
    }

    public void tearDown()
    {
        m_manager.dispose();
    }
}
