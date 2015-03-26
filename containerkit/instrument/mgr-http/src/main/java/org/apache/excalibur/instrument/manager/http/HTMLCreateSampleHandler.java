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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.excalibur.instrument.manager.http.server.HTTPRedirect;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class HTMLCreateSampleHandler
    extends AbstractHTMLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTMLCreateSampleHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public HTMLCreateSampleHandler( DefaultInstrumentManager manager,
                                    InstrumentManagerHTTPConnector connector )
    {
        super( "/create-sample.html", manager, connector );
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
        String description = getParameter( parameters, "description" );
        long interval = getLongParameter( parameters, "interval" );
        int size = getIntegerParameter( parameters, "size" );
        long lease = getLongParameter( parameters, "lease" );
        int type = getIntegerParameter( parameters, "type" );
        
        InstrumentDescriptor desc;
        try
        {
            desc = getInstrumentManager().locateInstrumentDescriptor( name );
        }
        catch ( NoSuchInstrumentException e )
        {
            // Sample no longer exists, go back to the parent instrument.
            int pos = name.lastIndexOf( '.' );
            if ( pos >= 0 )
            {
                throw new HTTPRedirect(
                    "instrumentable.html?name=" + urlEncode( name.substring( 0,  pos ) ) );
            }
            else
            {
                throw new HTTPRedirect( "instrument-manager.html" );
            }
        }
        
        // The instrument manager will do its own tests of the lease, but the
        //  restrictions on this connector may be stronger so they must be tested
        //  here as well.
        size = Math.max( 1, Math.min( size, getConnector().getMaxLeasedSampleSize() ) );
        lease = Math.max( 1, Math.min( lease, getConnector().getMaxLeasedSampleLease() ) );
        
        if ( getInstrumentManager().getLeaseSampleCount() >= getConnector().getMaxLeasedSamples() )
        {
            lease = 1;
        }
        
        // Register the new lease
        InstrumentSampleDescriptor sample;
        try
        {
            sample = desc.createInstrumentSample( description, interval, size, lease, type );
        }
        catch ( IllegalArgumentException e )
        {
            // The sample type is not valid.
            throw new FileNotFoundException( e.getMessage() );
        }
        catch ( IllegalStateException e )
        {
            // The sample type was incompatible with the instrument.
            throw new FileNotFoundException( e.getMessage() );
        }
        
        // Redirect to the new sample page.
        throw new HTTPRedirect( "sample.html?name=" + urlEncode( sample.getName() ) );
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

