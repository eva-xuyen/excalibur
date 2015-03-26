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
package org.apache.excalibur.xml.impl;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.xml.EntityResolver;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.sax.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * An XMLParser that is only dependant on JAXP 1.1 compliant parsers.
 *
 * The configuration can contain the following parameters :
 * <ul>
 * <li>validate (boolean, default = <code>false</code>) : should the parser
 *     validate parsed documents ?
 * </li>
 * <li>namespace-prefixes (boolean, default = <code>false</code>) : do we want
 *     namespaces declarations also as 'xmlns:' attributes ?<br>
 *     <i>Note</i> : setting this to <code>true</code> confuses some XSL
 *     processors (e.g. Saxon).
 * </li>
 * <li>stop-on-warning (boolean, default = <code>true</code>) : should the parser
 *     stop parsing if a warning occurs ?
 * </li>
 * <li>stop-on-recoverable-error (boolean, default = <code>true</code>) : should the parser
 *     stop parsing if a recoverable error occurs ?
 * </li>
 * <li>reuse-parsers (boolean, default = <code>true</code>) : do we want to reuse
 *     parsers or create a new parser for each parse ?<br>
 *     <i>Note</i> : even if this parameter is <code>true</code>, parsers are not
 *     recycled in case of parsing errors : some parsers (e.g. Xerces) don't like
 *     to be reused after failure.
 * </li>
 * <li>sax-parser-factory (string, optional) : the name of the <code>SAXParserFactory</code>
 *     implementation class to be used instead of using the standard JAXP mechanism
 *     (<code>SAXParserFactory.newInstance()</code>). This allows to choose
 *     unambiguously the JAXP implementation to be used when several of them are
 *     available in the classpath.
 * </li>
 * <li>drop-dtd-comments : should comment() events from DTD's be dropped? Since this implementation
 * does not support the DeclHandler interface anyway, it is quite useless to only have the comments
 * from DTD. And the comment events from the internal DTD subset would appear in the serialized output
 * again.
 * </li>
 * </ul>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/28 18:56:45 $
 * @avalon.component
 */
public final class JaxpParser
    extends AbstractLogEnabled
    implements SAXParser, DOMParser, 
                Poolable, Component, Parameterizable, Serviceable, Disposable, ErrorHandler
{
    /** the SAX Parser factory */
    private SAXParserFactory m_factory;

    /** The SAX reader. It is created lazily by {@link #setupXMLReader()}
     and cleared if a parsing error occurs. */
    private XMLReader m_reader;

    /** the Entity Resolver */
    private EntityResolver m_resolver;

    /** do we want namespaces also as attributes ? */
    private boolean m_nsPrefixes;

    /** do we want to reuse parsers ? */
    private boolean m_reuseParsers;

    /** do we stop on warnings ? */
    private boolean m_stopOnWarning;

    /** do we stop on recoverable errors ? */
    private boolean m_stopOnRecoverableError;

    /** the Document Builder factory */
    private DocumentBuilderFactory m_docFactory;

    /** The DOM builder. It is created lazily by {@link #setupDocumentBuilder()}
     and cleared if a parsing error occurs. */
    private DocumentBuilder m_docBuilder;

    /** Should comments appearing between start/endDTD events be dropped ? */
    private boolean m_dropDtdComments;

    /** The serviec manager */
    private ServiceManager m_manager;
    
    /**
     * Get the Entity Resolver from the component m_manager
     *
     * @avalon.dependency type="EntityResolver" optional="true"
     */
    public void service( final ServiceManager manager )
        throws ServiceException
    {
        m_manager = manager;
        
        if( manager.hasService( EntityResolver.ROLE ) )
        {
            m_resolver = (EntityResolver)manager.lookup( EntityResolver.ROLE );
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "JaxpParser: Using EntityResolver: " + m_resolver );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() 
    {
        if ( m_manager != null )
        {
            m_manager.release( m_resolver );
            m_manager = null;
            m_resolver = null;
        }
    }

    public void parameterize( final Parameters params )
        throws ParameterException
    {
        // Validation and namespace prefixes parameters
        boolean validate = params.getParameterAsBoolean( "validate", false );
        m_nsPrefixes = params.getParameterAsBoolean( "namespace-prefixes", false );
        m_reuseParsers = params.getParameterAsBoolean( "reuse-parsers", true );
        m_stopOnWarning = params.getParameterAsBoolean( "stop-on-warning", true );
        m_stopOnRecoverableError = params.getParameterAsBoolean( "stop-on-recoverable-error", true );
        m_dropDtdComments = params.getParameterAsBoolean( "drop-dtd-comments", false );

        // Get the SAXFactory
        final String saxParserFactoryName = params.getParameter( "sax-parser-factory",
                                                                 "javax.xml.parsers.SAXParserFactory" );
        if( "javax.xml.parsers.SAXParserFactory".equals( saxParserFactoryName ) )
        {
            m_factory = SAXParserFactory.newInstance();
        }
        else
        {
            try
            {
                final Class factoryClass = loadClass( saxParserFactoryName );
                m_factory = (SAXParserFactory)factoryClass.newInstance();
            }
            catch( Exception e )
            {
                throw new ParameterException( "Cannot load SAXParserFactory class " + saxParserFactoryName, e );
            }
        }
        m_factory.setNamespaceAware( true );
        m_factory.setValidating( validate );

        // Get the DocumentFactory
        final String documentBuilderFactoryName = params.getParameter( "document-builder-factory",
                                                                       "javax.xml.parsers.DocumentBuilderFactory" );
        if( "javax.xml.parsers.DocumentBuilderFactory".equals( documentBuilderFactoryName ) )
        {
            m_docFactory = DocumentBuilderFactory.newInstance();
        }
        else
        {
            try
            {
                final Class factoryClass = loadClass( documentBuilderFactoryName );
                m_docFactory = (DocumentBuilderFactory)factoryClass.newInstance();
            }
            catch( Exception e )
            {
                throw new ParameterException( "Cannot load DocumentBuilderFactory class " + documentBuilderFactoryName, e );
            }
        }
        m_docFactory.setNamespaceAware( true );
        m_docFactory.setValidating( validate );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "JaxpParser: validating: " + validate +
                               ", namespace-prefixes: " + m_nsPrefixes +
                               ", reuse parser: " + m_reuseParsers +
                               ", stop on warning: " + m_stopOnWarning +
                               ", stop on recoverable-error: " + m_stopOnRecoverableError +
                               ", saxParserFactory: " + saxParserFactoryName +
                               ", documentBuilderFactory: " + documentBuilderFactoryName );
        }
    }

    /**
     * Load a class
     */
    private Class loadClass( String name ) throws Exception
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if( loader == null )
        {
            loader = getClass().getClassLoader();
        }
        return loader.loadClass( name );
    }

    /**
     * Parse the <code>InputSource</code> and send
     * SAX events to the consumer.
     * Attention: the consumer can  implement the
     * <code>LexicalHandler</code> as well.
     * The parse should take care of this.
     */
    public void parse( final InputSource in,
                       final ContentHandler contentHandler,
                       final LexicalHandler lexicalHandler )
        throws SAXException, IOException
    {
        setupXMLReader();

        // Ensure we will use a fresh new parser at next parse in case of failure
        XMLReader tmpReader = m_reader;
        m_reader = null;

        try
        {
            LexicalHandler theLexicalHandler = null;
            if ( null == lexicalHandler 
                 && contentHandler instanceof LexicalHandler)
            {
                theLexicalHandler = (LexicalHandler)contentHandler;
            }   
            if( null != lexicalHandler )
            {
                theLexicalHandler = lexicalHandler;
            }
            if (theLexicalHandler != null)
            {
                if (m_dropDtdComments)
                    theLexicalHandler = new DtdCommentEater(theLexicalHandler);
                tmpReader.setProperty( "http://xml.org/sax/properties/lexical-handler",
                                       theLexicalHandler );
            }
        }
        catch( final SAXException e )
        {
            final String message =
                "SAX2 driver does not support property: " +
                "'http://xml.org/sax/properties/lexical-handler'";
            getLogger().warn( message );
        }

        tmpReader.setErrorHandler( this );
        tmpReader.setContentHandler( contentHandler );
        if( null != m_resolver )
        {
            tmpReader.setEntityResolver( m_resolver );
        }

        tmpReader.parse( in );

        // Here, parsing was successful : restore reader
        if( m_reuseParsers )
        {
            m_reader = tmpReader;
        }
    }

    /**
     * Parse the {@link InputSource} and send
     * SAX events to the consumer.
     * Attention: the consumer can  implement the
     * {@link LexicalHandler} as well.
     * The parse should take care of this.
     */
    public void parse( InputSource in, ContentHandler consumer )
        throws SAXException, IOException
    {
        this.parse( in, consumer, 
                    (consumer instanceof LexicalHandler ? (LexicalHandler)consumer : null));
    }

    /**
     * Creates a new {@link XMLReader} if needed.
     */
    private void setupXMLReader()
        throws SAXException
    {
        if( null == m_reader )
        {
            // Create the XMLReader
            try
            {
                m_reader = m_factory.newSAXParser().getXMLReader();
            }
            catch( final ParserConfigurationException pce )
            {
                final String message = "Cannot produce a valid parser";
                throw new SAXException( message, pce );
            }
            
            m_reader.setFeature( "http://xml.org/sax/features/namespaces", true );
            
            if( m_nsPrefixes )
            {
                System.out.println( "NAMESPACE PREFIX!!!!!!!!!!!!!!!!!!!!!!!!!!" );
                try
                {
                    m_reader.setFeature( "http://xml.org/sax/features/namespace-prefixes",
                                         m_nsPrefixes );
                }
                catch( final SAXException se )
                {
                    final String message =
                        "SAX2 XMLReader does not support setting feature: " +
                        "'http://xml.org/sax/features/namespace-prefixes'";
                    getLogger().warn( message );
                }
            }
        }
    }

    /**
     * Parses a new Document object from the given InputSource.
     */
    public Document parseDocument( final InputSource input )
        throws SAXException, IOException
    {
        setupDocumentBuilder();

        // Ensure we will use a fresh new parser at next parse in case of failure
        DocumentBuilder tmpBuilder = m_docBuilder;
        m_docBuilder = null;

        if( null != m_resolver )
        {
            tmpBuilder.setEntityResolver( m_resolver );
        }

        Document result = tmpBuilder.parse( input );

        // Here, parsing was successful : restore builder
        if( m_reuseParsers )
        {
            m_docBuilder = tmpBuilder;
        }

        return result;
    }

    /**
     * Creates a new {@link DocumentBuilder} if needed.
     */
    private void setupDocumentBuilder()
        throws SAXException
    {
        if( null == m_docBuilder )
        {
            try
            {
                m_docBuilder = m_docFactory.newDocumentBuilder();
            }
            catch( final ParserConfigurationException pce )
            {
                final String message = "Could not create DocumentBuilder";
                throw new SAXException( message, pce );
            }
        }
    }

    /**
     * Return a new {@link Document}.
     */
    public Document createDocument()
        throws SAXException
    {
        setupDocumentBuilder();
        return m_docBuilder.newDocument();
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
        if( m_stopOnRecoverableError )
        {
            throw new SAXException( message, spe );
        }
        getLogger().error( message, spe );
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

        if( m_stopOnWarning )
        {
            throw new SAXException( message, spe );
        }
        getLogger().warn( message, spe );
    }

    /**
     * A LexicalHandler implementation that strips all comment events between
     * startDTD and endDTD. In all other cases the events are forwarded to another
     * LexicalHandler.
     */
    private static class DtdCommentEater implements LexicalHandler
    {
        private LexicalHandler next;
        private boolean inDTD;

        public DtdCommentEater(LexicalHandler nextHandler)
        {
            this.next = nextHandler;
        }

        public void startDTD (String name, String publicId, String systemId)
            throws SAXException
        {
            inDTD = true;
            next.startDTD(name, publicId, systemId);
        }

        public void endDTD ()
            throws SAXException
        {
            inDTD = false;
            next.endDTD();
        }

        public void startEntity (String name)
            throws SAXException
        {
            next.startEntity(name);
        }

        public void endEntity (String name)
            throws SAXException
        {
            next.endEntity(name);
        }

        public void startCDATA ()
            throws SAXException
        {
            next.startCDATA();
        }

        public void endCDATA ()
            throws SAXException
        {
            next.endCDATA();
        }

        public void comment (char ch[], int start, int length)
            throws SAXException
        {
            if (!inDTD)
                next.comment(ch, start, length);
        }
    }

}
