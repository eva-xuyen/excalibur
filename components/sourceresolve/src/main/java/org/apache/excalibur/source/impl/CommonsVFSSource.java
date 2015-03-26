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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceUtil;

/**
 * Source implementation that provides resolver access to all protocols
 * supported by <a href="http://jakarta.apache.org/commons/sandbox/vfs">Commons VFS</a>. 
 * 
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version $Revision:$
 * @since Nov 19, 2004 10:54:02 AM
 */
public class CommonsVFSSource extends AbstractSource
    implements LogEnabled, ModifiableSource {
    
    /**
     * Constructor, creates instance of class.
     *
     * @param location location to resolve
     * @param parameters protocol specific parameters
     * @throws FileSystemException if an error occurs
     */
    public CommonsVFSSource(final String location, final Map parameters)
        throws FileSystemException {
        m_location = location;
        m_manager = VFS.getManager();
        m_fileObject = m_manager.resolveFile(location); // REVISIT: parameters
        m_fileContent = m_fileObject.getContent();
    }
    
    /**
     * Sets content information for this source.
     * 
     * @see org.apache.excalibur.source.impl.AbstractSource#getInfos()
     */
    protected void getInfos() {
        try {
            setContentLength(m_fileContent.getSize());
        } catch (final FileSystemException e) {
            
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(
                    "Unable to determine content length for " + m_location, e
                );
            }
            setContentLength(-1); // Source API says return -1 if unknown
        }
        
        try {
            setLastModified(m_fileContent.getLastModifiedTime());
        } catch (final FileSystemException e) {

            if (getLogger().isWarnEnabled()) {
                getLogger().warn(
                    "Unable to determine last modified date for " + m_location, e
                );
            }
            setLastModified(0); // Source API says return 0 if unknown
        }

        setSystemId(m_location);
        setScheme(SourceUtil.getScheme(m_location));
    }
    
    /**
     * Obtain an {@link InputStream} for this source.
     * 
     * @throws IOException if an IO error occurs
     * @throws SourceException if a source exception occurs
     * @return {@link InputStream}
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceException {
        return m_fileContent.getInputStream();
    }

    /**
     * Whether this resource exists or not
     * 
     * @return true if it does exist, false otherwise, false on error
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        try {
            return m_fileObject.exists();
        } catch (final FileSystemException e) {

            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Unable to determine existence for " + m_location, e);
            }
            return false;
        }
    }
    
    // ModifiableSource methods

    /**
     * Whether we can cancel writing to the output stream
     * 
     * @param stream stream to cancel
     * @return true if we can cancel, false otherwise
     * @see org.apache.excalibur.source.ModifiableSource#canCancel(java.io.OutputStream)
     */
    public boolean canCancel(final OutputStream stream) {
        // VFS API doesn't support buffering of write streams till close() directly
        return false;
    }
    
    /**
     * Cancels writing to the specified output stream.
     * 
     * @param stream stream to cancel writing to
     * @throws IOException if an error occurs
     * @see org.apache.excalibur.source.ModifiableSource#cancel(java.io.OutputStream)
     */
    public void cancel(final OutputStream stream) throws IOException {
        throw new IOException("Cancel() not implemented");
    }
    
    /**
     * Deletes the source.
     * 
     * @throws SourceException if an error occurs
     * @see org.apache.excalibur.source.ModifiableSource#delete()
     */
    public void delete() throws SourceException {
        try {
            m_fileObject.delete();
        } catch (final FileSystemException e) {
            throw new SourceException("Unable to delete resource: " + m_location, e);
        }
    }
    
    /**
     * Obtain an {@link OutputStream} to the source
     * 
     * @return an {@link OutputStream} 
     * @see org.apache.excalibur.source.ModifiableSource#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        return m_fileContent.getOutputStream();
    }
    
    /**
     * Enables logging for this source.
     * 
     * @param logger {@link Logger} instance to use
     * @see org.apache.avalon.framework.logger.LogEnabled
     *      #enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(final Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Obtain access to this components logger.
     * 
     * @return the logger
     */
    private Logger getLogger() {
        return m_logger;
    }

    /** Resource location */
    private final String m_location;
    
    /** {@link FileSystemManager} reference */
    private final FileSystemManager m_manager;
    
    /** The resource itself */
    private final FileObject m_fileObject;
    
    /** The content of the resource */
    private final FileContent m_fileContent;
    
    /** Our logging target */
    private Logger m_logger;
}
