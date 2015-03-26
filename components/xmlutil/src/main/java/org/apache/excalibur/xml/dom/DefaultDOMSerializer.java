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
package org.apache.excalibur.xml.dom;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @avalon.component
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultDOMSerializer
    extends AbstractLogEnabled
    implements DOMSerializer
{
    private final TransformerFactory m_factory = TransformerFactory.newInstance();

    public void serialize( Document document,
                           ContentHandler contentHandler,
                           LexicalHandler lexicalHandler )
        throws SAXException
    {
        try
        {
            final Transformer transformer = m_factory.newTransformer();
            final DOMSource source = new DOMSource( document );
            final SAXResult result = new SAXResult( contentHandler );
            result.setLexicalHandler( lexicalHandler );

            transformer.transform( source, result );
        }
        catch( TransformerConfigurationException e )
        {
            getLogger().error( "Cannot create transformer", e );
            throw new SAXException( e );
        }
        catch( TransformerException e )
        {
            getLogger().error( "Cannot serialize document", e );
            throw new SAXException( e );
        }
    }
}
