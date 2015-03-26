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

package org.apache.excalibur.instrument;

/**
 * The AbstractInstrument class can be used by an class wishing to implement
 *  the Instruement interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:28 $
 * @since 4.1
 */
public abstract class AbstractInstrument
    implements Instrument
{
    /** The name of the Instrument. */
    private String m_name;

    /** Proxy object used to communicate with the InstrumentManager. */
    private InstrumentProxy m_proxy;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractInstrument.
     *
     * @param name The name of the Instrument.  The value should be a string
     *             which does not contain spaces or periods.
     */
    protected AbstractInstrument( String name )
    {
        m_name = name;
    }

    /*---------------------------------------------------------------
     * Instrument Methods
     *-------------------------------------------------------------*/
    /**
     * Gets the name for the Instrument.  When an Instrumentable publishes more
     *  than one Instrument, this name makes it possible to identify each
     *  Instrument.  The value should be a string which does not contain
     *  spaces or periods.
     *
     * @return The name of the Instrument.
     */
    public String getInstrumentName()
    {
        return m_name;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * When the InstrumentManager is present, an InstrumentProxy will be set
     *  to enable the Instrument to communicate with the InstrumentManager.
     *  Once the InstrumentProxy is set, it should never be changed or set
     *  back to null.  This restriction removes the need for synchronization
     *  within the Instrument classes.  Which in turn makes them more
     *  efficient.
     *
     * @param proxy Proxy object used to communicate with the
     *              InstrumentManager.
     */
    public void setInstrumentProxy( InstrumentProxy proxy )
    {
        if( m_proxy != null )
        {
            throw new IllegalStateException(
                "Once an InstrumentProxy has been set, it can not be changed." );
        }
        m_proxy = proxy;
    }

    /**
     * Used by classes being profiled so that they can avoid unnecessary
     *  code when the data from an Instrument is not being used.
     *
     * @return True if an InstrumentProxy has been set and is active.
     */
    public boolean isActive()
    {
        return ( m_proxy != null ) && ( m_proxy.isActive() );
    }

    /**
     * Returns the InstrumentProxy object assigned to the instrument by the
     *  InstrumentManager.
     *
     * @return Proxy object used to communicate with the InstrumentManager.
     */
    protected InstrumentProxy getInstrumentProxy()
    {
        return m_proxy;
    }
}
