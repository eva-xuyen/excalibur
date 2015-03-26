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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.client.InstrumentData;
import org.apache.excalibur.instrument.client.InstrumentSampleData;

class HTTPInstrumentData
    extends AbstractHTTPElementData
    implements InstrumentData
{
    /* The registered flag of the remote object. */
    private boolean m_registered;
    
    /** The type of the Instrument. */
    private int m_type;
    
    private List m_samples = new ArrayList();
    private HTTPInstrumentSampleData[] m_sampleAry;
    private Map m_sampleMap = new HashMap();
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentData.
     */
    HTTPInstrumentData( HTTPInstrumentableData parent,
                        String name )
    {
        super( (HTTPInstrumentManagerConnection)parent.getConnection(), parent, name );
        
        m_registered = false;
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPElementData Methods
     *-------------------------------------------------------------*/
    /**
     * Update the contents of the object using values from the Configuration object.
     *
     * @param configuration Configuration object to load from.
     * @param recurse True if state should be ignored and we should drill down
     *                using data in this configuration.
     *
     * @throws ConfigurationException If there are any problems.
     */
    protected void update( Configuration configuration, boolean recurse )
        throws ConfigurationException
    {
        super.update( configuration );
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Updated Instrument '" + getName() + "' to version " + getStateVersion() );
        }
        
        m_registered = configuration.getAttributeAsBoolean( "registered" );
        m_type = configuration.getAttributeAsInteger( "type" );
        
        // Samples can be added as well as removed for each update.  Build up a list of
        //  old samples and remove each one that still exists on the server.  Any left
        //  over have expired and must be removed.
        Map oldSampleMap;
        synchronized( m_samples )
        {
            oldSampleMap = new HashMap( m_sampleMap );
        }
        
        Configuration[] sampleConfs = configuration.getChildren( "sample" );
        for ( int i = 0; i < sampleConfs.length; i++ )
        {
            Configuration sConf = sampleConfs[i];
            String sName = sConf.getAttribute( "name" );
            int sStateVersion = sConf.getAttributeAsInteger( "state-version" );
            
            HTTPInstrumentSampleData sData;
            synchronized( m_samples )
            {
                sData = (HTTPInstrumentSampleData)m_sampleMap.get( sName );
                if ( sData == null )
                {
                    // It is new.
                    sData = new HTTPInstrumentSampleData( this, sName );
                    sData.enableLogging( getLogger().getChildLogger( sName ) );
                    m_samples.add( sData );
                    m_sampleMap.put( sName, sData );
                    m_sampleAry = null;
                }
                oldSampleMap.remove( sName );
            }
            
            if ( recurse )
            {
                sData.update( sConf );
            }
            else
            {
                if ( sStateVersion != sData.getStateVersion() )
                {
                    // Needs to be updated.
                    sData.update();
                }
            }
        }
        
        // Purge any old samples.
        if ( !oldSampleMap.isEmpty() )
        {
            synchronized( m_samples )
            {
                for ( Iterator iter = oldSampleMap.values().iterator(); iter.hasNext(); )
                {
                    HTTPInstrumentSampleData sample = (HTTPInstrumentSampleData)iter.next();
                    m_samples.remove( sample );
                    m_sampleMap.remove( sample.getName() );
                    m_sampleAry = null;
                }
            }
        }
    }
    
    /**
     * Causes the InstrumentData to update itself with the latest data from
     *  the server.
     *
     * @return true if successful.
     */
    public boolean update()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState(
            "instrument.xml?packed=true&name=" + urlEncode( getName() ) );
        if ( configuration != null )
        {
            try
            {
                update( configuration, false );
                
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
     * InstrumentData Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the registered flag of the remote object.
     *
     * @return The registered flag of the remote object.
     */
    public boolean isRegistered()
    {
        return m_registered;
    }
    
    /**
     * Returns the type of the Instrument.  Possible values include
     *  InstrumentData.INSTRUMENT_TYPE_COUNTER,
     *  InstrumentData.INSTRUMENT_TYPE_VALUE or
     *  InstrumentData.INSTRUMENT_TYPE_NONE, if the type was never set.
     *
     * @return The type of the Instrument.
     */
    public int getType()
    {
        return m_type;
    }
    
    /**
     * Returns an array of the Instrument Samples assigned to the Instrument.
     *
     * @return An array of Instrument Samples.
     */
    public InstrumentSampleData[] getInstrumentSamples()
    {
        HTTPInstrumentSampleData[] samples = m_sampleAry;
        if ( samples == null )
        {
            synchronized ( m_samples )
            {
                m_sampleAry = new HTTPInstrumentSampleData[m_samples.size()];
                m_samples.toArray( m_sampleAry );
                samples = m_sampleAry;
            }
        }
        return samples;
    }
    
    /**
     * Requests that a sample be created or that its lease be updated.
     *
     * @param description Description to assign to the new sample.
     * @param interval Sample interval of the new sample.
     * @param sampleCount Number of samples in the new sample.
     * @param leaseTime Requested lease time.  The server may not grant the full lease.
     * @param sampleType The type of sample to be created.
     *
     * @return True if successful.
     */
    public boolean createInstrumentSample( String description,
                                           long interval,
                                           int sampleCount,
                                           long leaseTime,
                                           int sampleType )
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState(
            "create-sample.xml?name=" + urlEncode( getName() )
            + "&description=" + urlEncode( description ) + "&interval=" + interval
            + "&size=" + sampleCount + "&lease=" + leaseTime + "&type=" + sampleType );
        
        // If there were any errors on the server while creating the sample then null
        //  will be returned.
        return configuration != null;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}