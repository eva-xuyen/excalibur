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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.client.InstrumentableData;
import org.apache.excalibur.instrument.client.InstrumentData;

class HTTPInstrumentableData
    extends AbstractHTTPElementData
    implements InstrumentableData
{
    /* The registered flag of the remote object. */
    private boolean m_registered;
    
    private List m_instrumentables = new ArrayList();
    private HTTPInstrumentableData[] m_instrumentableAry;
    private Map m_instrumentableMap = new HashMap();
    
    private List m_instruments = new ArrayList();
    private HTTPInstrumentData[] m_instrumentAry;
    private Map m_instrumentMap = new HashMap();
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentableData.
     */
    HTTPInstrumentableData( AbstractHTTPData parent,
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
                "Updated Instrumentable '" + getName() + "' to version " + getStateVersion() );
        }
        
        m_registered = configuration.getAttributeAsBoolean( "registered" );
        
        Configuration[] instrumentableConfs = configuration.getChildren( "instrumentable" );
        for ( int i = 0; i < instrumentableConfs.length; i++ )
        {
            Configuration iaConf = instrumentableConfs[i];
            String iaName = iaConf.getAttribute( "name" );
            int iaStateVersion = iaConf.getAttributeAsInteger( "state-version" );
            
            HTTPInstrumentableData iaData;
            synchronized( m_instrumentables )
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
        
        Configuration[] instrumentConfs = configuration.getChildren( "instrument" );
        for ( int i = 0; i < instrumentConfs.length; i++ )
        {
            Configuration iConf = instrumentConfs[i];
            String iName = iConf.getAttribute( "name" );
            int iStateVersion = iConf.getAttributeAsInteger( "state-version" );
            
            HTTPInstrumentData iData;
            synchronized( m_instruments )
            {
                iData = (HTTPInstrumentData)m_instrumentMap.get( iName );
                if ( iData == null )
                {
                    // It is new.
                    iData = new HTTPInstrumentData( this, iName );
                    iData.enableLogging( getLogger().getChildLogger( iName ) );
                    m_instruments.add( iData );
                    m_instrumentAry = null;
                    m_instrumentMap.put( iName, iData );
                }
            }
            
            if ( recurse )
            {
                iData.update( iConf, recurse );
            }
            else
            {
                if ( iStateVersion != iData.getStateVersion() )
                {
                    // Needs to be updated.
                    iData.update();
                }
            }
        }
    }
    
    /**
     * Causes the InstrumentableData to update itself with the latest data from
     *  the server.
     *
     * @return true if successful.
     */
    public boolean update()
    {
        HTTPInstrumentManagerConnection connection =
            (HTTPInstrumentManagerConnection)getConnection();
        
        Configuration configuration = connection.getState(
            "instrumentable.xml?packed=true&name=" + urlEncode( getName() ) );
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
     * InstrumentableData Methods
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
     * Gets a thread-safe snapshot of the child instrumentable list.
     *
     * @return A thread-safe snapshot of the child instrumentable list.
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
     * Gets a thread-safe snapshot of the instrument list.
     *
     * @return A thread-safe snapshot of the instrument list.
     */
    public InstrumentData[] getInstruments()
    {
        HTTPInstrumentData[] instruments = m_instrumentAry;
        if ( instruments == null )
        {
            synchronized ( m_instruments )
            {
                m_instrumentAry = new HTTPInstrumentData[m_instruments.size()];
                m_instruments.toArray( m_instrumentAry );
                instruments = m_instrumentAry;
            }
        }
        return instruments;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}