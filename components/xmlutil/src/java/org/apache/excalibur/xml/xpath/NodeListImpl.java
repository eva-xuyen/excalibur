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

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Implementation of the <code>NodeList</code> interface.<P>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: NodeListImpl.java,v 1.4 2004/02/28 11:47:15 cziegeler Exp $
*/
public class NodeListImpl implements NodeList {

    private Node[] nodelist;

    /**
     * Construct a NodeList by copying.
     */
    public NodeListImpl(NodeList list) {
      if (list == null || list.getLength() == 0) {
        nodelist = null;
      } else {
        nodelist = new Node[list.getLength()];
        for(int i = 0; i < list.getLength(); i++) {
          nodelist[i] = list.item(i).cloneNode(true);
        }
      }
    }

    /**
     * Constructor
     */
    public NodeListImpl(Node[] nodes) {
        this.nodelist = nodes;
    }

    /**
     * Constructor
     */
    public NodeListImpl() {}

    /**
     * Construct a NodeList by copying.
     */
    public NodeListImpl(DocumentFragment fragment, String rootName) {
      if (fragment != null) {
        Element root = fragment.getOwnerDocument().createElementNS(null, rootName);
        Node    current;
        while (fragment.hasChildNodes() == true) {
          current = fragment.getFirstChild();
          fragment.removeChild(current);
          root.appendChild(current);
        }
        nodelist = new Node[1];
        nodelist[0] = root;
      }
    }

    /**
     * Add a node to list
     */
    public void addNode(Node node) {
        if (this.nodelist == null) {
            this.nodelist = new Node[1];
            this.nodelist[0] = node;
        } else {
            Node[] copy = new Node[this.nodelist.length+1];
            System.arraycopy(this.nodelist, 0, copy, 0, this.nodelist.length);
            copy[copy.length-1] = node;
            this.nodelist = copy;
        }
    }

    /**
     *  Returns the <code>index</code> th item in the collection. If
     * <code>index</code> is greater than or equal to the number of nodes in
     * the list, this returns <code>null</code> .
     * @param index  Index into the collection.
     * @return  The node at the <code>index</code> th position in the
     *   <code>NodeList</code> , or <code>null</code> if that is not a valid
     *   index.
     */
    public Node item(int index) {
      if (nodelist == null || index >= nodelist.length) {
        return null;
      } else {
        return nodelist[index];
      }
    }

    /**
     *  The number of nodes in the list. The range of valid child node indices
     * is 0 to <code>length-1</code> inclusive.
     */
    public int getLength() {
      return (nodelist == null ? 0 : nodelist.length);
    }

}
