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
package org.apache.excalibur.xmlizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Converter for transforming any input stream with a given mime-type
 * into SAX events.
 * This component acts like a selector. All XMLizer can "register"
 * themselfes for a given mime-type and this component forwards
 * the transformation to the registered on.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 */
public final class DefaultXMLizer extends AbstractLogEnabled
        implements XMLizer, Serviceable, Configurable, ThreadSafe, Component
{
    /** The service manager */
    private ServiceManager m_serviceManager;
    /** Mapping between mime-type and role for a SAXParser handling the mime-type */
    private Map m_mimeTypes = new HashMap();

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service( ServiceManager serviceManager ) throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure( Configuration configuration ) throws ConfigurationException
    {
        final Configuration[] parsers = configuration.getChildren("parser");
        for ( int i = 0; i < parsers.length; i++ )
        {
            final Configuration parser = parsers[i];
            final String mimeType = parser.getAttribute("mime-type");
            final String role = parser.getAttribute("role");
            m_mimeTypes.put(mimeType, role);
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug("XMLizer: Registering parser '"+role+"' for mime-type '"+mimeType+"'.");
            }
        }
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug("XMLizer: Default parser is '"+SAXParser.ROLE+"'.");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.xmlizer.XMLizer#toSAX(java.io.InputStream, java.lang.String, java.lang.String, org.xml.sax.ContentHandler)
     */
    public void toSAX( final InputStream stream,
                       final String mimeType,
                       final String systemID,
                       final ContentHandler handler )
            throws SAXException, IOException
    {
        if ( null == stream )
        {
            throw new NullPointerException( "stream" );
        }
        if ( null == handler )
        {
            throw new NullPointerException( "handler" );
        }

        final String parserRole;
        if ( m_mimeTypes.containsKey(mimeType) )
        {
            parserRole = (String) m_mimeTypes.get(mimeType);
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                final String message = "No mime-type for xmlizing " + systemID +
                        ", guessing text/xml";
                getLogger().debug( message );
            }
            parserRole = SAXParser.ROLE;
        }

        SAXParser parser = null;
        try
        {
            parser = (SAXParser) m_serviceManager.lookup( parserRole );

            final InputSource inputSource = new InputSource( stream );
            inputSource.setSystemId( systemID );
            parser.parse( inputSource, handler, null );
        }
        catch ( ServiceException e )
        {
            throw new SAXException( "Cannot parse content of type " + mimeType, e );
        }
        finally 
        {
            m_serviceManager.release(parser);        
        }
    }
}

