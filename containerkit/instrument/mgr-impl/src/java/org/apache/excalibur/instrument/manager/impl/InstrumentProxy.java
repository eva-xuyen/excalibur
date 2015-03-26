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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.instrument.manager.CounterInstrumentListener;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentListener;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleUtils;
import org.apache.excalibur.instrument.manager.ValueInstrumentListener;

/**
 * Instrumentables which do not implement ThreadSafe may have multiple instances
 *  created by the ComponentLocator.  Each of these Instruments will share
 *  a common key and are instrumented as a group.  The InstrumentProxy is used
 *  make it easy for the InstrumentManager to control groups of Instruments
 *  as one.
 * <p>
 * The type of a Instrument can not be determined at configuration time.
 *  It is resolved when the Instrumentable actually registers the Instrument.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
public class InstrumentProxy
    extends AbstractLogEnabled
    implements org.apache.excalibur.instrument.InstrumentProxy, Configurable
{
    /** The InstrumentableProxy which owns the InstrumentProxy. */
    private InstrumentableProxy m_instrumentableProxy;
    
    /** Configured flag. */
    private boolean m_configured;
    
    /** Registered flag. */
    private boolean m_registered;
    
    /** The name used to identify a Instrument. */
    private String m_name;
    
    /** The description of the Instrumente. */
    private String m_description;
    
    /** The Descriptor for the Instrument. */
    private InstrumentDescriptor m_descriptor;
    
    /** Type of the Instrument */
    private int m_type;
    
    /** Array of registered Counter/ValueInstrumentListeners. */
    private InstrumentListener[] m_listeners;
    
    /** Map of the maintained InstrumentSamples. */
    private HashMap m_samples = new HashMap();
    
    /** Optimized array of the InstrumentSamples. */
    private InstrumentSample[] m_sampleArray;
    
    /** Optimized array of the InstrumentSampleDescriptors. */
    private InstrumentSampleDescriptor[] m_sampleDescriptorArray;
    
    /** Child logger to use for logging of new values. */
    private Logger m_valueLogger;
    
    /** The most recent value set if this is a value instrument. */
    private int m_lastValue;
    
    /** State Version. */
    private int m_stateVersion;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentProxy.
     *
     * @param instrumentableProxy The InstrumentableProxy which owns the
     *                            InstrumentProxy.
     * @param name The name used to identify a Instrumentable.
     * @param description The description of the the Instrumentable.
     */
    InstrumentProxy( InstrumentableProxy instrumentableProxy,
                     String name,
                     String description )
    {
        m_instrumentableProxy = instrumentableProxy;
        m_name = name;
        m_description = description;
        
        // Create the descriptor
        m_descriptor = new InstrumentDescriptorImpl( this );
    }
    
    /*---------------------------------------------------------------
     * LogEnabled Methods
     *-------------------------------------------------------------*/
    public void enableLogging( Logger logger )
    {
        super.enableLogging( logger );
        
        // Create a child logger for logging setValue and increment calls so
        //  that they can be filtered out.
        m_valueLogger = logger.getChildLogger( "values" );
    }
    
    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Configures the Instrument.  Called from the InstrumentManager's
     *  configure method.  The class does not need to be configured to
     *  function correctly.
     *
     * @param configuration Instrument configuration element from the
     *                      InstrumentManager's configuration.
     *
     * @throws ConfigurationException If there are any configuration problems.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        synchronized(this)
        {
            // The description is optional.  Default to the description from the constructor.
            m_description = configuration.getAttribute( "description", m_description );
            
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Configuring Instrument: " + m_name + " as \"" +
                    m_description + "\"" );
            }
            
            m_configured = true;
            
            // Configure any Samples
            Configuration[] sampleConfs = configuration.getChildren( "sample" );
            for ( int i = 0; i < sampleConfs.length; i++ )
            {
                Configuration sampleConf = sampleConfs[i];
                
                int sampleType = InstrumentSampleUtils.resolveInstrumentSampleType(
                    sampleConf.getAttribute( "type" ) );
                long sampleInterval = sampleConf.getAttributeAsLong( "interval" );
                int sampleSize = sampleConf.getAttributeAsInteger( "size", 1 );
                
                // Build the sample name from its attributes.  This makes it
                //  possible to avoid forcing the user to maintain a name as well.
                String sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
                    m_name, sampleType, sampleInterval, sampleSize );
                
                String defaultDescription = InstrumentSampleUtils.generateInstrumentSampleName(
                    sampleType, sampleInterval, sampleSize );
                String sampleDescription =
                    sampleConf.getAttribute( "description", defaultDescription );
                
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Configuring InstrumentSample: " + sampleName + 
                        " as \"" + sampleDescription + "\"" );
                }
                
                // See if the sample already exists.
                String fullSampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
                    m_name, sampleType, sampleInterval, sampleSize );
                InstrumentSample sample = getInstrumentSample( fullSampleName );
                if ( sample == null )
                {
                    AbstractInstrumentSample instrumentSample = 
                        (AbstractInstrumentSample)InstrumentSampleFactory.getInstrumentSample( this,
                        sampleType, sampleName, sampleInterval, sampleSize, sampleDescription, 0 );
                    instrumentSample.enableLogging( getLogger() );
                    instrumentSample.setConfigured();
                                
                    addInstrumentSample( instrumentSample );
                }
                else
                {
                    // Sample already existed.
                    if ( sample instanceof AbstractInstrumentSample )
                    {
                        AbstractInstrumentSample instrumentSample =
                            (AbstractInstrumentSample)sample;
                        instrumentSample.setConfigured();
                        // If the sample already existed we will need to make it permanent.
                        instrumentSample.makePermanent();
                    }
                    else
                    {
                        // Should never happen with normal operation but an error is better than
                        //  silently failing.
                        getLogger().warn(
                            "Instrument sample was configured but already existed and "
                            + "could not be cast to an AbstractInstrumentSample.  "
                            + "Name: " + fullSampleName + ", Class: "
                            + sample.getClass().getName() );
                    }
                }
            }
        }
    }
    
    /*---------------------------------------------------------------
     * InstrumentProxy Methods
     *-------------------------------------------------------------*/
    /**
     * Used by classes being instrumented so that they can avoid unnecessary
     *  code when the data from a Instrument is not being used.
     *
     * @returns True if listeners are registered with the Instrument.
     */
    public boolean isActive() {
        return m_listeners != null;
    }
    
    /**
     * Increments the Instrument by a specified count.  This method should be
     *  optimized to be extremely light weight when there are no registered
     *  CounterInstrumentListeners.
     *
     * @param count A positive integer to increment the counter by.
     */
    public void increment( int count )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle CounterInstruments." );
        }
        
        // Get a local reference to the listeners, so that synchronization can be avoided.
        InstrumentListener[] listeners = m_listeners;
        if ( listeners != null )
        {
            if ( m_valueLogger.isDebugEnabled() )
            {
                m_valueLogger.debug( "increment() called for Instrument, " + m_name );
            }
            
            long time = System.currentTimeMillis();
            for ( int i = 0; i < listeners.length; i++ )
            {
                CounterInstrumentListener listener =
                    (CounterInstrumentListener)listeners[i];
                listener.increment( getName(), count, time );
            }
        }
    }
    
    /**
     * Sets the current value of the Instrument.  This method is optimized
     *  to be extremely light weight when there are no registered
     *  ValueInstrumentListeners.
     *
     * @param value The new value for the Instrument.
     */
    public void setValue( int value )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle ValueInstruments." );
        }
        
        // Store the most recent value so that new listeners can be informed
        //  of the current value when they register.  ints are single memory
        //  locations, so synchronization is not needed here.
        m_lastValue = value;
        
        // Get a local reference to the listeners, so that synchronization can be avoided.
        InstrumentListener[] listeners = m_listeners;
        if ( listeners != null )
        {
            if ( m_valueLogger.isDebugEnabled() )
            {
                m_valueLogger.debug( "setValue( " + value + " ) called for Instrument, " + m_name );
            }
            
            long time = System.currentTimeMillis();
            for ( int i = 0; i < listeners.length; i++ )
            {
                ValueInstrumentListener listener =
                    (ValueInstrumentListener)listeners[i];
                listener.setValue( getName(), value, time );
            }
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the InstrumentableProxy which owns the InstrumentProxy.
     *
     * @return The InstrumentableProxy which owns the InstrumentProxy.
     */
    InstrumentableProxy getInstrumentableProxy()
    {
        return m_instrumentableProxy;
    }
    
    /**
     * Returns true if the Instrument was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    boolean isConfigured()
    {
        return m_configured;
    }

    /**
     * Returns true if the Instrument was registered with the Instrument
     *  Manager.
     *
     * @return True if registered.
     */
    boolean isRegistered()
    {
        return m_registered;
    }
    
    /**
     * Called by the InstrumentManager whenever an Instrument assigned to
     *  this proxy is registered.
     */
    void setRegistered()
    {
        if ( !m_registered )
        {
            m_registered = true;
            stateChanged();
        }
    }
    
    /**
     * Gets the name for the Instrument.  The Instrument Name is used to
     *  uniquely identify the Instrument during configuration and to gain
     *  access to a InstrumentDescriptor through an InstrumentManager.
     *
     * @return The name used to identify a Instrumentable.
     */
    String getName() 
    {
        return m_name;
    }
    
    /**
     * Sets the description for the Instrument.  This description will
     *  be set during configuration if a configuration exists for this
     *  Instrument.
     *
     * @param description The description of the Instrument.
     */
    void setDescription( String description )
    {
        String oldDescription = m_description; // thread safety.
        if ( ( oldDescription == description ) || ( ( description != null ) && description.equals( oldDescription ) ) )
        {
            // No change
        }
        else
        {
            m_description = description;
            stateChanged();
        }
    }
    
    /**
     * Gets the description of the Instrument.
     *
     * @return The description of the Instrument.
     */
    String getDescription()
    {
        return m_description;
    }
    
    /**
     * Returns a Descriptor for the Instrument.
     *
     * @return A Descriptor for the Instrument.
     */
    InstrumentDescriptor getDescriptor()
    {
        return m_descriptor;
    }
    
    /**
     * Set the type of the Instrument.  Once set, the type can not be changed.
     *
     * @param type Type of the Instrument.
     */
    void setType( int type )
    {
        synchronized(this)
        {
            if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_NONE )
            {
                throw new IllegalStateException( "Type already set." );
            }
            switch ( type )
            {
            case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
            case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
                m_type = type;
                break;
            default:
                throw new IllegalStateException( type + " is not a valid type." );
            }
        }
    }
    
    /**
     * Returns the type of the Instrument.
     *
     * @return The type of the Instrument.
     */
    int getType()
    {
        return m_type;
    }
    
    /**
     * Adds a CounterInstrumentListener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener CounterInstrumentListener which will start receiving
     *                 updates.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.INSTRUMENT_TYPE_COUNTER.
     */
    void addCounterInstrumentListener( CounterInstrumentListener listener )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle CounterInstruments." );
        }
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "A CounterInstrumentListener was added to Instrument, " +
                m_name + " : " + listener.getClass().getName() );
        }
        
        addInstrumentListener( listener );
    }
    
    /**
     * Removes a InstrumentListener from the list of listeners which will
     *  receive events.
     *
     * @param listener InstrumentListener which will stop receiving events.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.INSTRUMENT_TYPE_COUNTER.
     */
    void removeCounterInstrumentListener( CounterInstrumentListener listener )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle CounterInstruments." );
        }
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "A CounterInstrumentListener was removed from Instrument, " + 
                m_name + " : " + listener.getClass().getName() );
        }
        
        removeInstrumentListener( listener );
    }
    
    /**
     * Adds a ValueInstrumentListener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener ValueInstrumentListener which will start receiving
     *                 updates.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.INSTRUMENT_TYPE_VALUE.
     */
    void addValueInstrumentListener( ValueInstrumentListener listener )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle ValueInstruments." );
        }
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "A ValueInstrumentListener was added to Instrument, " + m_name +
                " : " + listener.getClass().getName() );
        }
        
        addInstrumentListener( listener );
        
        // Inform the new listener of the current value
        long time = System.currentTimeMillis();
        listener.setValue( getName(), m_lastValue, time );
    }
    
    /**
     * Removes a InstrumentListener from the list of listeners which will
     *  receive events.
     *
     * @param listener InstrumentListener which will stop receiving events.
     *
     * @throws IllegalStateException If the Instrument's type is not
     *         InstrumentManager.INSTRUMENT_TYPE_VALUE.
     */
    void removeValueInstrumentListener( ValueInstrumentListener listener )
    {
        if ( m_type != DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE )
        {
            // Type is not correct.
            throw new IllegalStateException(
                "The proxy is not configured to handle ValueInstruments." );
        }
        
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "A ValueInstrumentListener was removed from Instrument, " + m_name +
                " : " + listener.getClass().getName() );
        }
        
        removeInstrumentListener( listener );
    }
    
    /**
     * Add a InstrumentSample to the Instrument.
     *
     * @param InstrumentSample InstrumentSample to be added.
     */
    private void addInstrumentSample( InstrumentSample instrumentSample )
    {
        synchronized(this)
        {
            // If the type has not been set, set it.  If it has been set, make sure this sample has
            //  the same type.
            if ( m_type == DefaultInstrumentManager.INSTRUMENT_TYPE_NONE )
            {
                setType( instrumentSample.getInstrumentType() );
            }
            else if ( m_type != instrumentSample.getInstrumentType() )
            {
                // The type is different.
                throw new IllegalStateException( "The sample '" + instrumentSample.getName() + 
                    "' had its type set to " + getTypeName( m_type ) + 
                    " by another sample.  This sample has a type of " + 
                    getTypeName( instrumentSample.getInstrumentType() ) + " and is not compatible." );
            }
            
            // Make sure that a sample with the same name has not already been set.
            String sampleName = instrumentSample.getName();
            if ( m_samples.get( sampleName ) != null )
            {
                throw new IllegalStateException( "More than one sample with the same name, '" +
                    sampleName + "', can not be configured." );
            }
        
            // Sample is safe to add.
            m_samples.put( sampleName, instrumentSample );
            
            // Clear the optimized arrays
            m_sampleArray = null;
            m_sampleDescriptorArray = null;
            
            // Add the sample as a listener for this Instrument.
            switch ( m_type )
            {
            case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
                addCounterInstrumentListener( (CounterInstrumentSample)instrumentSample );
                break;
                
            case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
                addValueInstrumentListener( (AbstractValueInstrumentSample)instrumentSample );
                break;
                
            default:
                throw new IllegalStateException(
                    "Don't know how to deal with the type: " + m_type );
            }
        }
        
        if ( instrumentSample.getLeaseExpirationTime() == 0 )
        {
            m_instrumentableProxy.getInstrumentManager().incrementPermanentSampleCount();
        }
        else
        {
            m_instrumentableProxy.getInstrumentManager().incrementLeasedSampleCount();
        }
        
        stateChanged();
    }

    /**
     * Removes an InstrumentSample from the Instrument.
     *
     * @param InstrumentSample InstrumentSample to be removed.
     */
    void removeInstrumentSample( InstrumentSample instrumentSample )
    {
        synchronized(this)
        {
            // Remove the sample from the listener list for this Instrument.
            switch ( m_type )
            {
            case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
                removeCounterInstrumentListener( (CounterInstrumentSample)instrumentSample );
                break;
                
            case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
                removeValueInstrumentListener( (AbstractValueInstrumentSample)instrumentSample );
                break;
                
            default:
                throw new IllegalStateException(
                    "Don't know how to deal with the type: " + m_type );
            }
            
            // Remove the sample.
            m_samples.remove( instrumentSample.getName() );
            
            // Clear the optimized arrays
            m_sampleArray = null;
            m_sampleDescriptorArray = null;
        }
        
        m_instrumentableProxy.getInstrumentManager().decrementLeasedSampleCount();
        
        stateChanged();
    }
    
    /**
     * Returns a InstrumentSample based on its name.
     *
     * @param InstrumentSampleName Name of the InstrumentSample being requested.
     *
     * @return The requested InstrumentSample or null if does not exist.
     */
    InstrumentSample getInstrumentSample( String InstrumentSampleName )
    {
        synchronized(this)
        {
            return (InstrumentSample)m_samples.get( InstrumentSampleName );
        }
    }
    
    /**
     * Returns an array of the InstrumentSamples in the Instrument.
     *
     * @return An array of the InstrumentSamples in the Instrument.
     */
    InstrumentSample[] getInstrumentSamples()
    {
        InstrumentSample[] samples = m_sampleArray;
        if ( samples == null )
        {
            samples = updateInstrumentSampleArray();
        }
        return samples;
    }
    
    /**
     * Returns an InstrumentSampleDescriptor based on its name.  If the
     *  requested sample is invalid in any way, then an expired Descriptor
     *  will be returned.
     *
     * @param sampleDescription Description to assign to the new Sample.
     * @param sampleInterval Sample interval to use in the new Sample.
     * @param sampleLease Requested lease time for the new Sample in
     *                    milliseconds.  The InstrumentManager may grant a
     *                    lease which is shorter or longer than the requested
     *                    period.
     * @param sampleType Type of sample to request.  Must be one of the
     *                   following:  DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM,
     *                   DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN.
     *
     * @return The requested InstrumentSample.
     *
     * @throws NoSuchInstrumentSampleException If the specified InstrumentSample
     *                                      does not exist.
     */
    InstrumentSample createInstrumentSample( String sampleDescription,
                                             long sampleInterval,
                                             int sampleSize,
                                             long sampleLease,
                                             int sampleType )
    {
        getLogger().debug( "Create new sample for " + m_name + ": interval=" + sampleInterval +
            ", size=" + sampleSize + ", lease=" + sampleLease + ", type=" +
            InstrumentSampleUtils.getInstrumentSampleTypeName( sampleType ) );
        
        DefaultInstrumentManagerImpl manager = m_instrumentableProxy.getInstrumentManager();
        
        // Make sure the sampleSize is valid.
        sampleSize = Math.max( 1, Math.min( sampleSize, manager.getMaxLeasedSampleSize() ) );
        
        // Generate a name for the new sample
        String sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
            m_name, sampleType, sampleInterval, sampleSize );
        
        InstrumentSample instrumentSample;
        synchronized( this )
        {
            // It is possible that the requested sample already exists.
            instrumentSample = getInstrumentSample( sampleName );
            if ( instrumentSample != null )
            {
                // The requested sample already exists.
                instrumentSample.extendLease( sampleLease );
            }
            else
            {
                // The new sample needs to be created.
                
                // Make sure the lease is valid.
                boolean expired = false;
                sampleLease =
                    Math.max( 1, Math.min( sampleLease, manager.getMaxLeasedSampleLease() ) );
                if ( ( manager.getLeaseSampleCount() >= manager.getMaxLeasedSamples() )
                    || ( sampleLease == 1 ) )
                {
                    // Unable to grant any more leases.  We always need to return a lease
                    //  object, but return one which expires immediately.
                    sampleLease = 1;
                    expired = true;
                }
                
                instrumentSample = InstrumentSampleFactory.getInstrumentSample(
                    this, sampleType, sampleName, sampleInterval, sampleSize,
                    sampleDescription, sampleLease );
                instrumentSample.enableLogging( getLogger() );
                
                // Do not bother registering expired dummy leases.
                if ( !expired )
                {
                    m_instrumentableProxy.getInstrumentManager().incrementLeaseRequests();
                    
                    addInstrumentSample( instrumentSample );
                    
                    // Register the new sample with the InstrumentManager
                    getInstrumentableProxy().getInstrumentManager().
                        registerLeasedInstrumentSample( instrumentSample );
                }
            }
        }
        
        return instrumentSample;
    }
    
    /**
     * Returns an array of Descriptors for the InstrumentSamples in the
     *  Instrument.
     *
     * @return An array of Descriptors for the InstrumentSamples in the
     *         Instrument.
     */
    InstrumentSampleDescriptor[] getInstrumentSampleDescriptors()
    {
        InstrumentSampleDescriptor[] descriptors = m_sampleDescriptorArray;
        if ( descriptors == null )
        {
            descriptors = updateInstrumentSampleDescriptorArray();
        }
        return descriptors;
    }
    
    /**
     * Returns the stateVersion of the instrument.  The state version will be
     *  incremented each time any of the configuration of the instrument or
     *  any of its children is modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrument.
     */
    int getStateVersion()
    {
        return m_stateVersion;
    }
    
    /**
     * Common code to add a listener to the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener InstrumentListener which will start receiving
     *                 updates.
     */
    private void addInstrumentListener( InstrumentListener listener )
    {
        synchronized(this)
        {
            // Store the listeners in an array.  This makes it possible to
            //  avoid synchronization while propagating events.  Never change
            //  the contents of the listener array once it has been set to the
            //  m_listeners field.
            InstrumentListener[] oldListeners = m_listeners;
            InstrumentListener[] newListeners;
            if ( oldListeners == null )
            {
                newListeners = new InstrumentListener[] { listener };
            }
            else
            {
                newListeners = new InstrumentListener[ oldListeners.length + 1 ];
                System.arraycopy( oldListeners, 0, newListeners, 0, oldListeners.length );
                newListeners[ oldListeners.length ] = listener;
            }
            
            // Update the m_listeners field.
            m_listeners = newListeners;
        }
    }
    
    /**
     * Common code to remove a listener from the list of listeners which will
     *  receive updates of the value of the Instrument.
     *
     * @param listener InstrumentListener which will stop receiving
     *                 updates.
     */
    private void removeInstrumentListener( InstrumentListener listener )
    {
        synchronized(this)
        {
            // Store the listeners in an array.  This makes it possible to
            //  avoid synchronization while propagating events.  Never change
            //  the contents of the listener array once it has been set to the
            //  m_listeners field.
            InstrumentListener[] oldListeners = m_listeners;
            InstrumentListener[] newListeners;
            if ( oldListeners == null )
            {
                // Means that this method should not have been called, but
                //  don't throw an error.
                newListeners = null;
            }
            else if ( oldListeners.length == 1 )
            {
                if ( oldListeners[0] == listener )
                {
                    newListeners = null;
                }
                else
                {
                    // The listener was not in the list.
                    newListeners = oldListeners;
                }
            }
            else
            {
                // Look for the listener in the array.
                int pos = -1;
                for ( int i = 0; i < oldListeners.length; i++ )
                {
                    if ( oldListeners[i] == listener )
                    {
                        pos = i;
                        break;
                    }
                }
                
                if ( pos < 0 )
                {
                    // The listener was not in the list.
                    newListeners = oldListeners;
                }
                else
                {
                    newListeners = new InstrumentListener[ oldListeners.length - 1 ];
                    if ( pos > 0 )
                    {
                        // Copy the head of the array
                        System.arraycopy( oldListeners, 0, newListeners, 0, pos );
                    }
                    if ( pos < oldListeners.length - 1 )
                    {
                        // Copy the tail of the array
                        System.arraycopy( oldListeners, pos + 1, 
                            newListeners, pos, oldListeners.length - 1 - pos );
                    }
                }
            }
            
            // Update the m_listeners field.
            m_listeners = newListeners;
        }
    }
    
    /**
     * Updates the cached array of InstrumentSamples taking synchronization into
     *  account.
     *
     * @return An array of the InstrumentSamples.
     */
    private InstrumentSample[] updateInstrumentSampleArray()
    {
        synchronized(this)
        {
            m_sampleArray = new InstrumentSample[ m_samples.size() ];
            m_samples.values().toArray( m_sampleArray );
            
            // Sort the array.  This is not a performance problem because this
            //  method is rarely called and doing it here saves cycles in the
            //  client.
            Arrays.sort( m_sampleArray, new Comparator()
                {
                    public int compare( Object o1, Object o2 )
                    {
                        return ((InstrumentSample)o1).getDescription().
                            compareTo( ((InstrumentSample)o2).getDescription() );
                    }
                    
                    public boolean equals( Object obj )
                    {
                        return false;
                    }
                } );
            
            return m_sampleArray;
        }
    }
    
    /**
     * Updates the cached array of InstrumentSampleDescriptors taking
     *  synchronization into account.
     *
     * @return An array of the InstrumentSampleDescriptors.
     */
    private InstrumentSampleDescriptor[] updateInstrumentSampleDescriptorArray()
    {
        synchronized(this)
        {
            if ( m_sampleArray == null )
            {
                updateInstrumentSampleArray();
            }
            
            m_sampleDescriptorArray =
                new InstrumentSampleDescriptor[ m_sampleArray.length ];
            for ( int i = 0; i < m_sampleArray.length; i++ )
            {
                m_sampleDescriptorArray[i] = m_sampleArray[i].getDescriptor();
            }
            
            return m_sampleDescriptorArray;
        }
    }
    
    /**
     * Saves the current state into a Configuration.
     *
     * @return The state as a Configuration.  Returns null if the configuration
     *         would not contain any information.
     */
    Configuration saveState()
    {
        boolean empty = true;
        DefaultConfiguration state = new DefaultConfiguration( "instrument", "-" );
        state.setAttribute( "name", m_name );
        
        InstrumentSample[] samples = getInstrumentSamples();
        for ( int i = 0; i < samples.length; i++ )
        {
            Configuration childState = samples[i].saveState();
            if ( childState != null )
            {
                state.addChild( childState );
                empty = false;
            }
        }
        
        // Only return a state if it contains information.
        if ( empty )
        {
            state = null;
        }
        return state;
    }
    
    /**
     * Loads the state into the Instrument.
     *
     * @param state Configuration object to load state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    void loadState( Configuration state ) throws ConfigurationException
    {
        synchronized( this )
        {
            Configuration[] instrumentSampleConfs = state.getChildren( "sample" );
            for ( int i = 0; i < instrumentSampleConfs.length; i++ )
            {
                Configuration instrumentSampleConf = instrumentSampleConfs[i];
                
                int sampleType = InstrumentSampleUtils.resolveInstrumentSampleType(
                    instrumentSampleConf.getAttribute( "type" ) );
                long sampleInterval = instrumentSampleConf.getAttributeAsLong( "interval" );
                int sampleSize = instrumentSampleConf.getAttributeAsInteger( "size", 1 );
                
                // Build the sample name from its attributes.  This makes it
                //  possible to avoid forcing the user to maintain a name as well.
                String fullSampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
                    m_name, sampleType, sampleInterval, sampleSize );
                InstrumentSample sample = getInstrumentSample( fullSampleName );
                if ( sample == null )
                {
                    // Sample does not exist, see if it is a leased sample.
                    long leaseExpirationTime =
                        instrumentSampleConf.getAttributeAsLong( "lease-expiration", 0 );
                    if ( leaseExpirationTime > 0 )
                    {
                        
                        String sampleName = InstrumentSampleUtils.generateInstrumentSampleName(
                            sampleType, sampleInterval, sampleSize );
                        String sampleDescription =
                            instrumentSampleConf.getAttribute( "description", sampleName );
                        
                        // Create the sample with a permanent expiration time.  The correct
                        //  expiration time will be set when its state is loaded.
                        AbstractInstrumentSample instrumentSample = 
                            (AbstractInstrumentSample)InstrumentSampleFactory.getInstrumentSample(
                            this, sampleType, fullSampleName, sampleInterval, sampleSize,
                            sampleDescription, 0 );
                        instrumentSample.enableLogging( getLogger() );
                        instrumentSample.loadState( instrumentSampleConf );
                        
                        addInstrumentSample( instrumentSample );
                    }
                    else
                    {
                        getLogger().debug( "InstrumentSample entry ignored while loading state " +
                            "because the sample does not exist: " + fullSampleName );
                    }
                }
                else
                {
                    sample.loadState( instrumentSampleConf );
                }
            }
        }
        
        stateChanged();
    }
    
    /**
     * Called whenever the state of the instrument is changed.
     */
    protected void stateChanged()
    {
        m_stateVersion++;
        
        // Propagate to the parent
        m_instrumentableProxy.stateChanged();
    }
    
    /**
     * Returns the name of a Instrument type.
     *
     * @param type Type whose name is wanted.
     *
     * @return The name of a Instrument type.
     */
    public static String getTypeName( int type )
    {
        switch ( type )
        {
        case DefaultInstrumentManager.INSTRUMENT_TYPE_NONE:
            return "none";
        case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
            return "counter";
        case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
            return "value";
        default:
            throw new IllegalArgumentException( type + " is not a known Instrument type." );
        }
    }
}
