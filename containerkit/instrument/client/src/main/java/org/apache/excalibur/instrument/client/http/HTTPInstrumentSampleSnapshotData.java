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

import java.util.StringTokenizer;

import org.apache.excalibur.instrument.client.Data;
import org.apache.excalibur.instrument.client.InstrumentSampleSnapshotData;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

class HTTPInstrumentSampleSnapshotData
    extends AbstractHTTPInstrumentSampleElementData
    implements InstrumentSampleSnapshotData
{
    /** Array of values which make up the sample. */
    private int[] m_samples;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentSampleSnapshotData.
     *
     * @param connection The connection used to communicate with the server.
     * @param name The name of the data element.
     */
    HTTPInstrumentSampleSnapshotData( HTTPInstrumentManagerConnection connection,
                                      String name )
    {
        super( connection, null, name );
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPElementData Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the parent data object.
     *
     * @return The parent data object.
     */
    public Data getParent()
    {
        throw new IllegalStateException( "getParent() can not be called for snapshots." );
    }
    
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
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Updated Instrument Sample snapshot '" + getName() + "' "
                + "to version " + getStateVersion() );
        }
        
        // Get the actual number of samples returned.
        int count = configuration.getAttributeAsInteger( "count", getSize() );
        
        m_samples = new int[count];
        
        String rawSamples = configuration.getChild( "values" ).getValue( "" );
        StringTokenizer st = new StringTokenizer( rawSamples, ", " );
        
        // The token count should always match the size.  But mne careful just in
        //  case as the server may not be the same version.
        int i = 0;
        while ( st.hasMoreTokens() && ( i < m_samples.length ) )
        {
            int value;
            try
            {
                value = Integer.parseInt( st.nextToken() );
            }
            catch ( NumberFormatException e )
            {
                value = 0;
            }
            m_samples[i] = value;
            i++;
        }
    }
    
    /**
     * Causes the InstrumentSampleSnapshotData to update itself with the latest
     *  data from the server.
     *
     * @return true if successful.
     */
    public boolean update()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState(
            "snapshot.xml?packed=true&name=" + urlEncode( getName() ) + "&compact=true" );
        if ( configuration != null )
        {
            try
            {
                update( configuration );
                
                return true;
            }
            catch ( ConfigurationException e )
            {
                getLogger().debug( "Unable to update.", e );
            }
        }
        
        return false;
    }
    
    /*---------------------------------------------------------------
     * InstrumentSampleSnapshotData Methods
     *-------------------------------------------------------------*/
    /**
     * Returns an array of the individual values which make up the sample.
     *
     * @return An array of sample values.
     */
    public int[] getSamples()
    {
        return m_samples;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}