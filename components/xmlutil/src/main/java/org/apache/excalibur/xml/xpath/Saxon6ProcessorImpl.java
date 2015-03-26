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

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerFactory;

import org.apache.avalon.framework.thread.ThreadSafe;

import com.icl.saxon.Context;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.tinytree.TinyBuilder;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class defines the implementation of the {@link XPathProcessor}
 * component. This implementation depends on Saxon 6.X XSLT processor.
 * This implementation was tested with Saxon 6.5.2 release.
 *
 * To configure it, add the following lines in the
 * <file>cocoon.xconf</file> file:
 *
 * <pre>
 * &lt;xpath-processor class="org.apache.cocoon.components.xpath.Saxon6ProcessorImpl"&gt;
 * &lt;/xpath-processor&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:15 $ $Author: cziegeler $
 */
public class Saxon6ProcessorImpl
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
            Value value = evaluate(contextNode, str, resolver);
            if (value == null)
            {
                return false;
            }

            return value.asBoolean();
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
            Value value = evaluate(contextNode, str, resolver);
            if (value == null)
            {
                return null;
            }

            return new Double(value.asNumber());
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
            Value value = evaluate(contextNode, str, resolver);
            if (value == null)
            {
                return null;
            }

            return value.asString();
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
            Value value = evaluate(contextNode, str, resolver);
            if (value == null || value.getDataType() != Value.NODESET)
            {
                return null;
            }

            return (Node)((NodeSetValue)value).getFirst();
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
            Value value = evaluate(contextNode, str, resolver);
            if (value.getDataType() != Value.NODESET)
            {
                return null;
            }

            NodeSetValue nodeset = (NodeSetValue)value;
            NodeEnumeration enumeration = nodeset.enumerate();
            Node[] nodes = new Node[nodeset.getCount()];
            for (int i = 0; i < nodes.length; i++)
            {
                nodes[i] = (Node)enumeration.nextElement();
            }

            return new NodeListImpl(nodes);
        } catch (final Exception e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to evaluate '" + str + "'", e);
            }

            // ignore it
            return null;
        }
    }

    private Value evaluate(Node contextNode, String str, PrefixResolver resolver)
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

            DocumentInfo doc = ((NodeInfo)contextNode).getDocumentRoot();
            NamePool pool = doc.getNamePool();
            if (pool == null)
            {
                pool = NamePool.getDefaultNamePool();
                doc.setNamePool(pool);
            }
            Expression expression = Expression.make(str, new Saxon6Context(pool, resolver));

            Context context = new Context();
            context.setContextNode((NodeInfo)contextNode);
            context.setPosition(1);
            context.setLast(1);

            return expression.evaluate(context);
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

    private class Saxon6Context extends StandaloneContext
    {
        private final PrefixResolver resolver;

        public Saxon6Context(NamePool namePool, PrefixResolver resolver)
        {
            super(namePool);
            this.resolver = resolver;
        }

        public String getURIForPrefix(String prefix) throws XPathException
        {
            return resolver.prefixToNamespace(prefix);
        }
    }
}
