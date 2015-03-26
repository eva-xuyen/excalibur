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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Managed Writer.  This is convenient when you want to dynamically
 * set and get the information from the resource.  For instance, the Resource does
 * not need to be actively monitored if all access to the resource goes through
 * this type of Resource.  It can notify the change as soon as the Writer or
 * OutputStream has been closed.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ResourceWriter.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public final class ResourceWriter
    extends FilterWriter
{
    private final StreamResource m_resource;

    /**
     * Set up the ResourceOutputStream.
     */
    public ResourceWriter( final Writer out,
                           final StreamResource resource )
    {
        super( out );
        m_resource = resource;
    }

    /**
     * Override the close method so that we can be notified when the update is
     * complete.
     */
    public final void close()
        throws IOException
    {
        super.close();
        m_resource.streamClosedEvent();
    }
}
