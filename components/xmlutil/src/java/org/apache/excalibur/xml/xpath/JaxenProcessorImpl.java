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

import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.jaxen.NamespaceContext;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class defines the implementation of the {@link XPathProcessor}
 * component.
 *
 * To configure it, add the following lines in the
 * <file>cocoon.xconf</file> file:
 *
 * <pre>
 * &lt;xpath-processor class="org.apache.cocoon.components.xpath.JaxenProcessorImpl"&gt;
 * &lt;/xpath-processor&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:15 $ $Author: cziegeler $
 */
public final class JaxenProcessorImpl
        extends AbstractProcessorImpl
        implements XPathProcessor, Component, ThreadSafe
{
    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as boolean.
     */
    public boolean evaluateAsBoolean(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            final DOMXPath path = new DOMXPath( str );
            path.setNamespaceContext( new JaxenResolver(resolver) );
            return path.booleanValueOf( contextNode );
        }
        catch( final Exception e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return false;
        }
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as number.
     */
    public Number evaluateAsNumber(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            final DOMXPath path = new DOMXPath( str );
            path.setNamespaceContext( new JaxenResolver(resolver) );
            return path.numberValueOf( contextNode );
        }
        catch( final Exception e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as string.
     */
    public String evaluateAsString(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            final DOMXPath path = new DOMXPath( str );
            path.setNamespaceContext( new JaxenResolver(resolver) );
            return path.stringValueOf( contextNode );
        }
        catch( final Exception e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    /**
     * Use an XPath string to select a single node.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return The first node found that matches the XPath, or null.
     */
    public Node selectSingleNode(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            final DOMXPath path = new DOMXPath( str );
            path.setNamespaceContext( new JaxenResolver(resolver) );
            return (Node)path.selectSingleNode( contextNode );
        }
        catch( final Exception e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    /**
     *  Use an XPath string to select a nodelist.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return A List, should never be null.
     */
    public NodeList selectNodeList(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            final DOMXPath path = new DOMXPath( str );
            path.setNamespaceContext( new JaxenResolver(resolver) );
            final List list = path.selectNodes( contextNode );
            return new SimpleNodeList( list );
        }
        catch( final Exception e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return new EmptyNodeList();
        }
    }

    /**
     * A Jaxen-specific wrapper for the PrefixResolver.
     */
    private static class JaxenResolver implements NamespaceContext
    {
        private final PrefixResolver resolver;

        public JaxenResolver(PrefixResolver resolver)
        {
            this.resolver = resolver;
        }

        public String translateNamespacePrefixToUri(String prefix)
        {
            return resolver.prefixToNamespace(prefix);
        }
    }
}
