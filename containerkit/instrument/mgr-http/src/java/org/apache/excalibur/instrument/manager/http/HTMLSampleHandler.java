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
import java.util.Date;
import java.util.Map;

import org.apache.excalibur.instrument.manager.http.server.HTTPRedirect;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleSnapshot;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/09 14:33:06 $
 * @since 4.1
 */
public class HTMLSampleHandler
    extends AbstractHTMLHandler
{
    /** Reference to the connector. */
    private InstrumentManagerHTTPConnector m_connector;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTMLSampleHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public HTMLSampleHandler( DefaultInstrumentManager manager,
                              InstrumentManagerHTTPConnector connector )
    {
        super( "/sample.html", manager );
        
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
        String chart = getParameter( parameters, "chart", null );
        
        InstrumentSampleSnapshot snapshot = desc.getSnapshot();
        
        String type;
        switch ( desc.getType() )
        {
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            type = "Counter";
            break;
            
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            type = "Max Value";
            break;
            
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            type = "Min Value";
            break;
            
        case DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN:
            type = "Mean Value";
            break;
            
        default:
            type = "Unknown";
            break;
        }
        
        out.println( "<html>" );
        out.println( "<head><title>" + desc.getDescription() + "</title></head>" );
        out.println( "<body>" );
        
        breadCrumbs( out, desc, false );
        
        out.println( "<h2>Instrument Sample</h2>" );
        startTable( out );
        tableRow( out, 0, "Name", desc.getName() );
        tableRow( out, 0, "Description", desc.getDescription() );
        tableRow( out, 0, "Type", type );
        tableRow( out, 0, "Interval", desc.getInterval() + "ms." );
        tableRow( out, 0, "Size", Integer.toString( desc.getSize() ) );
        if ( desc.getLeaseExpirationTime() > 0 )
        {
            String renewUrl = "sample-lease.html?name=" + urlEncode( desc.getName() )
                + ( chart == null ? "" : "&chart=true" ) + "&lease=";
            
            String value = new Date( desc.getLeaseExpirationTime() ).toString();
            
            if ( !m_connector.isReadOnly() )
            {
                value = value + " (Renew <a href='" + renewUrl + "600000'>10min</a>, "
                    + "<a href='" + renewUrl + "3600000'>1hr</a>, "
                    + "<a href='" + renewUrl + "86400000'>1day</a>)";
            }
            
            // Make the text red if it is about to expire.
            if ( desc.getLeaseExpirationTime() - System.currentTimeMillis() < 300000 )
            {
                value = "<font color='ff0000'>" + value + "</font>";
            }

            tableRow( out, 0, "Expiration", value );
        }
        else
        {
            tableRow( out, 0, "Expiration", "Permanent" );
        }
        endTable( out );
        
        if ( chart == null )
        {
            out.println( "<h2>Data Samples (<a href='sample.html?name="
                + urlEncode( desc.getName() ) + "&chart=true'>Chart</a>)</h2>" );
            
            startTable( out );
            startTableHeaderRow( out );
            tableHeaderCell( out, "Period" );
            tableHeaderCell( out, "Value" );
            endTableHeaderRow( out );
            long time = snapshot.getTime();
            int[] samples = snapshot.getSamples();
            for ( int i = 0; i < samples.length; i++ )
            {
                startTableRow( out, i );
                tableCell( out, new Date( time ).toString() );
                tableCellRight( out, Integer.toString( samples[samples.length - i - 1] ) );
                endTableRow( out );
                
                time -= snapshot.getInterval();
            }
            endTable( out );
        }
        else
        {
            out.println( "<h2>Data Samples (<a href='sample.html?name="
                + urlEncode( desc.getName() ) + "'>Plain</a>)</h2>" );
            
            // Originally, the JavaScript timer in the page made use of the setInterval
            //  function to build a simple timer.  This worked fine under normal
            //  operation.  But if a browser was suspended for 1 hour with a timer
            //  running at 1 second intervals, the timer would try to catch up when
            //  the machine was resumed.  This would result in the timer firing 3600
            //  times over the course of a few seconds.  The sudden burst of requests
            //  was swamping the server.
            // The current scripts below now always reset the timer each time it is
            //  fired.  If the timer ever falls behind it will recover smoothly
            //  without trying to catch up.  While not quite as accurate it is
            //  sufficient for our purposes.
            
            out.println( "<SCRIPT LANGUAGE=\"JavaScript\">" );
            out.println( "var timerId = 0;" );
            out.println( "var timerInterval = 5000;" );
            out.println( "function refreshChart() {" );
            //out.println( "  alert(\"in refreshChart()\");" );
            out.println( "  document.chart.src=\"sample-chart.jpg?name=" + urlEncode( desc.getName() ) + "&time=\" + new Date().getTime();" );
            out.println( "}" );
            out.println( "function timerFired() {" );
            //out.println( "  alert(\"in timerFired()\");" );
            out.println( "  if (timerId) {" );
            out.println( "    clearTimeout(timerId);" );
            out.println( "  }" );
            out.println( "  timerId = setTimeout(\"timerFired()\", timerInterval)" );
            out.println( "  refreshChart();" );
            out.println( "}" );
            out.println( "function setRefresh(refresh) {" );
            //out.println( "  alert(\"in setRefresh(\" + refresh + \")\");" );
            out.println( "  timerInterval = refresh;" );
            out.println( "  timerFired();" );
            out.println( "}" );
            out.println( "function chartError() {" );
            //out.println( "  alert(\"in chartError()\");" );
            out.println( "  clearTimeout(timerId);" );
            out.println( "  document.location=\"instrument.html?name=" + urlEncode( desc.getInstrumentDescriptor().getName() ) + "\";" );
            out.println( "}" );
            out.println( "</SCRIPT>" );
            
            out.println( "<form>" );
            startTable( out );
            // Add a time to the chart as is done in the Javascript.  Some browsers ignore the
            //  do not cache headers in the image and display a cached version of the image
            //  anyway.
            tableCell( out, "<img name='chart' src='sample-chart.jpg?name=" + urlEncode( desc.getName() )
                + "&time=" + System.currentTimeMillis() + "' onError='javascript:chartError()'>" );
            endTable( out );
            out.println( "Refresh rate:" );
            out.println( "<input type='button' value='No Refresh' onClick='javascript:clearTimeout(timerId)'>" );
            out.println( "<input type='button' value='1 Second' onClick='javascript:setRefresh(1000)'>" );
            out.println( "<input type='button' value='5 Seconds' onClick='javascript:setRefresh(5000)'>" );
            out.println( "<input type='button' value='10 Seconds' onClick='javascript:setRefresh(10000)'>" );
            out.println( "<input type='button' value='1 Minute' onClick='javascript:setRefresh(60000)'>" );
            out.println( "<input type='button' value='Refresh Now' onClick='javascript:refreshChart()'>" );
            out.println( "</form>" );
        }
        
        footer( out );
        
        out.println( "</body>" );
        out.println( "</html>" );
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}

