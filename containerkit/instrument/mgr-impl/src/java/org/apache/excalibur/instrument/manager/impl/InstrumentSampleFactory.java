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

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 * The InstrumentSample represents a single data sample in a ProfileDataSet.
 * Access to InstrumentSamples are synchronized through the ProfileDataSet.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class InstrumentSampleFactory
{
    /**
     * A Profile Sample Type loaded in from a Configuration.
     *
     * @param instrumentProxy The InstrumentProxy which owns the
     *                        InstrumentSample.
     * @param type Type of the InstrumentSample to create.
     * @param name The name of the new InstrumentSample.
     * @param interval The sample interval of the new InstrumentSample.
     * @param size The number of samples to store as history.
     * @param description The description of the new InstrumentSample.
     * @param lease Requested lease time in milliseconds.  A value of 0 implies
     *              that the lease will never expire.
     */
    static InstrumentSample getInstrumentSample( InstrumentProxy instrumentProxy,
                                                 int type,
                                                 String name,
                                                 long interval,
                                                 int size,
                                                 String description,
                                                 long lease )
    {
        switch ( type )
        {
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            return new MaximumValueInstrumentSample(
                instrumentProxy, name, interval, size, description, lease );
            
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            return new MinimumValueInstrumentSample(
                instrumentProxy, name, interval, size, description, lease );
        
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN:
            return new MeanValueInstrumentSample(
                instrumentProxy, name, interval, size, description, lease );
            
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            return new CounterInstrumentSample(
                instrumentProxy, name, interval, size, description, lease );
            
        default:
            throw new IllegalArgumentException( "'" + type + "' is not a valid sample type." );
        }
    }
}
