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

package org.apache.avalon.fortress.examples.extended;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.examples.extended.extensions.Extensions;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.NullLogger;

/**
 * Fortress container example with custom extensions
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Main.java,v 1.9 2004/02/24 22:31:21 niclas Exp $
 */
public final class Main
{
    // container reference
    private static ExtendedContainer m_container;

    /**
     * @param args a <code>String[]</code> array of command line arguments
     * @exception java.lang.Exception if an error occurs
     */
    public static final void main( String[] args )
        throws Exception
    {
        FortressConfig config = new FortressConfig();
        config.setContainerClass( ExtendedContainer.class );
        config.setContainerConfiguration( "resource://org/apache/avalon/fortress/examples/extended/ExtendedContainer.xconf" );
        config.setLoggerManagerConfiguration( "resource://org/apache/avalon/fortress/examples/extended/ExtendedContainer.xlog" );
        setupExtensions(config);

        final ContainerManager cm = new DefaultContainerManager( config.getContext() );
        ContainerUtil.initialize( cm );

        m_container = (ExtendedContainer)cm.getContainer();

        m_container.doLookups();

        ContainerUtil.dispose( cm );
    }

    private static void setupExtensions( FortressConfig config )
    {
        LifecycleExtensionManager extensions = new LifecycleExtensionManager();
        extensions.enableLogging(new NullLogger());
        extensions.addAccessorExtension( new Extensions() );

        config.setLifecycleExtensionManager(extensions);
    }
}

