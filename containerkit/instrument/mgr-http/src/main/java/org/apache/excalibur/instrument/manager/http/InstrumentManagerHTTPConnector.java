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

package org.apache.excalibur.instrument.manager.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManagerConnector;
import org.apache.excalibur.instrument.manager.http.server.AbstractHTTPURLHandler;
import org.apache.excalibur.instrument.manager.http.server.HTTPServer;

/**
 * An HTTP connector which allows a client to connect to the ServiceManager
 *  using the HTTP protocol.  This connector makes use of an extremely
 *  lightweight internal HTTP server to provide this access.
 *
 * If the application is already running a full blown Servlet Engine, one
 *  alternative to this connector is to make use of the InstrumentManagerServlet.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class InstrumentManagerHTTPConnector
    extends AbstractLogEnabledInstrumentable
    implements DefaultInstrumentManagerConnector, Configurable, Startable
{
    /** The default port. */
    public static final int DEFAULT_PORT = 15080;
    
    /** The encoding to use when writing out pages and reading in parameters. */
    public static final String ENCODING = "UTF-8";
    public static final String XML_BANNER = "<?xml version='1.0' encoding='" + ENCODING + "'?>";

    /** Reference to the actual instrument manager. */
    private DefaultInstrumentManager m_manager;
    
    /** The port to listen on for connections. */
    private int m_port;
    
    /** The address to bind the port server to.  Null for any address. */
    private InetAddress m_bindAddr;
    
    /** True if XML handlers should be registered. */
    private boolean m_xml;
    
    /** True if HTML handlers should be registered. */
    private boolean m_html;
    
    /** Default Chart Width. */
    private int m_chartWidth;
    
    /** Default Chart Height. */
    private int m_chartHeight;
    
    /** Antialias flag for images. */
    private boolean m_antialias;
    
    /** The maximum number of leased samples which will be allowed.  This is
     *   important to prevent denial of service attacks using connectors. */
    private int m_maxLeasedSamples;
    
    /** The maximum size of a leased sample.  This is important to prevent
     *   denial of service attacks using connectors. */
    private int m_maxLeasedSampleSize;
    
    /** The maximum amount of time that a lease will be granted for.  This is
     *   important to prevent denial of service attacks using connectors. */
    private long m_maxLeasedSampleLease;
    
    /** True if the connector should only provide read-only access to the
     *   Instrument Manager. */
    private boolean m_readOnly;
    
    /** The root bread crumb URL if configured. */
    private String m_rootBreadCrumbURL;
    
    /** The root bread crumb label if configured. */
    private String m_rootBreadCrumbLabel;
    
    private HTTPServer m_httpServer;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new InstrumentManagerHTTPConnector.
     */
    public InstrumentManagerHTTPConnector()
    {
        setInstrumentableName( "http" );
    }

    /*---------------------------------------------------------------
     * DefaultInstrumentManagerConnector Methods
     *-------------------------------------------------------------*/
    /**
     * Set the InstrumentManager to which the Connecter will provide
     *  access.  This method is called before the new connector is
     *  configured or started.
     */
    public void setInstrumentManager( DefaultInstrumentManager manager )
    {
        m_manager = manager;
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        m_port = configuration.getAttributeAsInteger( "port", DEFAULT_PORT );
        
        String bindAddress = configuration.getChild( "bind" ).getValue( null );
        try
        {
            if ( null != bindAddress )
            {
                m_bindAddr = InetAddress.getByName( bindAddress );
            }
        }
        catch ( final UnknownHostException e )
        {
            throw new ConfigurationException(
                "Unable to resolve the bind point: " + bindAddress, e );
        }
        
        m_xml = configuration.getAttributeAsBoolean( "xml", true );
        m_html = configuration.getAttributeAsBoolean( "html", true );
        
        m_chartWidth = configuration.getAttributeAsInteger( "chart-width", 600 );
        m_chartHeight = configuration.getAttributeAsInteger( "chart-height", 120 );
        m_antialias = configuration.getAttributeAsBoolean( "antialias", false );
        
        // Get configuration values which limit the leases that can be made.
        //  Make sure that none are any less restrictive than the InstrumentManager.
        int maxSamples = m_manager.getMaxLeasedSamples();
        m_maxLeasedSamples = Math.min( maxSamples, configuration.getAttributeAsInteger(
            "max-leased-samples", maxSamples ) );
        
        int maxSize = m_manager.getMaxLeasedSampleSize();
        m_maxLeasedSampleSize = Math.min( maxSize, configuration.getAttributeAsInteger(
            "max-leased-sample-size", maxSize ) );
        
        long maxLease = m_manager.getMaxLeasedSampleLease();
        m_maxLeasedSampleLease = Math.min( maxLease, 1000L * configuration.getAttributeAsInteger(
            "max-leased-sample-lease", (int)( maxLease / 1000 ) ) );
        
        m_readOnly = configuration.getAttributeAsBoolean( "read-only", false );
        
        m_rootBreadCrumbURL = configuration.getAttribute( "root-bread-crumb-url", null );
        m_rootBreadCrumbLabel = configuration.getAttribute(
            "root-bread-crumb-label", m_rootBreadCrumbURL );
        
        String accessLogFile = configuration.getAttribute( "access-log", null );
        
        m_httpServer = new HTTPServer( m_port, m_bindAddr );
        m_httpServer.enableLogging( getLogger().getChildLogger( "server" ) );
        m_httpServer.setInstrumentableName( "server" );
        m_httpServer.setAccessLogFile( accessLogFile );
        addChildInstrumentable( m_httpServer );
    }

    /*---------------------------------------------------------------
     * Startable Methods
     *-------------------------------------------------------------*/
    public void start()
        throws Exception
    {
        // Register all of the helpers that we support
        if ( m_xml )
        {
            // XML
            String nameBase = "xml-";
            initAndRegisterHandler( new XMLInstrumentManagerHandler( m_manager, this ),
                nameBase + "instrument-manager" );
            initAndRegisterHandler( new XMLInstrumentableHandler( m_manager, this ),
                nameBase + "instrumentable" );
            initAndRegisterHandler(	new XMLInstrumentHandler( m_manager, this ),
                nameBase + "instrument" );
            initAndRegisterHandler( new XMLSampleHandler( m_manager, this ), nameBase + "sample" );
            initAndRegisterHandler(	new XMLSnapshotHandler( m_manager, this ), nameBase + "snapshot" );
            initAndRegisterHandler(	new XMLSnapshotsHandler( m_manager, this ), nameBase + "snapshots" );
            
            if ( !m_readOnly )
            {
                initAndRegisterHandler(
                    new XMLSampleLeaseHandler( m_manager, this ), nameBase + "sample-lease" );
                initAndRegisterHandler(
                    new XMLSampleLeasesHandler( m_manager, this ), nameBase + "sample-leases" );
                initAndRegisterHandler(
                    new XMLCreateSampleHandler( m_manager, this ), nameBase + "create-sample" );
                initAndRegisterHandler(
                    new XMLCreateSamplesHandler( m_manager, this ), nameBase + "create-samples" );
                initAndRegisterHandler(	new XMLGCHandler( m_manager, this ), nameBase + "gc" );
            }
        }
        
        if ( m_html )
        {
            // HTML
            String nameBase = "html-";
            initAndRegisterHandler( new HTMLInstrumentManagerHandler( m_manager, this ),
                nameBase + "instrument-manager" );
            initAndRegisterHandler( new HTMLInstrumentableHandler( m_manager, this ),
                nameBase + "instrumentable" );
            initAndRegisterHandler( new HTMLInstrumentHandler( m_manager, this ),
                nameBase + "instrument" );
            initAndRegisterHandler( new HTMLSampleHandler( m_manager, this ),
                nameBase + "sample" );
            initAndRegisterHandler( new SampleChartHandler(
                m_manager, m_chartWidth, m_chartHeight, m_antialias ), "sample-chart" );
            initAndRegisterHandler( new FavIconHandler(), "favicon" );
            
            if ( !m_readOnly )
            {
                initAndRegisterHandler(
                    new HTMLSampleLeaseHandler( m_manager, this ), nameBase + "sample-lease" );
                initAndRegisterHandler(
                    new HTMLCreateSampleHandler( m_manager, this ), nameBase + "create-sample" );
                initAndRegisterHandler(	new HTMLGCHandler( m_manager, this ), nameBase + "gc" );
            }
        
            // The root handler must be registered last as it will handle any URL.
            initAndRegisterHandler( new HTMLRootHandler( m_manager, this ), nameBase + "root" );
        }
        
        getLogger().debug( "Starting Instrument Manager HTTP Connector" );
        m_httpServer.start();
        getLogger().info( "Instrument Manager HTTP Connector listening on port: " + m_port );
    }

    public void stop()
        throws Exception
    {
        getLogger().debug( "Stopping Instrument Manager HTTP Connector" );
        m_httpServer.stop();
        m_httpServer = null;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the maximum number of leased samples that will be approved.
     *
     * @return The maximum number of leased samples.
     */
    int getMaxLeasedSamples()
    {
        return m_maxLeasedSamples;
    }
    
    /**
     * Returns the maximum size of a leased sample.
     *
     * @return The maximum size of a leased sample.
     */
    int getMaxLeasedSampleSize()
    {
        return m_maxLeasedSampleSize;
    }
    
    /**
     * Returns the maximum number of milliseconds that a lease will be granted
     *  for.
     *
     * @return The maximum lease length.
     */
    long getMaxLeasedSampleLease()
    {
        return m_maxLeasedSampleLease;
    }
    
    /**
     * True if the connector should only provide read-only access to the
     *   Instrument Manager.
     *
     * @return The read-only flag.
     */
    boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    /**
     * Returns the root bread crumb URL or null if not configured.
     *
     * @return The root bread crumb URL or null if not configured.
     */
    String getRootBreadCrumbURL()
    {
        return m_rootBreadCrumbURL;
    }
    
    /**
     * Returns the root bread crumb label or null if not configured.
     *
     * @return The root bread crumb label or null if not configured.
     */
    String getRootBreadCrumbLabel()
    {
        return m_rootBreadCrumbLabel;
    }
    
    private void initAndRegisterHandler( AbstractHTTPURLHandler handler, String name )
        throws Exception
    {
        handler.enableLogging( getLogger().getChildLogger( name ) );
        handler.setInstrumentableName( name );
        addChildInstrumentable( handler );
        
        m_httpServer.registerHandler( handler );
    }
}

