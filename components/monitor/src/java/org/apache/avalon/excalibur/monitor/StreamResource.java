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
package org.apache.avalon.excalibur.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Managed Stream based Resource.  This is convenient when you want to dynamically
 * set and get the information from the resource.  For instance, the Resource does
 * not need to be actively monitored if all access to the resource goes through
 * this type of Resource.  It can notify the change as soon as the Writer or
 * OutputStream has been closed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: StreamResource.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public abstract class StreamResource
    extends Resource
{
    /**
     * Required constructor.  The <code>String</code> location is transformed by
     * the specific resource monitor.  For instance, a FileResource will be able
     * to convert a string representation of a path to the proper File object.
     */
    public StreamResource( final String location )
        throws Exception
    {
        super( location );
    }

    /**
     * Get the Resource contents as an InputStream.
     */
    public abstract InputStream getResourceAsStream()
        throws IOException;

    /**
     * Get the Resource contents as a Reader.
     */
    public abstract Reader getResourceAsReader()
        throws IOException;

    /**
     * Set the Resource contents as an OutputStream.
     */
    public abstract OutputStream setResourceAsStream()
        throws IOException;

    /**
     * Set the Resource contents as a Writer.
     */
    public abstract Writer setResourceAsWriter() throws IOException;

    /**
     * Automatically handle the streamClosedEvent from the ResourceOutputStream
     * and ResourceWriter.
     */
    protected void streamClosedEvent()
    {
        fireAndSetModifiedTime( System.currentTimeMillis() );
    }
}
