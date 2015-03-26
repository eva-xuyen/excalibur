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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.instrument.client.http.HTTPInstrumentManagerConnection;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
class InstrumentClientFrame
    extends JFrame
    implements InstrumentManagerConnectionListener, LogEnabled
{
    protected static final String MEDIA_PATH = "org/apache/excalibur/instrument/client/media/";
    
    static final String SHUTDOWN_HOOK_NAME = "InstrumentClientShutdownHook";
    
    private String m_title;
    
    private JTabbedPane m_connectionsPane;
    private JDesktopPane m_desktopPane;
    private JSplitPane m_splitPane;
    private MenuBar m_menuBar;
    private StatusBar m_statusBar;
    
    private boolean m_antialias;

    private File m_desktopFile;
    private File m_desktopFileDir;
    
    private Map m_connections = new HashMap();
    private InstrumentManagerConnection[] m_connectionArray;
    
    /** Shutdown hook */
    private Thread m_hook;
    
    private Logger m_logger;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentClient frame.
     *
     * @param title The title for the frame.
     */
    InstrumentClientFrame( String title )
    {
        super();

        m_title = title;
    }
    
    public void initialize()
    {
        init();
        
        ClassLoader cl = this.getClass().getClassLoader();
        setIconImage( new ImageIcon( cl.getResource( MEDIA_PATH + "client.gif") ).getImage() );
    }
    
    /*---------------------------------------------------------------
     * InstrumentManagerConnectionListener Methods
     *-------------------------------------------------------------*/
    /**
     * Called when the connection is opened.  May be called more than once if 
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was opened.
     */
    public void opened( InstrumentManagerConnection connection )
    {
        getLogger().debug( "opened: " + connection.getKey() );
        updateConnectionTab( connection );
    }
    
    /**
     * Called when the connection is closed.  May be called more than once if 
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was closed.
     */
    public void closed( InstrumentManagerConnection connection )
    {
        getLogger().debug( "closed: " + connection.getKey() );
        updateConnectionTab( connection );
    }
    
    /**
     * Called when the connection is deleted.  All references should be removed.
     *
     * @param connection Connection which was deleted.
     */
    public void deleted( InstrumentManagerConnection connection )
    {
        getLogger().debug( "deleted: " + connection.getKey() );
        // Remove the tab
        int tabIndex = m_connectionsPane.indexOfComponent( connection );
        if ( tabIndex >= 0 )
        {
            // Doing this within the shutdown hook causes a deadlock. Java bug?
            if ( Thread.currentThread() != m_hook )
            {
                m_connectionsPane.remove( connection );
            }
        }
        
        connection.removeInstrumentManagerConnectionListener( this );
        Object key = connection.getKey();
        synchronized ( m_connections )
        {
            m_connections.remove( key );
            m_connectionArray = null;
        }
    }
    
    /*---------------------------------------------------------------
     * LogEnabled Methods
     *-------------------------------------------------------------*/
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }
    
    Logger getLogger()
    {
        return m_logger;
    }
    
    /*---------------------------------------------------------------
     * State Methods
     *-------------------------------------------------------------*/
    /**
     * Stores the default state file name and attempts to load it if it exists.
     *  Should onl be called at startup.
     *
     * @param defaultStateFile The default statefile which will be loaded on
     *        startup.
     */
    void setDefaultStateFile( File defaultStateFile )
    {
        // See if the directory containing th defaultStateFile exists.  If so set it.
        File defaultStateFileDir = defaultStateFile.getParentFile();
        if ( defaultStateFileDir.exists() )
        {
            m_desktopFileDir = defaultStateFileDir;
        }
        if ( defaultStateFile.exists() )
        {
            try
            {
                m_desktopFile = null;
                loadStateFromFile( defaultStateFile, true );
                m_desktopFile = defaultStateFile;
            }
            catch( Exception e )
            {
                String msg = "Unable to load desktop file.";
                getLogger().debug( msg, e );
                showErrorDialog( msg, e );
            }
            updateTitle();
        }
    }
    
    /**
     * Loads the Instrument Client state from the specified file.
     *
     * @param stateFile File to read the client's state from.
     * @param showErrorDialog Controls whether an error dialog should be
     *                        displayed or not.
     *
     * @throws Exception if there are any problems loading the state.
     */
    void loadStateFromFile( File stateFile, boolean showErrorDialog ) throws Exception
    {
        long now = System.currentTimeMillis();
        getLogger().debug( "Loading Instrument Client state from: " +
            stateFile.getAbsolutePath() );

        FileInputStream is = new FileInputStream( stateFile );
        try
        {
            loadStateFromStream( is, showErrorDialog );
        }
        finally
        {
            is.close();
        }

        getLogger().debug( "Loading Instrument Client state took " +
                           ( System.currentTimeMillis() - now ) + "ms." );
    }

    /**
     * Loads the Instrument Client state from the specified stream.
     *
     * @param is Stream to read the instrument client's state from.
     * @param showErrorDialog Controls whether an error dialog should be
     *                        displayed or not.
     *
     * @throws Exception if there are any problems loading the state.
     */
    void loadStateFromStream( InputStream is, boolean showErrorDialog ) throws Exception
    {
        // Ride on top of the Configuration classes to load the state.
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration stateConfig = builder.build( is );

        loadStateFromConfiguration( stateConfig, showErrorDialog );
    }

    /**
     * Loads the Instrument Client state from the specified Configuration.
     *
     * @param state Configuration object to load the state from.
     * @param showErrorDialog Controls whether an error dialog should be
     *                        displayed or not.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    void loadStateFromConfiguration( Configuration state, boolean showErrorDialog )
        throws ConfigurationException
    {
        // Load the global client state information.
        
        try
        {
            // Load the frame information.  It is in a child node.
            Configuration frameState = state.getChild( "frame" );
            // Window position
            setLocation( frameState.getAttributeAsInteger( "x" ),
                frameState.getAttributeAsInteger( "y" ) );
            setSize( frameState.getAttributeAsInteger( "width" ),
                frameState.getAttributeAsInteger( "height" ) );
            // Window state
            if ( frameState.getAttributeAsBoolean( "iconized", false ) )
            {
                setState( Frame.ICONIFIED );
            }
            // Split Pane state
            m_splitPane.setDividerLocation(
                frameState.getAttributeAsInteger( "divider-location" ) );
            m_splitPane.setLastDividerLocation(
                frameState.getAttributeAsInteger( "last-divider-location" ) );
            // Antialias.
            m_antialias = frameState.getAttributeAsBoolean( "antialias", false );
        }
        catch ( ConfigurationException e )
        {
            String msg = "Unable to fully load the frame state.";
            if ( showErrorDialog )
            {
                getLogger().debug( msg, e );
                showErrorDialog( msg, e );
            }
            else
            {
                getLogger().warn( msg, e );
            }
        }
        
        // Show the frame here so that the rest of this works.
        show();
        
        // Store a map of the current connections.
        Map oldConnections;
        synchronized( m_connections )
        {
            oldConnections = new HashMap( m_connections );
        }
            
        // Load the state of any connections.
        Configuration[] connConfs = state.getChildren( "connection" );
        for( int i = 0; i < connConfs.length; i++ )
        {
            Configuration connConf = connConfs[ i ];
            String tURL = connConf.getAttribute( "url" );
            URL url;
            try
            {
                url = new URL( tURL );
            }
            catch ( MalformedURLException e )
            {
                throw new ConfigurationException( "Invalid url, '" + tURL + "'", e );
            }
            
            InstrumentManagerConnection conn;
            synchronized ( m_connections )
            {
                conn = (InstrumentManagerConnection)m_connections.get( url );
                
                if ( conn == null )
                {
                    // Need to create a new connection
                    conn = createConnection( url );
                }
                
                oldConnections.remove( url );
            }
            
            // Load the state into the connection.
            try
            {
                conn.loadState( connConf );
            }
            catch ( ConfigurationException e )
            {
                String msg = "Unable to fully load the state of connection, " + conn.getKey();
                if ( showErrorDialog )
                {
                    getLogger().debug( msg, e );
                    showErrorDialog( msg, e );
                }
                else
                {
                    getLogger().warn( msg, e );
                }
            }
        }
        
        // Any old connections left need to be deleted.
        for ( Iterator iter = oldConnections.values().iterator(); iter.hasNext(); )
        {
            InstrumentManagerConnection conn = (InstrumentManagerConnection)iter.next();
            conn.delete();
        }
        
        
        // Always hide all existing frames as new ones will be created.
        JInternalFrame frames[] = m_desktopPane.getAllFrames();
        for ( int i = 0; i < frames.length; i++ )
        {
            if ( frames[i] instanceof AbstractInternalFrame )
            {
                ((AbstractInternalFrame)frames[i]).hideFrame();
            }
        }
        
        // Load the state of any inner frames.
        Configuration[] frameConfs = state.getChildren( "inner-frame" );
        for( int i = 0; i < frameConfs.length; i++ )
        {
            Configuration frameConf = frameConfs[ i ];
            String type = frameConf.getAttribute( "type" );
            
            if ( type.equals( InstrumentSampleFrame.FRAME_TYPE ) )
            {
                // Figure out which connection the frame will belong to.
                String tURL = frameConf.getAttribute( "url" );
                URL url;
                try
                {
                    url = new URL( tURL );
                }
                catch ( MalformedURLException e )
                {
                    throw new ConfigurationException( "Invalid url, '" + tURL + "'", e );
                }
                
                InstrumentManagerConnection connection = getConnection( url );
                if ( connection == null )
                {
                    // Connection not found.
                    String msg = "Sample frame not being loaded becase no connection to " +
                        url.toExternalForm() + " exists.";
                    if ( showErrorDialog )
                    {
                        getLogger().debug( msg );
                        showErrorDialog( msg );
                    }
                    else
                    {
                        getLogger().warn( msg );
                    }
                } else {
                    // Let the connection load the frame.
                    try
                    {
                        connection.loadSampleFrame( frameConf );
                    }
                    catch ( ConfigurationException e )
                    {
                        String msg =
                            "Unable to fully load the state of an inner frame for sample: " +
                            frameConf.getAttribute( "sample", "Sample name missing" );
                        if ( showErrorDialog )
                        {
                            getLogger().debug( msg, e );
                            showErrorDialog( msg, e );
                        }
                        else
                        {
                            getLogger().warn( msg, e );
                        }
                    }
                }
            }
            else
            {
                // Ignore unknown types.
                getLogger().warn( "Not loading inner frame due to unknown type: " + type );
            }
        }
    }

    /**
     * Saves the Instrument Client's state to the specified file.  Any
     *  existing file is backed up before the save takes place and replaced
     *  in the event of an error.
     *
     * @param stateFile File to write the Instrument Client's state to.
     *
     * @throws Exception if there are any problems saving the state.
     */
    void saveStateToFile( File stateFile ) throws Exception
    {
        long now = System.currentTimeMillis();
        getLogger().debug( "Saving Instrument Client state to: " + stateFile.getAbsolutePath() );

        // First save the state to an in memory stream to shorten the
        //  period of time needed to write the data to disk.  This makes it
        //  less likely that the files will be left in a corrupted state if
        //  the JVM dies at the wrong time.
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] data;
        try
        {
            saveStateToStream( os );
            data = os.toByteArray();
        }
        finally
        {
            os.close();
        }
        
        // If the specified file exists, then rename it before we start writing.
        //  This makes it possible to recover from some errors.
        File renameFile = null;
        boolean success = false;
        if( stateFile.exists() )
        {
            renameFile = new File( stateFile.getAbsolutePath() + "." + now + ".backup" );
            stateFile.renameTo( renameFile );
        }
        
        // Write the data to the new file.
        FileOutputStream fos = new FileOutputStream( stateFile );
        try
        {
            fos.write( data );
            success = true;
        }
        finally
        {
            fos.close();
            
            if ( !success )
            {
                // Make sure that part of the file does not exist.
                stateFile.delete();
            }
            
            // Handle the backup file.
            if ( renameFile != null )
            {
                if ( success )
                {
                    // No longer need the backup.
                    renameFile.delete();
                }
                else
                {
                    // Need to replace the backup.
                    renameFile.renameTo( stateFile );
                }
            }
        }
        
        getLogger().debug( "Saving Instrument Client state took " +
                           ( System.currentTimeMillis() - now ) + "ms." );
    }

    /**
     * Saves the Instrument Client's state to the specified output stream.
     *
     * @param os Stream to write the Instrument Client's state to.
     *
     * @throws Exception if there are any problems saving the state.
     */
    void saveStateToStream( OutputStream os ) throws Exception
    {
        Configuration stateConfig = saveStateToConfiguration();

        // Ride on top of the Configuration classes to save the state.
        DefaultConfigurationSerializer serializer = new DefaultConfigurationSerializer();
        serializer.setIndent( true );
        serializer.serialize( os, stateConfig );
    }

    /**
     * Returns the Instrument Manager's state as a Configuration object.
     *
     * @return The Instrument Manager's state as a Configuration object.
     */
    Configuration saveStateToConfiguration()
    {
        DefaultConfiguration state = new DefaultConfiguration( "instrument-client-state", "-" );
        
        // Save the frame information.  Use a seperate element to keep it clean.
        DefaultConfiguration frameState = new DefaultConfiguration( "frame", "-" );
        // Window position
        frameState.setAttribute( "x", Integer.toString( getX() ) );
        frameState.setAttribute( "y", Integer.toString( getY() ) );
        frameState.setAttribute( "width", Integer.toString( getWidth() ) );
        frameState.setAttribute( "height", Integer.toString( getHeight() ) );
        // Window state
        if ( getState() == Frame.ICONIFIED )
        {
            frameState.setAttribute( "iconized", "true" );
        }
        // Split Pane state
        frameState.setAttribute( "divider-location", Integer.toString( m_splitPane.getDividerLocation() ) );
        frameState.setAttribute( "last-divider-location", Integer.toString( m_splitPane.getLastDividerLocation() ) );
        // Antialias
        frameState.setAttribute( "antialias", Boolean.toString( m_antialias ) );
        // Add frame state
        state.addChild( frameState );
        
        // Save the state of any connections.
        InstrumentManagerConnection[] connections = getConnections();
        for ( int i = 0; i < connections.length; i++ )
        {
            state.addChild( connections[i].saveState() );
        }
        
        // Save the state of any inner frames.
        JInternalFrame frames[] = m_desktopPane.getAllFrames();
        for ( int i = 0; i < frames.length; i++ )
        {
            if ( frames[i] instanceof AbstractInternalFrame )
            {
                AbstractInternalFrame internalFrame = (AbstractInternalFrame)frames[i];
                state.addChild( internalFrame.getState() );
            }
        }

        return state;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private void init()
    {
        updateTitle();
        
        // Add a shutdown hook to trap CTRL-C events.
        m_hook = new Thread( SHUTDOWN_HOOK_NAME )
        {
            public void run()
            {
                getLogger().debug( "InstrumentClientFrame.shutdownHook start");
                
                shutdown();
                
                getLogger().debug( "InstrumentClientFrame.shutdownHook end");
            }
        };
        Runtime.getRuntime().addShutdownHook( m_hook );
        
        // Add a Window listener to trap when the user hits the close box.
        addWindowListener( new WindowAdapter()
            {
                public void windowClosing( WindowEvent event )
                {
                    fileExit();
                }
            });

        getContentPane().setLayout( new BorderLayout() );
        
        // Create a Tabbed Panel of the connections.
        m_connectionsPane = new JTabbedPane( JTabbedPane.TOP );
        
        // Create a DesktopPane and place it in a BevelBorder
        m_desktopPane = new DesktopPane();
        JPanel dBorder = new JPanel();
        dBorder.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        dBorder.setLayout( new BorderLayout() );
        dBorder.add( m_desktopPane, BorderLayout.CENTER );

        // Create a SplitPane at the root.
        m_splitPane =
            new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, m_connectionsPane, dBorder );
        m_splitPane.setOneTouchExpandable( true );
        m_splitPane.setDividerLocation( 250 );
        
        getContentPane().add( m_splitPane, BorderLayout.CENTER );

        // Create a Menu Bar
        m_menuBar = new MenuBar( this );
        setJMenuBar( m_menuBar );
        
        m_statusBar = new StatusBar();
        getContentPane().add( m_statusBar, BorderLayout.SOUTH );

        Toolkit toolkit = getToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        // Set the default size and location of the window.  This will be overridden
        //  by whatever is stored in a state file if loaded.
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setLocation( screenWidth / 20, screenHeight / 20 );
        setSize( screenWidth * 9 / 10, screenHeight * 8 / 10 );
    }
    
    boolean isAntialias()
    {
        return m_antialias;
    }
    void setAntialias( boolean antialias )
    {
        m_antialias = antialias;
    }

    void updateConnectionTab( InstrumentManagerConnection connection )
    {
        // Update the tab title.
        int tabIndex = m_connectionsPane.indexOfComponent( connection );
        if ( tabIndex >= 0 )
        {
            m_connectionsPane.setTitleAt( tabIndex, connection.getTabTitle() );
            m_connectionsPane.setToolTipTextAt( tabIndex, connection.getTabTooltip() );
        }
    }
    
    private void updateTitle()
    {
        if( m_desktopFile == null )
        {
            setTitle( m_title );
        }
        else
        {
            setTitle( m_title + " - " + m_desktopFile.getAbsolutePath() );
        }
    }
    
    void setStatusMessage( String message )
    {
        m_statusBar.setStatusMessage( message );
    }
    
    JDesktopPane getDesktopPane()
    {
        return m_desktopPane;
    }
    
    void closeAllFrames()
    {
        JInternalFrame[] frames = m_desktopPane.getAllFrames();
        for( int i = 0; i < frames.length; i++ )
        {
            frames[ i ].setVisible( false );
            frames[ i ].dispose();
        }
    }
    
    /**
     * Tile all open frames
     */
    void tileFrames()
    {
        JInternalFrame[] openFrames = getOpenFrames();
        
        int count = openFrames.length;
        if ( count == 0)
        {
            return;
        }
        
        // Target the frames at the specified maximum aspect ratio.  The
        //  additional constraint that the frames will not be allowed to
        //  be less than 70 pixels in height unless their width is less
        //  than 100.
        float targetRatio = 5.0f;
        
        Dimension size = getDesktopPane().getSize();
        int cols = 1;
        int rows = count;
        int frameWidth = size.width / cols;
        int frameHeight = size.height / rows;
        float ratio = (float)frameWidth / frameHeight;
        while ( ( rows > 1 ) && ( ( ratio > targetRatio ) ||
            ( ( frameHeight < 70 ) && ( frameWidth > 100 ) ) ) )
        {
            cols++;
            rows = (int)Math.ceil( (float)count / cols );
            frameWidth = size.width / cols;
            frameHeight = size.height / rows;
            ratio = (float)frameWidth / frameHeight;
        }
        
        reorganizeFrames( rows, cols, openFrames );
    }
    
    /**
     * Get a list with all open frames. 
     *
     * @return Array of all open internal frames
     */
    JInternalFrame[] getOpenFrames()
    {
        JInternalFrame[] frames = m_desktopPane.getAllFrames();
        int count = frames.length;
        
        // No frames
        if (count == 0) 
        {
            // Array is empty, so it is safe to return.
            return frames;
        }
    
        // add only open frames to the list
        List openFrames = new ArrayList();
        for ( int i = 0; i < count; i++ )
        {
            JInternalFrame f = frames[i];
            if( ( f.isClosed() == false ) && ( f.isIcon() == false ) )
            {
                openFrames.add( f );
            }
        }
        
        // Create a simple array to be returned
        frames = new JInternalFrame[ openFrames.size() ];
        openFrames.toArray( frames );
        
        return frames;
    }
    
    /**
     * Reorganizes a list of internal frames to a specific
     * number of rows and columns.
     *
     * @param rows number of rows to use
     * @param cols number of columns to use
     * @param frames list with <code>JInternalFrames</code>
     */  
    void reorganizeFrames( int rows, int cols, JInternalFrame[] frames )
    {
        // Determine the size of one windows
        Dimension desktopsize = m_desktopPane.getSize();
        int w = desktopsize.width / cols;
        int h = desktopsize.height / rows;
        int x = 0;
        int y = 0;
        int count = frames.length;

        for ( int i = 0; i < rows; ++i)
        {
            for ( int j = 0; j < cols && ( ( i * cols ) + j < count ); ++j ) 
            {
                JInternalFrame f = frames[ ( i * cols ) + j ];
                m_desktopPane.getDesktopManager().resizeFrame( f, x, y, w, h );
                x += w;
            }
            y += h;
            x = 0;   
        }
    }
    
    /**
     * Tiles all internal frames horizontally
     */
    void tileFramesH()
    {
        JInternalFrame[] openFrames = getOpenFrames();
        
        int count = openFrames.length;
        if ( count == 0 )
        {
            return;
        }
        reorganizeFrames( count, 1, openFrames );
    }
    
    /**
     * Tiles all internal frames vertically
     */
    void tileFramesV()
    {
        JInternalFrame[] openFrames = getOpenFrames();
        
        int count=openFrames.length;
        if ( count == 0)
        {
            return;
        }
        reorganizeFrames( 1, count, openFrames );
    }
    
    InstrumentManagerConnection[] getConnections()
    {
        // Avoid synchronization when possible.
        InstrumentManagerConnection[] array = m_connectionArray;
        if ( array == null )
        {
            synchronized ( m_connections )
            {
                m_connectionArray = new InstrumentManagerConnection[m_connections.size()];
                m_connections.values().toArray( m_connectionArray );
                array = m_connectionArray;
            }
        }
        return array;
    }
    
    InstrumentManagerConnection getConnection( URL url )
    {
        synchronized ( m_connections )
        {
            return (InstrumentManagerConnection)m_connections.get( url );
        }
    }
    
    void showConnectDialog()
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                URL defaultURL;
                try
                {
                    defaultURL = new URL( "http://localhost:15080" );
                }
                catch ( MalformedURLException e )
                {
                    // Should never happen.
                    e.printStackTrace();
                    return;
                }
                
                ConnectDialog dialog = new ConnectDialog( InstrumentClientFrame.this );
                dialog.setURL( defaultURL );
                dialog.show();
                if ( dialog.getAction() == ConnectDialog.BUTTON_OK )
                {
                    synchronized( m_connections )
                    {
                        createConnection( dialog.getURL() );
                    }
                }
            }
        } );
    }
    
    /**
     * Creates an registers a new InstrumentManagerConnection.  This method
     *  should never be called in the connection already exists.  Caller must
     *  ensure that m_connections is synchronized.
     *
     * @param url URL of the connecton.
     *
     * @return The new InstrumentManagerConnection
     */
    private InstrumentManagerConnection createConnection( URL url )
    {
        InstrumentManagerConnection conn = new HTTPInstrumentManagerConnection( url );
        ContainerUtil.enableLogging(
            conn, getLogger().getChildLogger( url.getHost() + ":" + url.getPort() ) );
        conn.setFrame( this );
        conn.init();
        m_connections.put( conn.getKey(), conn );
        m_connectionArray = null;
        
        conn.addInstrumentManagerConnectionListener( this );
        
        m_connectionsPane.add( conn.getTabTitle(), conn );
        
        return conn;
    }
    
    private void showErrorDialog( String message )
    {
        JOptionPane.showMessageDialog( this,
                                       "<html><body><font color=\"black\">" + message + "</font>" +
                                       "</body></html>", m_title + " Error",
                                       JOptionPane.ERROR_MESSAGE );
    }
    
    private void showErrorDialog( String message, Throwable t )
    {
        JOptionPane.showMessageDialog( this,
                                       "<html><body><font color=\"black\">" + message + 
                                       "</font><br><br><font color=\"black\">Reason: " +
                                       t.getMessage() + "</font></body></html>",
                                       m_title + " Error", JOptionPane.ERROR_MESSAGE );
    }


    /**
     * Shutdown the InstrumentClient.
     */
    private void shutdown()
    {
        getLogger().debug( "InstrumentClientFrame.shutdown()" );
        boolean fallThrough = false;
        if ( m_hook != null )
        {
            if ( m_hook == Thread.currentThread() )
            {
                // This is the shutdown hook
                fallThrough = true;
            }
            else
            {
                // Unregister the shutdown hook
                Runtime.getRuntime().removeShutdownHook( m_hook );
                m_hook = null;
            }
        }
        
        // Delete all of the connections.
        InstrumentManagerConnection[] connections = getConnections();
        for ( int i = 0; i < connections.length; i++ )
        {
            connections[i].delete();
        }
        
        if ( !fallThrough )
        {
            // Kill the JVM.
            System.exit( 1 );
        }
    }
    
    /*---------------------------------------------------------------
     * Menu Callback Methods
     *-------------------------------------------------------------*/
    /**
     * File->New callback.
     */
    void fileNew()
    {
        m_desktopFile = null;
        closeAllFrames();
        updateTitle();
    }
    
    /**
     * File->Open callback.
     */
    void fileOpen()
    {
        JFileChooser chooser = new JFileChooser();
        
        FileFilter filter = new FileFilter()
        {
            public boolean accept( File f )
            {
                if( f.isDirectory() )
                {
                    return true;
                }
                else
                {
                    return f.getName().endsWith( ".desktop" );
                }
            }
            
            public String getDescription()
            {
                return "Desktop state files";
            }
        };
        
        if ( m_desktopFileDir != null )
        {
            chooser.setCurrentDirectory( m_desktopFileDir );
        }
        else
        {
            chooser.setCurrentDirectory( new File( System.getProperty( "user.dir" ) ) );
        }
        
        chooser.setFileFilter( filter );
        
        int returnVal = chooser.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            try
            {
                m_desktopFile = null;
                File file = chooser.getSelectedFile();
                m_desktopFileDir = file.getParentFile();
                loadStateFromFile( file, true );
                m_desktopFile = file;
            }
            catch( Exception e )
            {
                String msg = "Unable to load desktop file.";
                getLogger().debug( msg, e );
                showErrorDialog( msg, e );
            }
            updateTitle();
        }
    }
    
    void fileSave()
    {
        if( m_desktopFile != null )
        {
            try
            {
                saveStateToFile( m_desktopFile );
            }
            catch( Exception e )
            {
                String msg = "Unable to save desktop file.";
                getLogger().debug( msg, e );
                showErrorDialog( msg, e );
            }
        }
        else
        {
            fileSaveAs();
        }
    }
    
    void fileSaveAs()
    {
        JFileChooser chooser = new JFileChooser();
        
        FileFilter filter = new FileFilter()
        {
            public boolean accept( File f )
            {
                if( f.isDirectory() )
                {
                    return true;
                }
                else
                {
                    return f.getName().endsWith( ".desktop" );
                }
            }
            
            public String getDescription()
            {
                return "Desktop state files";
            }
        };
        
        if ( m_desktopFileDir != null )
        {
            chooser.setCurrentDirectory( m_desktopFileDir );
        }
        else
        {
            chooser.setCurrentDirectory( new File( System.getProperty( "user.dir" ) ) );
        }
        
        chooser.setFileFilter( filter );
        
        int returnVal = chooser.showSaveDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = chooser.getSelectedFile();
            if( file.getName().indexOf( '.' ) < 0 )
            {
                // Did not specify an extension.  Add one.
                file = new File( file.getAbsolutePath() + ".desktop" );
            }
            
            try
            {
                saveStateToFile( file );
                
                // If we were able to save the file, then set it as the current
                //  file.
                m_desktopFile = file;
                m_desktopFileDir = m_desktopFile.getParentFile();

            }
            catch( Exception e )
            {
                String msg = "Unable to save desktop file.";
                getLogger().debug( msg, e );
                showErrorDialog( msg, e );
            }
            updateTitle();
        }
    }
    
    /**
     * File-Exit callback.
     */
    void fileExit()
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                shutdown();
            }
        } );
    }
}

