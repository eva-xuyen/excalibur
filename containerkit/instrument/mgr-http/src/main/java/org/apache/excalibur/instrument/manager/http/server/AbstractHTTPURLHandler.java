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

package org.apache.excalibur.instrument.manager.http.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/02/29 18:11:04 $
 * @since 4.1
 */
public abstract class AbstractHTTPURLHandler
    extends AbstractLogEnabledInstrumentable
    implements HTTPURLHandler
{
    /** The path handled by this handler. */
    private String m_path;
    
    /** The content type. */
    private String m_contentType;
    
    /** The encoding. */
    private String m_encoding;
    
    /** Number of requests. */
    private CounterInstrument m_instrumentRequests;
    
    /** Time it takes to process each request. */
    private ValueInstrument m_instrumentRequestTime;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTTPURLHandler.
     *
     * @param path The path handled by this handler.
     * @param contentType The content type.
     */
    public AbstractHTTPURLHandler( String path, String contentType, String encoding )
    {
        m_path = path;
        m_contentType = contentType;
        m_encoding = encoding;
        
        addInstrument( m_instrumentRequests = new CounterInstrument( "requests" ) );
        addInstrument( m_instrumentRequestTime = new ValueInstrument( "request-time" ) );
    }
    
    /*---------------------------------------------------------------
     * HTTPURLHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the path handled by this handler.
     *
     * @return The path handled by this handler.
     */
    public String getPath()
    {
        return m_path;
    }
    
    /**
     * Returns the content type.
     *
     * @return The content type.
     */
    public String getContentType()
    {
        return m_contentType;
    }
    
    /**
     * Return the encoding to use.
     *
     * @return the encoding.
     */
    public String getEncoding()
    {
        return m_encoding;
    }
    
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    public final void handleRequest( String path, Map parameters, OutputStream os )
        throws IOException
    {
        long start = System.currentTimeMillis();
        try
        {
            doGet( path, parameters, os );
        }
        finally
        {
            m_instrumentRequests.increment();
            if ( m_instrumentRequestTime.isActive() )
            {
                m_instrumentRequestTime.setValue( (int)( System.currentTimeMillis() - start ) );
            }
        }
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    public abstract void doGet( String path, Map parameters, OutputStream os )
        throws IOException;
    
    public String getParameter( Map params, String name, String defaultValue )
    {
        Object param = params.get( name );
        if ( param == null )
        {
            return defaultValue;
        }
        else if ( param instanceof String )
        {
            return (String)param;
        }
        else
        {
            List list = (List)param;
            return (String)list.get( 0 );
        }
    }
    
    public String getParameter( Map params, String name )
        throws FileNotFoundException
    {
        String param = getParameter( params, name, null );
        if ( param == null )
        {
            throw new FileNotFoundException( "The " + name + " parameter was not specified." );
        }
        return param;
    }
    
    public boolean getBooleanParameter( Map params, String name )
        throws FileNotFoundException
    {
        return Boolean.getBoolean( getParameter( params, name ) );
    }
    
    public boolean getBooleanParameter( Map params, String name, boolean defaultValue )
    {
        String value = getParameter( params, name, null );
        if ( value == null )
        {
            return defaultValue;
        }
        
        if ( value.length() < 1 )
        {
            return false;
        }
        
        char c = value.charAt( 0 );
        switch ( c )
        {
        case 'T': // TRUE
        case 't': // true
        case 'Y': // YES
        case 'y': // yes
            return true;
        }
        
        return false;
    }
    
    public int getIntegerParameter( Map params, String name )
        throws FileNotFoundException
    {
        try
        {
            return Integer.parseInt( getParameter( params, name ) );
        }
        catch ( NumberFormatException e )
        {
            throw new FileNotFoundException( "The specified " + name + " was invalid." );
        }
    }
    
    public int getIntegerParameter( Map params, String name, int defaultValue )
    {
        String value = getParameter( params, name, null );
        if ( value == null )
        {
            return defaultValue;
        }
        
        try
        {
            return Integer.parseInt( value );
        }
        catch ( NumberFormatException e )
        {
            return defaultValue;
        }
    }
    
    public long getLongParameter( Map params, String name )
        throws FileNotFoundException
    {
        try
        {
            return Long.parseLong( getParameter( params, name ) );
        }
        catch ( NumberFormatException e )
        {
            throw new FileNotFoundException( "The specified " + name + " was invalid." );
        }
    }
    
    public long getLongParameter( Map params, String name, long defaultValue )
    {
        String value = getParameter( params, name, null );
        if ( value == null )
        {
            return defaultValue;
        }
        
        try
        {
            return Long.parseLong( value );
        }
        catch ( NumberFormatException e )
        {
            return defaultValue;
        }
    }
    
    public String[] getParameters( Map params, String name )
    {
        Object param = params.get( name );
        if ( param == null )
        {
            return new String[0];
        }
        else if ( param instanceof String )
        {
            return new String[] { (String)param };
        }
        else
        {
            List list = (List)param;
            String[] ary = new String[list.size()];
            list.toArray( ary );
            return ary;
        }
    }
    
    public int[] getIntegerParameters( Map params, String name, int defaultValue )
    {
        String[] values = getParameters( params, name );
        int[] iValues = new int[values.length];
        
        for ( int i = 0; i < values.length; i++ )
        {
            try
            {
                iValues[i] = Integer.parseInt( values[i] );
            }
            catch ( NumberFormatException e )
            {
                iValues[i] = defaultValue;
            }
        }
        
        return iValues;
    }
    
    public long[] getLongParameters( Map params, String name, long defaultValue )
    {
        String[] values = getParameters( params, name );
        long[] lValues = new long[values.length];
        
        for ( int i = 0; i < values.length; i++ )
        {
            try
            {
                lValues[i] = Long.parseLong( values[i] );
            }
            catch ( NumberFormatException e )
            {
                lValues[i] = defaultValue;
            }
        }
        
        return lValues;
    }
}

