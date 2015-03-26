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
package org.apache.excalibur.xml.xpath.test;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringReader;

/**
 * XPath test case.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:30 $
 */
public class XPathTestCase extends ExcaliburTestCase
{
    /** A small test document. */
    static final String CONTENT1 =
        "<?xml version=\"1.0\"?>" +
        "<test:root xmlns:test=\"http://localhost/test\">" +
            "<test:element1/>" +
            "<test:element2/>" +
        "</test:root>";

    /** Second test document, has a different namespace than {@link #CONTENT1}. */
    static final String CONTENT2 =
        "<?xml version=\"1.0\"?>" +
        "<test:root xmlns:test=\"http://localhost/test2\">" +
            "<test:element1/>" +
            "<test:element2/>" +
        "</test:root>";

    public XPathTestCase(String name) {
        super(name);
    }

    public void testXPath() throws Exception {
        DOMParser parser = null;
        XPathProcessor processor = null;
        try {
            parser = (DOMParser)manager.lookup(DOMParser.ROLE);
            processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);

            Document document1 = parser.parseDocument(new InputSource(new StringReader(CONTENT1)));
            Document document2 = parser.parseDocument(new InputSource(new StringReader(CONTENT2)));

            // 1. Test single node expression
            String expr = "/test:root/test:element1";
            Node node = processor.selectSingleNode(document1, expr);
            assertNotNull("Must select <test:element1/> node, but got null", node);
            assertEquals("Must select <test:element1/> node", Node.ELEMENT_NODE, node.getNodeType());
            assertEquals("Must select <test:element1/> node", "element1", node.getLocalName());

            // 2. Test single node expression with no expected result
            expr = "/test:root/test:element3";
            node = processor.selectSingleNode(document1, expr);
            assertNull("Must be null", node);

            // 3. Test multiple node expression
            expr = "/test:root/test:*";
            NodeList list = processor.selectNodeList(document1, expr);
            assertNotNull("Must select two nodes, but got null", list);
            assertEquals("Must select two nodes", 2, list.getLength());
            assertEquals("Must select <test:element1/> node", "element1", list.item(0).getLocalName());
            assertEquals("Must select <test:element2/> node", "element2", list.item(1).getLocalName());

            // 4. Test with a namespace prefix configured in the component configuration
            expr = "count(/test:root/*)";
            Number number = processor.evaluateAsNumber(document1, expr);
            assertEquals(2, number.intValue());

            // 5. Test with a custom prefix resolver using a different document in a different namespace,
            // to be sure the custom prefix resolver is used
            number = processor.evaluateAsNumber(document2, expr, new PrefixResolver() {
                public String prefixToNamespace(String prefix)
                {
                    if (prefix.equals("test"))
                        return "http://localhost/test2";
                    return null;
                }
            });
            assertEquals(2, number.intValue());

            // 6. Test boolean
            expr = "count(/test:root/*) = 2";
            boolean bool = processor.evaluateAsBoolean(document1, expr);
            assertEquals(true, bool);

            // 7. Test expression in the root element context
            expr = "/test:root/test:element1";
            node = processor.selectSingleNode(document1.getDocumentElement(), expr);
            assertNotNull("Must select <test:element1/> node, but got null", node);
            assertEquals("Must select <test:element1/> node", Node.ELEMENT_NODE, node.getNodeType());
            assertEquals("Must select <test:element1/> node", "element1", node.getLocalName());

            // 8. Test expression in the child node context
            node = processor.selectSingleNode(document1.getDocumentElement().getFirstChild(), expr);
            assertNotNull("Must select <test:element1/> node, but got null", node);
            assertEquals("Must select <test:element1/> node", Node.ELEMENT_NODE, node.getNodeType());
            assertEquals("Must select <test:element1/> node", "element1", node.getLocalName());

        } finally {
            if (parser != null) {
                manager.release((Component)parser);
            }
            if (processor != null) {
                manager.release((Component)processor);
            }
        }
    }
}
