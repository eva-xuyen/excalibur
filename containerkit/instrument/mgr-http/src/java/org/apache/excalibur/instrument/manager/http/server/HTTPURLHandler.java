/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
import java.util.Map;


/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:29 $
 * @since 4.1
 */
public interface HTTPURLHandler
{
    String CONTENT_TYPE_TEXT_HTML    = "text/html";
    String CONTENT_TYPE_TEXT_XML     = "text/xml";
    String CONTENT_TYPE_IMAGE_JPEG   = "image/jpeg";
    String CONTENT_TYPE_IMAGE_X_ICON = "image/x-icon";
    
    /**
     * Returns the path handled by this handler.
     *
     * @return The path handled by this handler.
     */
    String getPath();
    
    /**
     * Returns the content type.
     *
     * @return The content type.
     */
    String getContentType();
    
    /**
     * Return the encoding to use.
     *
     * @return the encoding.
     */
    String getEncoding();
    
    /**
     * Handles the specified request.
     *
     * @param The full path being handled.
     * @param parameters A Map of the parameters in the request.
     * @param os The OutputStream to write the result to.
     */
    void handleRequest( String path, Map parameters, OutputStream os )
        throws IOException;
}

