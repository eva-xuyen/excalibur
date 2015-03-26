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

public interface InstrumentManagerData
    extends Data
{
    /**
     * Returns the name.
     *
     * @return The name.
     */
    String getName();
    
    /**
     * Gets a thread-safe snapshot of the instrumentable list.
     *
     * @return A thread-safe snapshot of the instrumentable list.
     */
    InstrumentableData[] getInstrumentables();
    
    /**
     * Causes the the entire instrument tree to be updated in one call.  Very fast
     *  when it is known that all or most data has changed.
     *
     * @return true if successful.
     */
    boolean updateAll();
    
    /**
     * Requests that a sample be created or that its lease be updated.
     *
     * @param instrumentName The full name of the instrument whose sample is
     *                       to be created or updated.
     * @param description Description to assign to the new sample.
     * @param interval Sample interval of the new sample.
     * @param sampleCount Number of samples in the new sample.
     * @param leaseTime Requested lease time.  The server may not grant the
     *                  full lease.
     * @param sampleType The type of sample to be created.
     */
    void createInstrumentSample( String instrumentName,
                                 String description,
                                 long interval,
                                 int sampleCount,
                                 long leaseTime,
                                 int sampleType );
    
    /**
     * Requests that a set of samples be created or that their leases be
     *  updated.  All array parameters must be of the same length.
     *
     * @param instrumentNames The full names of the instruments whose sample
     *                        are to be created or updated.
     * @param descriptions Descriptions to assign to the new samples.
     * @param intervals Sample intervals of the new samples.
     * @param sampleCounts Number of samples in each the new samples.
     * @param leaseTimes Requested lease times.  The server may not grant the
     *                   full leases.
     * @param sampleTypes The types of samples to be created.
     */
    void createInstrumentSamples( String[] instrumentNames,
                                  String[] descriptions,
                                  long[] intervals,
                                  int[] sampleCounts,
                                  long[] leaseTimes,
                                  int[] sampleTypes );
}
