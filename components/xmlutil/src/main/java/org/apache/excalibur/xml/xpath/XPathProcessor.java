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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the interface of the XPath processor.
 *
 * <p>All methods have two variants: one which takes a PrefixResolver as an extra
 * argument, and one which doesn't. The {@link PrefixResolver} interface allows to provide
 * your own namespace prefix resolving.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:15 $ $Author: cziegeler $
 */
public interface XPathProcessor
{
    /**
     * The role implemented by an <code>XSLTProcessor</code>.
     */
    String ROLE = XPathProcessor.class.getName();

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as boolean.
     */
    boolean evaluateAsBoolean( Node contextNode, String str );

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as number.
     */
    Number evaluateAsNumber( Node contextNode, String str );

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @return expression result as string.
     */
    String evaluateAsString( Node contextNode, String str );

    /**
     * Use an XPath string to select a single node.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @return The first node found that matches the XPath, or null.
     */
    Node selectSingleNode( Node contextNode, String str );

    /**
     *  Use an XPath string to select a nodelist.
     *
     *  @param contextNode The node to start searching from.
     *  @param str A valid XPath string.
     *  @return A List, should never be null.
     */
    NodeList selectNodeList( Node contextNode, String str );

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as boolean.
     */
    boolean evaluateAsBoolean( Node contextNode, String str, PrefixResolver resolver );

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as number.
     */
    Number evaluateAsNumber( Node contextNode, String str, PrefixResolver resolver );

    /**
     * Evaluate XPath expression within a context.
     *
     * @param contextNode The context node.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return expression result as string.
     */
    String evaluateAsString( Node contextNode, String str, PrefixResolver resolver );

    /**
     * Use an XPath string to select a single node.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return The first node found that matches the XPath, or null.
     */
    Node selectSingleNode( Node contextNode, String str, PrefixResolver resolver );

    /**
     *  Use an XPath string to select a nodelist.
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @param resolver a PrefixResolver, used for resolving namespace prefixes
     * @return A List, should never be null.
     */
    NodeList selectNodeList( Node contextNode, String str, PrefixResolver resolver );
}
