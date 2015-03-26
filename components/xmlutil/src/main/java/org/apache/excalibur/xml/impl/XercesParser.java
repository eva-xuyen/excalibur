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
package org.apache.excalibur.xml.impl;

import java.io.IOException;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 * 
 * @avalon.component
 * @avalon.service type=org.apache.excalibur.xml.sax.SAXParser
 * @avalon.service type=org.apache.excalibur.xml.dom.DOMParser
 * @x-avalon.info name=xerces-parser
 * @x-avalon.lifestyle type=singleton
 */
public final class XercesParser
    extends AbstractLogEnabled
    implements org.apache.excalibur.xml.sax.SAXParser, org.apache.excalibur.xml.dom.DOMParser,
                ErrorHandler, ThreadSafe, Initializable, Component
{
    public void initialize()
        throws Exception
    {
        final String message =
            "WARNING: XercesParser has been deprecated in favour of " +
            "JaxpParser. Please use JaxpParser unless it is incompatible" +
            "with your environment";
        getLogger().warn( message );
    }

    public void parse( final InputSource in,
                       final ContentHandler consumer )
        throws SAXException, IOException
    {
        if( consumer instanceof LexicalHandler )
        {
            parse( in, consumer, (LexicalHandler)consumer );
        }
        else
        {
            parse( in, consumer, null );
        }
    }

    /**
     * Parse the {@link InputSource} and send
     * SAX events to the content handler and
     * the lexical handler.
     */
    public void parse( final InputSource in,
                       final ContentHandler contentHandler,
                       final LexicalHandler lexicalHandler )
        throws SAXException, IOException
    {
        final SAXParser parser = createSAXParser();

        if( null != lexicalHandler )
        {
            parser.setProperty( "http://xml.org/sax/properties/lexical-handler",
                                lexicalHandler );
        }
        parser.setErrorHandler( this );
        parser.setContentHandler( contentHandler );
        parser.parse( in );
    }

    /**
     * Parses a new Document object from the given {@link InputSource}.
     */
    public Document parseDocument( final InputSource input )
        throws SAXException, IOException
    {
        try
        {
            final DOMParser parser = new DOMParser();
            parser.setFeature( "http://xml.org/sax/features/validation", false );
            parser.setFeature( "http://xml.org/sax/features/namespaces", true );
            parser.setFeature( "http://xml.org/sax/features/namespace-prefixes",
                               true );

            parser.parse( input );

            return parser.getDocument();
        }
        catch( final Exception e )
        {
            final String message = "Could not build DocumentBuilder";
            getLogger().error( message, e );
            return null;
        }
    }

    /**
     * Return a new {@link Document}.
     */
    public Document createDocument()
        throws SAXException
    {
        return new DocumentImpl();
    }

    /**
     * Receive notification of a recoverable error.
     */
    public void error( final SAXParseException spe )
        throws SAXException
    {
        final String message =
            "Error parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();
        throw new SAXException( message, spe );
    }

    /**
     * Receive notification of a fatal error.
     */
    public void fatalError( final SAXParseException spe )
        throws SAXException
    {
        final String message =
            "Fatal error parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();
        throw new SAXException( message, spe );
    }

    /**
     * Receive notification of a warning.
     */
    public void warning( final SAXParseException spe )
        throws SAXException
    {
        final String message =
            "Warning parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();
        throw new SAXException( message, spe );
    }

    /**
     * Utility method to create a SAXParser.
     *
     * @return new SAXParser
     * @throws SAXException if unable to create parser
     */
    private SAXParser createSAXParser()
        throws SAXException
    {
        final SAXParser parser = new SAXParser();
        parser.setFeature( "http://xml.org/sax/features/validation", false );
        parser.setFeature( "http://xml.org/sax/features/namespaces", true );
        parser.setFeature( "http://xml.org/sax/features/namespace-prefixes",
                           true );
        return parser;
    }
}
