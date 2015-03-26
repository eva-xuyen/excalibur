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
import org.apache.avalon.excalibur.logger.DefaultLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.ContainerManagerConstants;
import org.apache.avalon.fortress.RoleManager;
import org.apache.avalon.fortress.impl.extensions.test.TestInstrumentManager;
import org.apache.avalon.fortress.impl.role.FortressRoleManager;
import org.apache.avalon.fortress.util.ContextManagerConstants;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.fortress.util.CommandSink;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.d_haven.event.Sink;
import org.d_haven.event.command.DefaultThreadManager;
import org.d_haven.event.command.ThreadManager;
import org.d_haven.event.command.CommandManager;
import org.d_haven.event.command.DefaultCommandManager;
import org.d_haven.event.impl.DefaultPipe;
import org.apache.excalibur.instrument.InstrumentManager;
import org.d_haven.mpool.DefaultPoolManager;
import org.d_haven.mpool.PoolManager;

import java.io.File;

/**
 * FortressConfigTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class FortressConfigTestCase extends TestCase
{
    private FortressConfig m_config;

    public FortressConfigTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        ThreadManager manager = new DefaultThreadManager();
        CommandManager commands = new DefaultCommandManager(manager);
        commands.start();

        m_config = new FortressConfig( FortressConfig.createDefaultConfig() );
        m_config.setCommandSink( new CommandSink(commands) );
        m_config.setContextClassLoader( FortressConfigTestCase.class.getClassLoader() );
        m_config.setInstrumentManager( new TestInstrumentManager() );
        m_config.setLifecycleExtensionManager( new LifecycleExtensionManager() );
        m_config.setLoggerCategory( "test" );
        m_config.setLoggerManager( new DefaultLoggerManager() );
        m_config.setNumberOfThreadsPerCPU( 10 );
        m_config.setPoolManager( new DefaultPoolManager(commands) );
        m_config.setRoleManager( new FortressRoleManager() );
        m_config.setServiceManager( new DefaultServiceManager() );
        m_config.setThreadTimeout( 50 );
    }

    public void testFortressConfigUsingURI() throws Exception
    {
        m_config.setContainerClass( FullLifecycleComponent.class.getName() );
        m_config.setContainerConfiguration( "resource://config.xml" );
        m_config.setContextDirectory( "/" );
        m_config.setWorkDirectory( "/" );
        m_config.setInstrumentManagerConfiguration( "resource://config.xml" );
        m_config.setLoggerManagerConfiguration( "resource://config.xml" );
        m_config.setRoleManagerConfiguration( "resource://config.xml" );

        checkContext( m_config.getContext(), true );
    }

    public void testFortressConfigUsingObject() throws Exception
    {
        m_config.setContainerClass( FullLifecycleComponent.class );
        m_config.setContainerConfiguration( new DefaultConfiguration( "test" ) );
        m_config.setContextDirectory( new File( "/" ) );
        m_config.setWorkDirectory( new File( "/" ) );
        m_config.setInstrumentManagerConfiguration( new DefaultConfiguration( "test" ) );
        m_config.setLoggerManagerConfiguration( new DefaultConfiguration( "test" ) );
        m_config.setRoleManagerConfiguration( new DefaultConfiguration( "test" ) );

        checkContext( m_config.getContext(), false );
    }

    private void checkContext( Context context, boolean useURI ) throws Exception
    {
        assertNotNull( context.get( Sink.class.getName() ) );
        assertInstanceof( context.get( Sink.class.getName() ), Sink.class );

        assertNotNull( context.get( ContainerManagerConstants.CONTAINER_CLASS ) );
        assertInstanceof( context.get( ContainerManagerConstants.CONTAINER_CLASS ), Class.class );
        assertEquals( FullLifecycleComponent.class, context.get( ContainerManagerConstants.CONTAINER_CLASS ) );

        assertNotNull( context.get( ClassLoader.class.getName() ) );
        assertInstanceof( context.get( ClassLoader.class.getName() ), ClassLoader.class );

        assertNotNull( context.get( ContextManagerConstants.CONTEXT_DIRECTORY ) );
        assertInstanceof( context.get( ContextManagerConstants.CONTEXT_DIRECTORY ), File.class );
        assertEquals( new File( "/" ), context.get( ContextManagerConstants.CONTEXT_DIRECTORY ) );

        assertNotNull( context.get( ContextManagerConstants.WORK_DIRECTORY ) );
        assertInstanceof( context.get( ContextManagerConstants.WORK_DIRECTORY ), File.class );
        assertEquals( new File( "/" ), context.get( ContextManagerConstants.WORK_DIRECTORY ) );

        assertNotNull( context.get( InstrumentManager.ROLE ) );
        assertInstanceof( context.get( InstrumentManager.ROLE ), InstrumentManager.class );

        assertNotNull( context.get( LifecycleExtensionManager.ROLE ) );
        assertInstanceof( context.get( LifecycleExtensionManager.ROLE ), LifecycleExtensionManager.class );

        assertNotNull( context.get( ContextManagerConstants.LOG_CATEGORY ) );
        assertInstanceof( context.get( ContextManagerConstants.LOG_CATEGORY ), String.class );
        assertEquals( "test", context.get( ContextManagerConstants.LOG_CATEGORY ) );

        assertNotNull( context.get( LoggerManager.ROLE ) );
        assertInstanceof( context.get( LoggerManager.ROLE ), LoggerManager.class );

        assertNotNull( context.get( ContextManagerConstants.THREADS_CPU ) );
        assertInstanceof( context.get( ContextManagerConstants.THREADS_CPU ), Integer.class );
        assertEquals( new Integer( 10 ), context.get( ContextManagerConstants.THREADS_CPU ) );

        assertNotNull( context.get( PoolManager.class.getName() ) );
        assertInstanceof( context.get( PoolManager.class.getName() ), PoolManager.class );

        assertNotNull( context.get( RoleManager.ROLE ) );
        assertInstanceof( context.get( RoleManager.ROLE ), RoleManager.class );

        assertNotNull( context.get( ContextManagerConstants.SERVICE_MANAGER ) );
        assertInstanceof( context.get( ContextManagerConstants.SERVICE_MANAGER ), ServiceManager.class );


        assertNotNull( context.get( ContextManagerConstants.THREAD_TIMEOUT ) );
        assertInstanceof( context.get( ContextManagerConstants.THREAD_TIMEOUT ), Long.class );
        assertEquals( new Long( 50 ), context.get( ContextManagerConstants.THREAD_TIMEOUT ) );

        if ( useURI )
        {
            assertNotNull( context.get( ContextManagerConstants.CONFIGURATION_URI ) );
            assertInstanceof( context.get( ContextManagerConstants.CONFIGURATION_URI ), String.class );
            assertEquals( "resource://config.xml", context.get( ContextManagerConstants.CONFIGURATION_URI ) );

            assertNotNull( context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI ) );
            assertInstanceof( context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI ), String.class );
            assertEquals( "resource://config.xml", context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION_URI ) );

            assertNotNull( context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI ) );
            assertInstanceof( context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI ), String.class );
            assertEquals( "resource://config.xml", context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION_URI ) );

            assertNotNull( context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION_URI ) );
            assertInstanceof( context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION_URI ), String.class );
            assertEquals( "resource://config.xml", context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION_URI ) );
        }
        else
        {
            assertNotNull( context.get( ContextManagerConstants.CONFIGURATION ) );
            assertInstanceof( context.get( ContextManagerConstants.CONFIGURATION ), Configuration.class );
            assertEquals( "test", ( (Configuration) context.get( ContextManagerConstants.CONFIGURATION ) ).getName() );

            assertNotNull( context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION ) );
            assertInstanceof( context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION ), Configuration.class );
            assertEquals( "test", ( (Configuration) context.get( ContextManagerConstants.INSTRUMENT_MANAGER_CONFIGURATION ) ).getName() );

            assertNotNull( context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION ) );
            assertInstanceof( context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION ), Configuration.class );
            assertEquals( "test", ( (Configuration) context.get( ContextManagerConstants.LOGGER_MANAGER_CONFIGURATION ) ).getName() );

            assertNotNull( context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION ) );
            assertInstanceof( context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION ), Configuration.class );
            assertEquals( "test", ( (Configuration) context.get( ContextManagerConstants.ROLE_MANAGER_CONFIGURATION ) ).getName() );
        }
    }

    protected void assertInstanceof( Object obj, Class klass )
    {
        assertTrue( obj.getClass().getName() + " is not an instance of " + klass.getName(),
            klass.isAssignableFrom( obj.getClass() ) );
    }
}
