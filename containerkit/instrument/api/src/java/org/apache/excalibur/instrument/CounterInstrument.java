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
 * CounterInstruments can be used to profile the number of times that
 *  something happens.  They are perfect for profiling things like the number
 *  of times a class instance is created or destroyed.  Or the number of
 *  times that a method is accessed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:28 $
 * @since 4.1
 */
public class CounterInstrument
    extends AbstractInstrument
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new CounterInstrument.
     *
     * @param name The name of the Instrument.  The value should be a string
     *             which does not contain spaces or periods.
     */
    public CounterInstrument( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Increments the Instrument.  This method is optimized to be extremely
     *  light weight when an InstrumentManager is not present and there are no
     *  registered CounterInstrumentListeners.
     */
    public void increment()
    {
        InstrumentProxy proxy = getInstrumentProxy();
        if( proxy != null )
        {
            proxy.increment( 1 );
        }
    }

    /**
     * Increments the Instrument by a specified count.  This method is
     *  optimized to be extremely light weight when an InstrumentManager is not
     *  present and there are no registered CounterInstrumentListeners.
     *
     * @param count A positive integer to increment the counter by.
     *
     * @throws IllegalArgumentException If the count is not positive.
     */
    public void increment( int count )
    {
        // Check the count
        if( count <= 0 )
        {
            throw new IllegalArgumentException( "Count must be a positive value." );
        }

        InstrumentProxy proxy = getInstrumentProxy();
        if( proxy != null )
        {
            proxy.increment( count );
        }
    }
}
