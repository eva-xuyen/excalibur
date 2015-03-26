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

package org.apache.avalon.fortress.examples.viewer;

import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.container.ContainerUtil;

/**
 * Fortress container example allowing you to perform lookups on components
 * via a simple swing gui.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Main.java,v 1.11 2004/03/08 16:00:23 farra Exp $
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
        ContainerManager cm = null;

        try
        {
            final FortressConfig config = new FortressConfig();
            config.setContainerClass( ComponentViewer.class );
            config.setContainerConfiguration( "resource://org/apache/avalon/fortress/examples/viewer/ComponentViewer.xconf" );
            config.setLoggerManagerConfiguration( "resource://org/apache/avalon/fortress/examples/viewer/ComponentViewer.xlog" );

            // needs the altrmi binaries
	    //   config.setInstrumentManagerConfiguration( "resource://org/apache/avalon/fortress/examples/viewer/ComponentViewer.instruments" );

            cm = new DefaultContainerManager( config.getContext() );
            org.apache.avalon.framework.container.ContainerUtil.initialize( cm );

            ( (ComponentViewer)cm.getContainer() ).run();
        }
        catch( CascadingException e )
        {
            e.printStackTrace();

            Throwable t = e.getCause();

            while( t != null )
            {
                t.printStackTrace();

                t = t.getCause();
            }
        }
        finally
        {
            ContainerUtil.dispose( cm );
        }
    }
}

