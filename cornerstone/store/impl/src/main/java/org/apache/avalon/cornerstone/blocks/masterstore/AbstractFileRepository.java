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

import org.apache.avalon.cornerstone.services.store.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This an abstract class implementing functionality for creating a file-store.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractFileRepository
    implements Repository
{
    protected static final boolean DEBUG = false;

    protected static final String HANDLED_URL = "file://";
    protected static final int BYTE_MASK = 0x0f;
    protected static final char[] HEX_DIGITS = new char[]
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    protected String m_path;
    protected String m_destination;
    protected String m_extension;
    protected String m_name;
    protected FilenameFilter m_filter;
    protected File m_baseDirectory;
    protected FileRepositoryMonitor monitor;

    protected abstract String getExtensionDecorator();


    protected void setDestination( final String destination )
        throws IOException
    {
        if( !destination.startsWith( HANDLED_URL ) )
        {
            throw new IOException( "cannot handle destination " + destination );
        }

        m_path = destination.substring( HANDLED_URL.length() );

        File directory;

        // Check for absolute path
        if( m_path.startsWith( "/" ) )
        {
            directory = new File( m_path );
        }
        else
        {
            directory = new File( m_baseDirectory, m_path );
        }

        try
        {
            directory = directory.getCanonicalFile();
        }
        catch( final IOException ioe )
        {
            throw new IOException( "Unable to form canonical representation of " +
                                              directory );
        }

        m_path = directory.toString();

        m_destination = destination;
    }

    protected AbstractFileRepository createChildRepository()
        throws Exception
    {
        return (AbstractFileRepository)getClass().newInstance();
    }

    public Repository getChildRepository( final String childName )
    {
        AbstractFileRepository child = null;

        try
        {
            child = createChildRepository();
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Cannot create child repository " +
                                        childName + " : " + e );
        }

        try
        {
            child.setDestination( m_destination + File.pathSeparatorChar +
                                  childName + File.pathSeparator );
        }
        catch( final IOException ioe )
        {
            throw new RuntimeException( "Cannot set destination for child child " +
                                        "repository " + childName +
                                        " : " + ioe );
        }

        try
        {
            initializeChild(child);
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Cannot initialize child " +
                                        "repository " + childName +
                                        " : " + e );
        }

        if( DEBUG )
        {
            monitor.repositoryCreated(AbstractFileRepository.class, m_name, m_destination, childName);
        }

        return child;
    }

    protected abstract void initializeChild(AbstractFileRepository child) throws Exception;

    protected File getFile( final String key )
        throws IOException
    {
        return new File( encode( key ) );
    }

    protected InputStream getInputStream( final String key )
        throws IOException
    {
        return new FileInputStream( getFile( key ) );
    }

    protected OutputStream getOutputStream( final String key )
        throws IOException
    {
        return new FileOutputStream( getFile( key ) );
    }

    /**
     * Remove the object associated to the given key.
     */
    public synchronized void remove( final String key )
    {
        try
        {
            final File file = getFile( key );
            file.delete();
            if( DEBUG )
              monitor.keyRemoved(AbstractFileRepository.class, key);
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Exception caught while removing" +
                                        " an object: " + e );
        }
    }

    /**
     * Indicates if the given key is associated to a contained object.
     */
    public synchronized boolean containsKey( final String key )
    {
        try
        {
            final File file = getFile( key );
            if( DEBUG ) monitor.checkingKey(AbstractFileRepository.class, key);
            return file.exists();
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Exception caught while searching " +
                                        "an object: " + e );
        }
    }

    /**
     * Returns the list of used keys.
     */
    public Iterator list()
    {
        final File storeDir = new File( m_path );
        final String[] names = storeDir.list( m_filter );
        final ArrayList list = new ArrayList();

        for( int i = 0; i < names.length; i++ )
        {
            list.add( decode( names[ i ] ) );
        }

        return list.iterator();
    }

    /**
     * Returns a String that uniquely identifies the object.
     * <b>Note:</b> since this method uses the Object.toString()
     * method, it's up to the caller to make sure that this method
     * doesn't change between different JVM executions (like
     * it may normally happen). For this reason, it's highly recommended
     * (even if not mandated) that Strings be used as keys.
     */
    protected String encode( final String key )
    {
        final byte[] bytes = key.getBytes();
        final char[] buffer = new char[ bytes.length << 1 ];

        for( int i = 0, j = 0; i < bytes.length; i++ )
        {
            final int k = bytes[ i ];
            buffer[ j++ ] = HEX_DIGITS[ ( k >>> 4 ) & BYTE_MASK ];
            buffer[ j++ ] = HEX_DIGITS[ k & BYTE_MASK ];
        }

        StringBuffer result = new StringBuffer();
        result.append( m_path );
        result.append( File.separator );
        result.append( buffer );
        result.append( m_extension );
        return result.toString();
    }

    /**
     * Inverse of encode exept it do not use path.
     * So decode(encode(s) - m_path) = s.
     * In other words it returns a String that can be used as key to retive
     * the record contained in the 'filename' file.
     */
    protected String decode( String filename )
    {
        filename = filename.substring( 0, filename.length() - m_extension.length() );
        final int size = filename.length();
        final byte[] bytes = new byte[ size >>> 1 ];

        for( int i = 0, j = 0; i < size; j++ )
        {
            bytes[ j ] = Byte.parseByte( filename.substring( i, i + 2 ), 16 );
            i += 2;
        }

        return new String( bytes );
    }
}
