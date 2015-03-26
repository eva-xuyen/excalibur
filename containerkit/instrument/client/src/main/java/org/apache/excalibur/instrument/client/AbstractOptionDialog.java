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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public abstract class AbstractOptionDialog
    extends JDialog
{
    public static final int BUTTON_OK = 1;
    public static final int BUTTON_CANCEL = 2;
    
    protected int m_action = BUTTON_CANCEL;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractOptionDialog.
     *
     * @param frame Frame which owns the dialog.
     * @param title Title for the dialog.
     * @param buttons List of buttons to display.
     */
    protected AbstractOptionDialog( JFrame frame, String title, int buttons )
    {
        super( frame, title, true );
        
        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setLayout( new BorderLayout() );
        contentPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        JPanel backPane = new JPanel();
        backPane.setLayout( new BorderLayout() );
        backPane.setBorder(
            new CompoundBorder(
                new EmptyBorder( 0, 0, 5, 0 ),
                new CompoundBorder(
                    new EtchedBorder( EtchedBorder.LOWERED ),
                    new EmptyBorder( 5, 5, 5, 5 )
                )
            )
        );
        contentPane.add( backPane, BorderLayout.CENTER );
        
        // Build the message
        backPane.add( new JLabel( getMessage(), SwingConstants.LEFT ), BorderLayout.NORTH );
        
        // Build the main panel
        JPanel mainPanel = getMainPanel();
        mainPanel.setBorder( new EmptyBorder( 5, 0, 0, 0 ) );
        backPane.add( mainPanel, BorderLayout.CENTER );
        
        
        // Build the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
        Box buttonBox = Box.createHorizontalBox();
        if ( ( buttons & BUTTON_OK ) != 0 )
        {
            Action action = new AbstractAction( "OK" )
            {
                public void actionPerformed( ActionEvent event )
                {
                    if ( validateFields() )
                    {
                        m_action = BUTTON_OK;
                        AbstractOptionDialog.this.hide();
                    }
                }
            };
            JButton button = new JButton( action );
            buttonBox.add( button );
            buttonBox.add( Box.createHorizontalStrut( 5 ) );
        }
        if ( ( buttons & BUTTON_CANCEL ) != 0 )
        {
            Action action = new AbstractAction( "Cancel" )
            {
                public void actionPerformed( ActionEvent event )
                {
                    m_action = BUTTON_CANCEL;
                    AbstractOptionDialog.this.hide();
                }
            };
            JButton button = new JButton( action );
            buttonBox.add( button );
            buttonBox.add( Box.createHorizontalStrut( 5 ) );
        }
        buttonPanel.add( buttonBox );
        contentPane.add( buttonPanel, BorderLayout.SOUTH );
        
        pack();
        
        // Position the dialog.
        Point frameLocation = frame.getLocation();
        Dimension frameSize = frame.getSize();
        Dimension size = getSize();
        
        setLocation(
            (int)( frameLocation.getX() + (frameSize.getWidth() - size.getWidth() ) / 2 ),
            (int)( frameLocation.getY() + (frameSize.getHeight() - size.getHeight() ) / 2 ) );
        
        // Make the dialog a fixed size.
        setResizable( false );
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the message to show at the top of the dialog.
     *
     * @return The text of the message.
     */
    protected abstract String getMessage();
    
    /**
     * Returns the main panel which makes up the guts of the dialog.
     *
     * @return The main panel.
     */
    protected abstract JPanel getMainPanel();
    
    /**
     * Goes through and validates the fields in the dialog.
     *
     * @return True if the fields were Ok.
     */
    protected boolean validateFields()
    {
        return true;
    }
    
    /**
     * Returns the button which the user selected.
     */
    public int getAction()
    {
        return m_action;
    }
}

