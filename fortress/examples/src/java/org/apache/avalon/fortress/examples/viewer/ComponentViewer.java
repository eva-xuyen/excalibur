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

package org.apache.avalon.fortress.examples.viewer;

/**
 * Simple Fortress container containing a Swing based viewer for performing
 * lookups on registered components.
 *
 * <p>
 * The intention of the viewer is to allow you to perform a lookup of a component
 * manually, (currently) so you can check the see the effect of lazy vs startup
 * initialization.
 * </p>
 *
 * <p>
 * REVISIT: add a text component which tracks the log file to make it easier to
 * see a component being initialized upon first lookup.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/24 22:31:22 $
 */
public final class ComponentViewer
    extends org.apache.avalon.fortress.impl.DefaultContainer
    implements org.apache.avalon.framework.activity.Startable, java.awt.event.ActionListener, Runnable
{
    // GUI references
    private javax.swing.JFrame m_frame;
    private javax.swing.JComboBox m_components;

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

        // create main frame
        m_frame = new javax.swing.JFrame( "Component Viewer" );
        m_frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );

        m_components = new javax.swing.JComboBox( getRoles() );

        javax.swing.JButton button = new javax.swing.JButton( "Lookup!" );
        button.addActionListener( this );

        /*
        // can we output the log data into this text area somehow ?
        JTextArea logData = new JTextArea();
        logData.setEditable( false );
        */

        javax.swing.JPanel selectionPanel = new javax.swing.JPanel();
        selectionPanel.add( m_components );
        selectionPanel.add( button );

        javax.swing.JPanel mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout( new javax.swing.BoxLayout( mainPanel, javax.swing.BoxLayout.Y_AXIS ) );
        mainPanel.add( selectionPanel );

        m_frame.setContentPane( mainPanel );
        m_frame.pack();

        // all done
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Initialized" );
        }
    }

    /**
     * Helper method to obtain a list of all Roles registered with this container
     *
     * @return an array of roles
     */
    private Object[] getRoles()
    {
        java.util.Set keys = m_mapper.keySet();
        Object[] roles = new Object[ keys.size() ];
        int j = 0;

        for( java.util.Iterator i = keys.iterator(); i.hasNext(); )
        {
            roles[ j++ ] = i.next();
        }

        return roles;
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
     * Handles the <i>lookup</i> event. Finds out which component
     * was selected and performs a lookup and release on this component.
     *
     * @param evt an <code>ActionEvent</code> value
     */
    public void actionPerformed( java.awt.event.ActionEvent evt )
    {
        String selected = (String)m_components.getSelectedItem();

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Looking up component " + selected );
        }

        Object component = null;

        try
        {
            component = m_serviceManager.lookup( selected );
        }
        catch( org.apache.avalon.framework.service.ServiceException e )
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "Error looking up component: " + e.getKey(), e );
            }
        }
        finally
        {
            if( component != null ) m_serviceManager.release( component );
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
}

