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
package org.apache.excalibur.xml.sax;

import java.util.Enumeration;

import org.xml.sax.AttributeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class is an utility class adapting a SAX version 2.0
 * {@link ContentHandler} to receive SAX version 1.0 events.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 */

public class ContentHandlerAdapter
    implements DocumentHandler
{
    private final static String XMLNS = "xmlns";
    private final static String XMLNS_PREFIX = "xmlns:";

    private final ContentHandler m_handler;
    private final NamespaceSupport m_support = new NamespaceSupport();

    public ContentHandlerAdapter( final ContentHandler handler )
    {
        m_handler = handler;
    }

    public void setDocumentLocator( final Locator locator )
    {
        m_handler.setDocumentLocator( locator );
    }

    public void startDocument() throws SAXException
    {
        m_handler.startDocument();
    }

    public void endDocument() throws SAXException
    {
        m_handler.endDocument();
    }

    public void characters( final char ch[],
                            final int start,
                            final int length ) throws SAXException
    {
        m_handler.characters( ch, start, length );
    }

    public void ignorableWhitespace( final char ch[],
                                     final int start,
                                     final int length ) throws SAXException
    {
        m_handler.ignorableWhitespace( ch, start, length );
    }

    public void processingInstruction( final String target,
                                       final String data ) throws SAXException
    {
        m_handler.processingInstruction( target, data );
    }

    public void startElement( final String name,
                              final AttributeList atts ) throws SAXException
    {
        m_support.pushContext();

        for( int i = 0; i < atts.getLength(); i++ )
        {
            final String attributeName = atts.getName( i );
            if( attributeName.startsWith( XMLNS_PREFIX ) )
            {
                m_support.declarePrefix( attributeName.substring( 6 ), atts.getValue( i ) );
            }
            else if( attributeName.equals( XMLNS ) )
            {
                m_support.declarePrefix( "", atts.getValue( i ) );
            }
        }

        final AttributesImpl attributes = new AttributesImpl();
        for( int i = 0; i < atts.getLength(); i++ )
        {
            final String attributeName = atts.getName( i );
            if( !attributeName.startsWith( XMLNS_PREFIX ) && !attributeName.equals( XMLNS ) )
            {
                final String[] parts = m_support.processName( attributeName, new String[ 3 ], true );
                attributes.addAttribute( parts[ 0 ], parts[ 1 ], parts[ 2 ], atts.getType( i ), atts.getValue( i ) );
            }
        }

        final Enumeration e = m_support.getDeclaredPrefixes();
        while( e.hasMoreElements() )
        {
            final String prefix = (String)e.nextElement();
            m_handler.startPrefixMapping( prefix, m_support.getURI( prefix ) );
        }

        final String[] parts = m_support.processName( name, new String[ 3 ], false );
        m_handler.startElement( parts[ 0 ], parts[ 1 ], parts[ 2 ], attributes );
    }

    public void endElement( final String name ) throws SAXException
    {
        final String[] parts = m_support.processName( name, new String[ 3 ], false );
        m_handler.endElement( parts[ 0 ], parts[ 1 ], parts[ 2 ] );

        final Enumeration e = m_support.getDeclaredPrefixes();
        while( e.hasMoreElements() )
        {
            final String prefix = (String)e.nextElement();
            m_handler.endPrefixMapping( prefix );
        }

        m_support.popContext();
    }
}
