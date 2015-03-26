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
package org.apache.avalon.framework.configuration.test;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Test the basic public methods of SAXConfigurationHandlerTestCase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class SAXConfigurationHandlerTestCase extends TestCase
{
    public SAXConfigurationHandlerTestCase()
    {
        this("SAXConfigurationHandler Test Case ");
    }

    public SAXConfigurationHandlerTestCase( final String name )
    {
        super( name );
    }

    /**
     * Test the ContentHandler.  The XML created should look like this:
     *
     * <pre>
     *   &lt;rawName attqName="attValue"&gt;
     *     &lt;child:localName xmlns:child="namespaceURI"&gt;value&lt;/child:localName&gt;
     *     &lt;emptyElement/&gt;
     *   &lt;/rawName&gt;
     * </pre>
     */
    public void testDefaultHandling() throws Exception
    {
        SAXConfigurationHandler handler = new SAXConfigurationHandler( );

        final String rootURI = "";
        final String rootlocal = "rawName";
        final String rootraw = "rawName";
        final String childURI = "namespaceURI";
        final String childlocal = "localName";
        final String childraw = "child:" + childlocal;
        final String childvalue = "value";
        final String attqName = "attqName";
        final String attValue = "attValue";
        final String emptylocal = "emptyElement";
        final String emptyraw = emptylocal;
        
        final AttributesImpl emptyAttributes  = new AttributesImpl();

        final AttributesImpl attributes  = new AttributesImpl();
        attributes.addAttribute("",attqName,attqName,
                                "CDATA",attValue);

        final AttributesImpl childAttributes  = new AttributesImpl();
        childAttributes.addAttribute("", "child", "xmlns:child", "CDATA", childURI);

        handler.startDocument();
        handler.startPrefixMapping( "child", childURI );
        handler.startElement( rootURI, rootlocal, rootraw, attributes );
        handler.startElement( childURI,
                                childlocal,
                                childraw,
                                childAttributes );

        handler.characters( childvalue.toCharArray(), 0, childvalue.length() );
        handler.endElement( childURI, childlocal, childraw );
        handler.startElement( rootURI, emptylocal, emptyraw, emptyAttributes );
        handler.endElement( rootURI, emptylocal, emptyraw );
        handler.endElement( null, null, rootraw);
        handler.endPrefixMapping( "child" );
        handler.endDocument();

        final Configuration configuration = handler.getConfiguration();
        assertEquals( attValue, configuration.getAttribute(attqName));
        assertEquals( childvalue, configuration.getChild(childraw).getValue());
        assertEquals( "", configuration.getChild(childraw).getNamespace() );
        assertEquals( rootraw, configuration.getName());
        assertEquals( "test", configuration.getChild(emptyraw).getValue( "test" ) );
    }

    public void testNamespaceHandling() throws Exception
    {
        SAXConfigurationHandler handler = new NamespacedSAXConfigurationHandler( );

        final String rootURI = "";
        final String rootlocal = "rawName";
        final String rootraw = "rawName";
        final String childURI = "namespaceURI";
        final String childlocal = "localName";
        final String childraw = "child:" + childlocal;
        final String childvalue = "value";
        final String attqName = "attqName";
        final String attValue = "attValue";

        final AttributesImpl attributes  = new AttributesImpl();
        attributes.addAttribute("",attqName,attqName,
                                "CDATA",attValue);

        final AttributesImpl childAttributes  = new AttributesImpl();
        childAttributes.addAttribute("", "child", "xmlns:child", "CDATA", childURI);

        handler.startDocument();
        handler.startPrefixMapping( "child", childURI );
        handler.startElement( rootURI, rootlocal, rootraw, attributes );
        handler.startElement( childURI,
                                childlocal,
                                childraw,
                                childAttributes );

        handler.characters( childvalue.toCharArray(), 0, childvalue.length() );
        handler.endElement( childURI, childlocal, childraw );
        handler.endElement( null, null, rootraw);
        handler.endPrefixMapping( "child" );
        handler.endDocument();

        final Configuration configuration = handler.getConfiguration();
        assertEquals( attValue, configuration.getAttribute(attqName));
        assertEquals( childvalue, configuration.getChild(childlocal).getValue());
        assertEquals( childURI, configuration.getChild(childlocal).getNamespace() );
        assertEquals( rootraw, configuration.getName());
    }
}





