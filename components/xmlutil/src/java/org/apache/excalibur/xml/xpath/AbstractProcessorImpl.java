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
package org.apache.excalibur.xml.xpath;

import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class defines base class for the implementations of the
 * {@link XPathProcessor} component.
 * Provides implementation of the {@link PrefixResolver} and common
 * implementation of five selectXXX methods.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:15 $ $Author: cziegeler $
 */
public abstract class AbstractProcessorImpl
        extends AbstractLogEnabled
        implements XPathProcessor, Configurable, PrefixResolver
{
    private final HashMap m_mappings = new HashMap();

    public void configure( Configuration configuration ) throws ConfigurationException
    {
        final Configuration namespaceMappings = configuration.getChild( "namespace-mappings", true );
        final Configuration[] namespaces = namespaceMappings.getChildren( "namespace" );
        for( int i = 0; i < namespaces.length; i++ )
        {
            final String prefix = namespaces[ i ].getAttribute( "prefix" );
            final String uri = namespaces[ i ].getAttribute( "uri" );
            m_mappings.put( prefix, uri );
        }
    }

    /**
     * Use an XPath string to select a single node. XPath namespace
     * prefixes are resolved from the context node, which may not
     * be what you want (see the next method).
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @return The first node found that matches the XPath, or null.
     */
    public Node selectSingleNode( final Node contextNode,
                                  final String str )
    {
        return selectSingleNode(contextNode, str, this);
    }

    /**
     *  Use an XPath string to select a nodelist.
     *  XPath namespace prefixes are resolved from the contextNode.
     *
     *  @param contextNode The node to start searching from.
     *  @param str A valid XPath string.
     *  @return A NodeList, should never be null.
     */
    public NodeList selectNodeList( final Node contextNode,
                                    final String str )
    {
        return selectNodeList(contextNode, str, this);
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as boolean.
     */
    public boolean evaluateAsBoolean( Node contextNode, String str )
    {
        return evaluateAsBoolean(contextNode, str, this);
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as number.
     */
    public Number evaluateAsNumber( Node contextNode, String str )
    {
        return evaluateAsNumber(contextNode, str, this);
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as string.
     */
    public String evaluateAsString( Node contextNode, String str )
    {
        return evaluateAsString(contextNode, str, this);
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as boolean.
     */
    public abstract boolean evaluateAsBoolean(Node contextNode, String str, PrefixResolver resolver);

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as number.
     */
    public abstract Number evaluateAsNumber(Node contextNode, String str, PrefixResolver resolver);

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as string.
     */
    public abstract String evaluateAsString(Node contextNode, String str, PrefixResolver resolver);

    /**
     * Use an XPath string to select a single node.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return The first node found that matches the XPath, or null.
     */
    public abstract Node selectSingleNode(Node contextNode, String str, PrefixResolver resolver);

    /**
     *  Use an XPath string to select a nodelist.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return A List, should never be null.
     */
    public abstract NodeList selectNodeList(Node contextNode, String str, PrefixResolver resolver);

    public String prefixToNamespace(String prefix)
    {
        return (String)m_mappings.get( prefix );
    }
}
