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
package org.apache.avalon.cornerstone.blocks.masterstore;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public abstract class AbstractFilePersistentStreamRepository extends AbstractFileRepository {

    protected final HashMap m_inputs = new HashMap();
    protected final HashMap m_outputs = new HashMap();

    /**
     * Get the object associated to the given unique key.
     */
    public synchronized InputStream get( final String key )
    {
        try
        {
            final ResettableFileInputStream stream =
                new ResettableFileInputStream( getFile( key ) );

            final Object o = m_inputs.get( key );
            if( null == o )
            {
                m_inputs.put( key, stream );
            }
            else if( o instanceof ArrayList )
            {
                ( (ArrayList)o ).add( stream );
            }
            else
            {
                final ArrayList list = new ArrayList();
                list.add( o );
                list.add( stream );
                m_inputs.put( key, stream );
            }

            return stream;
        }
        catch( final IOException ioe )
        {
            final String message = "Exception caught while retrieving a stream ";
            monitor.unExpectedIOException(File_Persistent_Stream_Repository.class, message, ioe);
            throw new RuntimeException( message + ": " + ioe );
        }
    }

    /**
     * Store the given object and associates it to the given key
     */
    public synchronized OutputStream put( final String key )
    {
        try
        {
            final OutputStream outputStream = getOutputStream( key );
            final BufferedOutputStream stream = new BufferedOutputStream( outputStream );

            final Object o = m_outputs.get( key );
            if( null == o )
            {
                m_outputs.put( key, stream );
            }
            else if( o instanceof ArrayList )
            {
                ( (ArrayList)o ).add( stream );
            }
            else
            {
                final ArrayList list = new ArrayList();
                list.add( o );
                list.add( stream );
                m_outputs.put( key, stream );
            }

            return stream;
        }
        catch( final IOException ioe )
        {
            final String message = "Exception caught while storing a stream ";
            monitor.unExpectedIOException(File_Persistent_Stream_Repository.class, message, ioe);
            throw new RuntimeException( message + ": " + ioe );
        }
    }

    public void remove( final String key )
    {
        Object o = m_inputs.remove( key );
        if( null != o )
        {
            if( o instanceof InputStream )
            {
                IOUtil.shutdownStream( (InputStream)o );
            }
            else
            {
                final ArrayList list = (ArrayList)o;
                final int size = list.size();

                for( int i = 0; i < size; i++ )
                {
                    IOUtil.shutdownStream( (InputStream)list.get( i ) );
                }
            }
        }

        o = m_outputs.remove( key );
        if( null != o )
        {
            if( o instanceof OutputStream )
            {
                IOUtil.shutdownStream( (OutputStream)o );
            }
            else
            {
                final ArrayList list = (ArrayList)o;
                final int size = list.size();

                for( int i = 0; i < size; i++ )
                {
                    IOUtil.shutdownStream( (OutputStream)list.get( 0 ) );
                }
            }
        }

        super.remove( key );
    }

    protected String getExtensionDecorator()
    {
        return ".FileStreamStore";
    }
}
