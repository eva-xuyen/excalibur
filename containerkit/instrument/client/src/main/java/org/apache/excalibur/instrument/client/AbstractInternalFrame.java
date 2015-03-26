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

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;


/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
abstract class AbstractInternalFrame
    extends JInternalFrame
    implements InternalFrameListener, LogEnabled
{
    private InstrumentClientFrame m_frame;
    private Logger m_logger;
    
    private JInternalFrame m_nextFrame;
    private boolean m_loaded;
    private boolean m_active;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    AbstractInternalFrame( Configuration stateConfig,
                           boolean resizable,
                           boolean closable,
                           boolean maximizable,
                           boolean iconifiable,
                           InstrumentClientFrame frame )
    {
        super( "", resizable, closable, maximizable, iconifiable );

        m_frame = frame;

        // Look for the location and size of the frame.
        int x = stateConfig.getAttributeAsInteger( "x", getX() );
        int y = stateConfig.getAttributeAsInteger( "y", getY() );
        int width = stateConfig.getAttributeAsInteger( "width", getWidth() );
        int height = stateConfig.getAttributeAsInteger( "height", getHeight() );
        setLocation( x, y );
        setSize( width, height );

        // Look for the window state.
        try
        {
            if( stateConfig.getAttributeAsBoolean( "iconized", false ) )
            {
                setIcon( true );
            }
            else if( stateConfig.getAttributeAsBoolean( "maximized", false ) )
            {
                this.setMaximum( true );
            }
        }
        catch( java.beans.PropertyVetoException e )
        {
        }

        // Set the content pane so that it is the right color
        JPanel contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout() );
        setContentPane( contentPane );

        addInternalFrameListener( this );

        m_loaded = true;
    }

    AbstractInternalFrame( String title,
                           boolean resizable,
                           boolean closable,
                           boolean maximizable,
                           boolean iconifiable,
                           InstrumentClientFrame frame )
    {
        super( title, resizable, closable, maximizable, iconifiable );

        m_frame = frame;

        // Set the content pane so that it is the right color
        JPanel contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout() );
        setContentPane( contentPane );

        addInternalFrameListener( this );

        m_loaded = false;
    }
    
    /*---------------------------------------------------------------
     * LogEnabled Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the Logger to be used by the component.
     *
     * @param logger The Logger.
     */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the logger to be used by the component.
     *
     * @return The Logger.
     */
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    /**
     * Adds the frame to the desktop in a simple and dumb cascading format.
     */
    void addToDesktop( JDesktopPane desktop )
    {
        // Make sure that the size is valid
        Dimension maxSize = desktop.getSize();
        Dimension size = getSize();
        if( ( maxSize.width < size.width ) || ( maxSize.height < size.height ) )
        {
            setSize( new Dimension( Math.min( maxSize.width, size.width ),
                                    Math.min( maxSize.height, size.height ) ) );
            size = getSize();
        }

        if( !m_loaded )
        {
            // Position the frame
            int max = (int)Math.min( Math.ceil( ( maxSize.width - size.width ) / 20.0 ),
                                     Math.ceil( ( maxSize.height - size.height ) / 20.0 ) );

            JInternalFrame[] frames = desktop.getAllFrames();
            int pos;
            if( max > 0 )
            {
                pos = ( frames.length % max ) * 20;
            }
            else
            {
                pos = 0;
            }
            setLocation( pos, pos );
        }

        desktop.add( this );
    }

    void hideFrame()
    {
        // calling setVisible in the shutdown hook will cause the thread to deadlock.
        if ( !Thread.currentThread().getName().equals( InstrumentClientFrame.SHUTDOWN_HOOK_NAME ) )
        {
            setVisible( false );
            dispose();
        }
    }

    public void updateUI()
    {
        super.updateUI();

        pack();
        setMinimumSize( getSize() );
    }

    /**
     * Allows subclasses to fill in configuration information.  At the least, they must set
     *  a type attribute.
     */
    abstract protected void getState( DefaultConfiguration stateConfig );

    final Configuration getState()
    {
        DefaultConfiguration stateConfig = new DefaultConfiguration( "inner-frame", "-" );

        // Save the location and size of the frame.
        stateConfig.setAttribute( "x", Integer.toString( getX() ) );
        stateConfig.setAttribute( "y", Integer.toString( getY() ) );
        stateConfig.setAttribute( "width", Integer.toString( getWidth() ) );
        stateConfig.setAttribute( "height", Integer.toString( getHeight() ) );

        // Save the window state.
        if( isIcon() )
        {
            stateConfig.setAttribute( "iconized", "true" );
        }
        else if( isMaximum() )
        {
            stateConfig.setAttribute( "maximized", "true" );
        }

        getState( stateConfig );

        return stateConfig;
    }
    
    protected InstrumentClientFrame getFrame()
    {
        return m_frame;
    }
    
    public void setTitle( String title )
    {
        super.setTitle( title );
        if ( m_active )
        {
            m_frame.setStatusMessage( getTitle() );
        }
    }

    /*---------------------------------------------------------------
     * InternalFrameListener Methods
     *-------------------------------------------------------------*/
    public void internalFrameOpened( InternalFrameEvent event )
    {
    }

    public void internalFrameClosing( InternalFrameEvent event )
    {
        // Select the new top frame
        JDesktopPane desktop = m_frame.getDesktopPane();
        JInternalFrame[] frames = desktop.getAllFrames();
        // Find the first frame other than the one being hidden and select and move it to the front
        m_nextFrame = null;
        for( int i = 0; i < frames.length; i++ )
        {
            JInternalFrame frame = frames[ i ];
            if( frame != this )
            {
                m_nextFrame = frame;

                // Break out
                i = frames.length;
            }
        }
    }

    public void internalFrameClosed( InternalFrameEvent event )
    {
        // On closing Swing will bring forward the window at the bottom,
        //  rather than the next window.  So we need to move it back and show the correct one.
        if( m_nextFrame != null )
        {
            // The getSelectedFrame method was added in JDK1.3, so it may not yet exist.
            // Cast this to our workaround DesktopPane to work around this.
            DesktopPane desktop = (DesktopPane)m_frame.getDesktopPane();
            JInternalFrame top = desktop.getSelectedFrame();

            if( top != null )
            {
                if( top != m_nextFrame )
                {
                    try
                    {
                        m_nextFrame.setSelected( true );
                        desktop.moveToFront( m_nextFrame );
                        desktop.moveToBack( top );
                    }
                    catch( java.beans.PropertyVetoException e )
                    {
                    }
                }
            }
        }
    }

    public void internalFrameIconified( InternalFrameEvent event )
    {
    }

    public void internalFrameDeiconified( InternalFrameEvent event )
    {
        // Swing always activates a frame when it is deiconified, but it down't
        //  always move it to the front
        JDesktopPane desktop = m_frame.getDesktopPane();
        desktop.moveToFront( this );
    }

    public void internalFrameActivated( InternalFrameEvent event )
    {
        m_active = true;
        m_frame.setStatusMessage( getTitle() );
    }

    public void internalFrameDeactivated( InternalFrameEvent event )
    {
        m_active = false;
        m_frame.setStatusMessage( "" );
    }
}

