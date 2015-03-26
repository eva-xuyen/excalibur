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

package org.apache.excalibur.instrument.manager.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.excalibur.instrument.manager.http.server.HTTPRedirect;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class HTMLSampleLeaseHandler
    extends AbstractHTMLHandler
{
    /** Reference to the connector. */
    private InstrumentManagerHTTPConnector m_connector;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTMLSampleLeaseHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public HTMLSampleLeaseHandler( DefaultInstrumentManager manager,
                                   InstrumentManagerHTTPConnector connector )
    {
        super( "/sample-lease.html", manager );
        
        m_connector = connector;
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPURLHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The PrintWriter to write the result to.
     */
    public void doGet( String path, Map parameters, PrintWriter out )
        throws IOException
    {
        String name = getParameter( parameters, "name" );
        long lease = getLongParameter( parameters, "lease" );
        String instrument = getParameter( parameters, "instrument", null );
        String chart = getParameter( parameters, "chart", null );
        
        InstrumentSampleDescriptor desc;
        try
        {
            desc = getInstrumentManager().locateInstrumentSampleDescriptor( name );
        }
        catch ( NoSuchInstrumentSampleException e )
        {
            // Sample no longer exists, go back to the parent instrument.
            int pos = name.lastIndexOf( '.' );
            if ( pos >= 0 )
            {
                throw new HTTPRedirect(
                    "instrument.html?name=" + urlEncode( name.substring( 0,  pos ) ) );
            }
            else
            {
                throw new HTTPRedirect( "instrument-manager.html" );
            }
        }
        
        // The instrument manager will do its own tests of the lease, but the
        //  restrictions on this connector may be stronger so they must be tested
        //  here as well.
        lease = Math.max( 1, Math.min( lease, m_connector.getMaxLeasedSampleLease() ) );
        
        if ( getInstrumentManager().getLeaseSampleCount() >= m_connector.getMaxLeasedSamples() )
        {
            lease = 1;
        }
        
        // Renew the lease
        desc.extendLease( lease );
        
        if ( instrument != null )
        {
            // Go to the instrument page.
            int pos = name.lastIndexOf( '.' );
            if ( pos >= 0 )
            {
                throw new HTTPRedirect(
                    "instrument.html?name=" + urlEncode( name.substring( 0,  pos ) ) );
            }
            else
            {
                throw new HTTPRedirect( "instrumentable.html" );
            }
        }
        else
        {
            // Redirect to the sample page.
            throw new HTTPRedirect( "sample.html?name=" + urlEncode( desc.getName() )
                + ( chart == null ? "" : "&chart=true" ) );
        }
        
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

