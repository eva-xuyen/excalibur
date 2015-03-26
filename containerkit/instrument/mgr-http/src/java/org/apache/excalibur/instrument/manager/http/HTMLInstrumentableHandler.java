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
import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentableException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class HTMLInstrumentableHandler
    extends AbstractHTMLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTMLInstrumentableHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     */
    public HTMLInstrumentableHandler( DefaultInstrumentManager manager )
    {
        super( "/instrumentable.html", manager );
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
        InstrumentableDescriptor desc;
        try
        {
            desc = getInstrumentManager().locateInstrumentableDescriptor( name );
        }
        catch ( NoSuchInstrumentableException e )
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
        
        out.println( "<html>" );
        out.println( "<head><title>" + desc.getDescription() + "</title></head>" );
        out.println( "<body>" );
        
        breadCrumbs( out, desc, false );
        
        out.println( "<h2>Instrumentable</h2>" );
        startTable( out );
        tableRow( out, 0, "Name", desc.getName() );
        tableRow( out, 0, "Description", desc.getDescription() );
        endTable( out );
        
        InstrumentableDescriptor[] instrumentables = desc.getChildInstrumentableDescriptors();
        if ( instrumentables.length > 0 )
        {
            out.println( "<h2>Instrumentables</h2>" );
            outputInstrumentables( out, instrumentables );
        }
        
        InstrumentDescriptor[] instruments = desc.getInstrumentDescriptors();
        if ( instruments.length > 0 )
        {
            out.println( "<h2>Instruments</h2>" );
            outputInstruments( out, instruments );
        }
        
        footer( out );
        
        out.println( "</body>" );
        out.println( "</html>" );
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

