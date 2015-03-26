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

import org.apache.excalibur.instrument.client.InstrumentSampleElementData;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

abstract class AbstractHTTPInstrumentSampleElementData
    extends AbstractHTTPElementData
    implements InstrumentSampleElementData
{
    /** The sample interval. */
    private long m_interval;
    
    /** The size of the sample history. */
    private int m_size;
    
    /** The type of the Instrument Sample. */
    private int m_type;
    
    /** The sample value. */
    private int m_value;
    
    /** The UNIX time of the beginning of the sample. */
    private long m_time;
    
    /** The UNIX time when the lease expires. */
    private long m_leaseExpirationTime;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTTPInstrumentSampleElementData.
     *
     * @param connection The connection used to communicate with the server.
     * @param parent The parent data element.
     * @param name The name of the data element.
     */
    AbstractHTTPInstrumentSampleElementData( HTTPInstrumentManagerConnection connection,
                                             AbstractHTTPData parent,
                                             String name )
    {
        super( connection, parent, name );
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
        
        m_interval = configuration.getAttributeAsLong( "interval" );
        m_size = configuration.getAttributeAsInteger( "size" );
        m_type = configuration.getAttributeAsInteger( "type" );
        m_value = configuration.getAttributeAsInteger( "value" );
        m_time = configuration.getAttributeAsLong( "time" );
        m_leaseExpirationTime = configuration.getAttributeAsLong( "expiration-time" );
    }
    
    /*---------------------------------------------------------------
     * InstrumentSampleElementData Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the sample interval.  The period of each sample in millisends.
     *
     * @return The sample interval.
     */
    public long getInterval()
    {
        return m_interval;
    }
    
    /**
     * Returns the number of samples in the sample history.
     *
     * @return The size of the sample history.
     */
    public int getSize()
    {
        return m_size;
    }
    
    /**
     * Returns the type of the Instrument Sample.  Possible values include
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MEAN, or
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MINIMUM.
     *
     * @return The type of the Instrument Sample.
     */
    public int getType()
    {
        return m_type;
    }
    
    /**
     * Obtain the value of the sample.  All samples are integers, so the profiled
     * objects must measure quantity (numbers of items), rate (items/period), time in
     * milliseconds, etc.
     *
     * @return The sample value.
     */
    public int getValue()
    {
        return m_value;
    }
    
    /**
     * Obtain the UNIX time of the beginning of the sample.
     *
     * @return The UNIX time of the beginning of the sample.
     */
    public long getTime()
    {
        return m_time;
    }
    
    /**
     * Obtain the UNIX time when the lease expires.
     *
     * @return The UNIX time when the lease expires.
     */
    public long getLeaseExpirationTime()
    {
        return m_leaseExpirationTime;
    }
    
    /**
     * Returns the Type of the Instrument which can use the sample.  This
     *  should be the same for all instances of a class.
     * <p>
     * Should be one of the following: InstrumentData.PROFILE_POINT_TYPE_COUNTER
     *  or InstrumentData.PROFILE_POINT_TYPE_VALUE
     *
     * @return The Type of the Instrument which can use the sample.
     */
    public int getInstrumentType()
    {
        return ((HTTPInstrumentData)getParent()).getType();
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}