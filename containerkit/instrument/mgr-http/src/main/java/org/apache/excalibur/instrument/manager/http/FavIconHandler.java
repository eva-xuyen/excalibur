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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.excalibur.instrument.manager.http.server.AbstractHTTPURLHandler;

/**
 * Serves up a favicon.ico file.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public class FavIconHandler
    extends AbstractHTTPURLHandler
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new FavIconHandler.
     */
    public FavIconHandler()
    {
        super( "/favicon.ico", CONTENT_TYPE_IMAGE_X_ICON,
            InstrumentManagerHTTPConnector.ENCODING );
    }

    /*---------------------------------------------------------------
     * AbstractHandler Methods
     *-------------------------------------------------------------*/
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    public void doGet( String path, Map parameters, OutputStream os )
        throws IOException
    {
        String imageResource = "favicon.ico";
        BufferedInputStream is =
            new BufferedInputStream( this.getClass().getResourceAsStream( imageResource ) );
        byte[] favIcon;
        try {
            favIcon = new byte[is.available()];
            is.read( favIcon, 0, favIcon.length );
        } finally {
            is.close();
        }
        // Now write the image out to the client.
        os.write( favIcon );
    }
}

