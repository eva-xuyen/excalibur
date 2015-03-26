/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.fortress.examples.swing;

/**
 * Simple Fortress based container containing a Swing implementation of Hello World.
 * This container creates a small Swing based GUI displaying a combobox of available
 * languages from the translator component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/24 22:31:22 $
 */
public final class SwingContainer extends org.apache.avalon.fortress.impl.DefaultContainer
    implements org.apache.avalon.framework.activity.Startable, java.awt.event.ActionListener, Runnable
{
    // Component references
    private org.apache.avalon.fortress.examples.components.Translator m_translator;

    // GUI references
    private javax.swing.JFrame m_frame;
    private javax.swing.JLabel m_label;

    // Dictionary key
    private String m_key = "hello-world";

    /**
     * Initializes this component. Creates simple Swing GUI containing
     * available translations for the key 'hello-world'.
     *
     * @exception java.lang.Exception if an error occurs
     */
    public void initialize()
        throws Exception
    {
        super.initialize();

        // obtain translator component
        m_translator = (org.apache.avalon.fortress.examples.components.Translator)m_serviceManager.lookup( org.apache.avalon.fortress.examples.components.Translator.ROLE );

        // create combo box
        javax.swing.JComboBox cb = new javax.swing.JComboBox( m_translator.getSupportedLanguages( m_key ) );
        cb.addActionListener( this );

        // create label
        m_label = new javax.swing.JLabel( "Select your language" );
        m_label.setPreferredSize( new java.awt.Dimension( 150, 30 ) );

        // create panel holding box and label
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.add( cb );
        panel.add( m_label );

        // create main frame
        m_frame = new javax.swing.JFrame( "Hello World!" );
        m_frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );
        m_frame.setContentPane( panel );
        m_frame.pack();

        // all done
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Initialized" );
        }
    }

    /**
     * Starts the component, makes GUI visible, ready for use.
     */
    public void start()
    {
        m_frame.setVisible( true );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "GUI Activated" );
        }
    }

    public void run()
    {
        while( m_frame.isVisible() )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException ie )
            {
                m_frame.setVisible( false );
            }
        }
    }

    /**
     * Stops component, make GUI invisible, ready for decomissioning.
     */
    public void stop()
    {
        m_frame.setVisible( false );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "GUI Disactivated" );
        }
    }

    /**
     * Method called when the user changes the selected item in the
     * combobox.
     *
     * @param evt an <code>ActionEvent</code> instance
     */
    public void actionPerformed( java.awt.event.ActionEvent evt )
    {
        javax.swing.JComboBox cb = (javax.swing.JComboBox)evt.getSource();
        String selected = (String)cb.getSelectedItem();

        m_label.setText( m_translator.getTranslation( m_key, selected ) );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Language changed to " + selected );
        }
    }

    /**
     * Cleans up references to retrieved components.
     */
    public void dispose()
    {
        if( m_translator != null )
            m_serviceManager.release( m_translator );

        m_frame.dispose();

        super.dispose();
    }
}

