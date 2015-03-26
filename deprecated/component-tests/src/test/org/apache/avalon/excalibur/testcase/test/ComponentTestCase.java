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
package org.apache.avalon.excalibur.testcase.test;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.testcase.FullLifecycleComponent;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.DefaultServiceManager;

/**
 * This class provides basic facilities for enforcing Avalon's contracts
 * within your own code.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/17 13:22:38 $
 */
public final class ComponentTestCase
    extends TestCase
{
    public ComponentTestCase( String test )
    {
        super( test );
    }

    public void testCorrectLifecycle()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        component.service( new DefaultServiceManager() );
        component.configure( new DefaultConfiguration( "", "" ) );
        component.parameterize( new Parameters() );
        component.initialize();
        component.start();
        component.suspend();
        component.resume();
        component.stop();
        component.dispose();
    }

    public void testMissingLogger()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        try
        {
            component.contextualize( new DefaultContext() );
        }
        catch( Exception e )
        {
            return;
        }
        fail( "Did not detect missing logger" );
    }

    public void testOutOfOrderInitialize()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        try
        {
            component.initialize();
            component.parameterize( new Parameters() );
        }
        catch( Exception e )
        {
            return;
        }
        fail( "Did not detect out of order initialization" );
    }

    public void testOutOfOrderDispose()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        component.service( new DefaultServiceManager() );
        component.configure( new DefaultConfiguration( "", "" ) );
        component.parameterize( new Parameters() );
        component.initialize();
        component.start();
        component.suspend();
        component.resume();

        try
        {
            component.dispose();
            component.stop();
        }
        catch( Exception e )
        {
            return;
        }
        fail( "Did not detect out of order disposal" );
    }

    public void testDoubleAssignOfLogger()
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        try
        {
            component.enableLogging( new NullLogger() );
            component.enableLogging( new NullLogger() );
        }
        catch( Exception e )
        {
            // test successfull
            return;
        }

        fail( "Did not detect double assignment of Logger" );
    }

    public void testDoubleAssignOfContext()
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        try
        {
            component.contextualize( new DefaultContext() );
            component.contextualize( new DefaultContext() );
        }
        catch( Exception e )
        {
            // test successfull
            return;
        }

        fail( "Did not detect double assignment of Context" );
    }

    public void testDoubleAssignOfParameters()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        component.service( new DefaultServiceManager() );
        component.configure( new DefaultConfiguration( "", "" ) );

        try
        {
            component.parameterize( new Parameters() );
            component.parameterize( new Parameters() );
        }
        catch( Exception e )
        {
            // test successfull
            return;
        }

        fail( "Did not detect double assignment of Parameters" );
    }

    public void testDoubleAssignOfConfiguration() throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        component.service( new DefaultServiceManager() );

        try
        {
            component.configure( new DefaultConfiguration( "", "" ) );
            component.configure( new DefaultConfiguration( "", "" ) );
        }
        catch( Exception e )
        {
            // test successfull
            return;
        }

        fail( "Did not detect double assignment of Configuration" );
    }

    /*public void testDoubleAssignOfComponentManger()
        throws Exception
    {
        FullLifecycleComponent component = new FullLifecycleComponent();

        component.enableLogging( new NullLogger() );
        component.contextualize( new DefaultContext() );
        try
        {
            component.compose( new DefaultComponentManager() );
            component.compose( new DefaultComponentManager() );
        }
        catch( Exception e )
        {
            // test successfull
            return;
        }

        fail( "Did not detect double assignment of ComponentLocator" );
    }*/
}
