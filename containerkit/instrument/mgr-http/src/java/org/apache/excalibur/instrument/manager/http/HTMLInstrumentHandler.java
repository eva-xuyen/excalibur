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
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class HTMLInstrumentHandler
    extends AbstractHTMLHandler
{
    /** Reference to the connector. */
    private InstrumentManagerHTTPConnector m_connector;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new HTMLInstrumentHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public HTMLInstrumentHandler( DefaultInstrumentManager manager,
                                  InstrumentManagerHTTPConnector connector )
    {
        super( "/instrument.html", manager );
        
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
        
        String type;
        StringBuffer types = new StringBuffer();
        types.append( "<select name='type' onKeyPress=\"javascript:fieldChanged()\">" );
        StringBuffer presets = new StringBuffer();
        presets.append( "<select name='preset' onChange=\"javascript:applyPreset(this.options[this.selectedIndex].value)\">" );
        presets.append( "<option value='0-0' selected>-- Select a Preset --</option>" );
        switch ( desc.getType() )
        {
        case DefaultInstrumentManager.INSTRUMENT_TYPE_COUNTER:
            type = "Counter";
            types.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER
                + "' selected>Count</option>" );
            
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER
                + "-0'>Count / Second Over 10 Minutes</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER
                + "-1'>Count / Minute Over 1 Day</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER
                + "-2'>Count / Hour Over 1 Month</option>" );
            break;
            
        case DefaultInstrumentManager.INSTRUMENT_TYPE_VALUE:
            type = "Value";
            types.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM
                + "' selected>Maximum Value</option>" );
            types.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM
                + "'>Minimum Value</option>" );
            types.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN
                + "'>Mean Value</option>" );
            
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM
                + "-0'>Max Value / Second Over 10 Minutes</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM
                + "-1'>Max Value / Minute Over 1 Day</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM
                + "-2'>Max Value / Hour Over 1 Month</option>" );
            
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM
                + "-0'>Min Value / Second Over 10 Minutes</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM
                + "-1'>Min Value / Minute Over 1 Day</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM
                + "-2'>Min Value / Hour Over 1 Month</option>" );
            
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN
                + "-0'>Mean Value / Second Over 10 Minutes</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN
                + "-1'>Mean Value / Minute Over 1 Day</option>" );
            presets.append( "<option value='"
                + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN
                + "-2'>Mean Value / Hour Over 1 Month</option>" );
            break;
            
        default:
            type = "Unknown";
            break;
        }
        types.append( "</select>" );
        presets.append( "</select>" );
        
        out.println( "<html>" );
        out.println( "<head><title>" + desc.getDescription() + "</title></head>" );
        out.println( "<body>" );
        
        breadCrumbs( out, desc, false );
        
        out.println( "<h2>Instrument</h2>" );
        startTable( out );
        tableRow( out, 0, "Name", desc.getName() );
        tableRow( out, 0, "Description", desc.getDescription() );
        tableRow( out, 0, "Type", type );
        endTable( out );
        
        InstrumentSampleDescriptor[] samples = desc.getInstrumentSampleDescriptors();
        if ( samples.length > 0 )
        {
            out.println( "<h2>Registered Samples</h2>" );
            outputInstrumentSamples( out, samples, m_connector.isReadOnly() );
        }
        
        if ( !m_connector.isReadOnly() )
        {
            out.println( "<h2>Register Sample</h2>" );
            out.println( "<SCRIPT LANGUAGE=\"JavaScript\">" );
            out.println( "function fieldChanged() {" );
            out.println( "  var form = document.forms[0];" );
            out.println( "  form.preset.value=\"0-0\";" );
            out.println( "}" );
            out.println( "function applyPreset(preset) {" );
            out.println( "  var form = document.forms[0];" );
            out.println( "  var pos = preset.indexOf('-');" );
            out.println( "  var type = preset.substring(0, pos);" );
            out.println( "  var spec = preset.substring(pos + 1);" );
            out.println( "  var prefix;" );
            out.println( "  if (type == 0) {" );
            out.println( "    return;" );
            out.println( "  } else if (type == " + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_COUNTER + ") {" );
            out.println( "    typeLbl = \"Count\"" );
            out.println( "  } else if (type == " + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MAXIMUM + ") {" );
            out.println( "    typeLbl = \"Max Value\"" );
            out.println( "  } else if (type == " + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MINIMUM + ") {" );
            out.println( "    typeLbl = \"Min Value\"" );
            out.println( "  } else if (type == " + DefaultInstrumentManager.INSTRUMENT_SAMPLE_TYPE_MEAN + ") {" );
            out.println( "    typeLbl = \"Mean Value\"" );
            out.println( "  } else {" );
            out.println( "    typeLbl = \"Unknown\"" );
            out.println( "  }" );
            out.println( "  var intervalLbl;" );
            out.println( "  var interval;" );
            out.println( "  var size;" );
            out.println( "  var lease;" );
            out.println( "  if (spec == 1) {" );
            out.println( "    intervalLbl = \"Minute\";" );
            out.println( "    interval = 60000;" );
            out.println( "    size = 1440;" );
            out.println( "    lease = 86400000;" );
            out.println( "  } else if (spec == 2) {" );
            out.println( "    intervalLbl = \"Hour\";" );
            out.println( "    interval = 3600000;" );
            out.println( "    size = 672;" );
            out.println( "    lease = 86400000;" );
            out.println( "  } else {" );
            out.println( "    intervalLbl = \"Second\";" );
            out.println( "    interval = 1000;" );
            out.println( "    size = 600;" );
            out.println( "    lease = 600000;" );
            out.println( "  }" );
            out.println( "  form.description.value = typeLbl + \" / \" + intervalLbl;" );
            out.println( "  form.interval.value = interval;" );
            out.println( "  form.size.value = size;" );
            out.println( "  form.lease.value = lease;" );
            out.println( "  form.type.value = type;" );
            out.println( "}" );
            out.println( "</SCRIPT>" );
    
            out.println( "<form action='create-sample.html' method='GET' accept-charset='UTF-8'>" );
            startTable( out );
            tableRow( out, 0, "Description", "<input name='description' type='text' size='40' value='' onKeyPress=\"javascript:fieldChanged()\">" );
            tableRow( out, 0, "Interval (ms.)", "<input name='interval' type='text' size='10' value='' onKeyPress=\"javascript:fieldChanged()\">" );
            tableRow( out, 0, "Size", "<input name='size' type='text' size='10' value='' onKeyPress=\"javascript:fieldChanged()\">" );
            tableRow( out, 0, "Lease (ms.)", "<input name='lease' type='text' size='10' value='' onKeyPress=\"javascript:fieldChanged()\">" );
            tableRow( out, 0, "Type", types.toString() );
            tableRow( out, 0, "Presets", presets.toString() ); 
            endTable( out );
            out.println( "<input type='hidden' name='name' value='" + desc.getName() + "'>" );
            out.println( "<input type='submit' value='Submit'>" );
            
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

