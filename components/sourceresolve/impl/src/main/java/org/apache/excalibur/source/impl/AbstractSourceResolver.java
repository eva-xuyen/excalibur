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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * This is an abstract implementation of a {@link SourceResolver}.
 *
 * The source resolving is done relative to a base directory/URI (if the given
 * location is relative). The base directory/URI has to be set using
 * {@link #setBaseURL(URL)}.
 *
 * A subclass should implement {@link #getSourceFactory(String)} and
 * {@link #releaseSourceFactory(SourceFactory)} if required.
 *
 * @see org.apache.excalibur.source.SourceResolver
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractSourceResolver.java 587637 2007-10-23 20:05:10Z
 *          cziegeler $
 */
public abstract class AbstractSourceResolver
    extends AbstractLoggable
    implements SourceResolver {

    /**
     * The base URL
     */
    protected URL baseURL;

    /** Set the base url. */
    public void setBaseURL(URL baseurl) {
        this.baseURL = baseurl;
    }

    protected abstract SourceFactory getSourceFactory(String protocol);

    protected void releaseSourceFactory(SourceFactory factory) {
        // nothing to do by default
    }

    /**
     * Get a <code>Source</code> object.
     *
     * @throws org.apache.excalibur.source.SourceNotFoundException
     *             if the source cannot be found
     */
    public Source resolveURI(String location)
    throws MalformedURLException, IOException, SourceException {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     *
     * @throws org.apache.excalibur.source.SourceNotFoundException
     *             if the source cannot be found
     */
    public Source resolveURI(String location, String baseURI, Map parameters)
    throws MalformedURLException, IOException, SourceException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(
                    "Resolving '" + location + "' with base '" + baseURI
                            + "' in context '" + baseURL + "'");
        }
        if (location == null) {
            throw new MalformedURLException("Invalid System ID");
        }
        if (null != baseURI && SourceUtil.indexOfSchemeColon(baseURI) == -1) {
            throw new MalformedURLException(
                    "BaseURI is not valid, it must contain a protocol: "
                            + baseURI);
        }

        if (baseURI == null) {
            baseURI = baseURL.toExternalForm();
        }

        String systemID = location;
        // special handling for windows file paths
        if (location.length() > 1 && location.charAt(1) == ':') {
            systemID = "file:/" + location;
        } else if (location.length() > 2 && location.charAt(0) == '/'
                && location.charAt(2) == ':') {
            systemID = "file:" + location;
        }

        // determine protocol (scheme): first try to get the one of the
        // systemID, if that fails, take the one of the baseURI
        String protocol;
        int protocolPos = SourceUtil.indexOfSchemeColon(systemID);
        if (protocolPos != -1) {
            protocol = systemID.substring(0, protocolPos);
        } else {
            protocolPos = SourceUtil.indexOfSchemeColon(baseURI);
            if (protocolPos != -1) {
                protocol = baseURI.substring(0, protocolPos);
            } else {
                protocol = "*";
            }
        }

        Source source = null;
        // search for a SourceFactory implementing the protocol
        SourceFactory factory = null;
        try {
            factory = this.getSourceFactory(protocol);
            if (factory != null) {
                systemID = absolutize(factory, baseURI, systemID);
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger()
                            .debug("Resolved to systemID : " + systemID);
                }
                source = factory.getSource(systemID, parameters);
            }
        } finally {
            this.releaseSourceFactory(factory);
        }

        if (null == source) {
            try {
                factory = this.getSourceFactory("*");
                if (factory == null) {
                    throw new SourceException(
                            "Unable to select source factory for '" + systemID
                                    + "'. No default factory found.");
                }
                systemID = absolutize(factory, baseURI, systemID);
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Resolved to systemID : " + systemID);
                }
                source = factory.getSource(systemID, parameters);
            } finally {
                this.releaseSourceFactory(factory);
            }
        }

        return source;
    }

    /**
     * Makes an absolute URI based on a baseURI and a relative URI.
     */
    private String absolutize(SourceFactory factory,
                              String baseURI,
                              String systemID) {
        if (factory instanceof URIAbsolutizer) {
            systemID = ((URIAbsolutizer) factory).absolutize(baseURI, systemID);
        } else {
            systemID = SourceUtil.absolutize(baseURI, systemID);
        }
        return systemID;
    }

    /**
     * Releases a resolved resource
     *
     * @param source
     *            the source to release
     */
    public void release(final Source source) {
        if (source == null) {
            return;
        }

        // search for a SourceFactory implementing the protocol
        final String scheme = source.getScheme();
        SourceFactory factory = null;

        try {
            factory = this.getSourceFactory(scheme);
            if (factory == null) {
                factory = this.getSourceFactory("*");
                if (factory == null) {
                    throw new RuntimeException("Unable to select source factory for '"
                                    + source.getURI() + "'.");
                }
            }
            factory.release(source);
        } finally {
            this.releaseSourceFactory(factory);
        }
    }
}
