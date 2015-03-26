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
package org.apache.avalon.excalibur.monitor.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.excalibur.monitor.ResourceOutputStream;
import org.apache.avalon.excalibur.monitor.ResourceWriter;
import org.apache.avalon.excalibur.monitor.StreamResource;

/**
 * The MockResource object so that we can enable the tests.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class MockResource extends StreamResource
{
    private final Mock m_mock;

    /**
     * Instantiate the FileResource
     */
    public MockResource( final String resource )
        throws Exception
    {
        this( new Mock( resource ) );
    }

    public MockResource( final Mock resource )
        throws Exception
    {
        super( resource.getName() );
        m_mock = resource;
        setPreviousModified( m_mock.lastModified() );
    }

    /**
     * Determines the last time this resource was modified
     */
    public long lastModified()
    {
        return m_mock.lastModified();
    }

    /**
     * Sets the resource value with an OutputStream
     */
    public InputStream getResourceAsStream()
        throws IOException
    {
        return new ByteArrayInputStream( m_mock.getContent().getBytes() );
    }

    /**
     * Sets the resource value with a Writer
     */
    public Reader getResourceAsReader()
        throws IOException
    {
        return new InputStreamReader( getResourceAsStream() );
    }

    /**
     * Sets the resource value with an OutputStream
     */
    public OutputStream setResourceAsStream()
        throws IOException
    {
        return new ResourceOutputStream( new MockOutputStream( m_mock ), this );
    }

    /**
     * Sets the resource value with a Writer
     */
    public Writer setResourceAsWriter()
        throws IOException
    {
        return new ResourceWriter( new OutputStreamWriter( new MockOutputStream( m_mock ) ), this );
    }
}
