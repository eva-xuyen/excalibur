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

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

/**
 * Abstract base class for a source implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractSource.java 641957 2008-03-27 19:21:49Z cziegeler $
 */

public abstract class AbstractSource
    extends AbstractLoggable
    implements Source {

    private boolean gotInfos;
    private long lastModificationDate;
    private long contentLength;
    private String systemId;

    private String scheme;

    /**
     * Get the last modification date and content length of the source.
     * Any exceptions are ignored.
     * Override this to get the real information
     */
    protected void getInfos() {
        this.contentLength = -1;
        this.lastModificationDate = 0;
    }

    /**
     * Call {@link #getInfos()} if it hasn't already been called since the last
     * call to {@link #refresh()}.
     */
    protected void checkInfos() {
        if( !gotInfos ) {
            getInfos();
            gotInfos = true;
        }
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     *
     * The returned stream must be closed by the calling code.
     *
     * @throws SourceException if file not found or
     *         HTTP location does not exist.
     * @throws IOException if I/O error occured.
     */
    public InputStream getInputStream()
    throws IOException, SourceException {
        return null;
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return systemId;
    }

    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return null;
    }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        gotInfos = false;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
        checkInfos();
        return this.contentLength;
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        checkInfos();
        return this.lastModificationDate;
    }

    /**
     * Sets the contentLength.
     * @param contentLength The contentLength to set
     */
    protected void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Sets the lastModificationDate.
     * @param lastModificationDate The lastModificationDate to set
     */
    protected void setLastModified(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * Sets the scheme.
     * @param scheme The scheme to set
     */
    protected void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the systemId.
     * @param systemId The systemId to set
     */
    protected void setSystemId(String systemId) {
        this.systemId = systemId;
    }

}
