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

import org.apache.excalibur.instrument.client.InstrumentSampleData;
import org.apache.excalibur.instrument.client.InstrumentSampleSnapshotData;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

class HTTPInstrumentSampleData
    extends AbstractHTTPInstrumentSampleElementData
    implements InstrumentSampleData
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentSampleData.
     */
    HTTPInstrumentSampleData( HTTPInstrumentData parent,
                              String name )
    {
        super( (HTTPInstrumentManagerConnection)parent.getConnection(), parent, name );
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPElementData Methods
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
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Updated Instrument Sample '" + getName() + "' to version " + getStateVersion() );
        }
    }
    
    /**
     * Causes the InstrumentSampleData to update itself with the latest data
     *  from the server.
     *
     * @return true if successful.
     */
    public boolean update()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState(
            "sample.xml?packed=true&name=" + urlEncode( getName() ) );
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
     * InstrumentSampleData Methods
     *-------------------------------------------------------------*/
    /**
     * Requests that the sample's lease be updated.
     */
    public void updateLease()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        connection.getState( "sample-lease.xml?name=" + urlEncode( getName() ) );
    }
    
    /**
     * Returns a snapshot of the data in the sample.
     *
     * @return A snapshot of the sample.
     */
    public InstrumentSampleSnapshotData getSnapshot()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        HTTPInstrumentSampleSnapshotData snapshot =
            new HTTPInstrumentSampleSnapshotData( connection, getName() );
        snapshot.enableLogging( getLogger() );
        if ( snapshot.update() )
        {
            return snapshot;
        }
        else
        {
            return null;
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}