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
     * Allow subclasses to add information into the saved state.
     *
     * @param state State configuration.
     */
    protected void saveState( DefaultConfiguration state )
    {
        super.saveState( state );
        
        state.setAttribute( "value-count", Integer.toString( m_valueCount ) );
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
    }
    
    /**
     * Called after a state is loaded if the sample period is not the same
     *  as the last period saved.
     */
    protected void postSaveNeedsReset()
    {
        m_value = 0;
        m_valueCount = 0;
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
