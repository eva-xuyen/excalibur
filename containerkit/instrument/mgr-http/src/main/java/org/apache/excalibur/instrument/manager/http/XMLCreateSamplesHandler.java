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

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentException;

/**
 * Handler which can be used to create multiple samples at once.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @since 1.2
 */
public class XMLCreateSamplesHandler
    extends AbstractXMLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new XMLCreateSamplesHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public XMLCreateSamplesHandler( DefaultInstrumentManager manager,
                                    InstrumentManagerHTTPConnector connector )
    {
        super( "/create-samples.xml", manager, connector );
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
        String[] names = this.getParameters( parameters, "name" );
        String[] descriptions = this.getParameters( parameters, "description" );
        long[] intervals = this.getLongParameters( parameters, "interval", 0 );
        int[] sizes = this.getIntegerParameters( parameters, "size", 0 );
        long[] leases = this.getLongParameters( parameters, "lease", 0 );
        int[] types = this.getIntegerParameters( parameters, "type", 0 );
        boolean packed = this.getBooleanParameter( parameters, "packed", false );

        if ( names.length != descriptions.length )
        {
            throw new FileNotFoundException(
                "The number of descriptions not equal to the number of names." );
        }
        if ( names.length != intervals.length )
        {
            throw new FileNotFoundException(
                "The number of intervals not equal to the number of names." );
        }
        if ( names.length != sizes.length )
        {
            throw new FileNotFoundException(
                "The number of sizes not equal to the number of names." );
        }
        if ( names.length != leases.length )
        {
            throw new FileNotFoundException(
                "The number of leases not equal to the number of names." );
        }
        if ( names.length != types.length )
        {
            throw new FileNotFoundException(
                "The number of types not equal to the number of names." );
        }

        out.println( InstrumentManagerHTTPConnector.XML_BANNER );
        if ( names.length > 0 )
        {
            this.outputLine( out, "", packed, "<samples>" );

            for ( int i = 0; i < names.length; i++ )
            {
                String name = names[i];
                String description = descriptions[i];
                long interval = intervals[i];
                int size = sizes[i];
                long lease = leases[i];
                int type = types[i];

                InstrumentDescriptor desc;
                try
                {
                    desc = this.getInstrumentManager().locateInstrumentDescriptor( name );
                }
                catch ( NoSuchInstrumentException e )
                {
                    // Not found, ignore.
                    desc = null;
                }

                if ( desc != null )
                {
                    // The instrument manager will do its own tests of the lease, but the
                    //  restrictions on this connector may be stronger so they must be tested
                    //  here as well.
                    size = Math.max( 1, Math.min( size, this.getConnector().getMaxLeasedSampleSize() ) );
                    lease = Math.max(
                        1, Math.min( lease, this.getConnector().getMaxLeasedSampleLease() ) );

                    if ( this.getInstrumentManager().getLeaseSampleCount()
                        >= this.getConnector().getMaxLeasedSamples() )
                    {
                        lease = 1;
                    }

                    // Register the new lease
                    InstrumentSampleDescriptor sample;
                    try
                    {
                        sample =
                            desc.createInstrumentSample( description, interval, size, lease, type );
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

                    this.outputSample( out, sample, "  ", packed );
                }
            }

            this.outputLine( out, "", packed, "</samples>" );
        }
        else
        {
            this.outputLine( out, "", packed, "<samples/>" );
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

