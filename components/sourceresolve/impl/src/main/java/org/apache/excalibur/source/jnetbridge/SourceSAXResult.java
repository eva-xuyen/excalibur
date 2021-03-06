/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.excalibur.source.jnetbridge;

import javax.xml.transform.sax.SAXResult;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SourceSAXResult extends SAXResult {

    protected final Source source;

    protected final SourceFactory factory;

    protected final XMLizable xmlizable;

    protected boolean closed = false;

    public SourceSAXResult(final SourceFactory f, final Source s, final XMLizable x) {
        this.factory = f;
        this.source = s;
        this.xmlizable = x;
    }

    public void setHandler(ContentHandler handler) {
        if ( !this.closed ) {
            try {
                this.xmlizable.toSAX(handler);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            } finally {
                this.closed = true;
                this.factory.release(this.source);
            }
        } else {
            throw new RuntimeException("Source already closed.");
        }
    }


}
