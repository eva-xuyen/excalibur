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
package org.apache.avalon.excalibur.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SourceResource.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public final class SourceResource
    extends StreamResource
{
    /** The wrapped source object */
    private final Source m_source;

    /** The last validity object */
    private SourceValidity m_validity;

    /**
     * Instantiate the SourceResource
     */
    public SourceResource( final Source source )
        throws Exception
    {
        super( source.getURI() );

        m_source = source;
        setPreviousModified( System.currentTimeMillis() );
        m_validity = source.getValidity();
    }

    /**
     * Determines the last time this resource was modified
     */
    public long lastModified()
    {
        if( null == m_validity )
        {
            return System.currentTimeMillis();
        }
        else
        {
            int valid = m_validity.isValid();
            boolean isValid = false;
            SourceValidity newVal = null;
            if ( valid == 0 ) {
                m_source.refresh();
                newVal = m_source.getValidity();
                if( newVal != null)
                {
                    valid = m_validity.isValid( newVal );
                    isValid = (valid == 1);
                }
            } else {
                isValid = (valid == 1);
            }
            if ( isValid ) {
                return getPreviousModified();
            } else {
                if ( null == newVal ) {
                    m_source.refresh();
                    m_validity = m_source.getValidity();
                }
                return System.currentTimeMillis();
            }
        }
    }

    /**
     * Sets the resource value with an OutputStream
     */
    public InputStream getResourceAsStream()
        throws IOException
    {
        try
        {
            return m_source.getInputStream();
        }
        catch( SourceException se )
        {
            throw new IOException( "SourceException: " + se.getMessage() );
        }
    }

    /**
     * Sets the resource value with a Writer
     */
    public Reader getResourceAsReader()
        throws IOException
    {
        return new InputStreamReader( getResourceAsStream() );
    }

    /**
     * Sets the resource value with an OutputStream
     */
    public OutputStream setResourceAsStream()
        throws IOException
    {
        throw new IOException( "setResourceAsStream() not supported for URLResource" );
    }

    /**
     * Sets the resource value with a Writer
     */
    public Writer setResourceAsWriter()
        throws IOException
    {
        throw new IOException( "setResourceAsWriter() not supported for URLResource" );
    }

    public Source getSource()
    {
        return m_source;
    }
}
