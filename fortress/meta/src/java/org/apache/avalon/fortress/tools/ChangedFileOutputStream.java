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

package org.apache.avalon.fortress.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream which will only update an existing file if its contents
 *  actually change.  Needed to keep Ant from rebuilding jars even when
 *  nothing has changed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/02 08:29:44 $
 */
public final class ChangedFileOutputStream
    extends OutputStream
{
    /** The file to write to. */
    private File m_file;
    
    /** The output stream used to buffer data being writen. */
    private ByteArrayOutputStream m_bos;
    
    /*---------------------------------------------------------------
     * Constructor
     *-------------------------------------------------------------*/
    /**
     * Creates a new ChangedFileOutputStream.
     *
     * @param file The file to write to.
     */
    public ChangedFileOutputStream( File file )
    {
        m_file = file;
        m_bos = new ByteArrayOutputStream();
    }
    
    /*---------------------------------------------------------------
     * OutputStream Methods
     *-------------------------------------------------------------*/
    /**
     * Writes the specified byte to this output stream.
     *
     * @param b Byte to write.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void write( int b )
        throws IOException
    {
        m_bos.write( b );
    }
    
    /**
     * Close the stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void close()
        throws IOException
    {
        byte[] newContent = m_bos.toByteArray();
        m_bos.close();
        
        boolean changed;
        if ( m_file.exists() )
        {
            byte[] oldContent = readBytes( m_file );
            
            // Compare the old and new bytes.
            if ( newContent.length != oldContent.length )
            {
                changed = true;
            }
            else
            {
                changed = false;
                for ( int i = 0; i < newContent.length; i++ )
                {
                    if ( newContent[i] != oldContent[i] )
                    {
                        changed = true;
                        break;
                    }
                }
            }
        }
        else
        {
            // File does not exist.
            changed = true;
        }
        
        if ( changed )
        {
            writeBytes( m_file, newContent );
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Reads the full contents of a file into a byte array.  The file contents
     *  are treated as binary data.
     *
     * @param file File to read.
     *
     * @return The contents of the file as a byte array.
     *
     * @throws IOException If the file could not be read for any reason.
     */
    private byte[] readBytes( File file )
        throws IOException
    {
        byte[] bytes = new byte[(int)file.length()];
        
        FileInputStream is = new FileInputStream( file );
        try
        {
            // Make sure all bytes are read in.
            int pos = 0;
            int read;
            while ( ( pos < bytes.length ) &&
                ( ( read = is.read( bytes, pos, bytes.length - pos ) ) >= 0 ) )
            {
                pos += read;
            }
        }
        finally
        {
            is.close();
        }
        
        return bytes;
    }
    
    /**
     * Reads the full contents of a byte array out to a file.
     *
     * @param file File to write to.
     * @param bytes The binary data to write.
     *
     * @throws IOException If the file could not be written to for any reason.
     */
    private void writeBytes( File file, byte[] bytes )
        throws IOException
    {
        FileOutputStream os = new FileOutputStream( file );
        try
        {
            os.write( bytes, 0, bytes.length );
        }
        finally
        {
            os.close();
        }
    }
}
