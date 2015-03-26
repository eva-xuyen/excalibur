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

package org.apache.excalibur.instrument.test;

import org.apache.excalibur.instrument.AbstractInstrumentable;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * Test Implementation of an AbstractInstrumentable.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public class AbstractInstrumentableImpl
    extends AbstractInstrumentable
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public AbstractInstrumentableImpl( String name )
    {
        setInstrumentableName( name );
    }
    
    /*---------------------------------------------------------------
     * AbstractInstrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Adds an Instrument to the list of Instruments published by the component.
     *  This method may not be called after the Instrumentable has been
     *  registered with the InstrumentManager.
     *
     * @param instrument Instrument to publish.
     */
    public void addInstrument( Instrument instrument )
    {
        // Make this method public for testing.
        super.addInstrument( instrument );
    }

    /**
     * Adds a child Instrumentable to the list of child Instrumentables
     *  published by the component.  This method may not be called after the
     *  Instrumentable has been registered with the InstrumentManager.
     * <p>
     * Note that Child Instrumentables must be named by the caller using the
     *  setInstrumentableName method.
     *
     * @param child Child Instrumentable to publish.
     */
    public void addChildInstrumentable( Instrumentable child )
    {
        // Make this method public for testing.
        super.addChildInstrumentable( child );
    }
}
