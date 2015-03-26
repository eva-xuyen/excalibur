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

import org.apache.excalibur.instrument.manager.CounterInstrumentListener;
import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;
import org.apache.excalibur.instrument.manager.ValueInstrumentListener;

/**
 * Describes a Instrument and acts as a Proxy to protect the original
 *  Instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
public class InstrumentDescriptorImpl
    implements InstrumentDescriptor
{
    /** InstrumentProxy being described. */
    private InstrumentProxy m_instrumentProxy;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentDescriptor.
     *
     * @param instrumentProxy InstrumentProxy being described.
     */
    InstrumentDescriptorImpl( InstrumentProxy instrumentProxy )
    {
        m_instrumentProxy = instrumentProxy;
    }
    
    /*---------------------------------------------------------------
     * InstrumentDescriptor Methods
     *-------------------------------------------------------------*/
    /**
     * Returns true if the Instrument was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    public boolean isConfigured()
    {
        return m_instrumentProxy.isConfigured();
    }

    /**
     * Returns true if the Instrument was registered with the Instrument
     *  Manager.
     *
     * @return True if registered.
     */
    public boolean isRegistered()
    {
        return m_instrumentProxy.isRegistered();
    }
    
    /**
     * Gets the name for the Instrument.  The Instrument Name is used to
     *  uniquely identify the Instrument during the configuration of the
     *  Profiler.  The value should be a string which does not contain spaces
     *  or periods.
     *
     * @return The name used to identify a Instrument.
     */
    public String getName() 
    {
        return m_instrumentProxy.getName();
    }
    
    /**
     * Gets the description of the Instrument.
     *
     * @return The description of the Instrument.
     */
    public String getDescription()
    {
        return m_instrumentProxy.getDescription();
    }
    
    /**
     * Returns the type of the Instrument.  Possible values include
     *  InstrumentManagerClient.INSTRUMENT_TYPE_COUNTER,
     *  InstrumentManagerClient.INSTRUMENT_TYPE_VALUE or
     *  InstrumentManagerClient.INSTRUMENT_TYPE_NONE, if the type was never set.
     *
     * @return The type of the Instrument.
     */
    public int getType()
    {
        return m_instrumentProxy.getType();
    }
    
    /**
     * Returns a reference to the descriptor of the Instrumentable of the
     *  instrument.
     *
     * @return A reference to the descriptor of the Instrumentable of the
     *  instrument.
     */
    public InstrumentableDescriptor getInstrumentableDescriptor()
    {
        return m_instrumentProxy.getInstrumentableProxy().getDescriptor();
    }
    
    /**
     * Adds a CounterInstrumentListener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener CounterInstrumentListener which will start receiving
     *                 profile updates.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.PROFILE_POINT_TYPE_COUNTER.
     */
    public void addCounterInstrumentListener( CounterInstrumentListener listener )
    {
        m_instrumentProxy.addCounterInstrumentListener( listener );
    }
    
    /**
     * Removes a InstrumentListener from the list of listeners which will
     *  receive profile events.
     *
     * @param listener InstrumentListener which will stop receiving profile
     *                 events.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.PROFILE_POINT_TYPE_COUNTER.
     */
    public void removeCounterInstrumentListener( CounterInstrumentListener listener )
    {
        m_instrumentProxy.removeCounterInstrumentListener( listener );
    }
    
    /**
     * Adds a ValueInstrumentListener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener ValueInstrumentListener which will start receiving
     *                 profile updates.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE.
     */
    public void addValueInstrumentListener( ValueInstrumentListener listener )
    {
        m_instrumentProxy.addValueInstrumentListener( listener );
    }
        
    /**
     * Removes a InstrumentListener from the list of listeners which will
     *  receive profile events.
     *
     * @param listener InstrumentListener which will stop receiving profile
     *                 events.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE.
     */
    public void removeValueInstrumentListener( ValueInstrumentListener listener )
    {
        m_instrumentProxy.removeValueInstrumentListener( listener );
    }
    
    /**
     * Returns a InstrumentSampleDescriptor based on its name.
     *
     * @param instrumentSampleName Name of the InstrumentSample being requested.
     *
     * @return A Descriptor of the requested InstrumentSample.
     *
     * @throws NoSuchInstrumentSampleException If the specified InstrumentSample
     *                                      does not exist.
     */
    public InstrumentSampleDescriptor getInstrumentSampleDescriptor( String instrumentSampleName )
        throws NoSuchInstrumentSampleException
    {
        InstrumentSample instrumentSample =
            m_instrumentProxy.getInstrumentSample( instrumentSampleName );
        if ( instrumentSample == null )
        {
            throw new NoSuchInstrumentSampleException(
                "No instrument sample can be found using name: " + instrumentSampleName );
        }
        
        return instrumentSample.getDescriptor();
    }
    
    /**
     * Returns a InstrumentSampleDescriptor based on its name.  If the requested
     *  sample is invalid in any way, then an expired Descriptor will be
     *  returned.
     *
     * @param sampleDescription Description to assign to the new Sample.
     * @param sampleInterval Sample interval to use in the new Sample.
     * @param sampleLease Requested lease time for the new Sample in
     *                    milliseconds.  The InstrumentManager may grant a
     *                    lease which is shorter or longer than the requested
     *                    period.
     * @param sampleType Type of sample to request.  Must be one of the
     *                   following:  InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *                   InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MINIMUM,
     *                   InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *                   InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MEAN.
     *
     * @return A Descriptor of the requested InstrumentSample.
     *
     * @throws NoSuchInstrumentSampleException If the specified InstrumentSample
     *                                      does not exist.
     */
    public InstrumentSampleDescriptor createInstrumentSample( String sampleDescription,
                                                              long sampleInterval,
                                                              int sampleSize,
                                                              long sampleLease,
                                                              int sampleType )
    {
        InstrumentSample sample = m_instrumentProxy.createInstrumentSample(
            sampleDescription, sampleInterval, sampleSize, sampleLease, sampleType );
        return sample.getDescriptor();
    }
    
    /**
     * Returns an array of Descriptors for the InstrumentSamples configured for this
     *  Instrument.
     *
     * @return An array of Descriptors for the InstrumentSamples configured for this
     *  Instrument.
     */
    public InstrumentSampleDescriptor[] getInstrumentSampleDescriptors()
    {
        return m_instrumentProxy.getInstrumentSampleDescriptors();
    }
    
    /**
     * Returns the stateVersion of the instrument.  The state version will be
     *  incremented each time any of the configuration of the instrument or
     *  any of its children is modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrument.
     */
    public int getStateVersion()
    {
        return m_instrumentProxy.getStateVersion();
    }
}
