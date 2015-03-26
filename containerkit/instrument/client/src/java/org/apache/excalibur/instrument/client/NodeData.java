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

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
abstract class NodeData
{
    protected static final String MEDIA_PATH = "org/apache/excalibur/instrument/client/media/";
    protected static final JMenuItem[] EMPTY_MENU_ITEM_ARRAY = new JMenuItem[0];
    
    private String m_name;
    private String m_description;
    private int m_stateVersion;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    protected NodeData()
    {
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    String getName()
    {
        return m_name;
    }
    
    String getDescription()
    {
        return m_description;
    }
    
    void setDescription( String description )
    {
        m_description = description;
    }
    
    int getStateVersion()
    {
        return m_stateVersion;
    }
    
    /**
     * Get the icon to display for the node.
     *
     * @return the icon to display for the node.
     */
    abstract ImageIcon getIcon();
    
    /**
     * Return the text to use for a tool tip on this node.
     *
     * @return Tool Tip text.  May be null, for no tool tip.
     */
    abstract String getToolTipText();
    
    /**
     * Return the popup for the node.
     *
     * @return The the popup for the node.
     */
    public JPopupMenu getPopupMenu()
    {
        JPopupMenu popup;
        JMenuItem[] menuItems = getCommonMenuItems();
        if ( menuItems.length == 0 )
        {
            popup = null;
        }
        else
        {
            popup = new JPopupMenu( getDescription() );
            for ( int i = 0; i < menuItems.length; i++ )
            {
                popup.add( menuItems[i] );
            }
        }
        
        return popup;
    }
    
    /**
     * Returns an array of any menu items which will be displayed both
     *  in a popup menu and in the menus.
     *
     * @return An array of the common menu items.
     */
    public JMenuItem[] getCommonMenuItems()
    {
        return EMPTY_MENU_ITEM_ARRAY;
    }
    
    /**
     * Called when the node is selected.
     */
    void select()
    {
    }
    
    
    boolean update( String name, String description, int stateVersion )
    {
        boolean changed = false;
        
        changed |= name.equals( m_name );
        m_name = name;
        
        changed |= description.equals( m_description );
        m_description = description;
        
        changed |= stateVersion == m_stateVersion;
        m_stateVersion = stateVersion;
        
        return changed;
    }
    
    public String toString()
    {
        return m_description;
    }
}
