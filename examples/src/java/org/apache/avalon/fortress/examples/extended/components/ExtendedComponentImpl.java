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

package org.apache.avalon.fortress.examples.extended.components;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.fortress.examples.extended.extensions.SecurityManageable;

/**
 * <code>TestComponentImpl</code>, demonstrating the use of a custom
 * lifecycle stage <code>SecurityManageable</code>. This code does
 * a simple access check for several files on the file system and logs
 * the results accordingly.
 *
 * @avalon.component
 * @avalon.service type=ExtendedComponent
 * @x-avalon.info name=extended-component
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/02/24 22:31:21 $
 */
public class ExtendedComponentImpl
    extends AbstractLogEnabled
    implements ExtendedComponent, SecurityManageable
{
    /**
     * Pass a SecurityManager object to the component
     *
     * @param manager a <code>SecurityManager</code> value
     */
    public void secure( final SecurityManager manager )
        throws SecurityException
    {
        getLogger().debug( "Received SecurityManager instance: " + manager );

        final String[] files = {"/tmp", "/vmlinuz", "/usr/lib/libc.a"};

        for( int i = 0; i < files.length; ++i )
        {
            try
            {
                manager.checkRead( files[ i ] );
                getLogger().info( "Thread can read " + files[ i ] );
            }

            catch( SecurityException e )
            {
                getLogger().info( "Thread can not read " + files[ i ] );
            }
        }
    }
}

