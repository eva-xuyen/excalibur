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
 * A InstrumentSample which stores the mean value set during the sample
 *  period.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class MeanValueInstrumentSample
    extends AbstractValueInstrumentSample
{
    /** Total of all values seen during the sample period. */
    private long m_valueTotal;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new MeanValueInstrumentSample
     *
     * @param instrumentProxy The InstrumentProxy which owns the
     *                        InstrumentSample.
     * @param name The name of the new InstrumentSample.
     * @param interval The sample interval of the new InstrumentSample.
     * @param size The number of samples to store as history.  Assumes that size is at least 1.
     * @param description The description of the new InstrumentSample.
     * @param lease The length of the lease in milliseconds.
     */
    MeanValueInstrumentSample( InstrumentProxy instrumentProxy,
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
        return DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN;
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
        // Leave the value as is so that it will propagate to the next sample
        //  if needed.  But reset the value count so that new values will not
        //  be affected by the old.
        m_valueCount = 0;
    }

    /**
     * Returns the value to use for filling in the buffer when time is skipped.
     * <p>
     * Should only be called when synchronized.
     */
    protected int getFillValue()
    {
        return m_value;
    }
    
    /**
     * Allow subclasses to add information into the saved state.
     *
     * @param state State configuration.
     */
    protected void saveState( DefaultConfiguration state )
    {
        super.saveState( state );
        
        state.setAttribute( "value-total", Long.toString( m_valueTotal ) );
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
        
        m_valueTotal = state.getAttributeAsLong( "value-total" );
    }
    
    /**
     * Called after a state is loaded if the sample period is not the same
     *  as the last period saved.
     */
    protected void postSaveNeedsReset()
    {
        super.postSaveNeedsReset();
        
        m_valueTotal = 0;
    }
    
    /*---------------------------------------------------------------
     * AbstractValueInstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the current value of the sample.  The value will be set as the
     *  mean of the new value and other values seen during the sample period.
     *
     * @param value New sample value.
     * @param time Time that the new sample arrives.
     */
    protected void setValueInner( int value, long time )
    {
        int sampleValue;
        long sampleTime;
        
        synchronized(this)
        {
            update( time );
            
            if ( m_valueCount > 0 )
            {
                // Additional sample
                m_valueCount++;
                m_valueTotal += value;
                m_value = (int)(m_valueTotal / m_valueCount);
            }
            else
            {
                // First value of this sample.
                m_valueCount = 1;
                m_valueTotal = m_value = value;
            }
            sampleValue = m_value;
            sampleTime = m_time;
        }
        
        updateListeners( sampleValue, sampleTime );
    }
}
