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

package org.apache.excalibur.instrument.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * A Connection to the remote InstrumentManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public abstract class InstrumentManagerConnection
    extends JComponent
    implements LogEnabled, Runnable
{
    private Logger m_logger;
    private InstrumentManagerTreeModel m_treeModel;
    private InstrumentManagerTree m_tree;
    
    private InstrumentClientFrame m_frame;
    
    private boolean m_deleted;
    private Thread m_runner;
    
    private JLabel m_descriptionLabel;

    private final List m_listeners = new ArrayList();
    private InstrumentManagerConnectionListener[] m_listenerArray = null;

    private long m_lastLeaseRenewalTime;
    private HashMap m_maintainedSampleLeaseMap = new HashMap();
    private MaintainedSampleLease[] m_maintainedSampleLeaseArray = null;

    /** Maintain a list of all sample frames which are viewing data in this connection. */
    private Map m_sampleFrames = new HashMap();
    private InstrumentSampleFrame[] m_sampleFrameArray = null;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentManagerConnection.
     */
    public InstrumentManagerConnection()
    {
    }
    

    /*---------------------------------------------------------------
     * LogEnabled Methods
     *-------------------------------------------------------------*/
    public void enableLogging( Logger logger )
    {
        m_logger = logger.getChildLogger( "conn_" + getKey() );
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

    /*---------------------------------------------------------------
     * Runnable Methods
     *-------------------------------------------------------------*/
    public void run()
    {
        getLogger().debug( "Started " + Thread.currentThread().getName() );
        try
        {
            while( m_runner != null )
            {
                try
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch( InterruptedException e )
                    {
                        if ( m_runner == null )
                        {
                            return;
                        }
                    }
                    
                    update();
                    
                    m_frame.updateConnectionTab( this );
                    
                    updateSampleFrames();
                }
                catch( Throwable t )
                {
                    // Should not get here, but we want to make sure that this never happens.
                    getLogger().error(
                        "Unexpected error caught in " + Thread.currentThread().getName(), t );
                    
                    // Avoid thrashing.
                    try
                    {
                        Thread.sleep( 5000 );
                    }
                    catch ( InterruptedException e )
                    {
                        if ( m_runner == null )
                        {
                            return;
                        }
                    }
                }
            }
        }
        finally
        {
            getLogger().debug( "Stopped " + Thread.currentThread().getName() );
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Stores a reference to the client frame.
     *
     * @param frame The main client frame.
     */
    final void setFrame( InstrumentClientFrame frame )
    {
        m_frame = frame;
    }
    
    /**
     * Returns a reference to the client frame.
     *
     * @return A reference to the client frame.
     */
    final InstrumentClientFrame getFrame()
    {
        return m_frame;
    }
    
    /**
     * Called to initialize the connection object.
     */
    public void init()
    {
        m_treeModel = new InstrumentManagerTreeModel( this );
        m_treeModel.enableLogging( m_logger.getChildLogger( "treeModel" ) );
        addInstrumentManagerConnectionListener( m_treeModel );

        setLayout( new BorderLayout() );

        // Top Pane
        Box topPane = Box.createVerticalBox();

        // Top Labels
        // Row 1
        Box labels = Box.createHorizontalBox();
        labels.add( Box.createHorizontalStrut( 4 ) );
        m_descriptionLabel = new JLabel( getInstrumentManager().getDescription() );
        labels.add( m_descriptionLabel );
        labels.add( Box.createHorizontalGlue() );
        topPane.add( labels );
        
        // Row 2
        labels = Box.createHorizontalBox();
        labels.add( Box.createHorizontalStrut( 4 ) );
        labels.add( new JLabel( "URL: " + getKey().toString() ) );
        labels.add( Box.createHorizontalGlue() );
        topPane.add( labels );

        topPane.add( Box.createVerticalStrut( 4 ) );
        
        // Top Buttons
        Action gcAction = new AbstractAction( "Invoke GC" )
        {
            public void actionPerformed( ActionEvent event )
            {
                SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            InstrumentManagerConnection.this.invokeGC();
                        }
                    });
            }
        };
        JButton gcButton = new JButton( gcAction );

        Action refreshAction = new AbstractAction( "Refresh" )
        {
            public void actionPerformed( ActionEvent event )
            {
                SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            InstrumentManagerConnection.this.getTreeModel().refreshModel();
                        }
                    });
            }
        };
        JButton refreshButton = new JButton( refreshAction );

        Action deleteAction = new AbstractAction( "Delete" )
        {
            public void actionPerformed( ActionEvent event )
            {
                SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            InstrumentManagerConnection.this.delete();
                        }
                    });
            }
        };
        JButton deleteButton = new JButton( deleteAction );

        Box buttons = Box.createHorizontalBox();
        buttons.add( Box.createHorizontalStrut( 4 ) );
        buttons.add ( gcButton );
        buttons.add( Box.createHorizontalStrut( 4 ) );
        buttons.add ( refreshButton );
        buttons.add( Box.createHorizontalStrut( 4 ) );
        buttons.add ( deleteButton );
        buttons.add( Box.createHorizontalGlue() );
        topPane.add( buttons );
        
        topPane.add( Box.createVerticalStrut( 4 ) );

        add( topPane, BorderLayout.NORTH );

        // Tree Pane
        m_tree = new InstrumentManagerTree( this );
        add( m_tree, BorderLayout.CENTER );
        
        m_runner = new Thread( this, "InstrumentManagerConnection[" + getKey() + "]" );
        m_runner.start();
    }
    
    /**
     * Returns true once the connection has been deleted.
     *
     * @return True if deleted.
     */
    public boolean isDeleted()
    {
        return m_deleted;
    }
    
    /**
     * Returns the title to display in the tab for the connection.
     *
     * @return The tab title.
     */
    public String getTabTitle()
    {
        return getInstrumentManager().getName();
    }
    
    /**
     * Returns the tooltip to display in the tab for the connection.
     *
     * @return The tab tooltip.
     */
    public String getTabTooltip()
    {
        String key = getKey().toString();
        String tab = getInstrumentManager().getDescription();
        
        if ( key.equals( tab ) )
        {
            return tab;
        }
        else
        {
            return tab + " [" + key + "]";
        }
    }
    
    /**
     * Returns the title for the connection.
     *
     * @return The title.
     */
    public String getTitle()
    {
        return getInstrumentManager().getDescription();
    }
    
    /**
     * Returns the key used to identify this object.
     *
     * @return The key used to identify this object.
     */
    public abstract Object getKey();
    
    /**
     * Returns true if connected.
     *
     * @return True if connected.
     */
    public abstract boolean isConnected();
    
    /**
     * Returns the Instrument Manager.
     *
     * @return The Instrument Manager.
     */
    public abstract InstrumentManagerData getInstrumentManager();
    
    /**
     * Causes the InstrumentManagerConnection to update itself with the latest
     *  data from the server.  Called by the updateConnection method.
     */
    public void update()
    {
        // If we are currently connected then we are only looking for changes so do the
        //  regular update.  If not connected then we will want all of the data.
        if ( isConnected() )
        {
            getInstrumentManager().update();
        }
        else
        {
            getInstrumentManager().updateAll();
        }
        
        String description = getInstrumentManager().getDescription();
        if ( !m_descriptionLabel.getText().equals( description ) )
        {
            m_descriptionLabel.setText( description );
        }
        
        getTreeModel().refreshModel();
        
        // Handle the leased samples.
        if ( isConnected() )
        {
            handleLeasedSamples();
        }
    }

    /**
     * Invokes GC on the JVM running the InstrumentManager.
     */
    protected abstract void invokeGC();

    /**
     * Saves the current state into a Configuration.
     *
     * @return The state as a Configuration.
     */
    public Configuration saveState()
    {
        synchronized( this )
        {
            DefaultConfiguration state = new DefaultConfiguration( "connection", "-" );
            
            // Save any maintained samples
            MaintainedSampleLease[] samples = getMaintainedSampleLeaseArray();
            for ( int i = 0; i < samples.length; i++ )
            {
                state.addChild( samples[ i ].saveState() );
            }
            
            return state;
        }
    }

    /**
     * Loads the state from a Configuration object.
     *
     * @param state Configuration object to load state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    public void loadState( Configuration state )
        throws ConfigurationException
    {
        synchronized( this )
        {
            // Load any maintained samples
            Configuration[] sampleConfs = state.getChildren( "maintained-sample" );
            for( int i = 0; i < sampleConfs.length; i++ )
            {
                Configuration sampleConf = sampleConfs[ i ];
                String instrumentName = sampleConf.getAttribute( "instrument-name" );
                int sampleType = InstrumentSampleUtils.resolveInstrumentSampleType(
                    sampleConf.getAttribute( "type" ) );
                long sampleInterval = sampleConf.getAttributeAsLong( "interval" );
                int sampleSize = sampleConf.getAttributeAsInteger( "size" );
                long sampleLeaseDuration = sampleConf.getAttributeAsLong( "lease-duration" );
                String sampleDescription = sampleConf.getAttribute( "description" );

                startMaintainingSample( instrumentName, sampleType, sampleInterval, sampleSize,
                    sampleLeaseDuration, sampleDescription );
            }
        }
    }
    
    /**
     * Updates all registered SampleFrames with the latest data from the
     *  server.  The status of all Sample Frames is also handled by this
     *  method, so it must handle disconnected connections and missing or
     *  expired samples correctly.
     */
    public void updateSampleFrames()
    {
        InstrumentSampleFrame[] frames = getSampleFrameArray();
        for ( int i = 0; i < frames.length; i++ )
        {
            InstrumentSampleFrame frame = frames[i];
            frame.update();
        }
    }
    
    /**
     * Returns the TreeModel which contains the entire Instrument tree for
     *  this connection.
     *
     * @return The TreeModel.
     */
    InstrumentManagerTreeModel getTreeModel()
    {
        return m_treeModel;
    }

    DefaultMutableTreeNode getInstrumentSampleTreeNode( String sampleName )
    {
        return m_treeModel.getInstrumentSampleTreeNode( sampleName );
    }

    /**
     * Adds a InstrumentManagerConnectionListener to the list of listeners
     *  which receive state updates of the connection.
     *
     * @param listener InstrumentManagerConnectionListener to start receiving
     *                 state updates.
     */
    void addInstrumentManagerConnectionListener(
        InstrumentManagerConnectionListener listener )
    {
        synchronized (this)
        {
            m_listeners.add( listener );
            m_listenerArray = null;
        }
    }

    /**
     * Removes a InstrumentManagerConnectionListener from the list of listeners
     *  which receive state updates of the connection.
     *
     * @param listener InstrumentManagerConnectionListener to stop receiving
     *                 state updates.
     */
    void removeInstrumentManagerConnectionListener(
        InstrumentManagerConnectionListener listener )
    {
        synchronized (this)
        {
            m_listeners.remove( listener );
            m_listenerArray = null;
        }
    }

    /**
     * Get a threadsafe array of the current listeners avoiding synchronization
     *  when possible.  The contents of the returned array will never change.
     *
     * @return An array of the currently registered listeners
     */
    protected InstrumentManagerConnectionListener[] getListenerArray()
    {
        InstrumentManagerConnectionListener[] listenerArray = m_listenerArray;
        if ( listenerArray == null )
        {
            synchronized(this)
            {
                m_listenerArray = new InstrumentManagerConnectionListener[ m_listeners.size() ];
                m_listeners.toArray( m_listenerArray );
                listenerArray = m_listenerArray;
            }
        }
        return listenerArray;
    }

    /**
     * Returns a snapshot of the specified sample.  If a snapshot can not
     *  be returned for any reason, then return null.
     *
     * @param Returns a snapshot of the specified sample.
     */
    InstrumentSampleSnapshotData getSampleSnapshot( String sampleName )
    {
        DefaultMutableTreeNode sampleNode = getInstrumentSampleTreeNode( sampleName );
        if ( sampleNode == null )
        {
            return null;
        }

        InstrumentSampleNodeData sampleNodeData =
            (InstrumentSampleNodeData)sampleNode.getUserObject();
        InstrumentSampleData sampleData = sampleNodeData.getData();
        if ( sampleData == null )
        {
            return null;
        }

        // Request the actual snapshot.
        return sampleData.getSnapshot();
    }

    /**
     * Returns a sample frame given a sample name.
     * Caller must synchronize on this connection before calling.
     *
     * @param sampleName Name of the sample requested.
     *
     * @return A sample frame given a sample name.
     */
    private InstrumentSampleFrame getSampleFrame( String sampleName )
    {
        getLogger().debug("InstrumentManagerConnection.getSampleFrame(" + sampleName + ")");
        // Assumes "this" is synchronized.
        return (InstrumentSampleFrame)m_sampleFrames.get( sampleName );
    }

    /**
     * Adds a single sample frame.
     * Caller must synchronize on this connection before calling.
     */
    private void addSampleFrame( String sampleName, InstrumentSampleFrame sampleFrame )
    {
        getLogger().debug("InstrumentManagerConnection.addSampleFrame(" + sampleName + ", frame)");
        // Assumes "this" is synchronized.
        m_sampleFrames.put( sampleName, sampleFrame );
        m_sampleFrameArray = null;
    }

    /**
     * Removes a single sample frame.
     * Caller must synchronize on this connection before calling.
     */
    private void removeSampleFrame( String sampleName )
    {
        getLogger().debug("InstrumentManagerConnection.removeSampleFrame(" + sampleName + ")");
        // Assumes "this" is synchronized.
        m_sampleFrames.remove( sampleName );
        m_sampleFrameArray = null;
    }

    /**
     * Returns a thread save array representation of the InstrumentSampleFrames.
     *
     * @return A thread save array representation of the InstrumentSampleFrames.
     */
    protected InstrumentSampleFrame[] getSampleFrameArray()
    {
        InstrumentSampleFrame[] array = m_sampleFrameArray;
        if ( array == null )
        {
            synchronized(this)
            {
                m_sampleFrameArray = new InstrumentSampleFrame[m_sampleFrames.size()];
                m_sampleFrames.values().toArray( m_sampleFrameArray );
                array = m_sampleFrameArray;
            }
        }
        return array;
    }

    /**
     * Loads an InstrumentSampleFrame from a saved state.
     *
     * @param sampleFrameState Saved state of the frame to load.
     *
     * @throws ConfigurationException If there are any problems with the state.
     */
    void loadSampleFrame( Configuration sampleFrameState )
        throws ConfigurationException
    {
        // Get the sample name
        String sampleName = sampleFrameState.getAttribute( "sample" );
        getLogger().debug( "Loading sample frame: " + sampleName );

        // See if a frame already exists.
        InstrumentSampleFrame sampleFrame;
        synchronized(this)
        {
            sampleFrame = getSampleFrame( sampleName );
            if ( sampleFrame != null )
            {
                // A frame already existed.  It needs to be closed as it will be
                // replaced by the new one.
                sampleFrame.hideFrame();
                sampleFrame = null;
            }

            // Now create the frame
            sampleFrame = new InstrumentSampleFrame( sampleFrameState, this, m_frame );
            sampleFrame.enableLogging( getLogger() );
            addSampleFrame( sampleName, sampleFrame );
            sampleFrame.addToDesktop( m_frame.getDesktopPane() );
        }
        sampleFrame.show();  // Outside of synchronization to avoid deadlocks.
    }
    
    /**
     * Displays a frame for the given sample.
     *
     * @param sampleName Name of the sample to display.
     */
    void viewSample( String sampleName )
    {
        InstrumentSampleFrame sampleFrame;
        synchronized( this )
        {
            //String sampleName = sampleNodeData.getName();
            sampleFrame = getSampleFrame( sampleName );
            if ( sampleFrame == null )
            {
                sampleFrame = new InstrumentSampleFrame( this, sampleName, m_frame );
                sampleFrame.enableLogging( getLogger() );
                addSampleFrame( sampleName, sampleFrame );
                sampleFrame.addToDesktop( m_frame.getDesktopPane() );
            }
        }

        sampleFrame.show();
        // Need to restore the frame if it is an icon.
        if ( sampleFrame.isIcon() )
        {
            // Restore the sample frame.
            try
            {
                sampleFrame.setIcon( false );
            }
            catch ( PropertyVetoException e )
            {
                // Shouldn't happen.
                getLogger().warn( "Unexpected error", e );
            }
        }

        // Set the focus of the frame so that it is selected and on top.
        try
        {
            sampleFrame.setSelected( true );
        }
        catch ( PropertyVetoException e )
        {
            // Shouldn't happen.
            getLogger().warn( "Unexpected error", e );
        }
        
        // Always update the sample immediately to make the app look responsive.
        sampleFrame.update();
    }


    /**
     * Called when the connection should be closed and then deleted along with
     *  any frames and resources that are associated with it.
     */
    void delete()
    {
        getLogger().debug( "delete()" );

        m_deleted = true;
        
        Thread runner = m_runner;
        if ( runner != null )
        {
            m_runner = null;
            runner.interrupt();
        }
        
        // Hide any of our own sample frames.
        InstrumentSampleFrame[] frames = getSampleFrameArray();
        for ( int i = 0; i < frames.length; i++ )
        {
            frames[i].hideFrame();
        }

        // Notify the listeners.
        InstrumentManagerConnectionListener[] listenerArray = getListenerArray();
        for ( int i = 0; i < listenerArray.length; i++ )
        {
            listenerArray[i].deleted( this );
        }
    }
    
    /**
     * Called when a Sample Frame is closed.
     */
    void hideSampleFrame( InstrumentSampleFrame sampleFrame )
    {
        String sampleName = sampleFrame.getInstrumentSampleName();
        synchronized(this)
        {
            removeSampleFrame( sampleName );
        }
    }

    /**
     * Start maintaining the lease for an instrument sample which already
     *  exists.
     *
     * @param instrumentName The full name of the instrument whose sample is
     *                       to be created or updated.
     * @param type The type of sample to be created.
     * @param interval Sample interval of the new sample.
     * @param size Number of samples in the new sample.
     * @param leaseDuration Length of the lease to maintain in milliseconds.
     * @param description Description to assign to the new sample.
     */
    void startMaintainingSample( String instrumentName,
                                 int    type,
                                 long   interval,
                                 int    size,
                                 long   leaseDuration,
                                 String description )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "startMaintainingSample(" + instrumentName + ", " + type + ", " +
                interval + ", " + size + ", " + leaseDuration + ", " + description + ")" );
        }

        synchronized(this)
        {
            MaintainedSampleLease sampleLease = new MaintainedSampleLease(
                instrumentName, type, interval, size, leaseDuration, description );
            String sampleName = sampleLease.getSampleName();
            m_maintainedSampleLeaseMap.put( sampleName, sampleLease );
            m_maintainedSampleLeaseArray = null;

            // Reset the last lease renewal time so that the leases along with this
            //  new one will be renewed right away.
            m_lastLeaseRenewalTime = 0;

            // Update the appropriate node in the tree model.
            DefaultMutableTreeNode sampleTreeNode =
                m_treeModel.getInstrumentSampleTreeNode( sampleName );

            if ( sampleTreeNode != null )
            {
                InstrumentSampleNodeData sampleNodeData =
                    (InstrumentSampleNodeData)sampleTreeNode.getUserObject();

                sampleNodeData.setLeaseDuration( leaseDuration );
                sampleNodeData.setDescription( description );
                m_treeModel.updateInstrumentSample( sampleNodeData.getData(), sampleTreeNode );
            }
        }
    }

    /**
     * Stop maintaining the lease for an instrument sample which already
     *  exists.
     */
    void stopMaintainingSample( String sampleName )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "stopMaintainingSample(" + sampleName + ")" );
        }

        synchronized(this)
        {
            m_maintainedSampleLeaseMap.remove( sampleName );
            m_maintainedSampleLeaseArray = null;

            // Update the appropriate node in the tree model.
            DefaultMutableTreeNode sampleTreeNode =
                m_treeModel.getInstrumentSampleTreeNode( sampleName );
            if ( sampleTreeNode != null )
            {
                InstrumentSampleNodeData sampleNodeData =
                    (InstrumentSampleNodeData)sampleTreeNode.getUserObject();

                sampleNodeData.setLeaseDuration( 0 );
                m_treeModel.updateInstrumentSample( sampleNodeData.getData(), sampleTreeNode );
            }
        }
    }

    /**
     * Returns a MaintainedSampleLease given a name if the sample is being
     *  maintained. Otherwise returns null.
     *
     * @param sampleName Name of the sample being requested.
     *
     * @return A MaintainedSampleLease given a name.
     */
    MaintainedSampleLease getMaintainedSampleLease( String sampleName )
    {
        synchronized(this)
        {
            return (MaintainedSampleLease)m_maintainedSampleLeaseMap.get( sampleName );
        }
    }

    /**
     * Returns a thread save array representation of the MaintainedSampleLeases.
     *
     * @return A thread save array representation of the MaintainedSampleLeases.
     */
    private MaintainedSampleLease[] getMaintainedSampleLeaseArray()
    {
        MaintainedSampleLease[] array = m_maintainedSampleLeaseArray;
        if ( array == null )
        {
            synchronized(this)
            {
                m_maintainedSampleLeaseArray =
                    new MaintainedSampleLease[ m_maintainedSampleLeaseMap.size() ];
                m_maintainedSampleLeaseMap.values().toArray( m_maintainedSampleLeaseArray );
                array = m_maintainedSampleLeaseArray;
            }
        }
        return array;
    }

    /**
     * Called once each second by the main worker thread of the client.  This
     *  method is responsible for maintaining and expiring leased samples.
     */
    void handleLeasedSamples()
    {
        // If we are not connected, then there is nothing to be done here.

        // Only renew leases once every 30 seconds.
        long now = System.currentTimeMillis();
        if ( now - m_lastLeaseRenewalTime > 30000 )
        {
            getLogger().debug( "Renew Leases:" );
            
            MaintainedSampleLease[] leases = getMaintainedSampleLeaseArray();
            String[] instrumentNames = new String[leases.length];
            String[] descriptions = new String[leases.length];
            long[] intervals = new long[leases.length];
            int[] sampleCounts = new int[leases.length];
            long[] leaseTimes = new long[leases.length];
            int[] sampleTypes = new int[leases.length];
            for ( int i = 0; i < leases.length; i++ )
            {
                MaintainedSampleLease lease = leases[i];
                getLogger().debug( " lease: " + lease.getSampleName() );
                
                instrumentNames[i] = lease.getInstrumentName();
                descriptions[i] = lease.getDescription();
                intervals[i] = lease.getInterval();
                sampleCounts[i] = lease.getSize();
                leaseTimes[i] = lease.getLeaseDuration();
                sampleTypes[i] = lease.getType();
            }
            
            // Regardless of whether the samples already exists or not, they
            //  are created or extended the same way.  This way the client
            //  will recreate a sample if it has expored.
            getInstrumentManager().createInstrumentSamples(
                instrumentNames, descriptions, intervals, sampleCounts, leaseTimes, sampleTypes );

            // Also, take this oportunity to update all of the leased samples in
            //  the model.
            m_treeModel.renewAllSampleLeases();

            m_lastLeaseRenewalTime = now;
        }

        // Now have the TreeModel purge any expired samples from the tree.
        m_treeModel.purgeExpiredSamples();
    }
    
    
    /**
     * Create a new Sample assigned to the specified instrument data.
     *
     * @param instrumentData Instrument to add a sample to.
     */
    void showCreateSampleDialog( final InstrumentData instrumentData )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                CreateSampleDialog dialog = new CreateSampleDialog(
                    m_frame, instrumentData.getName(), instrumentData.getDescription(),
                    instrumentData.getType()  );
                dialog.show();

                if ( dialog.getAction() == CreateSampleDialog.BUTTON_OK )
                {
                    String description = dialog.getSampleDescription();
                    long interval = dialog.getInterval();
                    int sampleCount = dialog.getSampleCount();
                    long leaseTime = dialog.getLeaseTime();
                    int type = dialog.getSampleType();
                    boolean maintain = dialog.getMaintainLease();
                    
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "New Sample: desc=" + description
                            + ", interval=" + interval
                            + ", size=" + sampleCount
                            + ", lease=" + leaseTime
                            + ", type=" + type
                            + ", maintain=" + maintain );
                    }

                    // If the sample already exists on the server, then the existing one
                    //  will be returned.
                    boolean success = instrumentData.createInstrumentSample(
                        description,
                        interval,
                        sampleCount,
                        leaseTime,
                        type );
                    
                    // Figure out what the name of the new sample will be
                    String sampleName = InstrumentSampleUtils.generateFullInstrumentSampleName(
                        instrumentData.getName(), type, interval, sampleCount );
                    
                    if ( success )
                    {
                        // If configured to do so, start maintaining the sample
                        if ( maintain )
                        {
                            startMaintainingSample( instrumentData.getName(), type, interval,
                                sampleCount, leaseTime, description );
                        }
                        
                        // Display a sample frame.
                        viewSample( sampleName );
                    }
                    else
                    {
                        getLogger().warn( "Attempt to register the sample with the server failed: "
                            + sampleName );
                    }
                }
            }
        } );
    }
    
    /**
     * Returns a string representation of the connection.
     */
    public String toString()
    {
        return getClass().getName() + " : " + getKey();
    }
}