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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.excalibur.instrument.AbstractInstrument;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManagerConnector;
import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleUtils;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentException;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentableException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
public class DefaultInstrumentManagerImpl
    extends AbstractLogEnabled
    implements Configurable, Initializable, Disposable, DefaultInstrumentManager,
        Instrumentable, Runnable
{
    /** The name used to identify this InstrumentManager. */
    private String m_name;

    /** The description of this InstrumentManager. */
    private String m_description;

    /** The maximum number of leased samples which will be allowed.  This is
     *   important to prevent denial of service attacks using connectors. */
    private int m_maxLeasedSamples;

    /** The maximum size of a leased sample.  This is important to prevent
     *   denial of service attacks using connectors. */
    private int m_maxLeasedSampleSize;

    /** The maximum amount of time that a lease will be granted for.  This is
     *   important to prevent denial of service attacks using connectors. */
    private long m_maxLeasedSampleLease;

    /** List of configured connectors. */
    private List m_connectors = new ArrayList();

    /** State file. */
    private File m_stateFile;

    /** Save state interval. */
    private long m_stateInterval;

    /** Last time that the state was saved. */
    private long m_lastStateSave;

    /** Semaphore for actions which must be synchronized */
    private Object m_semaphore = new Object();

    /** HashMap of all of the registered InstrumentableProxies by their keys. */
    private Map m_instrumentableProxies = new HashMap();

    /** Optimized array of the InstrumentableProxies. */
    private InstrumentableProxy[] m_instrumentableProxyArray;

    /** Optimized array of the InstrumentableDescriptors. */
    private InstrumentableDescriptor[] m_instrumentableDescriptorArray;

    /** List of leased InstrumentSamples. */
    private List m_leasedInstrumentSamples = new ArrayList();

    /** Optimized array of the leased InstrumentSamples. */
    private InstrumentSample[] m_leasedInstrumentSampleArray;

    /** Logger dedicated to logging translations. */
    private Logger m_translationLogger;

    /** Map of all registered translations. */
    private Map m_nameTranslations = new HashMap();

    /** Optimized array of the registered translations. */
    private String[][] m_nameTranslationArray;

    /**
     * Thread used to keep the instruments published by the InstrumentManager
     *  up to date.
     */
    private Thread m_runner;

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName = "instrument-manager";

    /** Instrument used to profile the total memory. */
    private ValueInstrument m_totalMemoryInstrument;

    /** Instrument used to profile the free memory. */
    private ValueInstrument m_freeMemoryInstrument;

    /** Instrument used to profile the in use memory. */
    private ValueInstrument m_memoryInstrument;

    /** Instrument used to profile the active thread count of the JVM. */
    private ValueInstrument m_activeThreadCountInstrument;

    /** Instrument to track the number of times registerInstrumentable is called. */
    private CounterInstrument m_registrationsInstrument;

    /** The Instrumentable count. */
    private int m_instrumentableCount;

    /** Instrument used to track the number of Instrumentables in the system. */
    private ValueInstrument m_instrumentablesInstrument;

    /** The Instrument count. */
    private int m_instrumentCount;

    /** Instrument used to track the number of Instruments in the system. */
    private ValueInstrument m_instrumentsInstrument;

    /** The Permanent Instrument Sample count. */
    private int m_permanentSampleCount;

    /** The Leased Instrument Sample count. */
    private int m_leasedSampleCount;

    /** Instrument used to track the number of Instrument samples in the system. */
    private ValueInstrument m_samplesInstrument;

    /** Instrument used to track the number of Leased Instrument samples in the system. */
    private ValueInstrument m_leasedSamplesInstrument;

    /** Instrument used to track the number of lease requests. */
    private CounterInstrument m_leaseRequestsInstrument;

    /** Instrument used to track the number of state saves performed. */
    private CounterInstrument m_stateSavesInstrument;

    /** Instrument used to track the time it takes to save the state. */
    private ValueInstrument m_stateSaveTimeInstrument;

    /** State Version. */
    private int m_stateVersion;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new DefaultInstrumentManagerImpl.
     *
     * @param name The name used to identify this InstrumentManager.  Should not
     *             contain any spaces or periods.
     *
     * @deprecated Name should be set in the instrument configuration file.
     */
    public DefaultInstrumentManagerImpl( String name )
    {
        this();
    }

    /**
     * Creates a new DefaultInstrumentManagerImpl.
     */
    public DefaultInstrumentManagerImpl()
    {
        // Initialize the Instrumentable elements.
        this.m_totalMemoryInstrument = new ValueInstrument( "total-memory" );
        this.m_freeMemoryInstrument = new ValueInstrument( "free-memory" );
        this.m_memoryInstrument = new ValueInstrument( "memory" );
        this.m_activeThreadCountInstrument = new ValueInstrument( "active-thread-count" );
        this.m_registrationsInstrument = new CounterInstrument( "instrumentable-registrations" );
        this.m_instrumentablesInstrument = new ValueInstrument( "instrumentables" );
        this.m_instrumentsInstrument = new ValueInstrument( "instruments" );
        this.m_samplesInstrument = new ValueInstrument( "samples" );
        this.m_leasedSamplesInstrument = new ValueInstrument( "leased-samples" );
        this.m_leaseRequestsInstrument = new CounterInstrument( "lease-requests" );
        this.m_stateSavesInstrument = new CounterInstrument( "state-saves" );
        this.m_stateSaveTimeInstrument = new ValueInstrument( "state-save-time" );
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Initializes the configured instrumentables.
     *
     * @param configuration InstrumentManager configuration.
     *
     * @throws ConfigurationException If there are any configuration problems.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        this.m_translationLogger = this.getLogger().getChildLogger( "translation" );

        // Register the InstrumentManager as an Instrumentable.  This must be done before
        //  the configuration and state file is loaded or our own instruments will not yet
        //  have proxies.
        try
        {
            this.registerInstrumentable( this, this.getInstrumentableName() );
        }
        catch ( Exception e )
        {
            // Should never happen
            throw new ConfigurationException(
                "Unable to register the InstrumentManager's own instruments.", e );
        }

        synchronized( this.m_semaphore )
        {
            // Look for a configured name and description
            this.m_name = configuration.getChild( "name" ).getValue( "instrument-manager" );
            this.m_description = configuration.getChild( "description" ).getValue( this.m_name );

            // Get configuration values which limit the leases that can be made.
            this.m_maxLeasedSamples =
                configuration.getChild( "max-leased-samples" ).getValueAsInteger( 256 );
            this.m_maxLeasedSampleSize =
                configuration.getChild( "max-leased-sample-size" ).getValueAsInteger( 2048 );
            this.m_maxLeasedSampleLease = 1000L *
                configuration.getChild( "max-leased-sample-lease" ).getValueAsInteger( 86400 );

            // Configure any translations
            Configuration translationsConf = configuration.getChild( "translations" );
            Configuration[] translationConfs = translationsConf.getChildren( "translation" );
            for( int i = 0; i < translationConfs.length; i++ )
            {
                Configuration translationConf = translationConfs[i];
                String source = translationConf.getAttribute( "source" );
                String target = translationConf.getAttribute( "target" );
                try
                {
                    this.registerNameTranslationInner( source, target );
                }
                catch ( IllegalArgumentException e )
                {
                    throw new ConfigurationException( e.getMessage(), translationConf );
                }
            }

            // Configure the instrumentables.
            Configuration instrumentablesConf = configuration.getChild( "instrumentables" );
            Configuration[] instrumentableConfs =
                instrumentablesConf.getChildren( "instrumentable" );
            for( int i = 0; i < instrumentableConfs.length; i++ )
            {
                Configuration instrumentableConf = instrumentableConfs[ i ];
                String instrumentableName = instrumentableConf.getAttribute( "name" );

                // See if the instrumentable already exists.
                InstrumentableProxy instrumentableProxy =
                    (InstrumentableProxy)this.m_instrumentableProxies.get( instrumentableName );
                if ( instrumentableProxy == null )
                {
                    instrumentableProxy = new InstrumentableProxy(
                        this, null, instrumentableName, instrumentableName );
                    instrumentableProxy.enableLogging( this.getLogger() );
                    this.incrementInstrumentableCount();
                    this.m_instrumentableProxies.put( instrumentableName, instrumentableProxy );

                    // Clear the optimized arrays
                    this.m_instrumentableProxyArray = null;
                    this.m_instrumentableDescriptorArray = null;
                }
                // Always configure
                instrumentableProxy.configure( instrumentableConf );
            }

            // Configure the state file.
            Configuration stateFileConf = configuration.getChild( "state-file" );
            this.m_stateInterval = stateFileConf.getAttributeAsLong( "interval", 60000 );

            String stateFile = stateFileConf.getValue( null );
            if( stateFile != null )
            {
                this.m_stateFile = new File( stateFile );
                if( this.m_stateFile.exists() )
                {
                    try
                    {
                        this.loadStateFromFile( this.m_stateFile );
                    }
                    catch( Exception e )
                    {
                        String msg = "Unable to load the instrument manager state.  The "
                            + "configuration may have been corrupted.  A backup may have been "
                            + "made in the same directory when it was saved.";

                        if ( this.getLogger().isDebugEnabled() )
                        {
                            this.getLogger().error( msg, e );
                        }
                        else
                        {
                            this.getLogger().error( msg + " : " + e.toString() );
                        }
                    }
                }
            }

            // Create a logger to use with the connectors
            Logger connLogger = this.getLogger().getChildLogger( "connector" );

            // Configure the connectors
            Configuration connectorsConf = configuration.getChild( "connectors" );
            Configuration[] connectorConfs =
                connectorsConf.getChildren( "connector" );
            for( int i = 0; i < connectorConfs.length; i++ )
            {
                Configuration connectorConf = connectorConfs[ i ];
                String className = connectorConf.getAttribute( "class" );
                // Handle aliases
                if ( className.equals( "http" ) )
                {
                    // Don't use InstrumentManagerAltrmiConnector.class.getName() because
                    //  the class is optional for the build.
                    className = "org.apache.excalibur.instrument.manager.http."
                        + "InstrumentManagerHTTPConnector";
                }

                // Look for the connector class and create an instance.
                try
                {
                    Class clazz = Class.forName( className );
                    DefaultInstrumentManagerConnector connector =
                        (DefaultInstrumentManagerConnector)clazz.newInstance();

                    // Initialize the new connector
                    connector.setInstrumentManager( this );
                    ContainerUtil.enableLogging( connector, connLogger );
                    ContainerUtil.configure( connector, connectorConf );
                    ContainerUtil.start( connector );
                    if ( connector instanceof Instrumentable )
                    {
                        Instrumentable inst = (Instrumentable)connector;
                        this.registerInstrumentable( inst,
                            this.m_instrumentableName + ".connector." + inst.getInstrumentableName() );
                    }

                    this.m_connectors.add( connector );
                }
                catch ( Exception e )
                {
                    String msg = "Unable to create connector because: " + e;

                    // Was the optional flag set?
                    if ( connectorConf.getAttributeAsBoolean( "optional", true ) )
                    {
                        this.getLogger().warn( msg );
                    }
                    else
                    {
                        throw new ConfigurationException( msg );
                    }
                }
            }
        }
    }

    /*---------------------------------------------------------------
     * Initializable Methods
     *-------------------------------------------------------------*/
    /**
     * Initializes the InstrumentManager.
     *
     * @throws Exception If there were any problems initializing the object.
     */
    public void initialize()
        throws Exception
    {
        if( this.m_runner == null )
        {
            this.m_runner = new Thread( this, "InstrumentManagerRunner" );
            this.m_runner.start();
        }
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * Disposes the InstrumentManager.
     */
    public void dispose()
    {
        if( this.m_runner != null )
        {
            this.m_runner = null;
        }

        // Shutdown the connectors
        for ( Iterator iter = this.m_connectors.iterator(); iter.hasNext(); )
        {
            DefaultInstrumentManagerConnector connector =
                (DefaultInstrumentManagerConnector)iter.next();
            try
            {
                ContainerUtil.stop( connector );
                ContainerUtil.dispose( connector );
            }
            catch ( Exception e )
            {
                this.getLogger().error( "Encountered an unexpected error shutting down a connector", e );
            }
        }

        this.saveState();
    }

    /*---------------------------------------------------------------
     * InstrumentManager Methods
     *-------------------------------------------------------------*/
    /**
     * Instrumentable to be registered with the instrument manager.  Should be
     *  called whenever an Instrumentable is created.  The '.' character is
     *  used to denote a child Instrumentable and can be used to register the
     *  instrumentable at a specific point in an instrumentable hierarchy.
     *
     * @param instrumentable Instrumentable to register with the InstrumentManager.
     * @param instrumentableName The name to use when registering the Instrumentable.
     *
     * @throws Exception If there were any problems registering the Instrumentable.
     */
    public void registerInstrumentable( Instrumentable instrumentable, String instrumentableName )
        throws Exception
    {
        this.getLogger().debug( "Registering Instrumentable: " + instrumentableName );

        this.m_registrationsInstrument.increment();

        synchronized( this.m_semaphore )
        {
            // If the specified instrumentable name contains '.' chars then we need to
            //  make sure we register the instrumentable at the correct location, creating
            //  any parent instrumentables as necessary.
            int pos = instrumentableName.indexOf( '.' );
            if ( pos >= 0 )
            {
                String parentName = instrumentableName.substring( 0, pos );
                String childName =
                    instrumentableName.substring( pos + 1 );
                InstrumentableProxy instrumentableProxy =
                    (InstrumentableProxy)this.m_instrumentableProxies.get( parentName );
                if ( instrumentableProxy == null )
                {
                    // This is a Instrumentable that has not been seen before.
                    instrumentableProxy = new InstrumentableProxy(
                        this, null, parentName, parentName );
                    instrumentableProxy.enableLogging( this.getLogger() );
                    this.incrementInstrumentableCount();
                    // Do not call configure here because there is no configuration
                    //  for discovered instrumentables.
                    this.m_instrumentableProxies.put( parentName, instrumentableProxy );

                    // Clear the optimized arrays
                    this.m_instrumentableProxyArray = null;
                    this.m_instrumentableDescriptorArray = null;

                    // Recursively register all the Instruments in this and any child Instrumentables.
                    this.registerDummyInstrumentableInner(
                        instrumentable, instrumentableProxy, parentName, childName );
                }
                else
                {
                    // Additional Instrumentable instance.  Possible that new Instruments could be found.
                    this.registerDummyInstrumentableInner(
                        instrumentable, instrumentableProxy, parentName, childName );
                }
            } else {
                // If the instrumentable does not implement ThreadSafe, then it is possible that
                //  another one of its instance was already registered.  If so, then the
                //  Instruments will all be the same.  The new instances still need to be
                //  registered however.
                InstrumentableProxy instrumentableProxy =
                    (InstrumentableProxy)this.m_instrumentableProxies.get( instrumentableName );
                if( instrumentableProxy == null )
                {
                    // This is a Instrumentable that has not been seen before.
                    instrumentableProxy = new InstrumentableProxy(
                        this, null, instrumentableName, instrumentableName );
                    instrumentableProxy.enableLogging( this.getLogger() );
                    this.incrementInstrumentableCount();
                    // Do not call configure here because there is no configuration
                    //  for discovered instrumentables.
                    this.m_instrumentableProxies.put( instrumentableName, instrumentableProxy );

                    // Clear the optimized arrays
                    this.m_instrumentableProxyArray = null;
                    this.m_instrumentableDescriptorArray = null;

                    // Recursively register all the Instruments in this and any child Instrumentables.
                    this.registerInstrumentableInner(
                        instrumentable, instrumentableProxy, instrumentableName );
                }
                else
                {
                    // Additional Instrumentable instance.  Possible that new Instruments could be found.
                    this.registerInstrumentableInner(
                        instrumentable, instrumentableProxy, instrumentableName );
                }
            }
        }

        this.stateChanged();
    }

    /*---------------------------------------------------------------
     * DefaultInstrumentManager Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the name used to identify this DefaultInstrumentManager.
     *
     * @return The name used to identify this DefaultInstrumentManager.
     */
    public String getName()
    {
        return this.m_name;
    }

    /**
     * Returns the description of this DefaultInstrumentManager.
     *
     * @return The description of this DefaultInstrumentManager.
     */
    public String getDescription()
    {
        return this.m_description;
    }

    /**
     * Registers a name translation that will be applied to all named based
     *  lookups of instrumentables, instruments, and samples.   The more
     *  translations that are registered, the greater the impact on name
     *  based lookups will be.
     * <p>
     * General operation of the instrument manager will not be affected as
     *  collection on sample data is always done using direct object
     *  references.
     * <p>
     * Translations can be registered for exact name matches, or for
     *  the bases of names.  Any translation which ends in a '.' will
     *  imply a translation to any name beginning with that name base.
     *  If the source ends with a '.' then the target must as well.
     *
     * @param source The source name or name base of the translation.
     * @param target The target name or name base of the translation.
     *
     * @throws IllegalArgumentException If the one but not both of the source
     *                                  and target parameters end in '.'.
     */
    public void registerNameTranslation( String source, String target )
        throws IllegalArgumentException
    {
        synchronized( this.m_semaphore )
        {
            this.registerNameTranslationInner( source, target );
        }
    }

    /**
     * Returns a InstrumentableDescriptor based on its name or the name of any
     *  of its children.
     *
     * @param instrumentableName Name of the Instrumentable being requested.
     *
     * @return A Descriptor of the requested Instrumentable.
     *
     * @throws NoSuchInstrumentableException If the specified Instrumentable
     *                                       does not exist.
     */
    public InstrumentableDescriptor getInstrumentableDescriptor( String instrumentableName )
        throws NoSuchInstrumentableException
    {
        InstrumentableProxy proxy = this.getInstrumentableProxy( instrumentableName );
        if( proxy == null )
        {
            throw new NoSuchInstrumentableException(
                "No instrumentable can be found using name: " + instrumentableName );
        }

        return proxy.getDescriptor();
    }

    /**
     * Returns an array of Descriptors for the Instrumentables managed by this
     *  DefaultInstrumentManager.
     *
     * @return An array of InstrumentableDescriptors.
     */
    public InstrumentableDescriptor[] getInstrumentableDescriptors()
    {
        InstrumentableDescriptor[] descriptors = this.m_instrumentableDescriptorArray;
        if( descriptors == null )
        {
            descriptors = this.updateInstrumentableDescriptorArray();
        }
        return descriptors;
    }

    /**
     * Searches the entire instrument tree for an instrumentable with the given
     *  name.
     *
     * @param instrumentableName Name of the Instrumentable being requested.
     *
     * @return A Descriptor of the requested Instrumentable.
     *
     * @throws NoSuchInstrumentableException If the specified Instrumentable
     *                                       does not exist.
     */
    public InstrumentableDescriptor locateInstrumentableDescriptor( String instrumentableName )
        throws NoSuchInstrumentableException
    {
        InstrumentableProxy instrumentableProxy =
            this.locateDeepestInstrumentableProxy( instrumentableName );
        if ( instrumentableProxy != null )
        {
            if ( instrumentableProxy.getName().equals( instrumentableName ) )
            {
                // Found what we were looking for
                return instrumentableProxy.getDescriptor();
            }
        }

        // Unable to locate the requested Instrumentable
        throw new NoSuchInstrumentableException(
            "No instrumentable can be found with the name: " + instrumentableName );
    }

    /**
     * Searches the entire instrument tree for an instrument with the given
     *  name.
     *
     * @param instrumentName Name of the Instrument being requested.
     *
     * @return A Descriptor of the requested Instrument.
     *
     * @throws NoSuchInstrumentException If the specified Instrument does
     *                                   not exist.
     */
    public InstrumentDescriptor locateInstrumentDescriptor( String instrumentName )
        throws NoSuchInstrumentException
    {
        InstrumentableProxy instrumentableProxy =
            this.locateDeepestInstrumentableProxy( instrumentName );
        if ( instrumentableProxy != null )
        {
            // Now look for the specified instrument
            InstrumentProxy instrumentProxy =
                instrumentableProxy.getInstrumentProxy( instrumentName );
            if ( instrumentProxy != null )
            {
                if ( instrumentProxy.getName().equals( instrumentName ) )
                {
                    // Found what we were looking for
                    return instrumentProxy.getDescriptor();
                }
            }
        }

        // Unable to locate the requested Instrument
        throw new NoSuchInstrumentException(
            "No instrument can be found with the name: " + instrumentName );
    }

    /**
     * Searches the entire instrument tree for an instrument sample with the
     *  given name.
     *
     * @param sampleName Name of the Instrument Sample being requested.
     *
     * @return A Descriptor of the requested Instrument Sample.
     *
     * @throws NoSuchInstrumentSampleException If the specified Instrument
     *                                         Sample does not exist.
     */
    public InstrumentSampleDescriptor locateInstrumentSampleDescriptor( String sampleName )
        throws NoSuchInstrumentSampleException
    {
        InstrumentableProxy instrumentableProxy =
            this.locateDeepestInstrumentableProxy( sampleName );
        if ( instrumentableProxy != null )
        {
            // Now look for the specified instrument
            InstrumentProxy instrumentProxy =
                instrumentableProxy.getInstrumentProxy( sampleName );
            if ( instrumentProxy != null )
            {
                // Now look for the specified sample
                InstrumentSample sample = instrumentProxy.getInstrumentSample( sampleName );
                if ( sample != null )
                {
                    if ( sample.getName().equals( sampleName ) )
                    {
                        // Found what we were looking for
                        return sample.getDescriptor();
                    }
                }
            }
        }

        // Unable to locate the requested Instrument Sample
        throw new NoSuchInstrumentSampleException(
            "No instrument sample can be found with the name: " + sampleName );
    }

    /**
     * Returns the stateVersion of the DefaultInstrumeManager.  The state
     *  version will be incremented each time any of the configuration of
     *  the instrument manager or any of its children is modified.
     * <p>
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrument manager.
     */
    public int getStateVersion()
    {
        return this.m_stateVersion;
    }

    /**
     * Invokes garbage collection.
     */
    public void invokeGarbageCollection()
    {
        System.gc();
    }

    /**
     * Returns the current number of leased samples.
     *
     * @return The current number of leased samples.
     */
    public int getLeaseSampleCount()
    {
        return this.m_leasedSampleCount;
    }

    /**
     * Returns the maximum number of leased samples that will be approved.
     *
     * @return The maximum number of leased samples.
     */
    public int getMaxLeasedSamples()
    {
        return this.m_maxLeasedSamples;
    }

    /**
     * Returns the maximum size of a leased sample.
     *
     * @return The maximum size of a leased sample.
     */
    public int getMaxLeasedSampleSize()
    {
        return this.m_maxLeasedSampleSize;
    }

    /**
     * Returns the maximum number of milliseconds that a lease will be granted
     *  for.
     *
     * @return The maximum lease length.
     */
    public long getMaxLeasedSampleLease()
    {
        return this.m_maxLeasedSampleLease;
    }

    /*---------------------------------------------------------------
     * Instrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the name for the Instrumentable.  The Instrumentable Name is used
     *  to uniquely identify the Instrumentable during the configuration of
     *  the InstrumentManager and to gain access to an InstrumentableDescriptor
     *  through the InstrumentManager.  The value should be a string which does
     *  not contain spaces or periods.
     * <p>
     * This value may be set by a parent Instrumentable, or by the
     *  InstrumentManager using the value of the 'instrumentable' attribute in
     *  the configuration of the component.
     *
     * @param name The name used to identify a Instrumentable.
     */
    public void setInstrumentableName( String name )
    {
        this.m_instrumentableName = name;
    }

    /**
     * Gets the name of the Instrumentable.
     *
     * @return The name used to identify a Instrumentable.
     */
    public String getInstrumentableName()
    {
        return this.m_instrumentableName;
    }

    /**
     * Obtain a reference to all the Instruments that the Instrumentable object
     *  wishes to expose.  All sampling is done directly through the
     *  Instruments as opposed to the Instrumentable interface.
     *
     * @return An array of the Instruments available for profiling.  Should
     *         never be null.  If there are no Instruments, then
     *         EMPTY_INSTRUMENT_ARRAY can be returned.  This should never be
     *         the case though unless there are child Instrumentables with
     *         Instruments.
     */
    public Instrument[] getInstruments()
    {
        return new Instrument[]
        {
            this.m_totalMemoryInstrument,
            this.m_freeMemoryInstrument,
            this.m_memoryInstrument,
            this.m_activeThreadCountInstrument,
            this.m_registrationsInstrument,
            this.m_instrumentablesInstrument,
            this.m_instrumentsInstrument,
            this.m_samplesInstrument,
            this.m_leasedSamplesInstrument,
            this.m_leaseRequestsInstrument,
            this.m_stateSavesInstrument,
            this.m_stateSaveTimeInstrument
        };
    }

    /**
     * Any Object which implements Instrumentable can also make use of other
     *  Instrumentable child objects.  This method is used to tell the
     *  InstrumentManager about them.
     *
     * @return An array of child Instrumentables.  This method should never
     *         return null.  If there are no child Instrumentables, then
     *         EMPTY_INSTRUMENTABLE_ARRAY can be returned.
     */
    public Instrumentable[] getChildInstrumentables()
    {
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }

    /*---------------------------------------------------------------
     * Runnable Methods
     *-------------------------------------------------------------*/
    public void run()
    {
        while( this.m_runner != null )
        {
            try
            {
                Thread.sleep( 1000 );

                this.memoryInstruments();
                this.threadInstruments();
                this.testInstrumentSampleLeases();

                // Handle the state file if it is set
                long now = System.currentTimeMillis();
                if( now - this.m_lastStateSave >= this.m_stateInterval )
                {
                    this.saveState();
                }
            }
            catch( Throwable t )
            {
                this.getLogger().error( "Encountered an unexpected error.", t );
            }
        }
    }

    /*---------------------------------------------------------------
     * State File Methods
     *-------------------------------------------------------------*/
    /**
     * Loads the Instrument Manager state from the specified file.
     *
     * @param stateFile File to read the instrument manager's state from.
     *
     * @throws Exception if there are any problems loading the state.
     */
    public void loadStateFromFile( File stateFile )
        throws Exception
    {
        long now = System.currentTimeMillis();
        this.getLogger().debug( "Loading Instrument Manager state from: " +
            stateFile.getAbsolutePath() );

        FileInputStream is = new FileInputStream( stateFile );
        try
        {
            this.loadStateFromStream( is, stateFile.getCanonicalPath() );
        }
        finally
        {
            is.close();
        }

        this.getLogger().debug( "Loading Instrument Manager state took " +
                           ( System.currentTimeMillis() - now ) + "ms." );
    }

    /**
     * Loads the Instrument Manager state from the specified stream.
     *
     * @param is Stream to read the instrument manager's state from.
     *
     * @throws Exception if there are any problems loading the state.
     */
    public void loadStateFromStream( InputStream is )
        throws Exception
    {
        this.loadStateFromStream( is, null );
    }

    /**
     * Loads the Instrument Manager state from the specified stream.
     *
     * @param is Stream to read the instrument manager's state from.
     * @param location The location of the stream. Used to improve the
     *                 usefulness of any exceptions thrown.
     *
     * @throws Exception if there are any problems loading the state.
     */
    private void loadStateFromStream( InputStream is, String location )
        throws Exception
    {
        // Ride on top of the Configuration classes to load the state.
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration stateConfig;
        if ( location == null )
        {
            stateConfig = builder.build( is );
        }
        else
        {
            stateConfig = builder.build( is, location );
        }

        this.loadStateFromConfiguration( stateConfig );
    }

    /**
     * Loads the Instrument Manager state from the specified Configuration.
     *
     * @param state Configuration object to load the state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    public void loadStateFromConfiguration( Configuration state )
        throws ConfigurationException
    {
        this.loadStateFromConfiguration( state, null );
    }

    private void loadStateFromConfiguration( Configuration state, String parentName )
        throws ConfigurationException
    {
        // When loading state, the only thing that we are really interrested in are the samples.
        //  Don't bother looking anything up until one is found.  Doing it this way is also
        //  critical to make name translations work correctly.

        // Drill down into Instrumentables and Instruments by recursing into this method.
        //  This is not used by state files saved with version 2.2 or newer, but older
        //  versions of the instrument manager saved their states in a tree structure.
        Configuration[] instrumentableConfs = state.getChildren( "instrumentable" );
        for( int i = 0; i < instrumentableConfs.length; i++ )
        {
            this.loadStateFromConfiguration(
                instrumentableConfs[i], instrumentableConfs[i].getAttribute( "name", null ) );
        }
        Configuration[] instrumentConfs = state.getChildren( "instrument" );
        for( int i = 0; i < instrumentConfs.length; i++ )
        {
            this.loadStateFromConfiguration(
                instrumentConfs[i], instrumentConfs[i].getAttribute( "name", null ) );
        }

        // Look for any samples.
        Configuration[] sampleConfs = state.getChildren( "sample" );
        for( int i = 0; i < sampleConfs.length; i++ )
        {
            Configuration sampleConf = sampleConfs[i];

            // Obtain and translate the sample name.
            String sampleName = sampleConf.getAttribute( "name", null );
            if ( sampleName == null )
            {
                // The sample name was missing.  This can happen for very old state files.
                //  Build the name.
                if ( parentName == null )
                {
                    throw new ConfigurationException(
                        "Unable to resolve sample name.", sampleConf );
                }

                int sampleType = InstrumentSampleUtils.resolveInstrumentSampleType(
                    sampleConf.getAttribute( "type" ) );
                long sampleInterval = sampleConf.getAttributeAsLong( "interval" );
                int sampleSize = sampleConf.getAttributeAsInteger( "size" );

                sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
                    parentName, sampleType, sampleInterval, sampleSize );
            }
            // Translate the resulting name.
            sampleName = this.getTranslatedName( sampleName );

            // Before we do anything, decide how we want to handle the sample.
            long now = System.currentTimeMillis();
            long leaseExpirationTime = sampleConf.getAttributeAsLong( "lease-expiration", 0 );
            if ( leaseExpirationTime == 0 )
            {
                // This is the saved state of a permanent sample.  We only want to load it
                //  if the instrument and sample exists.  If it does not exist then it means
                //  that the user changed the configuration so the sample is no longer
                //  permanent.

                // Look for the existing instrument proxy.
                InstrumentProxy instrumentProxy = this.getInstrumentProxyForSample( sampleName, false );
                if ( instrumentProxy == null )
                {
                    // The instrument did not exist, so we want to skip this state.
                    this.getLogger().info(
                        "Skipping old permantent sample from state due to missing instrument: "
                        + sampleName );
                }
                else
                {
                    // The instrument exists, but the sample may not.  That is decided within
                    //  the instrument.
                    InstrumentSample sample = instrumentProxy.loadSampleState( sampleConf );
                    if ( sample == null )
                    {
                        this.getLogger().info(
                            "Skipping old permantent sample from state: " + sampleName );
                    }
                    else
                    {
                        if ( this.getLogger().isDebugEnabled() )
                        {
                            this.getLogger().debug( "Load permanent sample state: " + sampleName );
                        }
                    }
                }
            }
            else if ( leaseExpirationTime > now )
            {
                // This is a leased sample that has not yet expired.  Even if the sample
                //  or even its instrument does not yet exist, we want to go ahead and
                //  create it.  It is possible for instruments and samples to be created
                //  a while after the application has been started.

                // Get the instrument.  Will never return null.
                InstrumentProxy instrumentProxy = this.getInstrumentProxyForSample( sampleName, true );

                // Load the sample state.
                instrumentProxy.loadSampleState( sampleConf );

                if ( this.getLogger().isDebugEnabled() )
                {
                    this.getLogger().debug( "Load leased sample state : " + sampleName );
                }
            }
            else
            {
                // This sample has expired since the state was saved.  Do nothing.
                if ( this.getLogger().isDebugEnabled() )
                {
                    this.getLogger().debug( "Skip expired sample state: " + sampleName );
                }
            }
        }

        // If anything was actually loaded then stateChanged() will be called from the samples.
    }

    /**
     * Saves the Instrument Manager's state to the specified file.  Any
     *  existing file is backed up before the save takes place and replaced
     *  in the event of an error.
     *
     * @param stateFile File to write the Instrument Manager's state to.
     *
     * @throws Exception if there are any problems saving the state.
     */
    public void saveStateToFile( File stateFile )
        throws Exception
    {
        long now = System.currentTimeMillis();
        this.getLogger().debug( "Saving Instrument Manager state to: " + stateFile.getAbsolutePath() );

        // To make corruption as unlikely as possible, write the state file to a
        //  temporary file.  Only overwrite the previous state file when complete.
        File tempFile = new File( stateFile.getAbsolutePath() + "." + now + ".temp" );
        boolean success = false;
        FileOutputStream fos = new FileOutputStream( tempFile );
        try
        {
            this.saveStateToStream( fos );
            success = true;
        }
        finally
        {
            fos.close();

            File renameFile = null;
            try
            {
                if ( success )
                {
                    // Rename the old state file first of all.
                    if ( stateFile.exists() )
                    {
                        renameFile =
                            new File( stateFile.getAbsolutePath() + "." + now + ".backup" );
                        if ( !stateFile.renameTo( renameFile ) )
                        {
                            throw new IOException(
                                "Unable to rename the old instrument state file from '"
                                + stateFile.getAbsolutePath() + "' to '"
                                + renameFile.getAbsolutePath() + "'" );
                        }
                    }

                    // Now rename the new temp state file to the final name.
                    if ( !tempFile.renameTo( stateFile ) )
                    {
                        if ( renameFile != null )
                        {
                            // Attempt to restore the old state file.
                            if ( !renameFile.renameTo( stateFile ) )
                            {
                                // Failed for some reason.
                                this.getLogger().error(
                                    "Unable to save the instrument state.  The last known state "
                                    + "file is backed up as: " + renameFile.getAbsolutePath() );

                                // Clear the rename file so it does not get deleted.
                                renameFile = null;
                            }
                        }

                        throw new IOException(
                            "Unable to rename the new instrument state file from '"
                            + tempFile.getAbsolutePath() + "' to '"
                            + stateFile.getAbsolutePath() + "'" );
                    }
                    else
                    {
                        // Temp fle renamed, so clear its name.
                        tempFile = null;
                    }
                }
            }
            finally
            {
                // Delete the temp file if it still exists.
                if ( ( tempFile != null ) && tempFile.exists() )
                {
                    if ( !tempFile.delete() )
                    {
                        this.getLogger().warn( "Unable to delete temporary state file: "
                            + tempFile.getAbsolutePath() );
                    }
                }

                // Delete the rename file if it still exists.
                if ( ( renameFile != null ) && renameFile.exists() )
                {
                    if ( !renameFile.delete() )
                    {
                        this.getLogger().warn( "Unable to delete temporary state file: "
                            + renameFile.getAbsolutePath() );
                    }
                }
            }
        }

        this.getLogger().debug( "Saving Instrument Manager state took " +
                           ( System.currentTimeMillis() - now ) + "ms." );
    }

    /**
     * Saves the Instrument Manager's state to the specified output stream.
     *
     * @param os Stream to write the Instrument Manager's state to.
     *
     * @throws Exception if there are any problems saving the state.
     */
    public void saveStateToStream( OutputStream os )
        throws Exception
    {
        // We used to create a big Configuration object and then write it to an output
        //  stream.   While that worked, on applications with lots of samples, it
        //  could result in very large Configuration objects that would cause a large
        //  spike in memory usage when the state was saved.
        // The new method writes directly to the file thus avoiding the need for any
        //  significant amount of memory.
        PrintWriter out = new PrintWriter( new OutputStreamWriter( os, "UTF-8" ) );

        // Output the XML headers and main node.
        out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        out.println( "<instrument-manager-state>" );

        InstrumentableProxy[] instrumentableProxies = this.m_instrumentableProxyArray;
        if( instrumentableProxies == null )
        {
            instrumentableProxies = this.updateInstrumentableProxyArray();
        }

        for( int i = 0; i < instrumentableProxies.length; i++ )
        {
            instrumentableProxies[i].writeState( out );
        }

        // Close off the main node.
        out.println( "</instrument-manager-state>" );

        // We don't want to close the writer here or it will close the underlying stream
        //  Do the next best thing by flushing to make sure that nothing is left unflushed
        //  in writer buffers.
        out.flush();
    }

    /*---------------------------------------------------------------
     * Package Methods
     *-------------------------------------------------------------*/
    /**
     * Registers an InstrumentSample which has been leased so that the
     *  Instrument Manager can efficiently purge it when it has expired.
     *
     * @param instrumentSample Leased InstrumentSample to register.
     */
    void registerLeasedInstrumentSample( InstrumentSample instrumentSample )
    {
        synchronized( this.m_leasedInstrumentSamples )
        {
            // Make sure that the sample is really leased.
            if ( instrumentSample.getLeaseExpirationTime() <= 0 )
            {
                throw new IllegalStateException( "Got an InstrumentSample that was not leased." );
            }

            // Make sure that it is not already in the list.
            if ( this.m_leasedInstrumentSamples.indexOf( instrumentSample ) < 0 )
            {
                this.m_leasedInstrumentSamples.add( instrumentSample );
                this.m_leasedInstrumentSampleArray = null;
            }
        }
    }

    /**
     * Called whenever the state of the instrument manager is changed.
     */
    void stateChanged()
    {
        this.m_stateVersion++;
    }

    /**
     * Called to increment the number of Instrumentables registered.
     */
    void incrementInstrumentableCount()
    {
        int count;
        synchronized( this.m_semaphore )
        {
            count = ++this.m_instrumentableCount;
        }
        this.m_instrumentablesInstrument.setValue( count );
    }

    /**
     * Called to increment the number of Instruments registered.
     */
    void incrementInstrumentCount()
    {
        int count;
        synchronized( this.m_semaphore )
        {
            count = ++this.m_instrumentCount;
        }
        this.m_instrumentsInstrument.setValue( count );
    }

    /**
     * Called to increment the number of Permanent Instrument Samples registered.
     */
    void incrementPermanentSampleCount()
    {
        int count;
        synchronized( this.m_semaphore )
        {
            count = ++this.m_permanentSampleCount + this.m_leasedSampleCount;
        }
        this.m_samplesInstrument.setValue( count );
    }

    /**
     * Called to increment the number of Leased Instrument Samples registered.
     */
    void incrementLeasedSampleCount()
    {
        int count;
        int leasedCount;
        synchronized( this.m_semaphore )
        {
            leasedCount = ++this.m_leasedSampleCount;
            count = this.m_permanentSampleCount + this.m_leasedSampleCount;
        }
        this.m_samplesInstrument.setValue( count );
        this.m_leasedSamplesInstrument.setValue( leasedCount );
    }

    /**
     * Called to decrement the number of Leased Instrument Samples registered.
     */
    void decrementLeasedSampleCount()
    {
        int count;
        int leasedCount;
        synchronized( this.m_semaphore )
        {
            leasedCount = --this.m_leasedSampleCount;
            count = this.m_permanentSampleCount + this.m_leasedSampleCount;
        }
        this.m_samplesInstrument.setValue( count );
        this.m_leasedSamplesInstrument.setValue( leasedCount );
    }

    /**
     * Increment the lease requests.
     */
    void incrementLeaseRequests()
    {
        this.m_leaseRequestsInstrument.increment();
    }

    /*---------------------------------------------------------------
     * Private Methods
     *-------------------------------------------------------------*/
    /**
     * Saves the state to the current state file if configured.
     */
    private void saveState()
    {
        long now = System.currentTimeMillis();

        // Always set the time even if the save fails so that we don't thrash
        this.m_lastStateSave = now;

        if( this.m_stateFile == null )
        {
            return;
        }

        try
        {
            this.saveStateToFile( this.m_stateFile );
        }
        catch ( Exception e )
        {
            String msg = "Unable to save the Instrument Manager state";
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().warn( msg, e );
            }
            else
            {
                this.getLogger().warn( msg + " : " + e.toString() );
            }
        }

        this.m_stateSavesInstrument.increment();
        this.m_stateSaveTimeInstrument.setValue( (int)( System.currentTimeMillis() - now ) );
    }

    /**
     * Updates the cached array of registered name translations taking
     *  synchronization into account.
     *
     * @return An array of the String[2]s representing the registered name
     *         translations.
     */
    private String[][] updateNameTranslationArray()
    {
        synchronized( this.m_semaphore )
        {
            String[][] nameTranslations = new String[this.m_nameTranslations.size()][2];
            int i = 0;
            for ( Iterator iter = this.m_nameTranslations.entrySet().iterator(); iter.hasNext(); i++ )
            {
                Map.Entry entry = (Map.Entry)iter.next();
                nameTranslations[i][0] = (String)entry.getKey();
                nameTranslations[i][1] = (String)entry.getValue();
            }

            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            this.m_nameTranslationArray = nameTranslations;

            return nameTranslations;
        }
    }

    /**
     * Translates a item name depending on a set of configured translations.
     *  If the name does not exist as a translation then the requested name will
     *  be returned unmodified.
     *
     * @param name Requested name.
     *
     * @return target name.
     */
    String getTranslatedName( String name )
    {
        String[][] nameTranslations = this.m_nameTranslationArray;
        if( nameTranslations == null )
        {
            nameTranslations = this.updateNameTranslationArray();
        }

        for ( int i = 0; i < nameTranslations.length; i++ )
        {
            String[] nameTranslation = nameTranslations[i];

            if ( name.startsWith( nameTranslation[0] ) )
            {
                // Match
                if ( name.equals( nameTranslation[0] ) )
                {
                    // Exact match
                    String newName = nameTranslation[1];

                    if ( this.m_translationLogger.isDebugEnabled() )
                    {
                        this.m_translationLogger.debug(
                            "Translate \"" + name + "\" to \"" + newName + "\"" );
                    }

                    return newName;
                }
                else if ( nameTranslation[0].endsWith( "." ) )
                {
                    // Beginning match
                    String newName =
                        nameTranslation[1] + name.substring( nameTranslation[0].length() );

                    if ( this.m_translationLogger.isDebugEnabled() )
                    {
                        this.m_translationLogger.debug(
                            "Translate \"" + name + "\" to \"" + newName + "\"" );
                    }

                    return newName;
                }
                else
                {
                    // The name happened to match the beginning of this name, but it was not meant
                    //  as a base translation.
                }
            }
        }

        // No match.
        return name;
    }

    /**
     */
    private InstrumentableProxy getInstrumentableProxy( String instrumentableName, boolean create )
    {
        //getLogger().debug( "getInstrumentableProxy( " + instrumentableName + ", " + create + " )" );
        // The instrumable name may begin with the name of a parent Instrumentable
        int pos = instrumentableName.lastIndexOf( '.' );
        if ( pos <= 0 )
        {
            // This is a root level instrumentable.  Look for it within the Instrument Manager.
            InstrumentableProxy instrumentableProxy;
            synchronized( this.m_semaphore )
            {
                instrumentableProxy =
                    (InstrumentableProxy)this.m_instrumentableProxies.get( instrumentableName );

                if ( ( instrumentableProxy == null ) && create )
                {
                    //getLogger().debug( "     New Instrumentable" );
                    // Not found, create it.
                    instrumentableProxy = new InstrumentableProxy(
                        this, null, instrumentableName, instrumentableName );
                    instrumentableProxy.enableLogging( this.getLogger() );
                    this.incrementInstrumentableCount();
                    this.m_instrumentableProxies.put( instrumentableName, instrumentableProxy );

                    // Clear the optimized arrays
                    this.m_instrumentableProxyArray = null;
                    this.m_instrumentableDescriptorArray = null;
                }
            }

            //getLogger().debug( "  -> " + instrumentableProxy );
            return instrumentableProxy;
        }
        else
        {
            String parentInstrumentableName = instrumentableName.substring( 0, pos );

            // See if the parent Instrumentable exists.
            InstrumentableProxy parentInstrumentableProxy =
                this.getInstrumentableProxy( parentInstrumentableName, create );
            if ( parentInstrumentableProxy == null )
            {
                // Parent Instrumentable did not exist, so the Instrumentable did not exist.
                //  Will not get here if create is true.
                return null;
            }

            // Locate the Instrumentable within the parent Instrumentable.
            return parentInstrumentableProxy.getChildInstrumentableProxy(
                instrumentableName, create );
        }
    }

    /**
     * @throws IllegalArgumentException If the specified instrumentName is invalid.
     */
    private InstrumentProxy getInstrumentProxy( String instrumentName, boolean create )
        throws IllegalArgumentException
    {
        //getLogger().debug( "getInstrumentProxy( " + instrumentName + ", " + create + " )" );
        // The instrument name must always begin with the name of an Instrumentable
        int pos = instrumentName.lastIndexOf( '.' );
        if ( pos <= 0 )
        {
            throw new IllegalArgumentException(
                "\"" + instrumentName + "\" is not a valid instrument name." );
        }
        String instrumentableName = instrumentName.substring( 0, pos );

        // See if the Instrumentable exists.
        InstrumentableProxy instrumentableProxy =
            this.getInstrumentableProxy( instrumentableName, create );
        if ( instrumentableProxy == null )
        {
            // Instrumentable did not exist, so the instrument did not exist.  Will not get here
            //  if create is true.
            return null;
        }

        // Locate the instrument within the Instrumentable.
        return instrumentableProxy.getInstrumentProxy( instrumentName, create );
    }

    /**
     * @throws IllegalArgumentException If the specified sampleName is invalid.
     */
    private InstrumentProxy getInstrumentProxyForSample( String sampleName, boolean create )
        throws IllegalArgumentException
    {
        //getLogger().debug( "getInstrumentProxyForSample( " + sampleName + ", " + create + " )" );
        // The sample name must always begin with the name of an Instrument
        int pos = sampleName.lastIndexOf( '.' );
        if ( pos <= 0 )
        {
            throw new IllegalArgumentException(
                "\"" + sampleName + "\" is not a valid instrument sample name." );
        }
        String instrumentName = sampleName.substring( 0, pos );

        // Lookup the Instrument.
        return this.getInstrumentProxy( instrumentName, create );
    }

    /**
     * Returns a InstrumentableDescriptor based on its name or the name of any
     *  of its children.
     *
     * @param instrumentableName Name of the Instrumentable being requested.
     *
     * @return A Proxy of the requested Instrumentable or null if not found.
     */
    private InstrumentableProxy getInstrumentableProxy( String instrumentableName )
    {
        String name = instrumentableName;
        while( true )
        {
            InstrumentableProxy proxy = (InstrumentableProxy)this.m_instrumentableProxies.get( name );
            if( proxy != null )
            {
                return proxy;
            }

            // Assume this is a child name and try looking with the parent name.
            int pos = name.lastIndexOf( '.' );
            if( pos > 0 )
            {
                name = name.substring( 0, pos );
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Given the name of an instrumentable proxy, locate the deepest child
     *  instrumentable given the name.  The name can be the name of an
     *  instrumentable or of any of its children.
     *
     * @param instrumentableName Fully qualified name of the instrumentable
     *                           being requested, or of any of its children.
     *
     * @return The requested instrumentable, or null if not found.
     */
    private InstrumentableProxy locateDeepestInstrumentableProxy( String instrumentableName )
    {
        InstrumentableProxy deepestProxy = null;
        // Start by obtaining a top level instrumentable
        InstrumentableProxy proxy = this.getInstrumentableProxy( instrumentableName );

        // Now attempt to locate a child instrumentable
        while ( proxy != null )
        {
            deepestProxy = proxy;

            proxy = deepestProxy.getChildInstrumentableProxy( instrumentableName );
        }

        return deepestProxy;
    }

    /**
     * Updates the Memory based Profile Points published by the InstrumentManager.
     */
    private void memoryInstruments()
    {
        // Avoid doing unneeded work if profile points are not being used.
        Runtime runtime = null;
        long totalMemory = -1;
        long freeMemory = -1;

        // Total Memory
        if( this.m_totalMemoryInstrument.isActive() )
        {
            runtime = Runtime.getRuntime();
            totalMemory = runtime.totalMemory();
            this.m_totalMemoryInstrument.setValue( (int)totalMemory );
        }

        // Free Memory
        if( this.m_freeMemoryInstrument.isActive() )
        {
            if( runtime == null )
            {
                runtime = Runtime.getRuntime();
            }
            freeMemory = runtime.freeMemory();
            this.m_freeMemoryInstrument.setValue( (int)freeMemory );
        }

        // In use Memory
        if( this.m_memoryInstrument.isActive() )
        {
            if( runtime == null )
            {
                runtime = Runtime.getRuntime();
            }
            if( totalMemory < 0 )
            {
                totalMemory = runtime.totalMemory();
            }
            if( freeMemory < 0 )
            {
                freeMemory = runtime.freeMemory();
            }
            this.m_memoryInstrument.setValue( (int)( totalMemory - freeMemory ) );
        }
    }

    /**
     * Updates the Thread based Profile Points published by the InstrumentManager.
     */
    private void threadInstruments()
    {
        if( this.m_activeThreadCountInstrument.isActive() )
        {
            // Get the top level thread group.
            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parent;
            while( ( parent = threadGroup.getParent() ) != null )
            {
                threadGroup = parent;
            }

            this.m_activeThreadCountInstrument.setValue( threadGroup.activeCount() );
        }
    }

    /**
     * Handles the maintenance of all Instrument Samples which have been leased
     *  by a client.  Any Samples whose leases which have expired are cleaned
     *  up.
     */
    private void testInstrumentSampleLeases()
    {
        long now = System.currentTimeMillis();

        InstrumentSample[] samples;
        synchronized( this.m_leasedInstrumentSamples )
        {
            samples = this.m_leasedInstrumentSampleArray;
            if ( samples == null )
            {
                this.m_leasedInstrumentSampleArray =
                    new InstrumentSample[ this.m_leasedInstrumentSamples.size() ];
                this.m_leasedInstrumentSamples.toArray( this.m_leasedInstrumentSampleArray );
                samples = this.m_leasedInstrumentSampleArray;
            }
        }

        for ( int i = 0; i < samples.length; i++ )
        {
            InstrumentSample sample = samples[i];
            long expire = sample.getLeaseExpirationTime();
            if ( now >= expire )
            {
                // The sample lease has expired.
                InstrumentProxy instrument = sample.getInstrumentProxy();
                instrument.removeInstrumentSample( sample );
                sample.expire();

                this.m_leasedInstrumentSamples.remove( sample );
                this.m_leasedInstrumentSampleArray = null;
            }
        }
    }

    /**
     * Updates the cached array of InstrumentableProxies taking
     *  synchronization into account.
     *
     * @return An array of the InstrumentableProxies.
     */
    private InstrumentableProxy[] updateInstrumentableProxyArray()
    {
        synchronized( this.m_semaphore )
        {
            InstrumentableProxy[] instrumentableProxyArray =
                new InstrumentableProxy[ this.m_instrumentableProxies.size() ];
            this.m_instrumentableProxies.values().toArray( instrumentableProxyArray );

            // Sort the array.  This is not a performance problem because this
            //  method is rarely called and doing it here saves cycles in the
            //  client.
            Arrays.sort( instrumentableProxyArray, new Comparator()
                {
                    public int compare( Object o1, Object o2 )
                    {
                        return ((InstrumentableProxy)o1).getDescription().
                            compareTo( ((InstrumentableProxy)o2).getDescription() );
                    }

                    public boolean equals( Object obj )
                    {
                        return false;
                    }
                } );

            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            this.m_instrumentableProxyArray = instrumentableProxyArray;

            return instrumentableProxyArray;
        }
    }

    /**
     * Updates the cached array of InstrumentableDescriptors taking
     *  synchronization into account.
     *
     * @return An array of the InstrumentableDescriptors.
     */
    private InstrumentableDescriptor[] updateInstrumentableDescriptorArray()
    {
        synchronized( this.m_semaphore )
        {
            // Get the proxy array. This is done in synchronization so it is not possible that it
            //  will be reset before we obtain the descriptor array.  They are both set to null
            //  at the same time when there is a change.
            InstrumentableProxy[] instrumentableProxyArray = this.m_instrumentableProxyArray;
            if ( instrumentableProxyArray == null )
            {
                instrumentableProxyArray = this.updateInstrumentableProxyArray();
            }

            InstrumentableDescriptor[] instrumentableDescriptorArray =
                new InstrumentableDescriptor[ instrumentableProxyArray.length ];
            for( int i = 0; i < instrumentableProxyArray.length; i++ )
            {
                instrumentableDescriptorArray[ i ] = instrumentableProxyArray[ i ].getDescriptor();
            }

            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            this.m_instrumentableDescriptorArray = instrumentableDescriptorArray;

            return instrumentableDescriptorArray;
        }
    }

    /**
     * Registers a name translation that will be applied to all named based
     *  lookups of instrumentables, instruments, and samples.   The more
     *  translations that are registered, the greater the impact on name
     *  based lookups will be.
     * <p>
     * General operation of the instrument manager will not be affected as
     *  collection on sample data is always done using direct object
     *  references.
     * <p>
     * Translations can be registered for translations of sample names up to
     *  and including the name of the instrument.  This means that all source
     *  and target names must end in a '.'.
     * <p>
     * This method should only be called when m_semaphore is synchronized.
     *
     * @param source The source name or name base of the translation.
     * @param target The target name or name base of the translation.
     *
     * @throws IllegalArgumentException If either the source or target does
     *                                  not end in a '.' or is invalid.
     */
    private void registerNameTranslationInner( String source, String target )
        throws IllegalArgumentException
    {
        if ( !source.endsWith( "." ) )
        {
            throw new IllegalArgumentException( "The translation source must end with a '.'." );
        }
        if ( !target.endsWith( "." ) )
        {
            throw new IllegalArgumentException( "The translation target must end with a '.'." );
        }

        // No component of the source or target name may be 0 length.
        if ( source.startsWith( "." ) || ( source.indexOf( ".." ) >= 0 ) )
        {
            throw new IllegalArgumentException(
                "The translation source is invalid: \"" + source + "\"." );
        }
        if ( target.startsWith( "." ) || ( target.indexOf( ".." ) >= 0 ) )
        {
            throw new IllegalArgumentException(
                "The translation target is invalid: \"" + target + "\"." );
        }

        this.m_nameTranslations.put( source, target );
        this.m_nameTranslationArray = null;
    }

    /**
     * Called as a place holder to handle the registration of instrumentables
     *  that do not really exist.  This makes it possible to register
     *  instrumentables at arbitrary locations in the instrumentable hierarchy.
     *
     * @param instrumentable The instrumentable that was registered below a dummy
     *                       parent.
     * @param instrumentableProxy The proxy assigned to the current placeholder
     *                            instrumentable.
     * @param instrumentableName The name of the current placeholder
     *                           instrumentable.
     * @param childName The name of the child instrumentable to register.  May
     *                  contain further '.' characters.
     */
    private void registerDummyInstrumentableInner( Instrumentable instrumentable,
                                                   InstrumentableProxy instrumentableProxy,
                                                   String instrumentableName,
                                                   String childName )
        throws Exception
    {
        // If the specified instrumentable name contains '.' chars then we need to
        //  make sure we register the instrumentable at the correct location, creating
        //  any parent instrumentables as necessary.
        int pos = childName.indexOf( '.' );
        if ( pos >= 0 )
        {
            String newParentName = childName.substring( 0, pos );
            String newChildName =
                childName.substring( pos + 1 );

            String fullChildName = instrumentableName + "." + newParentName;

            this.getLogger().debug( "Registering Child Instrumentable: " + fullChildName );

            // See if a proxy exists for the child Instrumentable yet.
            InstrumentableProxy proxy =
                instrumentableProxy.getChildInstrumentableProxy( fullChildName );
            if( proxy == null )
            {
                proxy = new InstrumentableProxy(
                    this, instrumentableProxy, fullChildName, newParentName );
                proxy.enableLogging( this.getLogger() );
                this.incrementInstrumentableCount();

                instrumentableProxy.addChildInstrumentableProxy( proxy );
            }

            // Recurse to the child
            this.registerDummyInstrumentableInner( instrumentable, proxy, fullChildName, newChildName );
        }
        else
        {
            // The child does not contain and '.' characters, so we are at the correct location.
            String fullChildName = instrumentableName + "." + childName;

            this.getLogger().debug( "Registering Child Instrumentable: " + fullChildName );

            // See if a proxy exists for the child Instrumentable yet.
            InstrumentableProxy proxy =
                instrumentableProxy.getChildInstrumentableProxy( fullChildName );
            if( proxy == null )
            {
                proxy = new InstrumentableProxy(
                    this, instrumentableProxy, fullChildName, childName );
                proxy.enableLogging( this.getLogger() );
                this.incrementInstrumentableCount();

                instrumentableProxy.addChildInstrumentableProxy( proxy );
            }

            // Recurse to the child
            this.registerInstrumentableInner( instrumentable, proxy, fullChildName );
        }
    }

    /**
     * Examines a instrumentable and Registers all of its child Instrumentables
     *  and Instruments.
     * <p>
     * Only called when m_semaphore is locked.
     */
    private void registerInstrumentableInner( Instrumentable instrumentable,
                                              InstrumentableProxy instrumentableProxy,
                                              String instrumentableName )
        throws Exception
    {
        // Mark the instrumentable proxy as registered.
        instrumentableProxy.setRegistered();

        // Loop over the Instruments published by this Instrumentable
        Instrument[] instruments = instrumentable.getInstruments();
        for( int i = 0; i < instruments.length; i++ )
        {
            Instrument instrument = instruments[ i ];
            String instrumentName = instrument.getInstrumentName();
            String fullInstrumentName = instrumentableName + "." + instrumentName;

            this.getLogger().debug( "Registering Instrument: " + fullInstrumentName );

            // See if a proxy exists for the Instrument yet.
            InstrumentProxy proxy = instrumentableProxy.getInstrumentProxy( fullInstrumentName );
            if( proxy == null )
            {
                proxy = new InstrumentProxy(
                    instrumentableProxy, fullInstrumentName, instrumentName );
                proxy.enableLogging( this.getLogger() );
                this.incrementInstrumentCount();

                // Set the type of the new InstrumentProxy depending on the
                //  class of the actual Instrument.
                if( instrument instanceof CounterInstrument )
                {
                    proxy.setType( DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER );
                }
                else if( instrument instanceof ValueInstrument )
                {
                    proxy.setType( DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE );
                }
                else
                {
                    throw new ServiceException( fullInstrumentName, "Encountered an unknown "
                        + "Instrument type for the Instrument with key, "
                        + fullInstrumentName + ": " + instrument.getClass().getName() );
                }

                // Mark the instrument proxy as registered.
                proxy.setRegistered();

                // Store a reference to the proxy in the Instrument.
                ( (AbstractInstrument)instrument ).setInstrumentProxy( proxy );

                instrumentableProxy.addInstrumentProxy( proxy );
            }
            else
            {
                // Register the existing proxy with the Instrument.  Make sure that the
                //  type didn't change on us.
                if( instrument instanceof CounterInstrument )
                {
                    switch( proxy.getType() )
                    {
                        case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
                            // Type is the same.
                            // Store a reference to the proxy in the Instrument.
                            ( (AbstractInstrument)instrument ).setInstrumentProxy( proxy );
                            break;

                        case DefaultInstrumentManager.INSTRUMENT_TYPE_NONE:
                            // Not yet set.  Created in configuration.
                            proxy.setType( DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER );

                            // Store a reference to the proxy in the Instrument.
                            ( (AbstractInstrument)instrument ).setInstrumentProxy( proxy );
                            break;

                        default:
                            throw new ServiceException( instrumentName,
                                "Instruments of more than one type are assigned to name: "
                                + instrumentName );
                    }
                }
                else if( instrument instanceof ValueInstrument )
                {
                    switch( proxy.getType() )
                    {
                        case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
                            // Type is the same.
                            // Store a reference to the proxy in the Instrument.
                            ( (AbstractInstrument)instrument ).setInstrumentProxy( proxy );
                            break;

                        case DefaultInstrumentManager.INSTRUMENT_TYPE_NONE:
                            // Not yet set.  Created in configuration.
                            proxy.setType( DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE );

                            // Store a reference to the proxy in the Instrument.
                            ( (AbstractInstrument)instrument ).setInstrumentProxy( proxy );
                            break;

                        default:
                            throw new ServiceException( instrumentName,
                                "Instruments of more than one type are assigned to name: "
                                + instrumentName );
                    }
                }
                else
                {
                    throw new ServiceException( instrumentName, "Encountered an unknown Instrument "
                        + "type for the Instrument with name, " + instrumentName + ": "
                        + instrument.getClass().getName() );
                }

                // Mark the instrument proxy as registered.
                proxy.setRegistered();
            }
        }

        // Loop over the child Instrumentables published by this Instrumentable
        Instrumentable[] children = instrumentable.getChildInstrumentables();
        for ( int i = 0; i < children.length; i++ )
        {
            Instrumentable child = children[i];

            // Make sure that the child instrumentable name is set.
            String childName = child.getInstrumentableName();
            if( childName == null )
            {
                String msg = "The getInstrumentableName() method of a child Instrumentable of " +
                    instrumentableName + " returned null.  Child class: " +
                    child.getClass().getName();
                this.getLogger().debug( msg );
                throw new ServiceException( instrumentable.getClass().getName(), msg );
            }

            String fullChildName = instrumentableName + "." + childName;

            this.getLogger().debug( "Registering Child Instrumentable: " + fullChildName );

            // See if a proxy exists for the child Instrumentable yet.
            InstrumentableProxy proxy =
                instrumentableProxy.getChildInstrumentableProxy( fullChildName );
            if( proxy == null )
            {
                proxy = new InstrumentableProxy(
                    this, instrumentableProxy, fullChildName, childName );
                proxy.enableLogging( this.getLogger() );
                this.incrementInstrumentableCount();

                instrumentableProxy.addChildInstrumentableProxy( proxy );
            }

            // Recurse to the child
            this.registerInstrumentableInner( child, proxy, fullChildName );
        }
    }
}

