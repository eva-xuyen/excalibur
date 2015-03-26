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


import org.w3c.dom.*;

/**
 * This is a simple XPath helper class. It uses a faster approach
 * for simple XPath expressions and can create XPaths.
 * If you know that your XPath expression is simple, you should use this
 * helper instead.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: XPathUtil.java,v 1.4 2004/02/28 11:47:15 cziegeler Exp $
*/
public final class XPathUtil {

    /**
     * Return the <CODE>Node</CODE> from the DOM Node <CODE>rootNode</CODE>
     * using the XPath expression <CODE>path</CODE>.
     * If the node does not exist, it is created and then returned.
     * This is a very simple method for creating new nodes. If the
     * XPath contains selectors ([,,,]) or "*" it is of course not
     * possible to create the new node. So if you use such XPaths
     * the node must exist beforehand.
     * An simple exception is if the expression contains attribute
     * tests to values (e.g. [@id = 'du' and @number = 'you'],
     * the attributes with the given values are added. The attributes
     * must be separated with 'and'.
     * Another problem are namespaces: XPath requires sometimes selectors for
     * namespaces, e.g. : /*[namespace-uri()="uri" and local-name()="name"]
     * Creating such a node with a namespace is not possible right now as we use
     * a very simple XPath parser which is not able to parse all kinds of selectors
     * correctly.
     *
     * @param processor The XPathProcessor
     * @param rootNode The node to start the search.
     * @param path     XPath expression for searching the node.
     * @return         The node specified by the path.
     * @throws XPathException If no path is specified or the XPath engine fails.
     */
    public static Node getSingleNode(XPathProcessor processor,
                                     Node rootNode,
                                     String path)
    throws XPathException {
        // Now we have to parse the string
        // First test:  path? rootNode?
        if (path == null) {
            throw new XPathException("XPath is required.");
        }
        if (rootNode == null) return rootNode;

        if (path.length() == 0 || path.equals("/") == true) return rootNode;

        // now the first "quick" test is if the node exists using the
        // full XPathAPI
        Node testNode = searchSingleNode(processor, rootNode, path);
        if (testNode != null) return testNode;

        if (path.startsWith("/") == true) path = path.substring(1); // remove leading "/"
        if (path.endsWith("/") == true) { // remove ending "/" for root node
            path = path.substring(0, path.length() - 1);
        }

        // now step through the nodes!
        Node parent = rootNode;
        int pos;
        int posSelector;
        do {
            pos = path.indexOf("/"); // get next separator
            posSelector = path.indexOf("[");
            if (posSelector != -1 && posSelector < pos) {
                posSelector = path.indexOf("]");
                pos = path.indexOf("/", posSelector);
            }

            String  nodeName;
            boolean isAttribute = false;
            if (pos != -1) { // found separator
                nodeName = path.substring(0, pos); // string until "/"
                path = path.substring(pos+1); // rest of string after "/"
            } else {
                nodeName = path;
            }

            // test for attribute spec
            if (nodeName.startsWith("@") == true) {
                isAttribute = true;
            }

            Node singleNode = searchSingleNode(processor, parent, nodeName);

            // create node if necessary
            if (singleNode == null) {
                Node newNode;
                // delete XPath selectors
                int posSelect = nodeName.indexOf("[");
                String XPathExp = null;
                if (posSelect != -1) {
                    XPathExp = nodeName.substring(posSelect+1, nodeName.length()-1);
                    nodeName = nodeName.substring(0, posSelect);
                }
                if (isAttribute == true) {
                    try {
                        newNode = rootNode.getOwnerDocument().createAttributeNS(null, nodeName.substring(1));
                    } catch (DOMException local) {
                        throw new XPathException("Unable to create new DOM node: '"+nodeName+"'.", local);
                    }
                } else {
                    try {
                        newNode = rootNode.getOwnerDocument().createElementNS(null, nodeName);
                    } catch (DOMException local) {
                        throw new XPathException("Unable to create new DOM node: '"+nodeName+"'.", local);
                    }
                    if (XPathExp != null) {
                        java.util.List attrValuePairs = new java.util.ArrayList(4);
                        boolean noError = true;

                        String attr;
                        String value;
                        // scan for attributes
                        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(XPathExp, "= ");
                        while (tokenizer.hasMoreTokens() == true) {
                            attr = tokenizer.nextToken();
                            if (attr.startsWith("@") == true) {
                                if (tokenizer.hasMoreTokens() == true) {
                                    value = tokenizer.nextToken();
                                    if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length()-1);
                                    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
                                    attrValuePairs.add(attr.substring(1));
                                    attrValuePairs.add(value);
                                } else {
                                    noError = false;
                                }
                            } else if (attr.trim().equals("and") == false) {
                                noError = false;
                            }
                        }
                        if (noError == true) {
                            for(int l=0;l<attrValuePairs.size();l=l+2) {
                                ((Element)newNode).setAttributeNS(null, (String)attrValuePairs.get(l),
                                                                (String)attrValuePairs.get(l+1));
                            }
                        }
                    }
                }
                parent.appendChild(newNode);
                parent = newNode;
            } else {
                parent = singleNode;
            }
        } while (pos != -1);
        return parent;
    }

    /**
     * Use an XPath string to select a single node. XPath namespace
     * prefixes are resolved from the context node, which may not
     * be what you want ({@link #getSingleNode(XPathProcessor, Node ,String )}).
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @return The first node found that matches the XPath, or null.
     *
     */
    public static Node searchSingleNode(XPathProcessor processor,
                                        Node contextNode,
                                        String str) {
        String[] pathComponents = buildPathArray(str);
        if (pathComponents == null) {
            return processor.selectSingleNode(contextNode, str);
        } else {
            return getFirstNodeFromPath(contextNode, pathComponents, false);
        }
    }

    /**
     *  Use an XPath string to select a nodelist.
     *  XPath namespace prefixes are resolved from the contextNode.
     *
     *  @param contextNode The node to start searching from.
     *  @param str A valid XPath string.
     *  @return A NodeList, should never be null.
     *
     */
    public static NodeList searchNodeList(XPathProcessor processor,
                                          Node contextNode,
                                          String str) {
        String[] pathComponents = buildPathArray(str);
        if (pathComponents == null) {
            return processor.selectNodeList(contextNode, str);
        } else {
            return getNodeListFromPath(contextNode, pathComponents);
        }
    }

    /**
     * Build the input for the get...FromPath methods. If the XPath
     * expression cannot be handled by the methods, <code>null</code>
     * is returned.
     */
    public static String[] buildPathArray(String xpath) {
        String[] result = null;
        if (xpath != null && xpath.charAt(0) != '/') {
            // test
            int components = 1;
            int i, l;
            l = xpath.length();
            boolean found = false;
            i = 0;
            while (i < l && found == false) {
                switch (xpath.charAt(i)) {
                    case '[' : found = true; break;
                    case '(' : found = true; break;
                    case '*' : found = true; break;
                    case '@' : found = true; break;
                    case ':' : found = true; break;
                    case '/' : components++;
                    default: i++;
                }
            }
            if (found == false) {
                result = new String[components];
                if (components == 1) {
                    result[components-1] = xpath;
                } else {
                    i = 0;
                    int start = 0;
                    components = 0;
                    while (i < l) {
                        if (xpath.charAt(i) == '/') {
                            result[components] = xpath.substring(start, i);
                            start = i+1;
                            components++;
                        }
                        i++;
                    }
                    result[components] = xpath.substring(start);
                }
            }
        }
        return result;
    }

    /**
     * Use a path to select the first occurence of a node. The namespace
     * of a node is ignored!
     * @param contextNode The node starting the search.
     * @param path        The path to search the node. The
     *                    contextNode is searched for a child named path[0],
     *                    this node is searched for a child named path[1]...
     * @param create      If a child with the corresponding name is not found
     *                    and create is set, this node will be created.
    */
    public static Node getFirstNodeFromPath(Node contextNode,
                                            final String[] path,
                                            final boolean  create) {
        if (contextNode == null || path == null || path.length == 0)
            return contextNode;
        // first test if the node exists
        Node item = getFirstNodeFromPath(contextNode, path, 0);
        if (item == null && create == true) {
            int i = 0;
            NodeList childs;
            boolean found;
            int m, l;
            while (contextNode != null && i < path.length) {
                childs = contextNode.getChildNodes();
                found = false;
                if (childs != null) {
                    m = 0;
                    l = childs.getLength();
                    while (found == false && m < l) {
                        item = childs.item(m);
                        if (item.getNodeType() == Node.ELEMENT_NODE
                            && item.getLocalName().equals(path[i]) == true) {
                            found = true;
                            contextNode = item;
                        }
                        m++;
                    }
                }
                if (found == false) {
                    Element e = contextNode.getOwnerDocument().createElementNS(null, path[i]);
                    contextNode.appendChild(e);
                    contextNode = e;
                }
                i++;
            }
            item = contextNode;
        }
        return item;
    }

    /**
     * Private helper method for getFirstNodeFromPath()
     */
    private static Node getFirstNodeFromPath(final Node contextNode,
                                             final String[] path,
                                             final int      startIndex) {
        int i = 0;
        NodeList childs;
        boolean found;
        int l;
        Node item = null;

        childs = contextNode.getChildNodes();
        found = false;
        if (childs != null) {
            i = 0;
            l = childs.getLength();
            while (found == false && i < l) {
                item = childs.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE
                    && path[startIndex].equals(item.getLocalName()!=null?item.getLocalName():item.getNodeName())) {
                    if (startIndex == path.length-1) {
                        found = true;
                    } else {
                        item = getFirstNodeFromPath(item, path, startIndex+1);
                        if (item != null) found = true;
                    }
                }
                if (found == false) {
                    i++;
                }
            }
            if (found == false) {
                item = null;
            }
        }
        return item;
    }

    /**
     * Use a path to select all occurences of a node. The namespace
     * of a node is ignored!
     * @param contextNode The node starting the search.
     * @param path        The path to search the node. The
     *                    contextNode is searched for a child named path[0],
     *                    this node is searched for a child named path[1]...
     */
    public static NodeList getNodeListFromPath(Node contextNode,
                                               String[] path) {
        if (contextNode == null) return new NodeListImpl();
        if (path == null || path.length == 0) {
            return new NodeListImpl(new Node[] {contextNode});
        }
        NodeListImpl result = new NodeListImpl();
        try {
            getNodesFromPath(result, contextNode, path, 0);
        } catch (NullPointerException npe) {
            // this NPE is thrown because the parser is not configured
            // to use DOM Level 2
            throw new NullPointerException("XMLUtil.getNodeListFromPath() did catch a NullPointerException."+
                          "This might be due to a missconfigured XML parser which does not use DOM Level 2."+
                          "Make sure that you use the XML parser shipped with Cocoon.");
        }
        return result;
    }

    /**
     * Helper method for getNodeListFromPath()
     */
    private static void getNodesFromPath(final NodeListImpl result,
                                         final Node contextNode,
                                         final String[] path,
                                         final int startIndex) {
        final NodeList childs = contextNode.getChildNodes();
        int m, l;
        Node item;
        if (startIndex == (path.length-1)) {
            if (childs != null) {
                m = 0;
                l = childs.getLength();
                while (m < l) {
                    item = childs.item(m);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        // Work around for DOM Level 1
                        if (path[startIndex].equals(item.getLocalName()!=null?item.getLocalName():item.getNodeName()) == true) {
                            result.addNode(item);
                        }
                    }
                    m++;
                }
            }
        } else {
            if (childs != null) {
                m = 0;
                l = childs.getLength();
                while (m < l) {
                    item = childs.item(m);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        // Work around for DOM Level 1
                        if (path[startIndex].equals(item.getLocalName()!=null?item.getLocalName():item.getNodeName()) == true) {
                            getNodesFromPath(result, item, path, startIndex+1);
                        }
                    }
                    m++;
                }
            }
        }
    }

    /**
     * Get the value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node does not exist <CODE>null</CODE>
     * is returned.
     *
     * @param root The node to start the search.
     * @param path XPath search expression.
     * @return     The value of the node or <CODE>null</CODE>
     */
    public static String getValueOf(XPathProcessor processor, 
                                      Node root, String path)
    throws XPathException {
        if (path == null) {
            throw new XPathException("Not a valid XPath: " + path);
        }
        if (root == null) return null;
        if (path.startsWith("/") == true) path = path.substring(1); // remove leading "/"
        if (path.endsWith("/") == true) { // remove ending "/" for root node
            path = path.substring(0, path.length() - 1);
        }

        Node node = searchSingleNode(processor, root, path);
        if (node != null) {
            return getValueOfNode(processor, node);
        }
        return null;
    }

    /**
     * Get the value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node is not found
     * the <CODE>defaultValue</CODE> is returned.
     *
     * @param root The node to start the search.
     * @param path XPath search expression.
     * @param defaultValue The default value if the node does not exist.
     * @return     The value of the node or <CODE>defaultValue</CODE>
     */
    public static String getValueOf(XPathProcessor processor,
                                     Node root,
                                     String path,
                                     String defaultValue)
    throws XPathException {
        String value = getValueOf(processor, root, path);
        if (value == null) value = defaultValue;

        return value;
    }

    /**
     * Get the boolean value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node exists and has a value
     * this value is converted to a boolean, e.g. "true" or "false" as value
     * will result into the corresponding boolean values.
     *
     * @param root The node to start the search.
     * @param path XPath search expression.
     * @return     The boolean value of the node.
     * @throws XPathException If the node is not found.
     */
    public static boolean getValueAsBooleanOf(XPathProcessor processor,
                                                Node root, 
                                                String path)
    throws XPathException {
        String value = getValueOf(processor, root, path);
        if (value == null) {
            throw new XPathException("No such node: " + path);
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * Get the boolean value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node exists and has a value
     * this value is converted to a boolean, e.g. "true" or "false" as value
     * will result into the corresponding boolean values.
     * If the node does not exist, the <CODE>defaultValue</CODE> is returned.
     *
     * @param root The node to start the search.
     * @param path XPath search expression.
     * @param defaultValue Default boolean value.
     * @return     The value of the node or <CODE>defaultValue</CODE>
     */
    public static boolean getValueAsBooleanOf(XPathProcessor processor, 
                                                Node root,
                                                String path,
                                                boolean defaultValue)
    throws XPathException {
        String value = getValueOf(processor, root, path);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * Get the value of the DOM node.
     * The value of a node is the content of the first text node.
     * If the node has no text nodes, <code>null</code> is returned.
     */
    public static String getValueOfNode(XPathProcessor processor,
                                         Node node) {
        if (node == null) return null;
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return node.getNodeValue();
        } else {
            String value = null;
            node.normalize();
            NodeList childs = node.getChildNodes();
            int i, l;
            i = 0;
            l = childs.getLength();
            while (i < l && value == null) {
                if (childs.item(i).getNodeType() == Node.TEXT_NODE)
                    value = childs.item(i).getNodeValue().trim();
                else
                    i++;
            }
            return value;
        }
    }

    /**
     * Get the value of the node.
     * The value of the node is the content of the first text node.
     * If the node has no text nodes the <CODE>defaultValue</CODE> is
     * returned.
     */
    public static String getValueOfNode(XPathProcessor processor,
                                         Node node, String defaultValue) {
        String value = getValueOfNode(processor, node);
        if (value == null) value = defaultValue;
        return value;
    }


}
