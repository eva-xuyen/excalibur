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

package org.apache.excalibur.instrument.manager.impl;

import java.io.PrintWriter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.ValueInstrumentListener;

/**
 * An AbstractValueInstrumentSample contains all of the functionality common
 *  to all InstrumentSamples which represent a fixed value.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
abstract class AbstractValueInstrumentSample
    extends AbstractInstrumentSample
    implements ValueInstrumentListener
{
    /** The sample value. */
    protected int m_value;
    
    /** The number of times that the value has been changed in this sample period. */
    protected int m_valueCount;
    
    /** Last value set to the sample for use for sample periods where no value is set. */
    protected int m_lastValue;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractValueInstrumentSample
     *
     * @param instrumentProxy The InstrumentProxy which owns the
     *                        InstrumentSample.
     * @param name The name of the new InstrumentSample.
     * @param interval The sample interval of the new InstrumentSample.
     * @param size The number of samples to store as history.  Assumes that size is at least 1.
     * @param description The description of the new InstrumentSample.
     * @param lease The length of the lease in milliseconds.
     */
    protected AbstractValueInstrumentSample( InstrumentProxy instrumentProxy,
                                             String name,
                                             long interval,
                                             int size,
                                             String description,
                                             long lease )
    {
        super( instrumentProxy, name, interval, size, description, lease );
        
        // Set the current value to 0 initially.
        m_value = 0;
    }
    
    /*---------------------------------------------------------------
     * InstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the Type of the Instrument which can use the sample.  This
     *  should be the same for all instances of a class.
     * <p>
     * This InstrumentSample returns DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE
     *
     * @return The Type of the Instrument which can use the sample.
     */
    public final int getInstrumentType()
    {
        return DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE;
    }
    
    /**
     * Obtain the value of the sample.  All samples are integers, so the profiled
     * objects must measure quantity (numbers of items), rate (items/period), time in
     * milliseconds, etc.
     * <p>
     * Should only be called when synchronized.
     *
     * @return The sample value.
     */
    public int getValueInner()
    {
        return m_value;
    }

    /*---------------------------------------------------------------
     * AbstractInstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * The current sample has already been stored.  Reset the current sample
     *  and move on to the next.
     * <p>
     * Should only be called when synchronized.
     *
     * @param reset True if the next sample should be reset.
     */
    protected void advanceToNextSample( boolean reset )
    {
        // Reset the value count and set the value to the last known value.
        if ( reset )
        {
            m_lastValue = 0;
        }
        m_value = m_lastValue;
        m_valueCount = 0;
    }

    /**
     * Returns the value to use for filling in the buffer when time is skipped.
     * <p>
     * Should only be called when synchronized.
     */
    protected int getFillValue()
    {
        return m_lastValue;
    }
    
    /**
     * Allow subclasses to add information into the saved state.
     *
     * @param out PrintWriter to write to.
     */
    protected void writeStateAttributes( PrintWriter out )
    {
        super.writeStateAttributes( out );
        
        out.print( " value-count=\"" );
        out.print( m_valueCount );
        out.print( "\" last-value=\"" );
        out.print( m_lastValue );
        out.print( "\"" );
    }
    
    /**
     * Used to load the state, called from AbstractInstrumentSample.loadState();
     * <p>
     * Should only be called when synchronized.
     *
     * @param value Current value loaded from the state.
     * @param state Configuration object to load state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    protected void loadState( int value, Configuration state )
        throws ConfigurationException
    {
        m_value = value;
        m_valueCount = state.getAttributeAsInteger( "value-count" );
        m_lastValue = state.getAttributeAsInteger( "last-value" );
    }
    
    /*---------------------------------------------------------------
     * ValueInstrumentListener Methods
     *-------------------------------------------------------------*/
    /**
     * Called by a ValueInstrument whenever its value is set.
     *
     * @param instrumentName The key of Instrument whose value was set.
     * @param value Value that was set.
     * @param time The time that the Instrument was incremented.
     *
     * ValueInstrument
     */
    public void setValue( String instrumentName, int value, long time )
    {
        //System.out.println("AbstractValueInstrumentSample.setValue(" + instrumentName + ", "
        //    + value + ", " + time + ") : " + getName());
        setValueInner( value, time );
    }
    
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the current value of the sample.
     *
     * @param value New sample value.
     * @param time Time that the new sample arrives.
     */
    protected abstract void setValueInner( int value, long time );
}
