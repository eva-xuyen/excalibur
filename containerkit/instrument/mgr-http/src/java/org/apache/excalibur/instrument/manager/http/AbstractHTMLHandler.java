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
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.excalibur.instrument.manager.http.server.URLCoder;
import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/10 13:56:56 $
 * @since 4.1
 */
public abstract class AbstractHTMLHandler
    extends AbstractHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTMLHandler.
     *
     * @param path The path handled by this handler.
     * @param manager Reference to the instrument manager interface.
     */
    public AbstractHTMLHandler( String path, DefaultInstrumentManager manager )
    {
        super( path, CONTENT_TYPE_TEXT_HTML, manager );
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    protected String urlEncode( String str )
    {
        try
        {
            // Starting with Java 1.4, encode takes an encoding, but this needs to
            //  work with 1.3.   Use our own version.
            return URLCoder.encode( str, InstrumentManagerHTTPConnector.ENCODING );
        }
        catch ( UnsupportedEncodingException e )
        {
            // Should never happen because we control the encoding.
            throw new IllegalStateException( "Unknown encoding: " + e.toString() );
        }
    }
    
    protected void breadCrumbs( PrintWriter out, boolean link )
    {
        if ( link )
        {
            out.println( "<a href='instrument-manager.html'>"
                + getInstrumentManager().getDescription() + "</a>" );
        }
        else
        {
            out.println( getInstrumentManager().getDescription() );
        }
    }
    
    protected void breadCrumbs( PrintWriter out, InstrumentableDescriptor desc, boolean link )
    {
        InstrumentableDescriptor parent = desc.getParentInstrumentableDescriptor();
        if ( parent == null )
        {
            breadCrumbs( out, true );
        }
        else
        {
            breadCrumbs( out, parent, true );
        }
        out.print( " <b>&gt;</b> " );
        if ( link )
        {
            out.println( "<a href='instrumentable.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a>" );
        }
        else
        {
            out.println( desc.getDescription() );
        }
    }
    
    protected void breadCrumbs( PrintWriter out, InstrumentDescriptor desc, boolean link )
    {
        breadCrumbs( out, desc.getInstrumentableDescriptor(), true );
        out.print( " <b>&gt;</b> " );
        if ( link )
        {
            out.println( "<a href='instrument.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a>" );
        }
        else
        {
            out.println( desc.getDescription() );
        }
    }
    
    protected void breadCrumbs( PrintWriter out, InstrumentSampleDescriptor desc, boolean link )
    {
        breadCrumbs( out, desc.getInstrumentDescriptor(), true );
        out.print( " <b>&gt;</b> " );
        if ( link )
        {
            out.println( "<a href='sample.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a>" );
        }
        else
        {
            out.println( desc.getDescription() );
        }
    }
    
    protected void startTable( PrintWriter out )
        throws IOException
    {
        out.println( "<table cellpadding='1' cellspacing='0'><tr><td bgcolor='#bbbbbb'><table cellpadding='2' cellspacing='1'>" );
    }
    protected void endTable( PrintWriter out )
        throws IOException
    {
        out.println( "</table></td></tr></table>" );
    }
    
    protected void startTableHeaderRow( PrintWriter out )
        throws IOException
    {
        out.println( "<tr>" );
    }
    
    protected void endTableHeaderRow( PrintWriter out )
        throws IOException
    {
        out.println( "</tr>" );
    }
    
    protected void tableHeaderCell( PrintWriter out, String value )
        throws IOException
    {
        out.print( "<td bgcolor='#dddddd' nowrap><b>" + value + "</b></td>" );
    }
    
    protected void startTableRow( PrintWriter out, int row )
        throws IOException
    {
        String color;
        if ( row % 2 == 0 )
        {
            color = "#eeeeee";
        }
        else
        {
            color = "#e4e4e4";
        }
        out.println( "<tr bgcolor='" + color + "'>" );
    }
    
    protected void endTableRow( PrintWriter out )
        throws IOException
    {
        out.println( "</tr>" );
    }
    
    protected void tableCell( PrintWriter out, String value )
        throws IOException
    {
        out.print( "<td nowrap>" + value + "</td>" );
    }
    
    protected void tableCellRight( PrintWriter out, String value )
        throws IOException
    {
        out.print( "<td align='right' nowrap>" + value + "</td>" );
    }
    
    protected void tableRow( PrintWriter out, int row, String label, String value )
        throws IOException
    {
        startTableRow( out, row );
        tableHeaderCell( out, label );
        tableCell( out, value );
        endTableRow( out );
    }
    
    protected void footer( PrintWriter out )
    {
        out.println( "<br>" );
        out.print( "<font size='-1' color='#888888'>" );
        out.print( "<center>" );
        out.print( "<a href='http://excalibur.apache.org/instrument/html-client.html'>" );
        out.print( "Excalibur Instrument - HTTP Client" );
        out.print( "</a><br>" );
        out.print( "Copyright&copy; 2002-2004 The Apache Software Foundation.  All rights reserved." );
        out.print( "</center>" );
        out.println( "</font>" );
    }
    
    protected void outputInstrumentables( PrintWriter out, InstrumentableDescriptor[] descs )
        throws IOException
    {
        startTable( out );
        startTableHeaderRow( out );
        tableHeaderCell( out, "Name" );
        endTableHeaderRow( out );
        
        for ( int i = 0; i < descs.length; i++ )
        {
            InstrumentableDescriptor desc = descs[i];
            
            startTableRow( out, i );
            tableCell( out,
                "<a href='instrumentable.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a>" );
            endTableRow( out );
        }
        
        endTable( out );
    }
    
    protected void outputInstruments( PrintWriter out, InstrumentDescriptor[] descs )
        throws IOException
    {
        startTable( out );
        startTableHeaderRow( out );
        tableHeaderCell( out, "Name" );
        endTableHeaderRow( out );
        
        for ( int i = 0; i < descs.length; i++ )
        {
            InstrumentDescriptor desc = descs[i];
            
            startTableRow( out, i );
            tableCell( out, "<a href='instrument.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a>" );
            endTableRow( out );
        }
        
        endTable( out );
    }
    
    protected void outputInstrumentSamples( PrintWriter out,
                                            InstrumentSampleDescriptor[] descs,
                                            boolean readOnly )
        throws IOException
    {
        startTable( out );
        startTableHeaderRow( out );
        tableHeaderCell( out, "Name" );
        tableHeaderCell( out, "Last Sample" );
        tableHeaderCell( out, "Last Sample Period" );
        tableHeaderCell( out, "Interval" );
        tableHeaderCell( out, "Size" );
        tableHeaderCell( out, "Expiration Time" );
        endTableHeaderRow( out );
        
        for ( int i = 0; i < descs.length; i++ )
        {
            InstrumentSampleDescriptor desc = descs[i];
            
            startTableRow( out, i );
            tableCell( out, "<a href='sample.html?name=" + urlEncode( desc.getName() ) + "'>"
                + desc.getDescription() + "</a> (<a href='sample.html?name="
                + urlEncode( desc.getName() ) + "&chart=true'>Chart</a>)" );
            tableCellRight( out, Integer.toString( desc.getValue() ) );
            tableCell( out, new Date( desc.getTime() ).toString() );
            tableCellRight( out, Long.toString( desc.getInterval() ) );
            tableCellRight( out, Integer.toString( desc.getSize() ) );
            String value;
            if ( desc.getLeaseExpirationTime() == 0 )
            {
                value = "<i>Permanent</i>";
            }
            else
            {
                String renewUrl =
                    "sample-lease.html?name=" + urlEncode( desc.getName() ) + "&instrument=true&lease=";
                
                value = new Date( desc.getLeaseExpirationTime() ).toString();
                if ( !readOnly )
                {
                    value = value
                        + " (Renew <a href='" + renewUrl + "600000'>10min</a>, "
                        + "<a href='" + renewUrl + "3600000'>1hr</a>, "
                        + "<a href='" + renewUrl + "86400000'>1day</a>)";
                }
                
                // Make the text red if it is about to expire.
                if ( desc.getLeaseExpirationTime() - System.currentTimeMillis() < 300000 )
                {
                    value = "<font color='ff0000'>" + value + "</font>";
                }
            }
            tableCell( out, value );
            endTableRow( out );
        }
        
        endTable( out );
    }
}

