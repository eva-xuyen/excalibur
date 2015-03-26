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
package org.apache.excalibur.instrument.client;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class InstrumentNodeData
    extends NodeData
{
    /** A counter Instrument which exists because of configuration. */
    private static final ImageIcon m_iconInstrumentCtrConf;
    
    /** A counter Instrument which exists because of registration. */
    private static final ImageIcon m_iconInstrumentCtrReg;
    
    /** A counter Instrument which exists because of registration and
     *   configuration. */
    private static final ImageIcon m_iconInstrumentCtrRegConf;
    
    /** A counter Instrument which exists because it was loaded from the state
     *   file but is no longer used. */
    private static final ImageIcon m_iconInstrumentCtrOld;
    
    /** A value Instrument which exists because of configuration. */
    private static final ImageIcon m_iconInstrumentValConf;
    
    /** A value Instrument which exists because of registration. */
    private static final ImageIcon m_iconInstrumentValReg;
    
    /** A value Instrument which exists because of registration and
     *   configuration. */
    private static final ImageIcon m_iconInstrumentValRegConf;
    
    /** A value Instrument which exists because it was loaded from the state
     *   file but is no longer used. */
    private static final ImageIcon m_iconInstrumentValOld;
    
    private InstrumentData m_data;
    private InstrumentManagerConnection m_connection;
    
    private boolean m_configured;
    private boolean m_registered;
    private int m_type;
    
    /*---------------------------------------------------------------
     * Class Initializer
     *-------------------------------------------------------------*/
    static
    {
        // Load the images.
        ClassLoader cl = InstrumentManagerTreeCellRenderer.class.getClassLoader();
        m_iconInstrumentCtrConf =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_ctr_conf.gif") );
        m_iconInstrumentCtrReg =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_ctr_reg.gif") );
        m_iconInstrumentCtrRegConf =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_ctr_reg_conf.gif") );
        m_iconInstrumentCtrOld =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_ctr_old.gif") );
        
        m_iconInstrumentValConf =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_val_conf.gif") );
        m_iconInstrumentValReg =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_val_reg.gif") );
        m_iconInstrumentValRegConf =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_val_reg_conf.gif") );
        m_iconInstrumentValOld =
            new ImageIcon( cl.getResource( MEDIA_PATH + "instrument_val_old.gif") );
    }
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    InstrumentNodeData( InstrumentData data,
                        InstrumentManagerConnection connection )
    {
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
        ImageIcon icon;
        switch ( getType() )
        {
        case InstrumentData.INSTRUMENT_TYPE_COUNTER:
            if ( isConfigured() && isRegistered() )
            {
                icon = m_iconInstrumentCtrRegConf;
            }
            else if ( isConfigured() )
            {
                icon = m_iconInstrumentCtrConf;
            }
            else if ( isRegistered() )
            {
                icon = m_iconInstrumentCtrReg;
            }
            else
            {
                icon = m_iconInstrumentCtrOld;
            }
            break;
            
        case InstrumentData.INSTRUMENT_TYPE_VALUE:
            if ( isConfigured() && isRegistered() )
            {
                icon = m_iconInstrumentValRegConf;
            }
            else if ( isConfigured() )
            {
                icon = m_iconInstrumentValConf;
            }
            else if ( isRegistered() )
            {
                icon = m_iconInstrumentValReg;
            }
            else
            {
                icon = m_iconInstrumentValOld;
            }
            break;
            
        default:
            throw new IllegalStateException( "Encountered an unknown instrument type: " +
                getType() );
        }
        
        return icon;
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
        case InstrumentData.INSTRUMENT_TYPE_COUNTER:
            if ( isConfigured() && isRegistered() )
            {
                text = "Registered and Configured Counter Instrument";
            }
            else if ( isConfigured() )
            {
                text = "Configured but unregistered Counter Instrument";
            }
            else if ( isRegistered() )
            {
                text = "Registered Counter Instrument";
            }
            else
            {
                text = "Old Counter Instrument loaded from state file";
            }
            break;
            
        case InstrumentData.INSTRUMENT_TYPE_VALUE:
            if ( isConfigured() && isRegistered() )
            {
                text = "Registered and Configured Value Instrument";
            }
            else if ( isConfigured() )
            {
                text = "Configured but unregistered Value Instrument";
            }
            else if ( isRegistered() )
            {
                text = "Registered Value Instrument";
            }
            else
            {
                text = "Old Value Instrument loaded from state file";
            }
            break;
            
        default:
            throw new IllegalStateException( "Encountered an unknown instrument type: " +
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
        JMenuItem[] menuItems = new JMenuItem[1];
        
        Action createSampleAction = new AbstractAction( "Create Sample..." )
        {
            public void actionPerformed( ActionEvent event )
            {
                // Display a dialog to ask the user what new sample to create.
                InstrumentData data = InstrumentNodeData.this.getData();
                m_connection.showCreateSampleDialog( data );
            }
        };
        JMenuItem createSampleItem = new JMenuItem( createSampleAction );
        createSampleItem.setMnemonic( 'C' );
        menuItems[0] = createSampleItem;
        
        return menuItems;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    InstrumentData getData()
    {
        return m_data;
    }
    
    boolean isConfigured()
    {
        return m_configured;
    }
    
    boolean isRegistered()
    {
        return m_registered;
    }
    
    int getType()
    {
        return m_type;
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
        
        boolean newRegistered = m_data.isRegistered();
        if ( newRegistered != m_registered )
        {
            changed = true;
            m_registered = newRegistered;
        }
        
        int newType = m_data.getType();
        if ( newType != m_type )
        {
            changed = true;
            m_type = newType;
        }
        
        return changed;
    }
}
