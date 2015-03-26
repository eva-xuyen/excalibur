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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class is an utility class proxying a SAX version 2.0
 * {@link ContentHandler} and {@link LexicalHandler} and forwarding it those
 * events received throug its {@link XMLConsumer}s interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 */
public class XMLConsumerProxy
    extends ContentHandlerProxy implements XMLConsumer
{
    /** The {@link LexicalHandler} */
    private LexicalHandler m_lexicalHandler;

    /**
     * Create a new <code>XMLConsumerProxy</code> instance.
     */
    public XMLConsumerProxy( final ContentHandler contentHandler, final LexicalHandler lexicalHandler )
    {
        super( contentHandler );
        m_lexicalHandler = lexicalHandler;
    }

    /**
     * Create a new <code>XMLConsumerProxy</code> instance.
     * If you use this constructor, you have to set the content handler
     * by calling {@link #setContentHandler(ContentHandler)} and the
     * lexical handler by calling {@link #setLexicalHandler(LexicalHandler)}. 
     * Otherwise you get an NPE during streaming.
     */
    public XMLConsumerProxy()
    {       
    }
        
    /**
     * Create a new <code>XMLConsumerProxy</code> instance.
     */
    public XMLConsumerProxy( final XMLConsumer xmlConsumer )
    {
        this( xmlConsumer, xmlConsumer );
    }

    /**
     * Set the lexical handler
     */
    public void setLexicalHandler( final LexicalHandler lexicalHandler ) 
    {
        m_lexicalHandler = lexicalHandler;
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD( final String name,
                          final String publicId,
                          final String systemId )
        throws SAXException
    {
        m_lexicalHandler.startDTD( name, publicId, systemId );
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
        throws SAXException
    {
        m_lexicalHandler.endDTD();
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity( final String name )
        throws SAXException
    {
        m_lexicalHandler.startEntity( name );
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity( final String name )
        throws SAXException
    {
        m_lexicalHandler.endEntity( name );
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
        throws SAXException
    {
        m_lexicalHandler.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
        throws SAXException
    {
        m_lexicalHandler.endCDATA();
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment( final char[] ch,
                         final int start,
                         final int len )
        throws SAXException
    {
        m_lexicalHandler.comment( ch, start, len );
    }
}
