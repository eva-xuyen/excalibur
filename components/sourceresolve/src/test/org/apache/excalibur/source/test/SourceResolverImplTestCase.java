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
package org.apache.excalibur.source.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.DefaultServiceSelector;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.SourceResolverImpl;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class SourceResolverImplTestCase extends TestCase
{

    public SourceResolverImplTestCase( )
    {
        this( "source" );
    }

    public SourceResolverImplTestCase( String name )
    {
        super( name );
    }

    public void testResolver() throws Exception
    {
        Logger logger = new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG );

        //
        // create the component to handle source resolution
        //

        final SourceResolverImpl resolver = new SourceResolverImpl();
        resolver.enableLogging( logger );

        //
        // create the context argument and set the root directory and
        // contextualize the resolver
        //
        // NOTE: javadoc needed on contextualize method
        //

        final DefaultContext context = new DefaultContext();
        context.put( "context-root", new File( System.getProperty( "user.dir" ) ) );
        resolver.contextualize( context );

        //
        // create a service selector to be included in a service manager
        // to be supplied to the resolver
        //

        final ResourceSourceFactory factory = new ResourceSourceFactory();
        factory.enableLogging( logger.getChildLogger( "factory" ) );

        // create a selector and add the factory to the selector,
        // add the selector to the manager, and service the resolver
        // NOTE: javadoc missing on the serviceable method
        //

        final DefaultServiceSelector selector = new DefaultServiceSelector();
        selector.put( "resource", factory );

        final DefaultServiceManager manager = new DefaultServiceManager();
        manager.put( SourceFactory.ROLE + "Selector", selector );

        resolver.service( manager );


        logger.debug( "resolver created - but is this correct ?" );

        //
        // setup a protocol handler - TO BE DONE
        //

        logger.debug( "help me - need to setup a handler" );

        //
        // test source URL creation - TO BE DONE
        //

        logger.debug( "help me - need to test source creation" );

        //
        // test source resolution - TO BE DONE
        //

        logger.debug( "help me - need to test source resolution" );

        assertTrue( true );
    }

}
