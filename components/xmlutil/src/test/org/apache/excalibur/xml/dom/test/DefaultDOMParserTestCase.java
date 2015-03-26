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
package org.apache.excalibur.xml.dom.test;

import java.io.StringReader;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultDOMParserTestCase extends ExcaliburTestCase
{
    
    private static final String CONTENT = 
        "<?xml version=\"1.0\"?>" + 
        "<test:root xmlns:test=\"http://localhost/test\">" +
            "<test:element1/>" +
            "<test:element2/>" +
        "</test:root>";
    private static final StringReader IN = new StringReader( CONTENT );
    
    public DefaultDOMParserTestCase( String name )
    {
        super( name );
    }
    
    public void testCreateDOMHandler()
    {
        try 
        {
            final DOMParser parser = (DOMParser)manager.lookup( DOMParser.ROLE );

            final Document document = parser.parseDocument( new InputSource( IN ) );            
            
            final Element root = document.getDocumentElement();
            assertEquals( "Wrong root element", "test:root", root.getNodeName() );
            assertEquals( "Wrong namespace uri", "http://localhost/test", root.getNamespaceURI() );
            
            final Node element1 = root.getFirstChild();
            assertEquals( "Child is not an element", Node.ELEMENT_NODE, element1.getNodeType() );
            assertEquals( "Wrong first element", "test:element1", element1.getNodeName() );
            
            final Node element2 = root.getLastChild();
            assertEquals( "Child is not an element", Node.ELEMENT_NODE, element2.getNodeType() );
            assertEquals( "Wrong last element", "test:element2", element2.getNodeName() );                        
        }
        catch ( ComponentException e )
        {
            fail( "Failed to lookup components: " + e.getMessage() );
        }
        catch ( Exception e )
        {
            fail( "Failed to create handler: " + e.getMessage() );
        }
    }
    
}
