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

package org.apache.excalibur.instrument.client;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
class MaintainedSampleLease
{
    private String m_instrumentName;
    private String m_sampleName;
    private int    m_type;
    private long   m_interval;
    private int    m_size;
    private long   m_leaseDuration;
    private String m_description;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    MaintainedSampleLease( String instrumentName,
                           int    type,
                           long   interval,
                           int    size,
                           long   leaseDuration,
                           String description )
    {
        m_instrumentName = instrumentName;
        m_type           = type;
        m_interval       = interval;
        m_size           = size;
        m_leaseDuration  = leaseDuration;
        m_description    = description;
        
        m_sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
            m_instrumentName, m_type, m_interval, m_size );
    }
    
    MaintainedSampleLease( Configuration stateConfig ) throws ConfigurationException
    {
        m_instrumentName = stateConfig.getAttribute         ( "instrument-name" );
        m_type           = stateConfig.getAttributeAsInteger( "type" );
        m_interval       = stateConfig.getAttributeAsLong   ( "interval" );
        m_size           = stateConfig.getAttributeAsInteger( "size" );
        m_leaseDuration  = stateConfig.getAttributeAsLong   ( "lease-duration" );
        m_description    = stateConfig.getAttribute         ( "description" );
        
        m_sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
            m_instrumentName, m_type, m_interval, m_size );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Saves the current state into a Configuration.
     *
     * @return The state as a Configuration.
     */
    public final Configuration saveState()
    {
        DefaultConfiguration stateConfig = new DefaultConfiguration( "maintained-sample", "-" );
        
        stateConfig.setAttribute( "instrument-name", m_instrumentName );
        stateConfig.setAttribute( "type",
            InstrumentSampleUtils.getInstrumentSampleTypeName( m_type ) );
        stateConfig.setAttribute( "interval",        Long.toString( m_interval ) );
        stateConfig.setAttribute( "size",            Integer.toString( m_size ) );
        stateConfig.setAttribute( "lease-duration",  Long.toString( m_leaseDuration ) );
        stateConfig.setAttribute( "description",     m_description );
        
        return stateConfig;
    }
    
    String getInstrumentName()
    {
        return m_instrumentName;
    }
    
    String getSampleName()
    {
        return m_sampleName;
    }
    
    int getType()
    {
        return m_type;
    }
    
    long getInterval()
    {
        return m_interval;
    }
    
    int getSize()
    {
        return m_size;
    }
    
    long getLeaseDuration()
    {
        return m_leaseDuration;
    }
    
    String getDescription()
    {
        return m_description;
    }
}

