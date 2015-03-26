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

package org.apache.excalibur.instrument.client.http;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.instrument.client.InstrumentManagerConnection;
import org.apache.excalibur.instrument.client.InstrumentManagerConnectionListener;
import org.apache.excalibur.instrument.client.InstrumentManagerData;
import org.apache.excalibur.instrument.client.InstrumentSampleFrame;

/**
 * A Connection to the remote InstrumentManager which connects using
 *  the HTTP connector.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public class HTTPInstrumentManagerConnection
    extends InstrumentManagerConnection
{
    private URL m_url;

    /** Flag which keeps track of whether or not the remote server was there
    *   the last time we attempted to connect. */
    private boolean m_connected;

    /** If we ever decide that we are not talking to an Instrument Manager then
     *   disable the connection to avoid pounding the remote server with lots
     *   of 404 requests. */
    private boolean m_disabled;

    private HTTPInstrumentManagerData m_manager;

    private List m_leasedSamples = new ArrayList();
    private HTTPInstrumentSampleData[] m_leasedSampleAry;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTTPInstrumentManagerConnection.
     */
    public HTTPInstrumentManagerConnection( URL url )
    {
        this.m_url = url;
        this.m_connected = false;

        this.m_manager = new HTTPInstrumentManagerData( this );
    }

    /*---------------------------------------------------------------
     * InstrumentManagerConnection Methods
     *-------------------------------------------------------------*/
    public void enableLogging( Logger logger )
    {
        super.enableLogging( logger );
        this.m_manager.enableLogging( logger.getChildLogger( "manager" ) );
    }

    /**
     * Returns the key used to identify this object.
     *
     * @return The key used to identify this object.
     */
    public Object getKey()
    {
        return this.m_url;
    }

    /**
     * Returns true if connected.
     *
     * @return True if connected.
     */
    public boolean isConnected()
    {
        return this.m_connected;
    }

    /**
     * Returns the Instrument Manager.
     *
     * @return The Instrument Manager.
     */
    public InstrumentManagerData getInstrumentManager()
    {
        return this.m_manager;
    }

    /**
     * Returns the title to display in the tab for the connection.
     *
     * @return The tab title.
     */
    public String getTabTitle()
    {
        if ( this.m_disabled )
        {
            return "[DISABLED] " + super.getTabTitle();
        }
        else
        {
            return super.getTabTitle();
        }
    }

    /**
     * Invokes GC on the JVM running the InstrumentManager.
     */
    protected void invokeGC()
    {
        this.getState( "gc.xml" );
    }

    /**
     * Saves the current state into a Configuration.
     *
     * @return The state as a Configuration.
     */
    public Configuration saveState()
    {
        synchronized( this )
        {
            DefaultConfiguration state = (DefaultConfiguration)super.saveState();

            state.setAttribute( "url", this.m_url.toExternalForm() );

            return state;
        }
    }

    /**
     * Loads the state from a Configuration object.
     *
     * @param state Configuration object to load state from.
     *
     * @throws ConfigurationException If there were any problems loading the
     *                                state.
     */
    public void loadState( Configuration state )
        throws ConfigurationException
    {
        synchronized( this )
        {
            super.loadState( state );

            // URL will have already been set.
        }
    }

    /**
     * URL encode the specified string.
     *
     * @param val String to be URL encoded.
     *
     * @return The URL encoded string.
     */
    String urlEncode( String val )
    {
        try
        {
            return URLEncoder.encode( val, "UTF8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            // Should never happen.
            this.getLogger().error( "Bad encoding.", e );
            return val;
        }
    }

    /**
     * Updates all registered SampleFrames with the latest data from the
     *  server.  The status of all Sample Frames is also handled by this
     *  method, so it must handle disconnected connections and missing or
     *  expired samples correctly.
     *
     * This method overrides the default to implement a batch update to
     *  get all snapshots from the server in a single request.
     */
    public void updateSampleFrames()
    {
        InstrumentSampleFrame[] frames = this.getSampleFrameArray();
        if ( frames.length == 0 )
        {
            // Nothing to do.
            return;
        }

        // Build up a set of arrays so that all of the snapshots can be requested at once.
        String[] names = new String[frames.length];
        long[] lastTimes = new long[frames.length];
        HTTPInstrumentSampleSnapshotData[] snapshots =
            new HTTPInstrumentSampleSnapshotData[frames.length];
        for ( int i = 0; i < frames.length; i++ )
        {
            InstrumentSampleFrame frame = frames[i];
            names[i] = frame.getInstrumentSampleName();
            lastTimes[i] = frame.getLastSnapshotTime();
        }

        // Request the snapshots.  Don't bother if we know we are not connected.
        if ( this.isConnected() )
        {
            StringBuffer sb = new StringBuffer();
            sb.append( "snapshots.xml?packed=true&compact=true" );
            for ( int i = 0; i < frames.length; i++ )
            {
                sb.append( "&name=" );
                sb.append( this.urlEncode( names[i] ) );
                sb.append( "&base-time=" );
                sb.append( lastTimes[i] );
            }
            Configuration configuration = this.getState( sb.toString() );
            if ( configuration != null )
            {
                Configuration[] snapshotConfs = configuration.getChildren( "sample" );
                for ( int i = 0; i < snapshotConfs.length; i++ )
                {
                    Configuration snapshotConf = snapshotConfs[i];
                    String name = snapshotConf.getAttribute( "name", null );
                    if ( name != null )
                    {
                        boolean expired = snapshotConf.getAttributeAsBoolean( "expired", false );
                        if ( !expired )
                        {
                            // Look for the specified sample frame.  Should always exist.
                            for ( int j = 0; j < frames.length; j++ )
                            {
                                if ( name.equals( names[j] ) )
                                {
                                    snapshots[j] =
                                        new HTTPInstrumentSampleSnapshotData( this, name );
                                    snapshots[j].enableLogging( this.getLogger() );
                                    try
                                    {
                                        snapshots[j].update( snapshotConf );
                                    }
                                    catch ( ConfigurationException e )
                                    {
                                        // Should not happen.
                                        this.getLogger().info( "Snapshot update failed.", e );
                                        this.getLogger().info( " URL: " + sb.toString() );
                                        this.getLogger().info( " i:" + i + " j:" + j );
                                        snapshots[j] = null;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Now we should have all available snapshots.  Loop back over the frames
        //  and update them as is appropriate.
        for ( int i = 0; i < frames.length; i++ )
        {
            InstrumentSampleFrame frame = frames[i];
            frame.updateSnapshot( snapshots[i] );
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the URL of the remote InstrumentManager.
     *
     * @return The URL of the remote InstrumentManager.
     */
    URL getURL()
    {
        return this.m_url;
    }

    /**
     * Requests the current state of the object at the specified path.
     *  If the request fails for any reason, including not being valid
     *  content then the method will return null.
     *
     * @param path The path of the object whose state is requested.
     *
     * @return The state as a Configuration object, or null if it failed.
     */
    Configuration getState( String path )
    {
        if ( this.m_disabled )
        {
            return null;
        }

        URL url;
        try
        {
            url = new URL( this.m_url, path );
        }
        catch ( MalformedURLException e )
        {
            this.getLogger().debug( "Request failed.", e );
            return null;
        }

        try
        {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK )
            {
                boolean oldConnected = this.m_connected;
                this.m_connected = true;
                if ( !oldConnected )
                {
                    // Notify the listeners.
                    InstrumentManagerConnectionListener[] listenerArray = this.getListenerArray();
                    for ( int i = 0; i < listenerArray.length; i++ )
                    {
                        listenerArray[i].opened( this );
                    }
                }

                InputStream is = conn.getInputStream();
                try
                {
                    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                    try
                    {
                        return builder.build( is );
                    }
                    catch ( ConfigurationException e )
                    {
                        this.getLogger().warn( "Invalid XML reveived from the server.", e );
                        return null;
                    }
                    catch ( org.xml.sax.SAXException e )
                    {
                        this.getLogger().warn( "Invalid XML reveived from the server.", e );
                        return null;
                    }
                }
                finally
                {
                    is.close();
                }
            }
            else
            {
                if ( ( conn.getResponseCode() == 404 )
                    && path.startsWith( "instrument-manager.xml" ) )
                {
                    this.getLogger().warn( "Requested " + url + " resulted in error code 404.  "
                        + "Most likely not an Instrument Manager, disabling future requests." );
                    this.m_disabled = true;
                }
                else
                {
                    this.getLogger().debug( "Response: " + conn.getResponseCode() + " : "
                        + conn.getResponseMessage() );
                }
                return null;
            }
        }
        catch ( IOException e )
        {
            String msg = e.getMessage();
            if ( msg == null )
            {
                msg = e.toString();
            }

            if ( msg.indexOf( "Connect" ) >= 0 )
            {
                // Hide the stack trace as the server is simply down.
                this.getLogger().debug( "Request failed.  URL: " + url + "  Error: " + msg );
            }
            else
            {
                this.getLogger().debug( "Request failed.  URL: " + url + "  Error: ", e );
            }


            boolean oldConnected = this.m_connected;
            this.m_connected = false;
            if ( oldConnected )
            {
                // Notify the listeners.
                InstrumentManagerConnectionListener[] listenerArray = this.getListenerArray();
                for ( int i = 0; i < listenerArray.length; i++ )
                {
                    listenerArray[i].closed( this );
                }
            }

            return null;
        }
    }
}