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

package org.apache.excalibur.instrument;

/**
 * Because some components using Instruments will be created in large numbers
 *  a way is needed to collect data from the instances of all instances of a
 *  component class without maintaining references to Instruments of each
 *  instance.  An Instrument Manager can do this by making use of Instrument
 *  Proxies.  Each Instrument is assigned a proxy when it is registered with
 *  the manager, then all communication is made through the proxy
 * The Instrument interface must by implemented by any object wishing to act
 *  as an instrument used by the instrument manager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:28 $
 * @since 4.1
 */
public interface InstrumentProxy
{
    /**
     * Used by classes being profiles so that they can avoid unnecessary
     *  code when the data from a Instrument is not being used.
     *
     * @return True if listeners are registered with the Instrument.
     */
    boolean isActive();

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
    void increment( int count );

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
    void setValue( int value );
}
