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
package org.apache.excalibur.xml.dom;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.excalibur.xml.sax.ContentHandlerWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:31 $
 */
public class DefaultDOMHandler
    extends ContentHandlerWrapper
    implements DOMHandler
{
    private final DOMResult m_result;

    public DefaultDOMHandler( TransformerHandler handler,
                              Document document )
    {
        super( handler, handler );
        if ( document == null )
        {
            m_result = new DOMResult();
        }
        else
        {
            m_result = new DOMResult( document );
        }
        handler.setResult( m_result );
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.xml.dom.DOMHandler#getDocument()
     */
    public Document getDocument()
    {
        if ((m_result == null) || (m_result.getNode()==null))  {
            return null;
        } else if (m_result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
            return ( (Document)m_result.getNode() );
        } else {
            return ( m_result.getNode().getOwnerDocument() );
        }
    }
}
