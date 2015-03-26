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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class is an utility class proxying a SAX version 2.0
 * {@link ContentHandler} and forwarding the events to it.
 * <br>
 * If you are interested in lexical events as well, use the
 * {@link XMLConsumerProxy} instead.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 */
public class ContentHandlerProxy
    implements ContentHandler
{
    /** The current {@link ContentHandler}. */
    private ContentHandler m_contentHandler;

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerProxy( final ContentHandler contentHandler )
    {
        m_contentHandler = contentHandler;
    }

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     * If you use this constructor, you have to set the content handler
     * by calling {@link #setContentHandler(ContentHandler)}. Otherwise
     * you get an NPE during streaming
     */
    public ContentHandlerProxy(  )
    {
    }

    /**
     * Set the lexical handler
     */
    public void setContentHandler( final ContentHandler contentHandler ) 
    {
        m_contentHandler = contentHandler;
    }
    
    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator( final Locator locator )
    {
        m_contentHandler.setDocumentLocator( locator );
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
        throws SAXException
    {
        m_contentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
        throws SAXException
    {
        m_contentHandler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping( final String prefix,
                                    final String uri )
        throws SAXException
    {
        m_contentHandler.startPrefixMapping( prefix, uri );
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping( final String prefix )
        throws SAXException
    {
        m_contentHandler.endPrefixMapping( prefix );
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement( final String uri,
                              final String loc,
                              final String raw,
                              final Attributes a )
        throws SAXException
    {
        m_contentHandler.startElement( uri, loc, raw, a );
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement( final String uri,
                            final String loc,
                            final String raw )
        throws SAXException
    {
        m_contentHandler.endElement( uri, loc, raw );
    }

    /**
     * Receive notification of character data.
     */
    public void characters( final char[] ch,
                            final int start,
                            final int len )
        throws SAXException
    {
        m_contentHandler.characters( ch, start, len );
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace( final char[] ch,
                                     final int start,
                                     final int len )
        throws SAXException
    {
        m_contentHandler.ignorableWhitespace( ch, start, len );
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction( final String target,
                                       final String data )
        throws SAXException
    {
        m_contentHandler.processingInstruction( target, data );
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity( final String name )
        throws SAXException
    {
        m_contentHandler.skippedEntity( name );
    }
}
