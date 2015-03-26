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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public class InstrumentSampleFrame
    extends AbstractInternalFrame
{
    public static final String FRAME_TYPE = "sample-frame";

    private static final int STATE_NONE         = 0;
    private static final int STATE_DISCONNECTED = 1;
    private static final int STATE_MISSING      = 2;
    private static final int STATE_SNAPSHOT     = 3;
    private static final int STATE_EXPIRED      = 4;
    
    private static final ImageIcon m_iconDisconnected;
    private static final ImageIcon m_iconMissing;
    private static final ImageIcon m_iconExpired;
    
    private int m_state = STATE_NONE;
    private InstrumentManagerConnection m_connection;
    private String m_instrumentSampleName;
    private String m_fullName;
    
    /** The last time that a snapshot was received.  This is the time in the
     *   snapshot and is always in Server time.  Not the time on this system. */
    private long m_lastSnapshotTime;
    
    /** A buffered set of sample values.  May be null. */
    private int[] m_samples;
    
    /** The LineChart currently visible in the Frame.  May be null. */
    private LineChart m_lineChart;

    /*---------------------------------------------------------------
     * Class Initializer
     *-------------------------------------------------------------*/
    static
    {
        // Load the icons.
        ClassLoader cl = InstrumentManagerTreeCellRenderer.class.getClassLoader();
        m_iconDisconnected =
            new ImageIcon( cl.getResource( NodeData.MEDIA_PATH + "sample_disconnected.gif") );
        m_iconMissing =
            new ImageIcon( cl.getResource( NodeData.MEDIA_PATH + "sample_missing.gif") );
        m_iconExpired =
            new ImageIcon( cl.getResource( NodeData.MEDIA_PATH + "sample_expired.gif") );
    }
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    InstrumentSampleFrame( Configuration stateConfig,
                           InstrumentManagerConnection connection,
                           InstrumentClientFrame frame )
        throws ConfigurationException
    {
        super( stateConfig, true, true, true, true, frame );
        
        m_instrumentSampleName = stateConfig.getAttribute( "sample" );
        m_fullName = m_instrumentSampleName;
        
        m_connection = connection;
    }

    InstrumentSampleFrame( InstrumentManagerConnection connection,
                           String sampleName,
                           InstrumentClientFrame frame )
    {
        super( "", true, true, true, true, frame );

        m_connection = connection;
        m_instrumentSampleName = sampleName;
        m_fullName = m_instrumentSampleName;
        
        setSize( new Dimension( 600, 120 ) );
    }

    /*---------------------------------------------------------------
     * AbstractInternalFrame Methods
     *-------------------------------------------------------------*/
    /**
     * Allows subclasses to fill in configuration information.  At the least, they must set
     *  a type attribute.
     */
    protected void getState( DefaultConfiguration stateConfig )
    {
        stateConfig.setAttribute( "type", FRAME_TYPE );
        stateConfig.setAttribute( "url", m_connection.getKey().toString() );
        stateConfig.setAttribute( "sample", m_instrumentSampleName );
    }
    
    void hideFrame()
    {
        //System.out.println("InstrumentSampleFrame.hideFrame()");
        
        super.hideFrame();
    }

    /*---------------------------------------------------------------
     * InternalFrameListener Methods
     *-------------------------------------------------------------*/
    public void internalFrameClosed( InternalFrameEvent event )
    {
        //System.out.println("InstrumentSampleFrame.internalFrameClosed()");
        // Tell the connection that this frame is closing.
        m_connection.hideSampleFrame( this );
        
        super.internalFrameClosed( event );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the name of the sample being displayed.
     *
     * @return The name of the sample being displayed.
     */
    public String getInstrumentSampleName()
    {
        return m_instrumentSampleName;
    }
    
    /**
     * The last time that a snapshot was received.  This is the time in the
     *  snapshot and is always in Server time.  Not the time on this system.
     *
     * @return The last snapshot time.
     */
    public long getLastSnapshotTime()
    {
        return m_lastSnapshotTime;
    }
    
    /**
     * Update the icon that is displayed for the frame.
     */
    private void updateIcon()
    {
        ImageIcon icon;
        DefaultMutableTreeNode sampleNode =
            m_connection.getInstrumentSampleTreeNode( m_instrumentSampleName );
        if ( sampleNode != null )
        {
            // We have a sample node, so build up a nice name
            InstrumentSampleNodeData sampleNodeData =
                (InstrumentSampleNodeData)sampleNode.getUserObject();
                
            // Set the icon
            icon = sampleNodeData.getIcon();
        }
        else if ( m_state == STATE_MISSING )
        {
            icon = m_iconMissing;
        }
        else if ( m_state == STATE_EXPIRED )
        {
            icon = m_iconExpired;
        }
        else
        {
            icon = m_iconDisconnected;
        }
        
        // Only change the icon if it is really different
        if ( getFrameIcon() != icon )
        {
            setFrameIcon( icon );
        }
    }
    
    /**
     * Sets the title of the frame and obtains a reference to the
     *  InstrumentSampleDescriptor in the process.  The title is made up of the
     *  descriptions of all the elements up to the sample in reverse order.
     * <p>
     * Only called when synchronized.
     */
    private void updateTitle()
    {
        DefaultMutableTreeNode sampleNode =
            m_connection.getInstrumentSampleTreeNode( m_instrumentSampleName );
        if ( sampleNode != null )
        {
            // We have a sample node, so build up a nice name
            StringBuffer sb = new StringBuffer();
            InstrumentSampleNodeData sampleNodeData =
                (InstrumentSampleNodeData)sampleNode.getUserObject();
            
            sb.append( sampleNodeData.getDescription() );
            
            // Loop up to the root, appending each description.
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)sampleNode.getParent();
            while( parentNode != null )
            {
                Object userObject = parentNode.getUserObject();
                if ( ( userObject == null ) || !( userObject instanceof NodeData) )
                {
                    parentNode = null;
                }
                else
                {
                    sb.append( " / " );
                    sb.append( ((NodeData)userObject).getDescription() );
                    parentNode = (DefaultMutableTreeNode)parentNode.getParent();
                }
            }
            
            // Store the full name so that we can reuse it later.
            m_fullName = sb.toString();
        }
        
        // Build the title
        StringBuffer sb = new StringBuffer();
        switch ( m_state )
        {
        case STATE_SNAPSHOT:
            break;
            
        case STATE_MISSING:
            sb.append( "[Missing] " );
            break;
            
        case STATE_EXPIRED:
            sb.append( "[Expired] " );
            break;
            
        default:
            sb.append( "[Disconnected] " );
            break;
        }
        
        // Add the full name
        sb.append( m_fullName );
        
        // Add the connection info
        sb.append( " / " );
        sb.append( m_connection.getTitle() );
        
        String title = sb.toString();
        
        // Only set the title if it has changed to avoid repaints
        if ( !getTitle().equals( title ) )
        {
            setTitle( title );
        }
    }

    /**
     * Initializes the chart
     *
     * @param snapshot InstrumentSampleSnapshotData to use to initialize the chart.
     */
    private void initChart( InstrumentSampleSnapshotData snapshot )
    {
        // Decide on a line interval based on the interval of the sample.
        long interval = Math.max( 1, snapshot.getInterval() );
        int hInterval;
        String format;
        String detailFormat;
        if( interval < 1000 )
        {
            // Once per 10 seconds.
            hInterval = (int)( 10000 / interval );
            format = "{3}:{4}:{5}";
            detailFormat = "{1}/{2} {3}:{4}:{5}.{6}";
        }
        else if( interval < 60000 )
        {
            // Once per minute.
            hInterval = (int)( 60000 / interval );
            format = "{3}:{4}:{5}";
            detailFormat = "{1}/{2} {3}:{4}:{5}";
        }
        else if( interval < 600000 )
        {
            // Once per 10 minutes
            hInterval = (int)( 600000 / interval );
            format = "{1}/{2} {3}:{4}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 3600000 )
        {
            // Once per hour.
            hInterval = (int)( 3600000 / interval );
            format = "{1}/{2} {3}:{4}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 86400000 )
        {
            // Once per day.
            hInterval = (int)( 86400000 / interval );
            format = "{1}/{2}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 604800000 )
        {
            // Once per week.
            hInterval = (int)( 604800000 / interval );
            format = "{0}/{1}/{2}";
            detailFormat = "{0}/{1}/{2}";
        }
        else
        {
            // Default to every 10 points.
            hInterval = 10;
            format = "{0}/{1}/{2}";
            detailFormat = "{0}/{1}/{2}";
        }

        // Make sure that the content pane is empty.
        getContentPane().removeAll();
            
        // Actually create the chart and add it to the content pane
        m_lineChart = new LineChart(
            hInterval, interval, format, detailFormat, 20, getFrame().isAntialias() );
        getContentPane().add( m_lineChart );
    }
    
    private void setStateSnapshot( InstrumentSampleSnapshotData snapshot )
    {
        if ( m_state != STATE_SNAPSHOT )
        {
            initChart( snapshot );
            
            m_state = STATE_SNAPSHOT;
            
            updateTitle();
        }

        int size = snapshot.getSize();
        
        // The new samples that came in the snapshot may or may not be a complete
        //  set.  They usually will be partial.
        int[] newSamples = snapshot.getSamples();
        if ( newSamples.length == size )
        {
            // We got a full sample set.  Just replace the internal sample buffer.
            m_samples = newSamples;
        }
        else if ( newSamples.length > size )
        {
            // Should never happen.  But protect against it to avoid errors.
            //  Only use the last {size} samples.
            m_samples = new int[size];
            System.arraycopy( newSamples, newSamples.length - size, m_samples, 0, size );
        }
        else
        {
            // We received a partial sample set, so we need to adjust the existing
            //  data then copy the new data over the array.
            
            // Make sure that the local buffer is the correct size.
            if ( m_samples == null )
            {
                m_samples = new int[size];
            }
            else if ( m_samples.length != size )
            {
                // The sample size changed on the server.
                int[] tmpSamples = new int[size];
                if ( m_samples.length > size )
                {
                    // Got smaller.  The beginning of the existing array gets trimmed.
                    System.arraycopy( m_samples, m_samples.length - size, tmpSamples, 0, size );
                }
                else
                {
                    // Got larger.  The beginning of the new array will be all 0s.
                    System.arraycopy(
                        m_samples, 0, tmpSamples, size - m_samples.length, m_samples.length );
                }
                m_samples = tmpSamples;
            }
            
            // Calculate the age of the buffered samples.
            long age = snapshot.getTime() - m_lastSnapshotTime;
            int intervalAge = (int)( age / snapshot.getInterval() );
            if ( intervalAge > m_samples.length )
            {
                intervalAge = m_samples.length;
            }
            
            // Move existing values down in the buffer as necessary.
            if ( ( intervalAge > 0 ) && ( intervalAge < m_samples.length ) )
            {
                // We are moving down in the array so we can do this withtout a temp array.
                System.arraycopy( m_samples, intervalAge, m_samples, 0, size - intervalAge );
            }
            
            // This will only happen if there is a timing problem, but fill in any
            //  space between the old and new values with 0s.
            if ( intervalAge > newSamples.length )
            {
                for ( int i = size - intervalAge; i < size - newSamples.length; i++ )
                {
                    m_samples[i] = 0;
                }
            }
            
            // Copy the new samples over the end of the sample buffer.
            System.arraycopy(
                newSamples, 0, m_samples, size - newSamples.length, newSamples.length );
        }
        
        // Update the contents of the chart.
        m_lineChart.setValues( m_samples, snapshot.getTime() );
            
        // Store the time of this snapshot.
        m_lastSnapshotTime = snapshot.getTime();
        
        // Icon can change.
        updateIcon();
    }

    /**
     * Sets the state of the frame to show that the connection is closed.
     */
    private void setStateDisconnected()
    {
        if ( m_state != STATE_DISCONNECTED )
        {
            getContentPane().removeAll();
            
            // Not connected.
            JLabel label = new JLabel( "Not connected" );
            label.setForeground( Color.red );
            label.setHorizontalAlignment( SwingConstants.CENTER );
            label.setVerticalAlignment( SwingConstants.CENTER );
            
            getContentPane().add( label );
            
            m_state = STATE_DISCONNECTED;
            
            updateTitle();
            updateIcon();
        }
    }
    
    /**
     * Sets the state of the frame to show that the sample could not be found.
     */
    private void setStateSampleMissing()
    {
        if ( m_state != STATE_MISSING )
        {
            getContentPane().removeAll();
            
            // Not connected.
            JLabel label = new JLabel( "Sample not found" );
            label.setForeground( Color.red );
            label.setHorizontalAlignment( SwingConstants.CENTER );
            label.setVerticalAlignment( SwingConstants.CENTER );
            
            getContentPane().add( label );
            
            m_state = STATE_MISSING;
            
            updateTitle();
            updateIcon();
        }
    }
    
    /**
     * Sets the state of the frame to show that the sample could not be found.
     */
    private void setStateSampleExpired()
    {
        if ( m_state != STATE_EXPIRED )
        {
            // Leave the chart as is.  It will just stop updating.
            // Change its background color slightly.
            m_lineChart.setBackground( new Color( 220, 220, 220 ) );
            m_lineChart.repaint();
            
            m_state = STATE_EXPIRED;
            
            updateTitle();
            updateIcon();
        }
    }
    
    /**
     * Called to update the frame when a snapshot is already available.
     *
     * @param snapshot Snapshot to update the frame with.  Null if unavailable.
     */
    public void updateSnapshot( InstrumentSampleSnapshotData snapshot )
    {
        if ( m_connection.isDeleted() )
        {
            // The connection was closed and deleted.
            hideFrame();
        }
        else
        {
            if ( m_lineChart != null )
            {
                m_lineChart.setAntialias( getFrame().isAntialias() );
            }
            
            if ( snapshot == null )
            {
                // A sample was not available.  Why.
                if ( !m_connection.isConnected() )
                {
                    // Connection was closed.
                    setStateDisconnected();
                }
                else if ( ( m_state == STATE_SNAPSHOT ) || ( m_state == STATE_EXPIRED ) )
                {
                    // We were getting snapshots, then they stopped.  The sample expired.
                    setStateSampleExpired();
                }
                else
                {
                    // Sample not found.
                    setStateSampleMissing();
                }
            }
            else
            {
                setStateSnapshot( snapshot );
            }
        }
    }
    
    /**
     * Called to update the frame and request a new snapshot from the server.
     */
    void update()
    {
        InstrumentSampleSnapshotData snapshot = null;
        if ( !m_connection.isDeleted() )
        {
            snapshot = m_connection.getSampleSnapshot( m_instrumentSampleName );
        }
        
        updateSnapshot( snapshot );
    }
}

