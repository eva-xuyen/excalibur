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

import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleListener;
import org.apache.excalibur.instrument.manager.InstrumentSampleSnapshot;

/**
 * Describes an InstrumentSample and acts as a Proxy to protect the original
 *  InstrumentSample object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
public class InstrumentSampleDescriptorImpl
    implements InstrumentSampleDescriptor
{
    /** The InstrumentSample. */
    private InstrumentSample m_instrumentSample;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentSampleDescriptor.
     *
     * @param InstrumentSample InstrumentSample being described.
     */
    InstrumentSampleDescriptorImpl( InstrumentSample InstrumentSample )
    {
        m_instrumentSample = InstrumentSample;
    }
    
    /*---------------------------------------------------------------
     * Methods InstrumentSampleDescriptor
     *-------------------------------------------------------------*/
    /**
     * Returns true if the InstrumentSample was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    public boolean isConfigured()
    {
        return m_instrumentSample.isConfigured();
    }
    
    /**
     * Returns the name of the sample.
     *
     * @return The name of the sample.
     */
    public String getName()
    {
        return m_instrumentSample.getName();
    }
    
    /**
     * Returns the sample interval.  The period of each sample in millisends.
     *
     * @return The sample interval.
     */
    public long getInterval()
    {
        return m_instrumentSample.getInterval();
    }
    
    /**
     * Returns the number of samples in the sample history.
     *
     * @return The size of the sample history.
     */
    public int getSize()
    {
        return m_instrumentSample.getSize();
    }
    
    /**
     * Returns the description of the sample.
     *
     * @return The description of the sample.
     */
    public String getDescription()
    {
        return m_instrumentSample.getDescription();
    }
    
    /**
     * Returns the type of the Instrument Sample.  Possible values include
     *  InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *  InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *  InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MEAN, or
     *  InstrumentManagerClient.INSTRUMENT_SAMPLE_TYPE_MINIMUM.
     *
     * @return The type of the Instrument Sample.
     */
    public int getType()
    {
        return m_instrumentSample.getType();
    }
    
    /**
     * Obtain the value of the sample.  All samples are integers, so the profiled
     * objects must measure quantity (numbers of items), rate (items/period), time in
     * milliseconds, etc.
     *
     * @return The sample value.
     */
    public int getValue()
    {
        return m_instrumentSample.getValue();
    }
    
    /**
     * Obtain the UNIX time of the beginning of the sample.
     *
     * @return The UNIX time of the beginning of the sample.
     */
    public long getTime()
    {
        return m_instrumentSample.getTime();
    }
    
    /**
     * Returns the Type of the Instrument which can use the sample.  This
     *  should be the same for all instances of a class.
     * <p>
     * Should be one of the following: InstrumentManager.PROFILE_POINT_TYPE_COUNTER
     *  or InstrumentManager.PROFILE_POINT_TYPE_VALUE
     *
     * @return The Type of the Instrument which can use the sample.
     */
    public int getInstrumentType()
    {
        return m_instrumentSample.getInstrumentType();
    }
    
    /**
     * Returns a reference to the descriptor of the Instrument of the sample.
     *
     * @return A reference to the descriptor of the Instrument of the sample.
     */
    public InstrumentDescriptor getInstrumentDescriptor()
    {
        return m_instrumentSample.getInstrumentProxy().getDescriptor();
    }
    
    /**
     * Registers a InstrumentSampleListener with a InstrumentSample given a name.
     *
     * @param listener The listener which should start receiving updates from the
     *                 InstrumentSample.
     */
    public void addInstrumentSampleListener( InstrumentSampleListener listener )
    {
        m_instrumentSample.addInstrumentSampleListener( listener );
    }
    
    /**
     * Unregisters a InstrumentSampleListener from a InstrumentSample given a name.
     *
     * @param listener The listener which should stop receiving updates from the
     *                 InstrumentSample.
     */
    public void removeInstrumentSampleListener( InstrumentSampleListener listener )
    {
        m_instrumentSample.removeInstrumentSampleListener( listener );
    }
    
    /**
     * Returns the time that the current lease expires.  Permanent samples will
     *  return a value of 0.
     *
     * @return The time that the current lease expires.
     */
    public long getLeaseExpirationTime()
    {
        return m_instrumentSample.getLeaseExpirationTime();
    }
    
    /**
     * Extends the lease to be lease milliseconds from the current time.
     *
     * @param lease The length of the lease in milliseconds.
     *
     * @return The new lease expiration time.  Returns 0 if the sample is
     *         permanent.
     */
    public long extendLease( long lease )
    {
        return m_instrumentSample.extendLease( lease );
    }
    
    /**
     * Obtains a static snapshot of the InstrumentSample.
     *
     * @return A static snapshot of the InstrumentSample.
     */
    public InstrumentSampleSnapshot getSnapshot()
    {
        return m_instrumentSample.getSnapshot();
    }
    
    /**
     * Returns the stateVersion of the sample.  The state version will be
     *  incremented each time any of the configuration of the sample is
     *  modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the sample.
     */
    public int getStateVersion()
    {
        return m_instrumentSample.getStateVersion();
    }
}

