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
package org.apache.avalon.framework.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

/**
 * This class has a bunch of utility methods to work
 * with configuration objects.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ConfigurationUtil.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.1.4
 */
public class ConfigurationUtil
{
    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationUtil()
    {
    }

    /**
     * Convert a DOM Element tree into a configuration tree.
     *
     * @param element the DOM Element
     * @return the configuration object
     */
    public static Configuration toConfiguration( final Element element )
    {
        final DefaultConfiguration configuration =
            new DefaultConfiguration( element.getNodeName(), "dom-created" );
        final NamedNodeMap attributes = element.getAttributes();
        final int length = attributes.getLength();
        for( int i = 0; i < length; i++ )
        {
            final Node node = attributes.item( i );
            final String name = node.getNodeName();
            final String value = node.getNodeValue();
            configuration.setAttribute( name, value );
        }

        boolean flag = false;
        String content = "";
        final NodeList nodes = element.getChildNodes();
        final int count = nodes.getLength();
        for( int i = 0; i < count; i++ )
        {
            final Node node = nodes.item( i );
            if( node instanceof Element )
            {
                final Configuration child = toConfiguration( (Element)node );
                configuration.addChild( child );
            }
            else if( node instanceof CharacterData )
            {
                final CharacterData data = (CharacterData)node;
                content += data.getData();
                flag = true;
            }
        }

        if( flag )
        {
            configuration.setValue( content );
        }

        return configuration;
    }

    /**
     * Convert a configuration tree into a DOM Element tree.
     *
     * @param configuration the configuration object
     * @return the DOM Element
     */
    public static Element toElement( final Configuration configuration )
    {
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();

            return createElement( document, configuration );
        }
        catch( final ParserConfigurationException pce )
        {
            throw new IllegalStateException( pce.toString() );
        }
    }

    /**
     * Serialize the configuration object to a String.  If an exception
     * occurs, the exception message will be returned instead.  This method is
     * intended to aid debugging; {@link
     * DefaultConfigurationSerializer#serialize(Configuration)} lets the caller
     * handle exceptions.
     *
     * @param configuration Configuration instance to serialize
     * @return a non-null String representing the <code>Configuration</code>,
     * or an error message.
     * @since 12 March, 2003
     */
    public static String toString( final Configuration configuration )
    {
        DefaultConfigurationSerializer ser = new DefaultConfigurationSerializer();
        try
        {
            return ser.serialize( configuration );
        }
        catch( Exception e )
        {
            return e.getMessage();
        }
    }

    /**
     * Test to see if two Configuration's can be considered the same. Name, value, attributes
     * and children are test. The <b>order</b> of children is not taken into consideration
     * for equality.
     *
     * @param c1 Configuration to test
     * @param c2 Configuration to test
     * @return true if the configurations can be considered equals
     */
    public static boolean equals( final Configuration c1, final Configuration c2 )
    {
        return c1.getName().equals( c2.getName() ) && areValuesEqual( c1, c2 ) &&
            areAttributesEqual( c1, c2 ) && areChildrenEqual( c1, c2 );
    }

    /**
     * Return true if the children of both configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the children of both configurations are equal.
     */
    private static boolean areChildrenEqual( final Configuration c1,
                                             final Configuration c2 )
    {
        final Configuration[] kids1 = c1.getChildren();
        final ArrayList kids2 = new ArrayList( Arrays.asList( c2.getChildren() ) );
        if( kids1.length != kids2.size() )
        {
            return false;
        }

        for( int i = 0; i < kids1.length; i++ )
        {
            if( !findMatchingChild( kids1[ i ], kids2 ) )
            {
                return false;
            }
        }

        return kids2.isEmpty() ? true : false;
    }

    /**
     * Return true if find a matching child and remove child from list.
     *
     * @param c the configuration
     * @param matchAgainst the list of items to match against
     * @return true if the found.
     */
    private static boolean findMatchingChild( final Configuration c,
                                              final ArrayList matchAgainst )
    {
        final Iterator i = matchAgainst.iterator();
        while( i.hasNext() )
        {
            if( equals( c, (Configuration)i.next() ) )
            {
                i.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Return true if the attributes of both configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the attributes of both configurations are equal.
     */
    private static boolean areAttributesEqual( final Configuration c1,
                                               final Configuration c2 )
    {
        final String[] names1 = c1.getAttributeNames();
        final String[] names2 = c2.getAttributeNames();
        if( names1.length != names2.length )
        {
            return false;
        }

        for( int i = 0; i < names1.length; i++ )
        {
            final String name = names1[ i ];
            final String value1 = c1.getAttribute( name, null );
            final String value2 = c2.getAttribute( name, null );
            if( !value1.equals( value2 ) )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if the values of two configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the values of two configurations are equal.
     */
    private static boolean areValuesEqual( final Configuration c1,
                                           final Configuration c2 )
    {
        final String value1 = c1.getValue( null );
        final String value2 = c2.getValue( null );
        return ( value1 == null && value2 == null ) ||
            ( value1 != null && value1.equals( value2 ) );
    }

    /**
     * Create an DOM {@link Element} from a {@link Configuration}
     * object.
     *
     * @param document the DOM document
     * @param configuration the configuration to convert
     * @return the DOM Element
     */
    private static Element createElement( final Document document,
                                          final Configuration configuration )
    {
        final Element element = document.createElement( configuration.getName() );

        final String content = configuration.getValue( null );
        if( null != content )
        {
            final Text child = document.createTextNode( content );
            element.appendChild( child );
        }

        final String[] names = configuration.getAttributeNames();
        for( int i = 0; i < names.length; i++ )
        {
            final String name = names[ i ];
            final String value = configuration.getAttribute( name, null );
            element.setAttribute( name, value );
        }
        final Configuration[] children = configuration.getChildren();
        for( int i = 0; i < children.length; i++ )
        {
            final Element child = createElement( document, children[ i ] );
            element.appendChild( child );
        }
        return element;
    }
}
