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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @since 1.2
 */
public class XMLSampleLeasesHandler
    extends AbstractXMLHandler
{
    /** Reference to the connector. */
    private InstrumentManagerHTTPConnector m_connector;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new XMLSampleLeasesHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public XMLSampleLeasesHandler( DefaultInstrumentManager manager,
                                   InstrumentManagerHTTPConnector connector )
    {
        super( "/sample-leases.xml", manager );
        
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
        String[] names = getParameters( parameters, "name" );
        long[] leases = getLongParameters( parameters, "lease", 0 );
        boolean packed = getBooleanParameter( parameters, "packed", false );
        
        if ( names.length != leases.length )
        {
            throw new FileNotFoundException(
                "The number of leases values not equal to the number of names." );
        }
        
        out.println( InstrumentManagerHTTPConnector.XML_BANNER );
        if ( names.length > 0 )
        {
            outputLine( out, "", packed, "<samples>" );
            
            for ( int i = 0; i < names.length; i++ )
            {
                String name = names[i];
                long lease = leases[i];
                
                InstrumentSampleDescriptor desc;
                try
                {
                    desc = getInstrumentManager().locateInstrumentSampleDescriptor( name );
                }
                catch ( NoSuchInstrumentSampleException e )
                {
                    // Not found, ignore.
                    desc = null;
                }
                
                if ( desc != null )
                {
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
                    
                    outputSample( out, desc, "  ", packed );
                }
            }
            
            outputLine( out, "", packed, "</samples>" );
        }
        else
        {
            outputLine( out, "", packed, "<samples/>" );
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

