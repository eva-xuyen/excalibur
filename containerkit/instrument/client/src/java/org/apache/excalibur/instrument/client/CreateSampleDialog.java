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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
class CreateSampleDialog
    extends AbstractTabularOptionDialog
{
    private int m_instrumentType;
    private JTextField m_instrumentNameField;
    private JTextField m_instrumentDescriptionField;
    private JTextField m_sampleDescriptionField;
    private String m_sampleDescription;
    
    /** Remembers the last default description so we can tell whether or not the
     *   user has modified the description manually. */
    private String m_lastDefaultDescription;
    
    private JTextField m_intervalField;
    private long m_interval;
    private JTextField m_sizeField;
    private int m_size;
    private JTextField m_leaseTimeField;
    private long m_leaseTime;
    private JCheckBox m_maintainLeaseCheckBox;
    private Container m_sampleTypePanel;
    private ButtonGroup m_sampleTypeGroup;
    private int m_sampleType;
    private JRadioButton m_sampleTypeCounter;
    private JRadioButton m_sampleTypeMaximum;
    private JRadioButton m_sampleTypeMinimum;
    private JRadioButton m_sampleTypeMean;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new CreateSampleDialog.
     *
     * @param frame Frame which owns the dialog.
     * @param name Name of the instrument.
     * @param description Description of the instrument.
     * @param type Type of the instrument.
     */
    CreateSampleDialog( InstrumentClientFrame frame,
                        String name,
                        String description,
                        int type )
    {
        super( frame, "Create Instrument Sample",
            AbstractOptionDialog.BUTTON_OK | AbstractOptionDialog.BUTTON_CANCEL );
        
        m_instrumentType = type;
        m_instrumentNameField.setText( name );
        m_instrumentDescriptionField.setText( description );
        
        buildSampleTypeComponent();

        // Set the default values.
        setInterval( 1000 );
        setSampleCount( 600 );  // 10 minutes of history
        setLeaseTime( 600 );
        setMaintainLease( true );
        
        m_lastDefaultDescription =
            InstrumentSampleUtils.getDefaultDescriptionForType( m_sampleType, m_interval );
        setSampleDescription( m_lastDefaultDescription );
        
        pack();
    }
    
    /*---------------------------------------------------------------
     * AbstractOptionDialog Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the message to show at the top of the dialog.
     *
     * @return The text of the message.
     */
    protected String getMessage()
    {
        return "Please enter the parameters for the sample to be created.";
    }
    
    /**
     * Goes through and validates the fields in the dialog.
     * 
     * @return True if the fields were Ok.
     */
    protected boolean validateFields()
    {
        return validateFields( false );
    }
    
    /*---------------------------------------------------------------
     * AbstractTabularOptionDialog Methods
     *-------------------------------------------------------------*/
    /**
     * Returns an array of labels to use for the components returned from
     *  getMainPanelComponents().
     *
     * @returns An array of labels.
     */
    protected String[] getMainPanelLabels()
    {
        return new String[]
        {
            "Instrument Name:",
            "Instrument Description:",
            "Sample Description:",
            "Sample Interval (milliseconds):",
            "Number of Samples:",
            "Lease Time (Seconds):",
            "Maintain Lease:",
            "Sample Type:"
        };
    }
    
    /**
     * Returns an array of components to show in the main panel of the dialog.
     *
     * @returns An array of components.
     */
    protected Component[] getMainPanelComponents()
    {
        DocumentListener dl = new DocumentListener() {
            public void changedUpdate( DocumentEvent event )
            {
            }
            public void insertUpdate( DocumentEvent event )
            {
                CreateSampleDialog.this.validateFields( true );
            }
            public void removeUpdate( DocumentEvent event )
            {
                // When the description is replaced, a remove event is fired when its
                //  value is "".  If we validate on that then we will go into an infinite
                //  loop.
                if ( m_sampleDescriptionField.getText().length() > 0 )
                {
                    CreateSampleDialog.this.validateFields( true );
                }
            }
        };
        
        m_instrumentNameField = new JTextField();
        m_instrumentNameField.setColumns( 40 );
        m_instrumentNameField.setEditable( false );
        
        m_instrumentDescriptionField = new JTextField();
        m_instrumentDescriptionField.setColumns( 40 );
        m_instrumentDescriptionField.setEditable( false );
        
        m_sampleDescriptionField = new JTextField();
        m_sampleDescriptionField.setColumns( 40 );
        m_sampleDescriptionField.getDocument().addDocumentListener( dl );
        
        m_intervalField = new JTextField();
        m_intervalField.setColumns( 10 );
        m_intervalField.getDocument().addDocumentListener( dl );
        
        m_sizeField = new JTextField();
        m_sizeField.setColumns( 4 );
        m_sizeField.getDocument().addDocumentListener( dl );
        
        m_leaseTimeField = new JTextField();
        m_leaseTimeField.setColumns( 10 );
        m_leaseTimeField.getDocument().addDocumentListener( dl );
        
        m_maintainLeaseCheckBox = new JCheckBox();
        
        m_sampleTypePanel = Box.createVerticalBox();
        
        // Create a series of buttons to help the users work efficiently
        Box intervalBar = Box.createHorizontalBox();
        intervalBar.add( m_intervalField );
        intervalBar.add( Box.createHorizontalStrut( 20 ) );
        intervalBar.add( createIntervalButton( "1 Second", 1000, 600, 600 ) );
        intervalBar.add( Box.createHorizontalStrut( 5 ) );
        intervalBar.add( createIntervalButton( "1 Minute", 60000, 1440, 86400 ) );
        intervalBar.add( Box.createHorizontalStrut( 5 ) );
        intervalBar.add( createIntervalButton( "1 Hour", 3600000, 672, 86400 ) );
        
        Box leaseTimeBar = Box.createHorizontalBox();
        leaseTimeBar.add( m_leaseTimeField );
        leaseTimeBar.add( Box.createHorizontalStrut( 20 ) );
        leaseTimeBar.add( createLeaseTimeButton( "10 Minutes", 600 ) );
        leaseTimeBar.add( Box.createHorizontalStrut( 5 ) );
        leaseTimeBar.add( createLeaseTimeButton( "1 Hour", 3600 ) );
        leaseTimeBar.add( Box.createHorizontalStrut( 5 ) );
        leaseTimeBar.add( createLeaseTimeButton( "1 Day", 86400 ) );
        
        return new Component[]
        {
            m_instrumentNameField,
            m_instrumentDescriptionField,
            m_sampleDescriptionField,
            intervalBar,
            m_sizeField,
            leaseTimeBar,
            m_maintainLeaseCheckBox,
            m_sampleTypePanel
        };
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private JButton createIntervalButton( final String label,
                                          final long interval,
                                          final int size,
                                          final long leaseTime )
    {
        Action action = new AbstractAction( label )
        {
            public void actionPerformed( ActionEvent event )
            {
                CreateSampleDialog.this.setInterval( interval );
                CreateSampleDialog.this.setSampleCount( size );
                CreateSampleDialog.this.setLeaseTime( leaseTime );
            }
        };
        return new JButton( action );
    }
    
    private JButton createLeaseTimeButton( final String label,
                                           final long leaseTime )
    {
        Action action = new AbstractAction( label )
        {
            public void actionPerformed( ActionEvent event )
            {
                CreateSampleDialog.this.setLeaseTime( leaseTime );
            }
        };
        return new JButton( action );
    }
    
    /**
     * Builds the sample type component.
     */
    private void buildSampleTypeComponent()
    {
        ChangeListener cl = new ChangeListener()
        {
            public void stateChanged( ChangeEvent event )
            {
                if ( ((JRadioButton)event.getSource()).isSelected() )
                {
                    // Only validate on the selected value
                    CreateSampleDialog.this.validateFields( true );
                }
            }
        };
        
        m_sampleTypeGroup = new ButtonGroup();
        m_sampleTypeCounter = new JRadioButton( "Count over each sample" );
        m_sampleTypeMaximum = new JRadioButton( "Maximum value over each sample" );
        m_sampleTypeMinimum = new JRadioButton( "Minumum value over each sample" );
        m_sampleTypeMean    = new JRadioButton( "Mean value over each sample" );
        
        switch ( m_instrumentType )
        {
        case InstrumentData.INSTRUMENT_TYPE_COUNTER:
            m_sampleTypePanel.add( m_sampleTypeCounter );
            m_sampleTypeGroup.add( m_sampleTypeCounter );
            
            m_sampleTypeCounter.setSelected( true );
            m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER;
            break;
        case InstrumentData.INSTRUMENT_TYPE_VALUE:
            m_sampleTypePanel.add( m_sampleTypeMaximum );
            m_sampleTypeGroup.add( m_sampleTypeMaximum );
            
            m_sampleTypePanel.add( m_sampleTypeMinimum );
            m_sampleTypeGroup.add( m_sampleTypeMinimum );
            
            m_sampleTypePanel.add( m_sampleTypeMean );
            m_sampleTypeGroup.add( m_sampleTypeMean );
            
            m_sampleTypeMaximum.setSelected( true );
            m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM;
            break;
        default:
            // Unknown Type
            break;
        }
        
        // Add the change listeners down here so the initialization does not cause
        //  them to fire.
        m_sampleTypeCounter.addChangeListener( cl );
        m_sampleTypeMaximum.addChangeListener( cl );
        m_sampleTypeMinimum.addChangeListener( cl );
        m_sampleTypeMean.addChangeListener( cl );
    }
    
    /**
     * Sets the initial sample description to be shown in the TextField.
     *
     * @param sampleDescription The initial sample description.
     */
    void setSampleDescription( String sampleDescription )
    {
        m_sampleDescription = sampleDescription;
        m_sampleDescriptionField.setText( sampleDescription );
        
        // Validate the fields quietly to update other values correctly.
        validateFields( true );
    }
    
    /**
     * Returns the sample description set in the dialog.
     *
     * @return The sample description.
     */
    String getSampleDescription()
    {
        return m_sampleDescription;
    }
    
    /**
     * Sets the initial interval to be shown in the interval TextField.
     *
     * @param interval The initial interval.
     */
    void setInterval( long interval )
    {
        m_interval = interval;
        m_intervalField.setText( Long.toString( interval ) );
        
        // Validate the fields quietly to update other values correctly.
        validateFields( true );
    }
    
    /**
     * Returns the interval set in the dialog.
     *
     * @return The interval.
     */
    long getInterval()
    {
        return m_interval;
    }
    
    /**
     * Sets the initial size to be shown in the size TextField.
     *
     * @param size The initial size.
     */
    void setSampleCount( int size )
    {
        m_size = size;
        m_sizeField.setText( Integer.toString( size ) );
    }
    
    /**
     * Returns the size set in the dialog.
     *
     * @return The size.
     */
    int getSampleCount()
    {
        return m_size;
    }
    
    /**
     * Sets the initial lease time to be shown in the lease time TextField.
     *
     * @param leaseTime The initial lease time.
     */
    void setLeaseTime( long leaseTime )
    {
        m_leaseTime = leaseTime;
        m_leaseTimeField.setText( Long.toString( leaseTime ) );
    }
    
    /**
     * Returns the lease time set in the dialog.
     *
     * @return The lease time.
     */
    long getLeaseTime()
    {
        return m_leaseTime;
    }
    
    /**
     * Sets the initial maintain lease flag to be shown in the maintain lease
     *  CheckBox.
     *
     * @param maintainLease The initial maintain lease flag.
     */
    void setMaintainLease( boolean maintainLease )
    {
        m_maintainLeaseCheckBox.setSelected( maintainLease );
    }
    
    /**
     * Returns the maintain lease flag set in the dialog.
     *
     * @return The maintain lease flag.
     */
    boolean getMaintainLease()
    {
        return m_maintainLeaseCheckBox.isSelected();
    }
    
    /**
     * Sets the initial size to be shown in the size TextField.
     *
     * @param size The initial size.
     */
    void setSampleType( int type )
    {
        m_sampleType = type;
        
        switch ( type )
        {
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            m_sampleTypeCounter.setSelected( true );
            break;
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            m_sampleTypeMaximum.setSelected( true );
            break;
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MEAN:
            m_sampleTypeMean.setSelected( true );
            break;
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            m_sampleTypeMinimum.setSelected( true );
            break;
        default:
            break;
        }
        
        // Validate the fields quietly to update other values correctly.
        validateFields( true );
    }
    
    /**
     * Returns the type set in the dialog.
     *
     * @return The type.
     */
    int getSampleType()
    {
        return m_sampleType;
    }
    
    /**
     * Goes through and validates the fields in the dialog.
     *
     * @param quiet True if problems should be ignored.
     * 
     * @return True if the fields were Ok.
     */
    private boolean validateFields( boolean quiet )
    {
        // Check the interval.
        boolean intervalOk = true;
        long interval = 0;
        try
        {
            interval = Long.parseLong( m_intervalField.getText().trim() );
        }
        catch ( NumberFormatException e )
        {
            intervalOk = false;
        }
        if ( ( interval < 100 ) || ( interval > 24 * 60 * 60 * 1000 ) )
        {
            intervalOk = false;
        }
        if ( intervalOk )
        {
            m_interval = interval;
            m_intervalField.setForeground( null );
        }
        else
        {
            m_intervalField.setForeground( Color.red );
            if ( !quiet )
            {
                JOptionPane.showMessageDialog( this, "Please enter a valid interval. (100ms - 24hrs, 86400000)",
                    "Invalid interval", JOptionPane.ERROR_MESSAGE );
                return false;
            }
        }
        
        // Check the size.
        boolean sizeOk = true;
        int size = 0;
        try
        {
            size = Integer.parseInt( m_sizeField.getText().trim() );
        }
        catch ( NumberFormatException e )
        {
            sizeOk = false;
        }
        if ( ( size < 1 ) || ( size > 2048 ) )
        {
            sizeOk = false;
        }
        if ( sizeOk )
        {
            m_size = size;
            m_sizeField.setForeground( null );
        }
        else
        {
            m_sizeField.setForeground( Color.red );
            if ( !quiet )
            {
                JOptionPane.showMessageDialog( this, "Please enter a valid size. (1 - 2048)",
                    "Invalid size", JOptionPane.ERROR_MESSAGE );
                return false;
            }
        }
        
        // Check the leaseTime.
        boolean leaseTimeOk = true;
        int leaseTime = 0;
        try
        {
            leaseTime = Integer.parseInt( m_leaseTimeField.getText().trim() );
        }
        catch ( NumberFormatException e )
        {
            leaseTimeOk = false;
        }
        if ( ( leaseTime < 60 ) || ( leaseTime > ( size * interval / 1000 ) + 86400 ) )
        {
            leaseTimeOk = false;
        }
        if ( leaseTimeOk )
        {
            m_leaseTime = leaseTime * 1000L;
            m_leaseTimeField.setForeground( null );
        }
        else
        {
            m_leaseTimeField.setForeground( Color.red );
            if ( !quiet )
            {
                JOptionPane.showMessageDialog( this, "Please enter a valid lease time. Must be " +
                    "between 1 minute (60) and 24 hours greater than the interval * size (" +
                    ( ( size * interval / 1000 ) + 86400 ) + ")",
                    "Invalid leaseTime", JOptionPane.ERROR_MESSAGE );
                return false;
            }
        }
        
        // Store the sample type
        if ( m_sampleTypeCounter.isSelected() )
        {
             m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER;
        }
        else if ( m_sampleTypeMaximum.isSelected() )
        {
             m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM;
        }
        else if ( m_sampleTypeMean.isSelected() )
        {
             m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MEAN;
        }
        else if ( m_sampleTypeMinimum.isSelected() )
        {
             m_sampleType = InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MINIMUM;
        }
        else
        {
            // Should never get here.
            m_sampleType = -1;
        }
        
        // Update the default description
        String newDefaultDescription =
            InstrumentSampleUtils.getDefaultDescriptionForType( m_sampleType, m_interval );
        
        // Check the description.
        String description = m_sampleDescriptionField.getText().trim();
        if ( ( description.length() == 0 ) || ( description.equals( m_lastDefaultDescription ) ) )
        {
            if ( !description.equals( newDefaultDescription ) )
            {
                // Set the description to the default.
                description = newDefaultDescription;
                
                // We can't change the description field directly because this is called from
                //  its change listener.
                final String setDesc = description;
                SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            CreateSampleDialog.this.m_sampleDescriptionField.setText( setDesc );
                        }
                    } );
            }
        }
        m_sampleDescription = description;
        
        // Always remember the new default description
        m_lastDefaultDescription = newDefaultDescription;
        
        return true;
    }
}

