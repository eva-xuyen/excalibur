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
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.client.InstrumentableData;
import org.apache.excalibur.instrument.client.InstrumentManagerData;

class HTTPInstrumentManagerData
    extends AbstractHTTPData
    implements InstrumentManagerData
{
    /* Name of the remote object. */
    private String m_name;
    
    /* Flag which keeps track of whether the manager supports batched lease updates. */
    private boolean m_batchedUpdates;
    
    /* Flag which keeps track of whether the manager is read only or not. */
    private boolean m_readOnly;
    
    private List m_instrumentables = new ArrayList();
    private HTTPInstrumentableData[] m_instrumentableAry;
    private Map m_instrumentableMap = new HashMap();
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentManagerData.
     */
    HTTPInstrumentManagerData( HTTPInstrumentManagerConnection connection )
    {
        super( connection, connection.getURL().toExternalForm() );
        
        m_name = connection.getURL().toExternalForm();
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPData Methods
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
        
        m_name = configuration.getAttribute( "name" );
        
        // Support for batched lease creates and renewals added in version 1.2.
        m_batchedUpdates = configuration.getAttributeAsBoolean( "batched-updates", false );
        
        // read-only attribute added in version 1.2.
        m_readOnly = configuration.getAttributeAsBoolean( "read-only", false );
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Updated InstrumentManager '" + getName() + "' to version " + getStateVersion() );
        }
        
        Configuration[] instrumentableConfs = configuration.getChildren( "instrumentable" );
        for ( int i = 0; i < instrumentableConfs.length; i++ )
        {
            Configuration iaConf = instrumentableConfs[i];
            String iaName = iaConf.getAttribute( "name" );
            int iaStateVersion = iaConf.getAttributeAsInteger( "state-version" );
            
            HTTPInstrumentableData iaData;
            synchronized ( m_instrumentables )
            {
                iaData = (HTTPInstrumentableData)m_instrumentableMap.get( iaName );
                if ( iaData == null )
                {
                    // It is new.
                    iaData = new HTTPInstrumentableData( this, iaName );
                    iaData.enableLogging( getLogger().getChildLogger( iaName ) );
                    m_instrumentables.add( iaData );
                    m_instrumentableAry = null;
                    m_instrumentableMap.put( iaName, iaData );
                }
            }
            
            if ( recurse )
            {
                iaData.update( iaConf, recurse );
            }
            else
            {
                if ( iaStateVersion != iaData.getStateVersion() )
                {
                    // Needs to be updated.
                    iaData.update();
                }
            }
        }
    }
    
    /**
     * Causes the InstrumentManagerData to update itself with the latest data
     *  from the server.
     *
     * @return true if successful.
     */
    public boolean update()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState( "instrument-manager.xml?packed=true" );
        if ( configuration != null )
        {
            try
            {
                update( configuration, false );
                
                //updateLeasedSamples();
                
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
     * InstrumentManagerData Methods
     *-------------------------------------------------------------*/
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
     * Returns true if the InstrumentManager on the server is operating in
     *  read-only mode.
     *
     * @return True if read-only.
     *
     * @since 1.2
     */
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    /**
     * Returns true if batched lease creates and renewals are implemented on
     *  the server.
     *
     * @return True if read-only.
     *
     * @since 1.2
     */
    private boolean isSupportsBatchedUpdates()
    {
        return m_batchedUpdates;
    }
    
    /**
     * Gets a thread-safe snapshot of the instrumentable list.
     *
     * @return A thread-safe snapshot of the instrumentable list.
     */
    public InstrumentableData[] getInstrumentables()
    {
        HTTPInstrumentableData[] instrumentables = m_instrumentableAry;
        if ( instrumentables == null )
        {
            synchronized ( m_instrumentables )
            {
                m_instrumentableAry = new HTTPInstrumentableData[m_instrumentables.size()];
                m_instrumentables.toArray( m_instrumentableAry );
                instrumentables = m_instrumentableAry;
            }
        }
        return instrumentables;
    }
    
    /**
     * Causes the the entire instrument tree to be updated in one call.  Very fast
     *  when it is known that all or most data has changed.
     *
     * @return true if successful.
     */
    public boolean updateAll()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration =
            connection.getState( "instrument-manager.xml?packed=true&recurse=true" );
        if ( configuration != null )
        {
            try
            {
                update( configuration, true );
                
                //updateLeasedSamples();
                
                return true;
            }
            catch ( ConfigurationException e )
            {
                getLogger().debug( "Unable to update.", e );
            }
        }
        return false;
    }
    
    /**
     * Requests that a sample be created or that its lease be updated.
     *
     * @param instrumentName The full name of the instrument whose sample is
     *                       to be created or updated.
     * @param description Description to assign to the new sample.
     * @param interval Sample interval of the new sample.
     * @param sampleCount Number of samples in the new sample.
     * @param leaseTime Requested lease time.  The server may not grant the
     *                  full lease.
     * @param sampleType The type of sample to be created.
     */
    public void createInstrumentSample( String instrumentName,
                                        String description,
                                        long interval,
                                        int sampleCount,
                                        long leaseTime,
                                        int sampleType )
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        connection.getState( "create-sample.xml?name=" + urlEncode( instrumentName )
            + "&description=" + urlEncode( description ) + "&interval=" + interval
            + "&size=" + sampleCount + "&lease=" + leaseTime + "&type=" + sampleType );
    }
    
    /**
     * Requests that a set of samples be created or that their leases be
     *  updated.  All array parameters must be of the same length.
     *
     * @param instrumentNames The full names of the instruments whose sample
     *                        are to be created or updated.
     * @param descriptions Descriptions to assign to the new samples.
     * @param intervals Sample intervals of the new samples.
     * @param sampleCounts Number of samples in each the new samples.
     * @param leaseTimes Requested lease times.  The server may not grant the
     *                   full leases.
     * @param sampleTypes The types of samples to be created.
     */
    public void createInstrumentSamples( String[] instrumentNames,
                                         String[] descriptions,
                                         long[] intervals,
                                         int[] sampleCounts,
                                         long[] leaseTimes,
                                         int[] sampleTypes )
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        // Validate the arguments to avoid errors from misuse.
        if ( ( instrumentNames.length != descriptions.length )
            || ( instrumentNames.length != intervals.length )
            || ( instrumentNames.length != sampleCounts.length )
            || ( instrumentNames.length != leaseTimes.length )
            || ( instrumentNames.length != sampleTypes.length ) )
        {
            throw new IllegalArgumentException( "Array lengths of all parameters must be equal." );
        }
        
        // If batched updates are not supported, then do them individually
        if ( isSupportsBatchedUpdates() )
        {
            StringBuffer sb = new StringBuffer();
            sb.append( "create-samples.xml?" );
            for ( int i = 0; i < instrumentNames.length; i++ )
            {
                if ( i > 0 )
                {
                    sb.append( "&" );
                }
                sb.append( "name=" );
                sb.append( urlEncode( instrumentNames[i] ) );
                sb.append( "&description=" );
                sb.append( urlEncode( descriptions[i] ) );
                sb.append( "&interval=" );
                sb.append( intervals[i] );
                sb.append( "&size=" );
                sb.append( sampleCounts[i] );
                sb.append( "&lease=" );
                sb.append( leaseTimes[i] );
                sb.append( "&type=" );
                sb.append( sampleTypes[i] );
            }
            
            connection.getState( sb.toString() );
        }
        else
        {
            for ( int i = 0; i < instrumentNames.length; i++ )
            {
                createInstrumentSample( instrumentNames[i], descriptions[i], intervals[i],
                    sampleCounts[i], leaseTimes[i], sampleTypes[i] );
            }
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}