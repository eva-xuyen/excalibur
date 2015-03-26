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
package org.apache.excalibur.xml.xslt;

import javax.xml.transform.Result;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.XMLFilter;

/**
 * This is the interface of the XSLT processor.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: XSLTProcessor.java,v 1.4 2004/02/28 11:47:16 cziegeler Exp $
 * @version 1.0
 * @since   July 11, 2001
 */
public interface XSLTProcessor
    extends Component
{
    /**
     * The role implemented by an <code>XSLTProcessor</code>.
     */
    String ROLE = XSLTProcessor.class.getName();

    public static class TransformerHandlerAndValidity
    {
        private final TransformerHandler transformerHandler;
        private final SourceValidity transformerValidity;

        protected TransformerHandlerAndValidity( final TransformerHandler transformerHandler,
                                                 final SourceValidity transformerValidity )
        {
            this.transformerHandler = transformerHandler;
            this.transformerValidity = transformerValidity;
        }

        public TransformerHandler getTransfomerHandler()
        {
            return transformerHandler;
        }

        public SourceValidity getTransfomerValidity()
        {
            return transformerValidity;
        }
    }

    /**
     * Set the TransformerFactory for this instance.
     * The <code>factory</code> is invoked to return a
     * <code>TransformerHandler</code> to perform the transformation.
     *
     * @param classname the name of the class implementing
     * <code>TransformerFactory</code> value. If an error is found
     * or the indicated class doesn't implement the required interface
     * the original factory of the component is maintained.
     */
    void setTransformerFactory( String classname );

    /**
     * <p>Return a <code>TransformerHandler</code> for a given
     * stylesheet {@link Source}. This can be used in a pipeline to
     * handle the transformation of a stream of SAX events. See {@link
     * org.apache.cocoon.transformation.TraxTransformer#setConsumer} for
     * an example of how to use this method.
     *
     * <p>The additional <code>filter</code> argument, if it's not
     * <code>null</code>, is inserted in the chain SAX events as an XML
     * filter during the parsing or the source document.
     *
     * <p>This method caches the Templates object with meta information
     * (modification time and list of included stylesheets) and performs
     * a reparsing only if this changes.
     *
     * @param stylesheet a {@link Source} value
     * @param filter a {@link XMLFilter} value
     * @return a {@link TransformerHandler} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandler getTransformerHandler( Source stylesheet, XMLFilter filter )
        throws XSLTProcessorException;

    /**
     * <p>Return a {@link TransformerHandler} and
     * <code>SourceValidity</code> for a given stylesheet
     * {@link Source}. This can be used in a pipeline to
     * handle the transformation of a stream of SAX events. See {@link
     * org.apache.cocoon.transformation.TraxTransformer#setConsumer} for
     * an example of how to use this method.
     *
     * <p>The additional <code>filter</code> argument, if it's not
     * <code>null</code>, is inserted in the chain SAX events as an XML
     * filter during the parsing or the source document.
     *
     * <p>This method caches the Templates object with meta information
     * (modification time and list of included stylesheets) and performs
     * a reparsing only if this changes.
     *
     * @param stylesheet a {@link Source} value
     * @param filter a {@link XMLFilter} value
     * @return a <code>TransformerHandlerAndValidity</code> value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandlerAndValidity getTransformerHandlerAndValidity( Source stylesheet, XMLFilter filter )
        throws XSLTProcessorException;

    /**
     * Same as {@link #getTransformerHandler(Source,XMLFilter)}, with
     * <code>filter</code> set to <code>null</code>.
     *
     * @param stylesheet a {@link Source} value
     * @return a {@link TransformerHandler} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandler getTransformerHandler( Source stylesheet )
        throws XSLTProcessorException;

    /**
     * Same as {@link #getTransformerHandlerAndValidity(Source,XMLFilter)}, with
     * <code>filter</code> set to <code>null</code>.
     *
     * @param stylesheet a {@link Source} value
     * @return a {@link TransformerHandlerAndValidity} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandlerAndValidity getTransformerHandlerAndValidity( Source stylesheet )
        throws XSLTProcessorException;

    /**
     * Applies an XSLT stylesheet to an XML document. The source and
     * stylesheet documents are specified as {@link Source}
     * objects. The result of the transformation is placed in
     * {@link Result}, which should be properly initialized before
     * invoking this method. Any additional parameters passed in
     * {@link Parameters params} will become arguments to the stylesheet.
     *
     * @param source a {@link Source} value
     * @param stylesheet a {@link Source} value
     * @param params a <code>Parameters</code> value
     * @param result a <code>Result</code> value
     * @exception XSLTProcessorException if an error occurs
     */
    void transform( Source source, Source stylesheet, Parameters params, Result result )
        throws XSLTProcessorException;
}
