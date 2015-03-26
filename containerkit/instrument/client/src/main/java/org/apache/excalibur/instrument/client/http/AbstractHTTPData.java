/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.excalibur.instrument.client.Data;
import org.apache.excalibur.instrument.client.InstrumentManagerConnection;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
abstract class AbstractHTTPData
    extends AbstractLogEnabled
    implements Data
{
    /* Reference to the connection. */
    private HTTPInstrumentManagerConnection m_connection;
    
    /* Description of the remote object. */
    private String m_description;
    
    /** The current state version of the remote object. */
    private int m_stateVersion;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTTPData.
     *
     * @param connection The connection used to communicate with the server.
     * @param description An initial description.
     */
    protected AbstractHTTPData( HTTPInstrumentManagerConnection connection,
                                String description )
    {
        m_connection = connection;
        m_description = description;
        m_stateVersion = -1;
    }
    
    /*---------------------------------------------------------------
     * Data Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the InstrumentManagerConnection that owns the data object.
     *
     * @return The InstrumentManagerConnection that owns the data object.
     */
    public InstrumentManagerConnection getConnection()
    {
        return m_connection;
    }
    
    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return m_description;
    }
    
    /**
     * Returns the state version.
     *
     * @return The state version.
     */
    public int getStateVersion()
    {
        return m_stateVersion;
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
        m_description = configuration.getAttribute( "description", "" );
        m_stateVersion = configuration.getAttributeAsInteger( "state-version", 0 );
    }
    
    /**
     * URL encode the specified string.
     *
     * @param val String to be URL encoded.
     *
     * @return The URL encoded string.
     */
    protected String urlEncode( String val )
    {
        return m_connection.urlEncode( val );
    }
}
