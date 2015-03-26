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
import org.apache.avalon.framework.logger.LogEnabled;

import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleListener;
import org.apache.excalibur.instrument.manager.InstrumentSampleSnapshot;

/**
 * InstrumentSamples are used to provide an Instrument with state.  Samples
 *  have a sample interval, which is the period over which data is grouped.
 * <p>
 * InstrmentSamples can be created when the InstrumentManager is created as
 *  part of the configuration process, or as a result of a request from an
 *  InstrumentClient.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
interface InstrumentSample
    extends LogEnabled
{
    /**
     * Returns the InstrumentProxy which owns the InstrumentSample.
     *
     * @return The InstrumentProxy which owns the InstrumentSample.
     */
    InstrumentProxy getInstrumentProxy();
    
    /**
     * Returns true if the InstrumentSample was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    boolean isConfigured();
    
    /**
     * Returns the name of the sample.
     *
     * @return The name of the sample.
     */
    String getName();
    
    /**
     * Returns the sample interval.  The period of each sample in millisends.
     *
     * @return The sample interval.
     */
    long getInterval();
    
    /**
     * Returns the number of samples in the sample history.
     *
     * @return The size of the sample history.
     */
    int getSize();
    
    /**
     * Returns the description of the sample.
     *
     * @return The description of the sample.
     */
    String getDescription();
    
    /**
     * Returns the type of the Instrument Sample.  Possible values include
     *  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN, or
     *  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM.
     *
     * @return The type of the Instrument Sample.
     */
    int getType();
    
    /**
     * Returns a Descriptor for the InstrumentSample.
     *
     * @return A Descriptor for the InstrumentSample.
     */
    InstrumentSampleDescriptor getDescriptor();
    
    /**
     * Obtain the value of the sample.  All samples are integers, so the profiled
     * objects must measure quantity (numbers of items), rate (items/period), time in
     * milliseconds, etc.
     *
     * @return The sample value.
     */
    int getValue();
    
    /**
     * Obtain the UNIX time of the beginning of the sample.
     *
     * @return The UNIX time of the beginning of the sample.
     */
    long getTime();
    
    /**
     * Returns the Type of the Instrument which can use the sample.  This
     *  should be the same for all instances of a class.
     * <p>
     * Should be one of the following:
     *  DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER
     *  or DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE
     *
     * @return The Type of the Instrument which can use the sample.
     */
    int getInstrumentType();
    
    /**
     * Returns the time that the current lease expires.  Permanent samples will
     *  return a value of 0.
     *
     * @return The time that the current lease expires.
     */
    long getLeaseExpirationTime();
    
    /**
     * Extends the lease to be lease milliseconds from the current time.
     *
     * @param lease The length of the lease in milliseconds.
     *
     * @return The new lease expiration time.  Returns 0 if the sample is
     *         permanent.
     */
    long extendLease( long lease );
    
    /**
     * Tells the sample that its lease has expired.  No new references to
     *  the sample will be made available, but clients which already have
     *  access to the sample may continue to use it.
     */
    void expire();
    
    /**
     * Obtains a static snapshot of the InstrumentSample.
     *
     * @return A static snapshot of the InstrumentSample.
     */
    InstrumentSampleSnapshot getSnapshot();
    
    /**
     * Returns the stateVersion of the sample.  The state version will be
     *  incremented each time any of the configuration of the sample is
     *  modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the sample.
     */
    int getStateVersion();
    
    /**
     * Registers a InstrumentSampleListener with a InstrumentSample given a name.
     *
     * @param listener The listener which should start receiving updates from the
     *                 InstrumentSample.
     */
    void addInstrumentSampleListener( InstrumentSampleListener listener );
    
    /**
     * Unregisters a InstrumentSampleListener from a InstrumentSample given a name.
     *
     * @param listener The listener which should stop receiving updates from the
     *                 InstrumentSample.
     */
    void removeInstrumentSampleListener( InstrumentSampleListener listener );
    
    /**
     * Writes the current state to a PrintWriter as XML.
     *
     * @param out The PrintWriter to which the state should be written.
     */
    void writeState( PrintWriter out );
    
    /**
     * Loads the state into the InstrumentSample.
     *
     * @param state Configuration object to load state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    void loadState( Configuration state ) throws ConfigurationException;
}
