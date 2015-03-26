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

package org.apache.avalon.fortress.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Encapsulate the Service information, and encapsulate all the
 * logic to serialize the service.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/02 08:29:44 $
 */
final class Service
{
    private final Set m_components;

    private final String m_type;

    /**
     * Initialize a service with the type name.
     *
     * @param type
     */
    public Service( final String type )
    {
        if ( type == null ) throw new NullPointerException( "type" );

        m_type = type;
        m_components = new HashSet();
    }

    /**
     * Get the service type name.
     *
     * @return  the type name
     */
    public String getType()
    {
        return m_type;
    }

    /**
     * Add a component to the service.
     *
     * @param type  the type name for the component
     */
    public void addComponent( final Component type )
    {
        if ( type == null ) throw new NullPointerException( "type" );

        m_components.add( type );
    }

    public Iterator getComponents()
    {
        return m_components.iterator();
    }

    /**
     * Output the service info.
     *
     * @param rootDir
     * @throws IOException
     */
    public void serialize( final File rootDir ) throws IOException
    {
        if ( m_components.isEmpty() ) return;

        final File serviceFile = new File( rootDir, "META-INF/services/" + getType() );
        PrintWriter writer = null;

        try
        {
            writer = new PrintWriter( new FileWriter( serviceFile ) );

            final Iterator it = m_components.iterator();
            while ( it.hasNext() )
            {
                final Component comp = (Component) it.next();
                writer.println( comp.getType() );
            }
        }
        finally
        {
            if ( null != writer )
            {
                writer.close();
            }
        }
    }
}
