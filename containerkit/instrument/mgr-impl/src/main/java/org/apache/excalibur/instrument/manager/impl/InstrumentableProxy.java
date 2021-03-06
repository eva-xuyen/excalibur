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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;

/**
 * A InstrumentableProxy makes it easy for the InstrumentManager to manage
 *  Instrumentables and their Instruments.
 * <p>
 * Not Synchronized.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
class InstrumentableProxy
    extends AbstractLogEnabled
    implements Configurable
{
    /** InstrumentManager which owns the proxy. */
    private DefaultInstrumentManagerImpl m_instrumentManager;
    
    /** The parent Instrumentable proxy or null if this is a top level
     *   Instrumentable. */
    private InstrumentableProxy m_parentInstrumentableProxy;
    
    /** Configured flag. */
    private boolean m_configured;

    /** Registered flag. */
    private boolean m_registered;

    /** The name used to identify a Instrumentable. */
    private String m_name;

    /** The description of the Instrumentable. */
    private String m_description;

    /** The Descriptor for the Instrumentable. */
    private InstrumentableDescriptorImpl m_descriptor;

    /** Map of the Child InstrumentableProxies owned by this InstrumentableProxy. */
    private HashMap m_childInstrumentableProxies = new HashMap();

    /** Optimized array of the child InstrumentableProxies. */
    private InstrumentableProxy[] m_childInstrumentableProxyArray;

    /** Optimized array of the child InstrumentableDescriptors. */
    private InstrumentableDescriptor[] m_childInstrumentableDescriptorArray;

    /** Map of the InstrumentProxies owned by this InstrumentableProxy. */
    private HashMap m_instrumentProxies = new HashMap();

    /** Optimized array of the InstrumentProxies. */
    private InstrumentProxy[] m_instrumentProxyArray;

    /** Optimized array of the InstrumentDescriptors. */
    private InstrumentDescriptor[] m_instrumentDescriptorArray;
    
    /** State Version. */
    private int m_stateVersion;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentableProxy.
     *
     * @param instrumentManager InstrumentManager which owns the proxy.
     * @param parentInstrumentableProxy The parent Instrumentable proxy or null
     *                                  if this is a top level Instrumentable.
     * @param name The name used to identify a Instrumentable.
     * @param description The description of the the Instrumentable.
     */
    InstrumentableProxy( DefaultInstrumentManagerImpl instrumentManager,
                         InstrumentableProxy parentInstrumentableProxy,
                         String name,
                         String description )
    {
        m_instrumentManager = instrumentManager;
        m_parentInstrumentableProxy = parentInstrumentableProxy;
        m_name = name;
        m_description = description;

        // Create the descriptor
        m_descriptor = new InstrumentableDescriptorImpl( this );
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Configures the Instrumentable.  Called from the InstrumentManager's
     *  configure method.  The class does not need to be configured to
     *  function correctly.
     *
     * @param configuration Instrumentable configuration element from the
     *                      InstrumentManager's configuration.
     *
     * @throws ConfigurationException If there are any configuration problems.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        synchronized( this )
        {
            // The description is optional.  Default to the description from the constructor.
            m_description = configuration.getAttribute( "description", m_description );

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Configuring Instrumentable: " + m_name + " as \"" +
                                   m_description + "\"" );
            }

            m_configured = true;
            
            // Configure any child Instrumentables
            Configuration[] childConfs = configuration.getChildren( "instrumentable" );
            for( int i = 0; i < childConfs.length; i++ )
            {
                Configuration childConf = childConfs[ i ];
                String childName = childConf.getAttribute( "name" );
                String fullChildName = m_name + "." + childName;
                
                // See if the instrumentable already exists.
                InstrumentableProxy childProxy = getChildInstrumentableProxy( fullChildName );
                if( childProxy == null )
                {
                    childProxy = new InstrumentableProxy(
                        m_instrumentManager, this, fullChildName, childName );
                    childProxy.enableLogging( getLogger() );
                    m_instrumentManager.incrementInstrumentableCount();
                    m_childInstrumentableProxies.put( fullChildName, childProxy );
    
                    // Clear the optimized arrays
                    m_childInstrumentableProxyArray = null;
                    m_childInstrumentableDescriptorArray = null;
                }
                // Always configure the instrumentable.
                childProxy.configure( childConf );
            }
            
            // Configure any Instruments
            Configuration[] instrumentConfs = configuration.getChildren( "instrument" );
            for( int i = 0; i < instrumentConfs.length; i++ )
            {
                Configuration instrumentConf = instrumentConfs[ i ];
                String instrumentName = instrumentConf.getAttribute( "name" );
                String fullInstrumentName = m_name + "." + instrumentName;

                // See if the instrument already exists.
                InstrumentProxy instrumentProxy = getInstrumentProxy( fullInstrumentName );
                if ( instrumentProxy == null )
                {
                    instrumentProxy =
                        new InstrumentProxy( this, fullInstrumentName, instrumentName );
                    instrumentProxy.enableLogging( getLogger() );
                    m_instrumentManager.incrementInstrumentCount();
                    m_instrumentProxies.put( fullInstrumentName, instrumentProxy );
    
                    // Clear the optimized arrays
                    m_instrumentProxyArray = null;
                    m_instrumentDescriptorArray = null;
                }
                // Always configure the instrument
                instrumentProxy.configure( instrumentConf );
            }
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns instrumentManager which owns the proxy.
     *
     * @return InstrumentManager which owns the proxy.
     */
    DefaultInstrumentManagerImpl getInstrumentManager()
    {
        return m_instrumentManager;
    }
    
    /**
     * Returns the parent InstrumentableProxy or null if this is a top level
     *  proxy.
     *
     * @return The parent InstrumentableProxy or null.
     */
    InstrumentableProxy getParentInstrumentableProxy()
    {
        return m_parentInstrumentableProxy;
    }
    
    /**
     * Returns true if the instrumentable was configured in the instrumentables
     *  section of the configuration.
     *
     * @return True if configured.
     */
    boolean isConfigured()
    {
        return m_configured;
    }

    /**
     * Returns true if the Instrumentable was registered with the Instrument
     *  Manager.
     *
     * @return True if registered.
     */
    boolean isRegistered()
    {
        return m_registered;
    }
    
    /**
     * Called by the InstrumentManager whenever an Instrumentable assigned to
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
     * Gets the name for the Instrumentable.  The Instrumentable Name is used
     *  to uniquely identify the Instrumentable during its configuration and to
     *  gain access to a InstrumentableDescriptor through an InstrumentManager.
     *
     * @return The name used to identify a Instrumentable.
     */
    String getName()
    {
        return m_name;
    }

    /**
     * Sets the description for the instrumentable object.  This description will
     *  be set during the configuration of the Instrumentable if a configuration
     *  exists.
     *
     * @param description The description of the Instrumentable.
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
     * Gets the description of the Instrumentable.
     *
     * @return The description of the Instrumentable.
     */
    String getDescription()
    {
        return m_description;
    }

    /**
     * Returns a Descriptor for the Instrumentable.
     *
     * @return A Descriptor for the Instrumentable.
     */
    InstrumentableDescriptor getDescriptor()
    {
        return m_descriptor;
    }

    /*---------------------------------------------------------------
     * Methods (child Instrumentables)
     *-------------------------------------------------------------*/
    /**
     * Adds a child InstrumentableProxy to the Instrumentable.  This method
     *  will be called during the configuration phase if an element defining
     *  the child Instrumentable exists, or if the Instrumentable registers
     *  itself with the InstrumentManager as it is running.
     * <p>
     * This method should never be called for child Instrumentables which
     *  have already been added.
     *
     * @param childInstrumentableProxy Child InstrumentableProxy to be added.
     */
    void addChildInstrumentableProxy( InstrumentableProxy childInstrumentableProxy )
    {
        synchronized( this )
        {
            m_childInstrumentableProxies.put(
                childInstrumentableProxy.getName(), childInstrumentableProxy );

            // Clear the optimized arrays
            m_childInstrumentableProxyArray = null;
            m_childInstrumentableDescriptorArray = null;
        }
        
        stateChanged();
    }
    
    /**
     *
     */
    InstrumentableProxy getChildInstrumentableProxy( String childInstrumentableName,
                                                     boolean create )
    {
        synchronized( this )
        {
            InstrumentableProxy childInstrumentableProxy =
                (InstrumentableProxy)m_childInstrumentableProxies.get( childInstrumentableName );
            if ( ( childInstrumentableProxy == null ) && create )
            {
                //getLogger().debug( "     New Child Instrumentable" );
                // Not found, create it.
                int pos = childInstrumentableName.lastIndexOf( '.' );
                String childName;
                if ( pos >= 0 )
                {
                    childName = childInstrumentableName.substring( pos + 1 );
                }
                else
                {
                    childName = childInstrumentableName;
                }
                
                childInstrumentableProxy = new InstrumentableProxy(
                    m_instrumentManager, this, childInstrumentableName, childName );
                childInstrumentableProxy.enableLogging( getLogger() );
                m_instrumentManager.incrementInstrumentableCount();
                m_childInstrumentableProxies.put(
                    childInstrumentableName, childInstrumentableProxy );

                // Clear the optimized arrays
                m_childInstrumentableProxyArray = null;
                m_childInstrumentableDescriptorArray = null;
            }
            
            //getLogger().debug( "  -> " + childInstrumentableProxy );
            return childInstrumentableProxy;
        }
    }

    /**
     * Returns a child InstrumentableProxy based on its name or the name of any
     *  of its children.
     *
     * @param childInstrumentableName Name of the child Instrumentable being
     *                                requested.
     *
     * @return The requested child InstrumentableProxy or null if does not
     *         exist.
     */
    InstrumentableProxy getChildInstrumentableProxy( String childInstrumentableName )
    {
        synchronized( this )
        {
            String name = childInstrumentableName;
            while( true )
            {
                InstrumentableProxy proxy =
                    (InstrumentableProxy)m_childInstrumentableProxies.get( name );
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
    }

    /**
     * Returns an array of Proxies to the child Instrumentables in this
     *  Instrumentable.
     *
     * @return An array of Proxies to the child Instrumentables in this
     *         Instrumentable.
     */
    InstrumentableProxy[] getChildInstrumentableProxies()
    {
        InstrumentableProxy[] proxies = m_childInstrumentableProxyArray;
        if( proxies == null )
        {
            proxies = updateChildInstrumentableProxyArray();
        }
        
        return proxies;
    }

    /**
     * Returns an array of Descriptors for the child Instrumentables in this
     *  Instrumentable.
     *
     * @return An array of Descriptors for the child Instrumentables in this
     *         Instrumentable.
     */
    InstrumentableDescriptor[] getChildInstrumentableDescriptors()
    {
        InstrumentableDescriptor[] descriptors = m_childInstrumentableDescriptorArray;
        if( descriptors == null )
        {
            descriptors = updateChildInstrumentableDescriptorArray();
        }
        
        return descriptors;
    }

    /**
     * Updates the cached array of child InstrumentableProxies taking
     *  synchronization into account.
     *
     * @return An array of the child InstrumentableProxies.
     */
    private InstrumentableProxy[] updateChildInstrumentableProxyArray()
    {
        synchronized( this )
        {
            InstrumentableProxy[] childInstrumentableProxyArray =
                new InstrumentableProxy[ m_childInstrumentableProxies.size() ];
            m_childInstrumentableProxies.values().toArray( childInstrumentableProxyArray );

            // Sort the array.  This is not a performance problem because this
            //  method is rarely called and doing it here saves cycles in the
            //  client.
            Arrays.sort( childInstrumentableProxyArray, new Comparator()
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
            m_childInstrumentableProxyArray = childInstrumentableProxyArray;
            
            return childInstrumentableProxyArray;
        }
    }

    /**
     * Updates the cached array of child InstrumentableDescriptors taking
     *  synchronization into account.
     *
     * @return An array of the child InstrumentableDescriptors.
     */
    private InstrumentableDescriptor[] updateChildInstrumentableDescriptorArray()
    {
        synchronized( this )
        {
            // Get the proxy array. This is done in synchronization so it is not possible that it
            //  will be reset before we obtain the descriptor array.  They are both set to null
            //  at the same time when there is a change.
            InstrumentableProxy[] childInstrumentableProxyArray = m_childInstrumentableProxyArray;
            if( childInstrumentableProxyArray == null )
            {
                childInstrumentableProxyArray = updateChildInstrumentableProxyArray();
            }

            InstrumentableDescriptor[] childInstrumentableDescriptorArray =
                new InstrumentableDescriptor[ childInstrumentableProxyArray.length ];
            for( int i = 0; i < childInstrumentableProxyArray.length; i++ )
            {
                childInstrumentableDescriptorArray[ i ] =
                    childInstrumentableProxyArray[ i ].getDescriptor();
            }

            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            m_childInstrumentableDescriptorArray = childInstrumentableDescriptorArray;
            
            return childInstrumentableDescriptorArray;
        }
    }

    /*---------------------------------------------------------------
     * Methods (Instruments)
     *-------------------------------------------------------------*/
    /**
     * Adds a InstrumentProxy to the Instrumentable.  This method will be
     *  called during the configuration phase if an element defining the
     *  Instrument exists, or if the Instrument registers itself with the
     *  InstrumentManager as it is running.
     * <p>
     * This method should never be called for Instruments which have already
     *  been added.
     *
     * @param instrumentProxy InstrumentProxy to be added.
     */
    void addInstrumentProxy( InstrumentProxy instrumentProxy )
    {
        synchronized( this )
        {
            m_instrumentProxies.put( instrumentProxy.getName(), instrumentProxy );

            // Clear the optimized arrays
            m_instrumentProxyArray = null;
            m_instrumentDescriptorArray = null;
        }
        
        stateChanged();
    }

    /**
     *
     */
    InstrumentProxy getInstrumentProxy( String instrumentName, boolean create )
    {
        synchronized( this )
        {
            InstrumentProxy instrumentProxy =
                (InstrumentProxy)m_instrumentProxies.get( instrumentName );
            if ( ( instrumentProxy == null ) && create )
            {
                //getLogger().debug( "     New Instrument" );
                // Not found, create it.
                int pos = instrumentName.lastIndexOf( '.' );
                String instName;
                if ( pos >= 0 )
                {
                    instName = instrumentName.substring( pos + 1 );
                }
                else
                {
                    instName = instrumentName;
                }
                
                instrumentProxy = new InstrumentProxy( this, instrumentName, instName );
                instrumentProxy.enableLogging( getLogger() );
                m_instrumentManager.incrementInstrumentCount();
                m_instrumentProxies.put( instrumentName, instrumentProxy );

                // Clear the optimized arrays
                m_instrumentProxyArray = null;
                m_instrumentDescriptorArray = null;
            }
            
            //getLogger().debug( "  -> " + instrumentProxy );
            return instrumentProxy;
        }
    }

    /**
     * Returns a InstrumentProxy based on its name or the name of any
     *  of its children.
     *
     * @param instrumentName Name of the Instrument being requested.
     *
     * @return The requested InstrumentProxy or null if does not exist.
     */
    InstrumentProxy getInstrumentProxy( String instrumentName )
    {
        synchronized( this )
        {
            String name = instrumentName;
            while( true )
            {
                InstrumentProxy proxy = (InstrumentProxy)m_instrumentProxies.get( name );
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
    }

    /**
     * Returns an array of Proxies to the Instruments in the Instrumentable.
     *
     * @return An array of Proxies to the Instruments in the Instrumentable.
     */
    InstrumentProxy[] getInstrumentProxies()
    {
        InstrumentProxy[] proxies = m_instrumentProxyArray;
        if( proxies == null )
        {
            proxies = updateInstrumentProxyArray();
        }
        return proxies;
    }

    /**
     * Returns an array of Descriptors for the Instruments in the Instrumentable.
     *
     * @return An array of Descriptors for the Instruments in the Instrumentable.
     */
    InstrumentDescriptor[] getInstrumentDescriptors()
    {
        InstrumentDescriptor[] descriptors = m_instrumentDescriptorArray;
        if( descriptors == null )
        {
            descriptors = updateInstrumentDescriptorArray();
        }
        return descriptors;
    }
    
    /**
     * Returns the stateVersion of the instrumentable.  The state version
     *  will be incremented each time any of the configuration of the
     *  instrumentable or any of its children is modified.
     * Clients can use this value to tell whether or not anything has
     *  changed without having to do an exhaustive comparison.
     *
     * @return The state version of the instrumentable.
     */
    int getStateVersion()
    {
        return m_stateVersion;
    }

    /**
     * Updates the cached array of InstrumentProxies taking
     *  synchronization into account.
     *
     * @return An array of the InstrumentProxies.
     */
    private InstrumentProxy[] updateInstrumentProxyArray()
    {
        synchronized( this )
        {
            InstrumentProxy[] instrumentProxyArray =
                new InstrumentProxy[ m_instrumentProxies.size() ];
            m_instrumentProxies.values().toArray( instrumentProxyArray );

            // Sort the array.  This is not a performance problem because this
            //  method is rarely called and doing it here saves cycles in the
            //  client.
            Arrays.sort( instrumentProxyArray, new Comparator()
                {
                    public int compare( Object o1, Object o2 )
                    {
                        return ((InstrumentProxy)o1).getDescription().
                            compareTo( ((InstrumentProxy)o2).getDescription() );
                    }
                    
                    public boolean equals( Object obj )
                    {
                        return false;
                    }
                } );
            
            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            m_instrumentProxyArray = instrumentProxyArray;
            
            return instrumentProxyArray;
        }
    }

    /**
     * Updates the cached array of InstrumentDescriptors taking
     *  synchronization into account.
     *
     * @return An array of the InstrumentDescriptors.
     */
    private InstrumentDescriptor[] updateInstrumentDescriptorArray()
    {
        synchronized( this )
        {
            // Get the proxy array. This is done in synchronization so it is not possible that it
            //  will be reset before we obtain the descriptor array.  They are both set to null
            //  at the same time when there is a change.
            InstrumentProxy[] instrumentProxyArray = m_instrumentProxyArray;
            if( instrumentProxyArray == null )
            {
                instrumentProxyArray = updateInstrumentProxyArray();
            }
            
            InstrumentDescriptor[] instrumentDescriptorArray =
                new InstrumentDescriptor[ instrumentProxyArray.length ];
            for( int i = 0; i < instrumentProxyArray.length; i++ )
            {
                instrumentDescriptorArray[ i ] = instrumentProxyArray[ i ].getDescriptor();
            }
            
            // Once we are done modifying this array, set it to the variable accessable outside
            //  of synchronization.
            m_instrumentDescriptorArray = instrumentDescriptorArray;
            
            return instrumentDescriptorArray;
        }
    }
    
    /**
     * Writes the current state to a PrintWriter as XML.
     *
     * @param out The PrintWriter to which the state should be written.
     */
    void writeState( PrintWriter out )
    {
        // Samples are the only things written to the state, so all we need to do is drill down
        //  to them.
        
        // Write out the states of any child instrumentables.
        InstrumentableProxy[] childProxies = getChildInstrumentableProxies();
        for( int i = 0; i < childProxies.length; i++ )
        {
            childProxies[i].writeState( out );
        }
        
        // Write out the states of any instruments.
        InstrumentProxy[] proxies = getInstrumentProxies();
        for( int i = 0; i < proxies.length; i++ )
        {
            proxies[i].writeState( out );
        }
    }
    
    /**
     * Called whenever the state of the instrumentable is changed.
     */
    protected void stateChanged()
    {
        m_stateVersion++;
        
        // Propagate to the parent
        if ( m_parentInstrumentableProxy == null )
        {
            // This is a top level Instrumentable
            m_instrumentManager.stateChanged();
        }
        else
        {
            m_parentInstrumentableProxy.stateChanged();
        }
    }
}
