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

public interface InstrumentData
    extends ElementData
{
    /** Type which specifies that the type of a Instrument has not yet been determined. */
    int INSTRUMENT_TYPE_NONE = 0;
    
    /** Type which identifies CounterInstruments. */
    int INSTRUMENT_TYPE_COUNTER = 1;
    
    /** Type which identifies ValueInstruments. */
    int INSTRUMENT_TYPE_VALUE   = 2;
    
    /**
     * Returns the registered flag of the remote object.
     *
     * @return The registered flag of the remote object.
     */
    boolean isRegistered();
    
    /**
     * Returns the type of the Instrument.  Possible values include
     *  InstrumentData.INSTRUMENT_TYPE_COUNTER,
     *  InstrumentData.INSTRUMENT_TYPE_VALUE or
     *  InstrumentData.INSTRUMENT_TYPE_NONE, if the type was never set.
     *
     * @return The type of the Instrument.
     */
    int getType();
    
    /**
     * Returns an array of the Instrument Samples assigned to the Instrument.
     *
     * @return An array of Instrument Samples.
     */
    InstrumentSampleData[] getInstrumentSamples();
    
    /**
     * Requests that a sample be created or that its lease be updated.
     *
     * @param description Description to assign to the new sample.
     * @param interval Sample interval of the new sample.
     * @param sampleCount Number of samples in the new sample.
     * @param leaseTime Requested lease time.  The server may not grant the full lease.
     * @param sampleType The type of sample to be created.
     *
     * @return True if successful.
     */
    boolean createInstrumentSample( String description,
                                    long interval,
                                    int sampleCount,
                                    long leaseTime,
                                    int sampleType );
}
