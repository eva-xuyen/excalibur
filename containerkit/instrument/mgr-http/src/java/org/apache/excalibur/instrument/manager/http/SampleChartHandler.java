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

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.excalibur.instrument.manager.http.server.AbstractHTTPURLHandler;
import org.apache.excalibur.instrument.manager.http.server.HTTPRedirect;
import org.apache.excalibur.instrument.manager.http.server.URLCoder;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleSnapshot;
import org.apache.excalibur.instrument.manager.NoSuchInstrumentSampleException;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class SampleChartHandler
    extends AbstractHTTPURLHandler
{
    /** The instrument manager */
    private DefaultInstrumentManager m_manager;
    
    /** Default width of the image. */
    private int m_width;
    
    /** Default height of the image. */
    private int m_height;
    
    /** Default antialias flag. */
    private boolean m_antialias;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new SampleChartHandler.
     *
     * @param manager Reference to the instrument manager interface.
     * @param width Default image width.
     * @param height Default image height.
     * @param antialias True if the default antialias parameter should be true.
     */
    public SampleChartHandler( DefaultInstrumentManager manager,
                               int width,
                               int height,
                               boolean antialias )
    {
        super( "/sample-chart.jpg", CONTENT_TYPE_IMAGE_JPEG,
            InstrumentManagerHTTPConnector.ENCODING );
        
        m_manager = manager;
        m_width = width;
        m_height = height;
        m_antialias = antialias;
    }
    
    /*---------------------------------------------------------------
     * AbstractHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    public void doGet( String path, Map parameters, OutputStream os )
        throws IOException
    {
        String name = getParameter( parameters, "name" );
        InstrumentSampleDescriptor desc;
        try
        {
            desc = m_manager.locateInstrumentSampleDescriptor( name );
        }
        catch ( NoSuchInstrumentSampleException e )
        {
            // Sample no longer exists, go back to the parent instrument.
            int pos = name.lastIndexOf( '.' );
            if ( pos >= 0 )
            {
                // Starting with Java 1.4, encode takes an encoding, but this needs to
                //  work with 1.3.   Use our own version.
                String iName = URLCoder.encode( name.substring( 0,  pos ),
                    InstrumentManagerHTTPConnector.ENCODING );
                
                throw new HTTPRedirect( "instrument.html?name=" + iName );
            }
            else
            {
                throw new HTTPRedirect( "instrumentable.html" );
            }
        }
        
        int width = getIntegerParameter( parameters, "width", m_width );
        width = Math.max( 1, Math.min( 2048, width ) );
        int height = getIntegerParameter( parameters, "height", m_height );
        height = Math.max( 1, Math.min( 1024, height ) );
        
        boolean antialias = getBooleanParameter( parameters, "antialias", m_antialias );
        
        InstrumentSampleSnapshot snapshot = desc.getSnapshot();
        
        // Decide on a line interval based on the interval of the sample.
        long interval = snapshot.getInterval();
        int hInterval;
        String format;
        String detailFormat;
        if( interval < 1000 )
        {
            // Once per 10 seconds.
            hInterval = (int)( 10000 / interval );
            format = "{3}:{4}:{5}";
            detailFormat = "{1}/{2} {3}:{4}:{5}.{6}";
        }
        else if( interval < 60000 )
        {
            // Once per minute.
            hInterval = (int)( 60000 / interval );
            format = "{3}:{4}:{5}";
            detailFormat = "{1}/{2} {3}:{4}:{5}";
        }
        else if( interval < 600000 )
        {
            // Once per 10 minutes
            hInterval = (int)( 600000 / interval );
            format = "{1}/{2} {3}:{4}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 3600000 )
        {
            // Once per hour.
            hInterval = (int)( 3600000 / interval );
            format = "{1}/{2} {3}:{4}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 86400000 )
        {
            // Once per day.
            hInterval = (int)( 86400000 / interval );
            format = "{1}/{2}";
            detailFormat = "{1}/{2} {3}:{4}";
        }
        else if( interval < 604800000 )
        {
            // Once per week.
            hInterval = (int)( 604800000 / interval );
            format = "{0}/{1}/{2}";
            detailFormat = "{0}/{1}/{2}";
        }
        else
        {
            // Default to every 10 points.
            hInterval = 10;
            format = "{0}/{1}/{2}";
            detailFormat = "{0}/{1}/{2}";
        }
            
        // Actually create the chart and add it to the content pane
        LineChart chart = new LineChart( hInterval, interval, format, detailFormat, 20, antialias );
        chart.setValues( snapshot.getSamples(), snapshot.getTime() );
        
        byte[] imageData = null;
        
        // Create a new BufferedImage onto which the plant will be painted.
        BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        
        // Paint the chart onto the Graphics object of the BufferedImage.
        chart.setSize( bi.getWidth(), bi.getHeight() );
        
        Graphics g;
        try
        {
            g = bi.createGraphics();
        }
        catch ( Throwable t )
        {
            // Linux throws NoClassDefFoundError.
            // Solaris throws InternalError
            
            // On Headless UNIX machines this error will be thrown when attempting to
            //  create an graphic.  The AWT libraries require a native library that
            //  only exists on UNIX system which have X-Windows installed.  This is
            //  never a problem on Windows systems.
            
            // Rather than giving the user nothing, send them a preprepared jpeg file
            //  that notifies them of the problem.
            String imageResource = "noawtlibs.jpg";
            BufferedInputStream is =
                new BufferedInputStream( getClass().getResourceAsStream( imageResource ) );
            byte[] noAWTLibs;
            try {
                noAWTLibs = new byte[is.available()];
                is.read( noAWTLibs, 0, noAWTLibs.length );
            } finally {
                is.close();
            }
            // Now write the error image out to the client.
            os.write( noAWTLibs );
            return;
        }
        
        chart.paintComponent( g );

        // Encode the BufferedImage as a JPEG image and write it to the output stream.
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( os );
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam( bi );
        param.setQuality( 0.90f, true );
        encoder.encode( bi, param );
    }
}

