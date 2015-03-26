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

import org.apache.excalibur.instrument.InstrumentManager;

/**
 * The public interface to the DefaultInstrumentManager implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface DefaultInstrumentManager
    extends InstrumentManager
{
    /** Type which specifies that the type of a Instrument has not yet been determined. */
    int INSTRUMENT_TYPE_NONE = 0;
    
    /** Type which identifies CounterInstruments. */
    int INSTRUMENT_TYPE_COUNTER = 1;
    
    /** Type which identifies ValueInstruments. */
    int INSTRUMENT_TYPE_VALUE   = 2;
    
    /** Type which identifies CounterInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_COUNTER = 101;
    
    /** Type which identifies MinimumInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MINIMUM = 102;
    
    /** Type which identifies MaximumInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MAXIMUM = 103;
    
    /** Type which identifies MeanInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MEAN = 104;
    
    /**
     * Returns the name used to identify this DefaultInstrumentManager.
     *
     * @return The name used to identify this DefaultInstrumentManager.
     */
    String getName();
    
    /**
     * Returns the description of this DefaultInstrumentManager.
     *
     * @return The description of this DefaultInstrumentManager.
     */
    String getDescription();
    
    /**
     * Returns a InstrumentableDescriptor based on its name or the name of any
     *  of its children.
     *
     * @param instrumentableName Name of the Instrumentable being requested.
     *
     * @return A Descriptor of the requested Instrumentable.
     *
     * @throws NoSuchInstrumentableException If the specified Instrumentable
     *                                       does not exist.
     */
    InstrumentableDescriptor getInstrumentableDescriptor( String instrumentableName )
        throws NoSuchInstrumentableException;

    /**
     * Returns an array of Descriptors for the Instrumentables managed by this
     *  DefaultInstrumentManager.
     *
     * @return An array of InstrumentableDescriptors.
     */
    InstrumentableDescriptor[] getInstrumentableDescriptors();
    
    /**
     * Searches the entire instrument tree for an instrumentable with the given
     *  name.
     *
     * @param instrumentableName Name of the Instrumentable being requested.
     *
     * @return A Descriptor of the requested Instrumentable.
     *
     * @throws NoSuchInstrumentableException If the specified Instrumentable
     *                                       does not exist.
     */
    InstrumentableDescriptor locateInstrumentableDescriptor( String instrumentableName )
        throws NoSuchInstrumentableException;
    
    /**
     * Searches the entire instrument tree for an instrument with the given
     *  name.
     *
     * @param instrumentName Name of the Instrument being requested.
     *
     * @return A Descriptor of the requested Instrument.
     *
     * @throws NoSuchInstrumentException If the specified Instrument does
     *                                   not exist.
     */
    InstrumentDescriptor locateInstrumentDescriptor( String instrumentName )
        throws NoSuchInstrumentException;

    /**
     * Searches the entire instrument tree for an instrument sample with the
     *  given name.
     *
     * @param sampleName Name of the Instrument Sample being requested.
     *
     * @return A Descriptor of the requested Instrument Sample.
     *
     * @throws NoSuchInstrumentSampleException If the specified Instrument
     *                                         Sample does not exist.
     */
    InstrumentSampleDescriptor locateInstrumentSampleDescriptor( String sampleName )
        throws NoSuchInstrumentSampleException;

    /**
     * Returns the stateVersion of the DefaultInstrumeManager.  The state
     *  version will be incremented each time any of the configuration of
     *  the instrument manager or any of its children is modified.
     * <p>
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrument manager.
     */
    int getStateVersion();
        
    /**
     * Invokes garbage collection.
     */
    void invokeGarbageCollection();
    
    /**
     * Returns the current number of leased samples.
     *
     * @return The current number of leased samples.
     */
    int getLeaseSampleCount();
    
    /**
     * Returns the maximum number of leased samples that will be approved.
     *
     * @return The maximum number of leased samples.
     */
    int getMaxLeasedSamples();
    
    /**
     * Returns the maximum size of a leased sample.
     *
     * @return The maximum size of a leased sample.
     */
    int getMaxLeasedSampleSize();
    
    /**
     * Returns the maximum number of milliseconds that a lease will be granted
     *  for.
     *
     * @return The maximum lease length.
     */
    long getMaxLeasedSampleLease();
}

