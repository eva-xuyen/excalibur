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

package org.apache.excalibur.instrument.client.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.excalibur.instrument.client.Data;
import org.apache.excalibur.instrument.client.ElementData;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
abstract class AbstractHTTPElementData
    extends AbstractHTTPData
    implements ElementData
{
    /* The parent data object, or null for the root. */
    private AbstractHTTPData m_parent;
    
    /* The name of the data object. */
    private String m_name;
    
    /* Name of the configured flag of the remote object. */
    private boolean m_configured;
    
    /*---------------------------------------------------------------
     * Static Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the last element of a name separated by '.'s
     */
    protected static String lastNameToken( String name )
    {
        int pos = name.lastIndexOf( '.' );
        if ( pos >= 0 )
        {
            return name.substring( pos + 1 );
        }
        else
        {
            return name;
        }
    }
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTTPElementData.
     *
     * @param connection The connection used to communicate with the server.
     * @param parent The parent data element.
     * @param name The name of the data element.
     */
    protected AbstractHTTPElementData( HTTPInstrumentManagerConnection connection,
                                       AbstractHTTPData parent,
                                       String name )
    {
        super( connection, lastNameToken( name ) );
        m_parent = parent;
        m_name = name;
        m_configured = false;
    }
    
    /*---------------------------------------------------------------
     * HTTPElementData Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the parent data object.
     *
     * @return The parent data object.
     */
    public Data getParent()
    {
        return m_parent;
    }
    
    /**
     * Returns the name.
     *
     * @return The name.
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Returns the configured flag of the remote object.
     *
     * @return The configured flag of the remote object.
     */
    public boolean isConfigured()
    {
        return m_configured;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Update the contents of the object using values from the Configuration object.
     *
     * @param configuration Configuration object to load from.
     *
     * @throws ConfigurationException If there are any problems.
     */
    protected void update( Configuration configuration )
        throws ConfigurationException
    {
        super.update( configuration );
        
        m_configured = configuration.getAttributeAsBoolean( "configured", false );
    }
}
