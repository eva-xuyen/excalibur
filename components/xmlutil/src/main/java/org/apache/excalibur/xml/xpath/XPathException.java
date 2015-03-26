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
package org.apache.excalibur.xml.xpath;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.avalon.framework.CascadingException;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class XPathException extends CascadingException {

    /**
     * Construct a new <code>ProcessingException</code> instance.
     */
    public XPathException(String message) {
        super(message, null);
    }
    
    /**
     * Creates a new <code>ProcessingException</code> instance.
     *
     * @param ex an <code>Exception</code> value
     */
    public XPathException(Exception ex) {
        super(ex.getMessage(), ex);
    }
    
    /**
     * Construct a new <code>ProcessingException</code> that references
     * a parent Exception.
     */
    public XPathException(String message, Throwable t) {
        super(message, t);
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        final Throwable t = getCause();
        if(t!=null) {
            s.append(": ");
            s.append(t.toString());
        }
        return s.toString();
    }
    
    public void printStackTrace() {
        super.printStackTrace();
        if(getCause()!=null)
            getCause().printStackTrace();
    }
    
    public void printStackTrace( PrintStream s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }
    
    public void printStackTrace( PrintWriter s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }
}
