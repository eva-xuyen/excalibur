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

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 * A InstrumentSample which stores the minimum value set during the sample
 *  period.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class MinimumValueInstrumentSample
    extends AbstractValueInstrumentSample
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new MinimumValueInstrumentSample
     *
     * @param instrumentProxy The InstrumentProxy which owns the
     *                        InstrumentSample.
     * @param name The name of the new InstrumentSample.
     * @param interval The sample interval of the new InstrumentSample.
     * @param size The number of samples to store as history.  Assumes that size is at least 1.
     * @param description The description of the new InstrumentSample.
     * @param lease The length of the lease in milliseconds.
     */
    MinimumValueInstrumentSample( InstrumentProxy instrumentProxy,
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
        return DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM;
    }

    /*---------------------------------------------------------------
     * AbstractValueInstrumentSample Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the current value of the sample.  The value will be set as the
     *  sample value if it is the smallest value seen during the sample period.
     *
     * @param value New sample value.
     * @param time Time that the new sample arrives.
     */
    protected void setValueInner( int value, long time )
    {
        boolean update;
        int sampleValue;
        long sampleTime;

        synchronized(this)
        {
            update = this.update( time, false );

            // Always store the last value to use for samples where a value is not set.
            this.m_lastValue = value;

            if ( this.m_valueCount > 0 )
            {
                // Additional sample
                this.m_valueCount++;
                if ( value < this.m_value )
                {
                    this.m_value = value;
                    update = true;
                }
            }
            else
            {
                // First value of this sample.
                this.m_valueCount = 1;
                this.m_value = value;
            }

            sampleValue = this.m_value;
            sampleTime = this.m_time;
                update = true;
        }

        if ( update )
        {
            this.updateListeners( sampleValue, sampleTime );
        }
    }
}
