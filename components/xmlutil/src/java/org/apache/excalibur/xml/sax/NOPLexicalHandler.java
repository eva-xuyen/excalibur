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
package org.apache.excalibur.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class provides default implementation of the methods specified
 * by the <code>LexicalHandler</code> interface.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:20 $
 */
public class NOPLexicalHandler
    implements LexicalHandler
{
    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD( final String name,
                          final String publicId,
                          final String systemId )
        throws SAXException
    {
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
        throws SAXException
    {
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity( final String name )
        throws SAXException
    {
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity( final String name )
        throws SAXException
    {
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
        throws SAXException
    {
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
        throws SAXException
    {
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment( final char[] ch,
                         final int start,
                         final int len )
        throws SAXException
    {
    }
}
