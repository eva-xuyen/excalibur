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

package org.apache.excalibur.instrument.manager.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public abstract class AbstractHTTPURLPrintWriterHandler
    extends AbstractHTTPURLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractHTTPURLPrintWriterHandler.
     *
     * @param path The path handled by this handler.
     * @param contentType The content type.
     * @param encoding The encoding to use when writing servlet results.
     */
    public AbstractHTTPURLPrintWriterHandler( String path, String contentType, String encoding )
    {
        super( path, contentType + "; charset=" + encoding, encoding );
    }
    
    /*---------------------------------------------------------------
     * AbstractHTTPURLHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    public final void doGet( String path, Map parameters, OutputStream os )
        throws IOException
    {
        PrintWriter out = new PrintWriter( new OutputStreamWriter( os, getEncoding() ) );
        doGet( path, parameters, out );
        out.flush();
    }
            
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The PrintWriter to write the result to.
     */
    public abstract void doGet( String path, Map parameters, PrintWriter out )
        throws IOException;
}

