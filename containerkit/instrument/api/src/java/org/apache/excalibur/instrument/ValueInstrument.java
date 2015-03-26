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
 * Objects implementing Instrumentable can create Instruments with integer
 *  values using a ValueInstrument.  ValueInstruments are perfect for
 *  profiling things like system memory, or the size of a pool or cache.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:28 $
 * @since 4.1
 */
public class ValueInstrument
    extends AbstractInstrument
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new ValueInstrument.
     *
     * @param name The name of the Instrument.  The value should be a string
     *             which does not contain spaces or periods.
     */
    public ValueInstrument( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the current value of the Instrument.  This method is optimized
     *  to be extremely light weight when an InstrumentManager is not present
     *  and there are no registered ValueInstrumentListeners.
     * <p>
     * Note that in many cases is best to call this method even if the
     *  isActive() method returns false.  This is because the InstrumentManager
     *  will remember the last value set and use it if the instrument ever
     *  becomes active.  For things like pool sizes which do not change often,
     *  this behavior is critical so that users can begin monitoring the value
     *  and see what it was before they connected.   Setting the value is
     *  very light weight, but its calculation may not be.  It is up to the
     *  user to weigh the benefits and consequences one way or the other.
     *
     * @param value The new value for the Instrument.
     */
    public void setValue( int value )
    {
        InstrumentProxy proxy = getInstrumentProxy();
        if( proxy != null )
        {
            proxy.setValue( value );
        }
    }
}
