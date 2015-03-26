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
package org.apache.excalibur.xml.xpath;

import org.apache.avalon.framework.thread.ThreadSafe;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.xpath.XPathException;
import net.sf.saxon.xpath.StandaloneContext;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.tinytree.TinyBuilder;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.util.ArrayList;

/**
 * This class defines the implementation of the {@link XPathProcessor}
 * component. This implementation depends on Saxon 7.X XSLT processor.
 * This implementation was tested with Saxon 7.5 release.
 *
 * To configure it, add the following lines in the
 * <file>cocoon.xconf</file> file:
 *
 * <pre>
 * &lt;xslt-processor class="org.apache.cocoon.components.xpath.Saxon7ProcessorImpl"&gt;
 * &lt;/xslt-processor&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: Saxon7ProcessorImpl.java,v 1.6 2004/04/02 09:02:37 cziegeler Exp $
 */
public class Saxon7ProcessorImpl
        extends AbstractProcessorImpl
        implements ThreadSafe
{
    private static final TransformerFactory factory = new TransformerFactoryImpl();

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
            Item item = evaluateSingle(contextNode, str, resolver);
            if (item == null)
            {
                return false;
            }

            return Boolean.valueOf(item.getStringValue()).booleanValue();
        }
        catch (final Exception e)
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
            Item item = evaluateSingle(contextNode, str, resolver);
            if (item == null)
            {
                return null;
            }

            return Double.valueOf(item.getStringValue());
        }
        catch (final Exception e)
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
    public String evaluateAsString(Node contextNode, String str, PrefixResolver resolver) {
        try
        {
            Item item = evaluateSingle(contextNode, str, resolver);
            if (item == null)
            {
                return null;
            }

            return item.getStringValue();
        }
        catch (final Exception e)
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    public Node selectSingleNode(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            Item item = evaluateSingle(contextNode, str, resolver);

            return (Node)item;
        }
        catch (final Exception e)
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    public NodeList selectNodeList(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            SequenceIterator iterator = evaluate(contextNode, str, resolver);
            ArrayList nodes = new ArrayList();
            for (Node node = (Node)iterator.current(); node != null; node = (Node)iterator.next())
            {
                nodes.add(node);
            }

            return new NodeListImpl((Node[])nodes.toArray());
        } catch (final Exception e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    private Item evaluateSingle(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            SequenceIterator iterator = evaluate(contextNode, str, resolver);
            if (iterator == null)
            {
                return null;
            }

            return iterator.current();
        }
        catch (final Exception e)
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    private SequenceIterator evaluate(Node contextNode, String str, PrefixResolver resolver)
    {
        try
        {
            if (!(contextNode instanceof NodeInfo))
            {
                getLogger().debug("Input tree is not SAXON TinyTree, converting");
                DOMSource source = new DOMSource(contextNode);
                TinyBuilder result = new TinyBuilder();
                factory.newTransformer().transform(source, result);
                contextNode = (Node)result.getCurrentDocument();
            }

            Expression expression = ExpressionTool.make(
                    str, new Saxon7Context((NodeInfo)contextNode, resolver), 0, -1);
            XPathContext context = new XPathContext((NodeInfo)contextNode);
            return expression.iterate(context);
        }
        catch (final Exception e)
        {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }


    private class Saxon7Context extends StandaloneContext
    {
        private final PrefixResolver resolver;

        public Saxon7Context(NodeInfo node, PrefixResolver resolver)
        {
            super(node);
            this.resolver = resolver;
        }

        public String getURIForPrefix(String prefix) throws XPathException
        {
            return resolver.prefixToNamespace(prefix);
        }
    }
}
