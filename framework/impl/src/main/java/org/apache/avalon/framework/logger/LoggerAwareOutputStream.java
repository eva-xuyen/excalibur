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
package org.apache.avalon.framework.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Logger aware output stream, characters written to this {@link OutputStream}
 * are buffered until a newline character is encountered, or a flush() is called.
 * 
 * <p>
 * Extend to specify the log method that the message should be invoked. eg:
 * </p>
 * 
 * <pre>
 * setOutputStream( new LoggerAwareOutputStream( getLogger() ) {
 *     protected void logMessage( String message )
 *     {
 *         if ( m_logger.isDebugEnabled() )
 *         {
 *             m_logger.debug( message );
 *         }
 *     }
 * } );
 * </pre>
 * 
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version $Revision:$
 * @since Nov 19, 2004 7:03:50 PM
 */
public abstract class LoggerAwareOutputStream extends OutputStream
{
    /**
     * Constructor, creates instance of class.
     * 
     * @param logger logger this output stream should use
     */
    public LoggerAwareOutputStream( Logger logger ) {
        m_logger = logger;
    }

    /**
     * Writes a byte to the internal buffer. If a newline character is
     * encountered, then the buffer is sent to the logger.
     * 
     * @param b character to write
     * @throws IOException if an error occurs
     * @see java.io.OutputStream#write(int)
     */
    public void write( int b ) throws IOException
    {
        if ( b == '\n' )
        {
            final byte[] content = bos.toByteArray();
            logMessage( new String( content ) );
            bos.reset();
            return;
        }

        bos.write( b );
    }

    /**
     * Flushes this output stream, writing any buffered content to the log
     * 
     * @throws IOException on error
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException
    {
        final byte[] content = bos.toByteArray();
        logMessage( new String( content ) );
        bos.reset();
    }

    /**
     * Purposely flushes the stream, but doesn't close anything since the logger
     * is managed by another class.
     * 
     * @throws IOException if an IO error occurs
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException
    {
        flush();
    }

    /**
     * Writes the message to the log. Subclasses should override this method to
     * send the message to the log level they require.
     * 
     * @param message message to be written
     */
    protected abstract void logMessage( String message );

    /** Message buffer */
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /** {@link Logger} reference */
    protected final Logger              m_logger;
}