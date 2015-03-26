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

import javax.xml.transform.TransformerException;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;

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
 * &lt;xpath-processor class="org.apache.cocoon.components.xpath.XPathProcessorImpl"&gt;
 * &lt;/xpath-processor&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:15 $ $Author: cziegeler $
 */
public final class XPathProcessorImpl
        extends AbstractProcessorImpl
        implements XPathProcessor, Configurable, Component, ThreadSafe
{
    private String m_baseURI;

    public void configure( Configuration configuration ) throws ConfigurationException
    {
        super.configure(configuration);
        final Configuration namespaceMappings = configuration.getChild( "namespace-mappings", true );
        m_baseURI = namespaceMappings.getAttribute( "base-uri", null );
    }

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
            final XObject result = XPathAPI.eval( contextNode, str, new XalanResolver(resolver, m_baseURI) );
            return result.bool();
        }
        catch( final TransformerException e )
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
            final XObject result = XPathAPI.eval( contextNode, str, new XalanResolver(resolver, m_baseURI) );
            return new Double( result.num() );
        }
        catch( final TransformerException e )
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
            final XObject result = XPathAPI.eval( contextNode, str, new XalanResolver(resolver, m_baseURI) );
            return result.str();
        }
        catch( final TransformerException e )
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
            final XObject result = XPathAPI.eval( contextNode, str, new XalanResolver(resolver, m_baseURI) );
            return result.nodeset().nextNode();
        }
        catch( final TransformerException e )
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
            final XObject result = XPathAPI.eval( contextNode, str, new XalanResolver(resolver, m_baseURI) );
            return result.nodelist();
        }
        catch( final TransformerException e )
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return new EmptyNodeList();
        }
    }

    /**
     * A Xalan-specific wrapper for the PrefixResolver.
     */
    private final static class XalanResolver implements org.apache.xml.utils.PrefixResolver {
        private final PrefixResolver m_resolver;
        private final String m_baseURI;
        
        public XalanResolver(PrefixResolver resolver, String baseURI) {
            m_resolver = resolver;
            m_baseURI = baseURI;
        }

        public String getNamespaceForPrefix(String prefix)
        {
            return m_resolver.prefixToNamespace(prefix);
        }

        public String getNamespaceForPrefix(String prefix, Node context)
        {
            return m_resolver.prefixToNamespace(prefix);
        }

        public String getBaseIdentifier()
        {
            return m_baseURI;
        }

        public boolean handlesNullPrefixes()
        {
            return false;
        }
    }
}
