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

package org.apache.excalibur.instrument.manager.impl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 * A InstrumentSample which stores the minimum value set during the sample
 *  period.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class MinimumValueInstrumentSample
    extends AbstractValueInstrumentSample
{
    /** Last value set to the sample for use for sample periods where no value is set. */
    private int m_lastValue;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new MinimumValueInstrumentSample
     *
     * @param instrumentProxy The InstrumentProxy which owns the
     *                        InstrumentSample.
     * @param name The name of the new InstrumentSample.
     * @param interval The sample interval of the new InstrumentSample.
     * @param size The number of samples to store as history.  Assumes that size is at least 1.
     * @param description The description of the new InstrumentSample.
     * @param lease The length of the lease in milliseconds.
     */
    MinimumValueInstrumentSample( InstrumentProxy instrumentProxy,
                                  String name,
                                  long interval,
                                  int size,
                                  String description,
                                  long lease )
    {
        super( instrumentProxy, name, interval, size, description, lease );
    }
    
    /*---------------------------------------------------------------
     * InstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the type of the Instrument Sample.
     *
     * @return The type of the Instrument Sample.
     */
    public int getType()
    {
        return DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM;
    }
    
    /*---------------------------------------------------------------
     * AbstractInstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * The current sample has already been stored.  Reset the current sample
     *  and move on to the next.
     * <p>
     * Should only be called when synchronized.
     */
    protected void advanceToNextSample()
    {
        // Reset the value count and set the value to the last known value.
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
     * @param state State configuration.
     */
    protected void saveState( DefaultConfiguration state )
    {
        super.saveState( state );
        
        state.setAttribute( "last-value", Integer.toString( m_lastValue ) );
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
        super.loadState( value, state );
        
        m_lastValue = state.getAttributeAsInteger( "last-value" );
    }
    
    /**
     * Called after a state is loaded if the sample period is not the same
     *  as the last period saved.
     */
    protected void postSaveNeedsReset()
    {
        super.postSaveNeedsReset();
        
        m_lastValue = 0;
    }
    
    /*---------------------------------------------------------------
     * AbstractValueInstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the current value of the sample.  The value will be set as the
     *  sample value if it is the smallest value seen during the sample period.
     *
     * @param value New sample value.
     * @param time Time that the new sample arrives.
     */
    protected void setValueInner( int value, long time )
    {
        boolean update;
        int sampleValue;
        long sampleTime;
        
        synchronized(this)
        {
            update( time );
            
            // Always store the last value to use for samples where a value is not set.
            m_lastValue = value;
            
            if ( m_valueCount > 0 )
            {
                // Additional sample
                m_valueCount++;
                if ( value < m_value )
                {
                    m_value = value;
                    update = true;
                }
            }
            else
            {
                // First value of this sample.
                m_valueCount = 1;
                m_value = value;
            }
            
            sampleValue = m_value;
            sampleTime = m_time;
                update = true;
        }
        
        if ( update )
        {
            updateListeners( sampleValue, sampleTime );
        }
    }
}
