/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.source.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;

import sun.net.ftp.FtpClient;

/**
 * Source implementation for the File Transfer Protocol.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class FTPSource extends URLSource implements ModifiableSource
{
    private boolean m_isAscii;

    public FTPSource()
    {
        super();
    }

    /**
     * Initialize a new object from a <code>URL</code>.
     * @param parameters This is optional
     */
    public void init( final URL url, final Map parameters ) throws IOException
    {
        final String systemId = url.toExternalForm();
        setSystemId( systemId );
        setScheme( SourceUtil.getScheme( systemId ) );

        m_url = url;
        m_isAscii = false;

        if ( parameters != null )
        {
            m_parameters = (SourceParameters) parameters.get( SourceResolver.URI_PARAMETERS );
            final String method = (String) parameters.get( SourceResolver.METHOD );

            if ( "ASCII".equalsIgnoreCase( method ) )
            {
                m_isAscii = true;
            }
        }
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     * 
     * @return <code>true</code> if the stream can be cancelled
     */
    public boolean canCancel( final OutputStream stream )
    {
        if ( stream instanceof FTPSourceOutputStream )
        {
            FTPSourceOutputStream fsos = (FTPSourceOutputStream) stream;
            if ( fsos.getSource() == this )
            {
                return fsos.canCancel();
            }
        }

        throw new IllegalArgumentException( "The stream is not associated to this source" );
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should not be used.
     */
    public void cancel( final OutputStream stream ) throws IOException
    {
        if ( stream instanceof FTPSourceOutputStream )
        {
            FTPSourceOutputStream fsos = (FTPSourceOutputStream) stream;
            if ( fsos.getSource() == this )
            {
                try
                {
                    fsos.cancel();
                }
                catch ( Exception e )
                {
                    throw new SourceException( "Exception during cancel.", e );
                }
                return;
            }
        }

        throw new IllegalArgumentException( "The stream is not associated to this source" );
    }

    /**
     * Delete the source.
     */
    public void delete() throws SourceException
    {
        EnhancedFtpClient ftpClient = null;
        try
        {
            ftpClient = getFtpClient();
            final String relativePath = URLDecoder.decode( m_url.getPath().substring( 1 ), "UTF-8" );
            ftpClient.delete( relativePath );
        }
        catch ( IOException e )
        {
            if ( e instanceof FileNotFoundException )
            {
                throw new SourceNotFoundException( e.getMessage() );
            }
            else
            {
                final String message =
                    "Failure during delete";
                throw new SourceException( message, e );
            }
        }
        finally
        {
            if ( ftpClient != null )
            {
                try
                {
                    ftpClient.closeServer();
                }
                catch ( IOException e ) {}
                }
            }
        }

    /**
     * Get the last modification date and content length of the source.
     * Any exceptions are ignored.
     */
    protected void getInfos()
    {
        // exists will be set below depending on the m_url type
        m_exists = false;
        try
        {
            if ( null == m_connection )
            {
                m_connection = m_url.openConnection();
                //String userInfo = URLDecoder.decode( m_url.getUserInfo(), "UTF-8" );
            }
            setLastModified( m_connection.getLastModified() );
            setContentLength( m_connection.getContentLength() );
            // getting the content type here seems to screw up=20
            // the InputStream on the URLConnection.
            // m_mimeType = m_connection.getContentType();
            m_mimeType = null;
            m_exists = true;
        }
        catch ( IOException ignore )
        {
            setContentLength( -1 );
            setLastModified( 0 );
        }
    }

    /**
     * Return an {@link OutputStream} to write to.
     *
     * The returned stream must be closed or cancelled by the calling code.
     */
    public OutputStream getOutputStream() throws IOException
    {
        return new FTPSourceOutputStream( this );
    }

    /**
     * Creates an FtpClient and logs in the current user.
     */
    private final EnhancedFtpClient getFtpClient()
        throws IOException
    {
        final EnhancedFtpClient ftpClient = 
            new EnhancedFtpClient( m_url.getHost() );
        ftpClient.login( getUser(), getPassword() );
        return ftpClient;
    }

    /**
     * @return the user part of the user info string, 
     * <code>null</code> if there is no user info.
     */
    private final String getUser()
    {
        final String userInfo = m_url.getUserInfo();
        // decode after splitting
        if ( userInfo != null )
        {
            int index = userInfo.indexOf( ':' );
            if ( index != -1 )
            {
                try
                {
                    return URLDecoder.decode( userInfo.substring( 0, index ), "UTF-8" );
                }
                catch ( java.io.UnsupportedEncodingException e ) {}
            }
        }
        return null;
    }

    /**
     * @return the password part of the user info string, 
     * <code>null</code> if there is no user info.
     */
    private final String getPassword()
    {
        final String userInfo = m_url.getUserInfo();
        // decode after splitting
        if ( userInfo != null )
        {
            int index = userInfo.indexOf( ':' );
            if ( index != -1 && userInfo.length() > index + 1 )
            {
                try
                {
                    return URLDecoder.decode( userInfo.substring( index + 1 ), "UTF-8" );
                }
                catch ( java.io.UnsupportedEncodingException e ) { }
            }
        }
        return null;
    }

    /**
     * Need to extend FtpClient in order to get to protected issueCommand
     * and implement additional functionality.
     */
    private static class EnhancedFtpClient extends FtpClient
    {

        private EnhancedFtpClient( String host ) throws IOException
        {
            super( host );
        }

        void delete( final String path ) throws IOException
        {
            issueCommand( "DELE " + path );
        }

        /**
         * Create a directory in the current working directory.
         */
        void mkdir( final String directoryName ) throws IOException
        {
            issueCommand( "MKD " + directoryName );
        }

        /**
         * Create all directories along a directory path if they
         * do not already exist.
         * 
         * The algorithm traverses the directory tree in reversed
         * direction. cd'ing first to the deepest level 
         * and if that directory doesn't exist try cd'ing to its
         * parent from where it can be created.
         * 
         * NOTE: after completion the current working directory 
         * will be the directory identified by directoryPath.
         */
        void mkdirs( final String directoryPath ) throws IOException
        {
            try
            {
                cd( directoryPath );
            }
            catch ( FileNotFoundException e )
            {
                // doesn't exist, create it
                String directoryName = null;
                final int index = directoryPath.lastIndexOf( '/' );
                if ( index != -1 )
                {
                    final String parentDirectoryPath = 
                        directoryPath.substring( 0, index );
                    directoryName = directoryPath.substring( index + 1 );
                    mkdirs( parentDirectoryPath );
                }
                else
                {
                    directoryName = directoryPath;
                }
                mkdir( directoryName );
                cd( directoryName );
            }
        }

    }

    /**
     * Buffers the output in a byte array and only writes to the remote 
     * FTP location at closing time.
     */
    private static class FTPSourceOutputStream extends ByteArrayOutputStream
    {
        private final FTPSource m_source;
        private boolean         m_isClosed = false;

        FTPSourceOutputStream( final FTPSource source )
        {
            super( 8192 );
            m_source = source;
        }

        public void close() throws IOException
        {
            if ( !m_isClosed )
            {
                EnhancedFtpClient ftpClient = null;
                OutputStream out = null;
                try
                {
                    ftpClient = m_source.getFtpClient();
                    if (m_source.m_isAscii) {
                        ftpClient.ascii();
                    } else {
                        ftpClient.binary();
                    }
                    String parentPath = null;
                    String fileName = null;
                    final String relativePath = URLDecoder.decode( m_source.m_url.getPath(), "UTF-8" ).substring( 1 );
                    final int index = relativePath.lastIndexOf( '/' );
                    if ( index != -1 )
                    {
                        parentPath = relativePath.substring( 0, index );
                        fileName = relativePath.substring( index + 1 );
                        ftpClient.mkdirs( parentPath );
                    }
                    else
                    {
                        fileName = relativePath;
                    }
                    out = ftpClient.put( fileName );
                    final byte[] bytes = toByteArray();
                    out.write( bytes );
                }
                finally
                {
                    if ( out != null )
                    {
                        try
                        {
                            out.close();
                        }
                        catch ( IOException e ) {}
                    }
                    if ( ftpClient != null )
                    {
                        try
                        {
                            ftpClient.closeServer();
                        }
                        catch ( IOException e ) {}
                        }
                    m_isClosed = true;
                }
            }
        }

        boolean canCancel()
        {
            return !m_isClosed;
        }

        void cancel() throws Exception
        {
            if ( m_isClosed )
            {
                final String message =
                    "Cannot cancel: outputstrem is already closed";
                throw new IllegalStateException( message );
            }
            m_isClosed = true;
        }

        FTPSource getSource()
        {
            return m_source;
        }

    }

}
