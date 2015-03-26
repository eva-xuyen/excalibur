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

package org.apache.excalibur.instrument.client;

public interface InstrumentSampleElementData
    extends ElementData
{
    /** Type which identifies CounterInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_COUNTER = 101;
    
    /** Type which identifies MinimumInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MINIMUM = 102;
    
    /** Type which identifies MaximumInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MAXIMUM = 103;
    
    /** Type which identifies MeanInstrumentSamples. */
    int INSTRUMENT_SAMPLE_TYPE_MEAN = 104;
    
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
     * Returns the type of the Instrument Sample.  Possible values include
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MEAN, or
     *  InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MINIMUM.
     *
     * @return The type of the Instrument Sample.
     */
    int getType();
    
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
     * Obtain the UNIX time when the lease expires.
     *
     * @return The UNIX time when the lease expires.
     */
    long getLeaseExpirationTime();
    
    /**
     * Returns the Type of the Instrument which can use the sample.  This
     *  should be the same for all instances of a class.
     * <p>
     * Should be one of the following: InstrumentData.PROFILE_POINT_TYPE_COUNTER
     *  or InstrumentData.PROFILE_POINT_TYPE_VALUE
     *
     * @return The Type of the Instrument which can use the sample.
     */
    int getInstrumentType();
}