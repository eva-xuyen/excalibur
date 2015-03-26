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

package org.apache.excalibur.instrument.manager.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class XMLGCHandler
    extends AbstractXMLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new XMLGCHandler.
     *
     * @param manager Reference to the DefaultInstrumentManager.
     * @param connector The InstrumentManagerHTTPConnector.
     */
    public XMLGCHandler( DefaultInstrumentManager manager,
                         InstrumentManagerHTTPConnector connector )
    {
        super( "/gc.xml", manager, connector );
    }

    /*---------------------------------------------------------------
     * AbstractHTTPURLHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The PrintWriter to write the result to.
     */
    public void doGet( String path, Map parameters, PrintWriter out )
        throws IOException
    {
        long oldMemory = this.getMemory();

        System.gc();

        long newMemory = this.getMemory();

        out.println( InstrumentManagerHTTPConnector.XML_BANNER );
        out.println( "<gc old-memory=\"" + oldMemory + "\" new-memory=\"" + newMemory + "\"/>" );
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private long getMemory()
    {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}

