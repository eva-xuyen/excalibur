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

package org.apache.excalibur.instrument.manager;

/**
 * Describes a Instrument and acts as a Proxy to protect the original
 *  Instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface InstrumentDescriptor
{
    /**
     * Returns true if the Instrument was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    boolean isConfigured();

    /**
     * Returns true if the Instrument was registered with the Instrument
     *  Manager.
     *
     * @return True if registered.
     */
    boolean isRegistered();

    
    /**
     * Gets the name for the Instrument.  The Instrument Name is used to
     *  uniquely identify the Instrument during the configuration of the
     *  Profiler.  The value should be a string which does not contain spaces
     *  or periods.
     *
     * @return The name used to identify a Instrument.
     */
    String getName();
    
    /**
     * Gets the description of the Instrument.
     *
     * @return The description of the Instrument.
     */
    String getDescription();
    
    /**
     * Returns the type of the Instrument.  Possible values include
     *  DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER,
     *  DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE or
     *  DefaultInstrumentManager.INSTRUMENT_TYPE_NONE, if the type was never set.
     *
     * @return The type of the Instrument.
     */
    int getType();
    
    /**
     * Returns a reference to the descriptor of the Instrumentable of the
     *  instrument.
     *
     * @return A reference to the descriptor of the Instrumentable of the
     *  instrument.
     */
    InstrumentableDescriptor getInstrumentableDescriptor();
    
    /**
     * Adds a CounterInstrumentListener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener CounterInstrumentListener which will start receiving
     *                 profile updates.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER.
     */
    void addCounterInstrumentListener( CounterInstrumentListener listener );
    
    /**
     * Removes a InstrumentListener from the list of listeners which will
     *  receive profile events.
     *
     * @param listener InstrumentListener which will stop receiving profile
     *                 events.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         DefaultInstrumentManager.PROFILE_POINT_TYPE_COUNTER.
     */
    void removeCounterInstrumentListener( CounterInstrumentListener listener );
    
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
    void addValueInstrumentListener( ValueInstrumentListener listener );
        
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
    void removeValueInstrumentListener( ValueInstrumentListener listener );

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
    InstrumentSampleDescriptor getInstrumentSampleDescriptor( String instrumentSampleName )
        throws NoSuchInstrumentSampleException;
    
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
     *                   following:  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN.
     *
     * @return A Descriptor of the requested InstrumentSample.
     */
    InstrumentSampleDescriptor createInstrumentSample( String sampleDescription,
                                                       long sampleInterval,
                                                       int sampleSize,
                                                       long sampleLease,
                                                       int sampleType );
    
    /**
     * Returns an array of Descriptors for the InstrumentSamples configured for this
     *  Instrument.
     *
     * @return An array of Descriptors for the InstrumentSamples configured for this
     *  Instrument.
     */
    InstrumentSampleDescriptor[] getInstrumentSampleDescriptors();
    
    /**
     * Returns the stateVersion of the instrument.  The state version will be
     *  incremented each time any of the configuration of the instrument or
     *  any of its children is modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrument.
     */
    int getStateVersion();
}
