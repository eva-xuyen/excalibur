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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Creates a dialog which displays a table of labeled components to the user.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public abstract class AbstractTabularOptionDialog
    extends AbstractOptionDialog
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractTabularOptionDialog.
     *
     * @param frame Frame which owns the dialog.
     * @param title Title for the dialog.
     * @param buttons List of buttons to display.
     */
    protected AbstractTabularOptionDialog( JFrame frame, String title, int buttons )
    {
        super( frame, title, buttons );
    }
    
    /*---------------------------------------------------------------
     * AbstractOptionDialog Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the main panel which makes up the guts of the dialog.
     *  This implementaton builds a table of labeled components using
     *  arrays returned by getMainPanelLabels() and getMainPanelComponents();
     *
     * @return The main panel.
     */
    protected JPanel getMainPanel()
    {
        String[] labels = getMainPanelLabels();
        Component[] components = getMainPanelComponents();
        
        JPanel panel = new JPanel();
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout( gbl );
        
        for ( int i = 0; i < labels.length; i++ )
        {
            addRow( panel, labels[i], components[i], gbl, gbc );
        }
        
        return panel;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns an array of labels to use for the components returned from
     *  getMainPanelComponents().
     *
     * @returns An array of labels.
     */
    protected abstract String[] getMainPanelLabels();
    
    /**
     * Returns an array of components to show in the main panel of the dialog.
     *
     * @returns An array of components.
     */
    protected abstract Component[] getMainPanelComponents();
    
    /**
     * Adds a row to the panel consisting of a label and component, separated by
     *  a 5 pixel spacer and followed by a 5 pixel high row between this and the
     *  next row.
     *
     * @param panel Panel to which the row will be added.
     * @param label Text of the label for the component.
     * @param component Component which makes up the row.
     * @param gbl GridBagLayout which must have been set as the layour of the
     *            panel.
     * @param gbc GridBagConstraints to use when laying out the row.
     */
    private void addRow( JPanel panel,
                         String label,
                         Component component,
                         GridBagLayout gbl,
                         GridBagConstraints gbc )
    {
        JLabel jLabel = new JLabel( label );
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints( jLabel, gbc );
        panel.add( jLabel );
        
        // Add a 5 pixel high spacer
        Component spacer = Box.createRigidArea( new Dimension( 5, 5 ) );
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints( spacer, gbc );
        panel.add( spacer );
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints( component, gbc );
        panel.add( component );
        
        // Add a 5 pixel high spacer
        spacer = Box.createRigidArea( new Dimension( 5, 5 ) );
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints( spacer, gbc );
        panel.add( spacer );
    }
}

