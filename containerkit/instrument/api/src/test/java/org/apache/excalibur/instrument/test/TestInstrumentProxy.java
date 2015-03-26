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

package org.apache.excalibur.instrument.test;

import org.apache.excalibur.instrument.InstrumentProxy;

/**
 * Dummy InstrumentProxy used to test instruments.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public class TestInstrumentProxy
    implements InstrumentProxy
{
    private boolean m_active;
    private int m_value;
    
    /*---------------------------------------------------------------
     * InstrumentProxy Methods
     *-------------------------------------------------------------*/
    /**
     * Used by classes being profiles so that they can avoid unnecessary
     *  code when the data from a Instrument is not being used.
     *
     * @return True if listeners are registered with the Instrument.
     */
    public boolean isActive()
    {
        return m_active;
    }
    
    /**
     * Increments the Instrument by a specified count.  This method should be
     *  optimized to be extremely light weight when there are no registered
     *  CounterInstrumentListeners.
     * <p>
     * This method may throw an IllegalStateException if the proxy is not meant
     *  to handle calls to increment.
     *
     * @param count A positive integer to increment the counter by.
     */
    public void increment( int count )
    {
        m_value += count;
    }
    
    /**
     * Sets the current value of the Instrument.  This method is optimized
     *  to be extremely light weight when there are no registered
     *  ValueInstrumentListeners.
     * <p>
     * This method may throw an IllegalStateException if the proxy is not meant
     *  to handle calls to setValue.
     *
     * @param value The new value for the Instrument.
     */
    public void setValue( int value )
    {
        m_value = value;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the activate flag on the proxy so that it will collect information.
     */
    public void activate()
    {
        m_active = true;
    }
    
    /**
     **/
    public int getValue()
    {
        return m_value;
    }
}
