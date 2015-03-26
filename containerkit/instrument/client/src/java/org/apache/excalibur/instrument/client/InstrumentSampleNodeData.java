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

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class InstrumentSampleNodeData
    extends NodeData
{
    private static final int ICON_TYPE_CNT                 = 0;
    private static final int ICON_TYPE_MAX                 = 1;
    private static final int ICON_TYPE_MEAN                = 2;
    private static final int ICON_TYPE_MIN                 = 3;
    
    private static final int ICON_SUBTYPE_CONF             = 0;
    private static final int ICON_SUBTYPE_LEASE            = 1;
    private static final int ICON_SUBTYPE_MAINTAINED_LEASE = 2;
    private static final int ICON_SUBTYPE_OLD              = 3;
    
    private static final ImageIcon[][] m_icons = new ImageIcon[4][4];
    
    private String m_instrumentName;
    private InstrumentSampleData m_data;
    private InstrumentManagerConnection m_connection;
    
    private boolean m_configured;
    
    /* The time the current lease expires, or 0. */
    private long m_leaseExpireTime;
    
    /** The length of the maintained lease. */
    private long m_leaseDuration;
    
    /** Type of the sample. */
    private int m_type;
    
    /** Number of points in the sample. */
    private int m_size;
    
    /** Time interval of the sample points. */
    private long m_interval;
    
    /*---------------------------------------------------------------
     * Class Initializer
     *-------------------------------------------------------------*/
    static
    {
        // Load the icons.
        ClassLoader cl = InstrumentManagerTreeCellRenderer.class.getClassLoader();
        
        loadTypeIcons( cl, ICON_TYPE_CNT,  MEDIA_PATH + "sample_cnt_" );
        loadTypeIcons( cl, ICON_TYPE_MAX,  MEDIA_PATH + "sample_max_" );
        loadTypeIcons( cl, ICON_TYPE_MEAN, MEDIA_PATH + "sample_mean_" );
        loadTypeIcons( cl, ICON_TYPE_MIN , MEDIA_PATH + "sample_min_" );
    }
    
    private static void loadTypeIcons( ClassLoader cl, int type, String prefix )
    {
        m_icons[type][ICON_SUBTYPE_CONF] =
            new ImageIcon( cl.getResource( prefix + "conf.gif") );
        
        m_icons[type][ICON_SUBTYPE_LEASE] =
            new ImageIcon( cl.getResource( prefix + "lease.gif") );
        
        m_icons[type][ICON_SUBTYPE_MAINTAINED_LEASE] =
            new ImageIcon( cl.getResource( prefix + "mlease.gif") );
        
        m_icons[type][ICON_SUBTYPE_OLD] =
            new ImageIcon( cl.getResource( prefix + "old.gif") );
    }
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    InstrumentSampleNodeData( String instrumentName,
                              InstrumentSampleData data,
                              InstrumentManagerConnection connection )
    {
        m_instrumentName = instrumentName;
        m_data = data;
        m_connection = connection;
        
        update();
    }
    
    
    /*---------------------------------------------------------------
     * NodeData Methods
     *-------------------------------------------------------------*/
    /**
     * Get the icon to display for the node.
     *
     * @return the icon to display for the node.
     */
    ImageIcon getIcon()
    {
        int iconType;
        switch ( getType() )
        {
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            iconType = ICON_TYPE_CNT;
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            iconType = ICON_TYPE_MAX;
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MEAN:
            iconType = ICON_TYPE_MEAN;
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            iconType = ICON_TYPE_MIN;
            break;
            
        default:
            throw new IllegalStateException( "Encountered an unknown instrument sample type: " +
                getType() );
        }
        
        int iconSubtype;
        if ( isConfigured() )
        {
            iconSubtype = ICON_SUBTYPE_CONF;
        }
        else if ( isLeased() )
        {
            if ( isLeaseMaintained() )
            {
                iconSubtype = ICON_SUBTYPE_MAINTAINED_LEASE;
            }
            else
            {
                iconSubtype = ICON_SUBTYPE_LEASE;
            }
        }
        else
        {
            iconSubtype = ICON_SUBTYPE_OLD;
        }
        
        return m_icons[iconType][iconSubtype];
    }
    
    /**
     * Return the text to use for a tool tip on this node.
     *
     * @return Tool Tip text.  May be null, for no tool tip.
     */
    String getToolTipText()
    {
        String text;
        switch ( getType() )
        {
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            if ( isConfigured() )
            {
                text = "Configured Counter Instrument Sample";
            }
            else if ( isLeased() )
            {
                if ( isLeaseMaintained() )
                {
                    text = "Leased Counter Instrument Sample (Maintained " +
                        ( getLeaseDuration() / 1000 ) + " seconds)";
                }
                else
                {
                    text = "Leased Counter Instrument Sample (Expires in " +
                        ( getRemainingLeaseTime() / 1000 ) + " seconds)";
                }
            }
            else
            {
                text = "Old Counter Instrument Sample loaded from state file";
            }
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            if ( isConfigured() )
            {
                text = "Configured Maximum Value Instrument Sample";
            }
            else if ( isLeased() )
            {
                if ( isLeaseMaintained() )
                {
                    text = "Leased Maximum Value Instrument Sample (Maintained " +
                        ( getLeaseDuration() / 1000 ) + " seconds)";
                }
                else
                {
                    text = "Leased Maximum Value Instrument Sample (Expires in " +
                        ( getRemainingLeaseTime() / 1000 ) + " seconds)";
                }
            }
            else
            {
                text = "Old Maximum Value Instrument Sample loaded from state file";
            }
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MEAN:
            if ( isConfigured() )
            {
                text = "Configured Mean Value Instrument Sample";
            }
            else if ( isLeased() )
            {
                if ( isLeaseMaintained() )
                {
                    text = "Leased Mean Value Instrument Sample (Maintained " +
                        ( getLeaseDuration() / 1000 ) + " seconds)";
                }
                else
                {
                    text = "Leased Mean Value Instrument Sample (Expires in " +
                        ( getRemainingLeaseTime() / 1000 ) + " seconds)";
                }
            }
            else
            {
                text = "Old Mean Value Instrument Sample loaded from state file";
            }
            break;
            
        case InstrumentSampleData.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            if ( isConfigured() )
            {
                text = "Configured Minimum Value Instrument Sample";
            }
            else if ( isLeased() )
            {
                if ( isLeaseMaintained() )
                {
                    text = "Leased Minimum Value Instrument Sample (Maintained " +
                        ( getLeaseDuration() / 1000 ) + " seconds)";
                }
                else
                {
                    text = "Leased Minimum Value Instrument Sample (Expires in " +
                        ( getRemainingLeaseTime() / 1000 ) + " seconds)";
                }
            }
            else
            {
                text = "Old Minimum Value Instrument Sample loaded from state file";
            }
            break;
            
        default:
            throw new IllegalStateException( "Encountered an unknown instrument sample type: " +
                getType() );
        }
        
        return text;
    }
    
    /**
     * Returns an array of any menu items which will be displayed both
     *  in a popup menu and in the menus.
     *
     * @return An array of the common menu items.
     */
    public JMenuItem[] getCommonMenuItems()
    {
        ArrayList menuItems = new ArrayList();
        
        Action viewAction = new AbstractAction( "View" )
        {
            public void actionPerformed( ActionEvent event )
            {
                m_connection.viewSample( InstrumentSampleNodeData.this.getName() );
            }
        };
        JMenuItem viewItem = new JMenuItem( viewAction );
        viewItem.setMnemonic( 'V' );
        menuItems.add( viewItem );
        
        if ( isLeased() )
        {
            if ( isLeaseMaintained() )
            {
                Action stopMaintainingAction = new AbstractAction( "Stop Maintaining Lease..." )
                {
                    public void actionPerformed( ActionEvent event )
                    {
                        m_connection.stopMaintainingSample(
                            InstrumentSampleNodeData.this.getName() );
                    }
                };
                JMenuItem stopMaintainingItem = new JMenuItem( stopMaintainingAction );
                stopMaintainingItem.setMnemonic( 'S' );
                menuItems.add( stopMaintainingItem );
            }
            else
            {
                Action startMaintainingAction = new AbstractAction( "Start Maintaining Lease..." )
                {
                    public void actionPerformed( ActionEvent event )
                    {
                        // Need to show a dialog here.
                        long leaseDuration = 600000;
                        String description = InstrumentSampleUtils.getDefaultDescriptionForType(
                            m_type, m_interval );
                        m_connection.startMaintainingSample(
                            m_instrumentName, m_type, m_interval, m_size, leaseDuration,
                            description );
                    }
                };
                JMenuItem startMaintainingItem = new JMenuItem( startMaintainingAction );
                startMaintainingItem.setMnemonic( 'S' );
                menuItems.add( startMaintainingItem );
            }
        }
        
        JMenuItem[] menuItemArray = new JMenuItem[menuItems.size()];
        menuItems.toArray( menuItemArray );
        
        return menuItemArray;
    }
    
    /**
     * Called when the node is selected.
     */
    void select()
    {
        m_connection.viewSample( getName() );
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    InstrumentSampleData getData()
    {
        return m_data;
    }
    
    boolean isConfigured()
    {
        return m_configured;
    }
    
    boolean isLeased()
    {
        return m_leaseExpireTime > 0;
    }
    
    boolean isLeaseMaintained()
    {
        return m_leaseDuration > 0;
    }
    
    int getType()
    {
        return m_type;
    }
    
    int getSize()
    {
        return m_size;
    }
    
    long getInterval()
    {
        return m_interval;
    }
    
    boolean update()
    {
        boolean changed = false;
        changed |= update( m_data.getName(), m_data.getDescription(), m_data.getStateVersion() );
        
        boolean newConfigured = m_data.isConfigured();
        if ( newConfigured != m_configured )
        {
            changed = true;
            m_configured = newConfigured;
        }
        
        long newLeaseExpireTime = m_data.getLeaseExpirationTime();
        if ( newLeaseExpireTime != m_leaseExpireTime )
        {
            changed = true;
            m_leaseExpireTime = newLeaseExpireTime;
        }
        
        int newType = m_data.getType();
        if ( newType != m_type )
        {
            changed = true;
            m_type = newType;
        }
        
        int newSize = m_data.getSize();
        if ( newSize != m_size )
        {
            changed = true;
            m_size = newSize;
        }
        
        long newInterval = m_data.getInterval();
        if ( newInterval != m_interval )
        {
            changed = true;
            m_interval = newInterval;
        }
        
        return changed;
    }
    
    long getRemainingLeaseTime()
    {
        long now = System.currentTimeMillis();
        return m_leaseExpireTime - now;
    }

    void setLeaseExpireTime( long leaseExpireTime )
    {
        m_leaseExpireTime = leaseExpireTime;
    }
    
    /**
     * Sets the maintained lease time.  If 0 or less means that the the lease
     *  is not maintained.
     *
     * @param leaseDuration The maintained lease time.
     */
    void setLeaseDuration( long leaseDuration )
    {
        m_leaseDuration = leaseDuration;
    }

    /**
     * Returns the length of the maintained lease.
     *
     * @return The length of the maintained lease.
     */
    long getLeaseDuration()
    {
        return m_leaseDuration;
    }
}
