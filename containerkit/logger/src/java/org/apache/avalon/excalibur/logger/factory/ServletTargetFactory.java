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
package org.apache.avalon.excalibur.logger.factory;

import javax.servlet.ServletContext;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.ServletOutputLogTarget;

/**
 * ServletTargetFactory class.
 *
 * This factory creates a ServletOutputLogTargets. It uses the
 * context-key attribute to locate the required ServletContext from
 * the Context object passed to this factory.  The default context-key
 * is <code>servlet-context</code>.
 *
 * <pre>
 *
 * &lt;servlet id="target-id" context-key="context-key-to-servlet-context-object"&gt;
 *  &lt;format type="raw|pattern|extended"&gt;pattern to be used if needed&lt;/format&gt;
 * &lt;/servlet&gt;
 *
 * </pre>
 * <dl>
 *  <dt>&lt;format&gt;</dt>
 *  <dd>
 *   The type attribute of the pattern element denotes the type of
 *   Formatter to be used and according to it the pattern to use for.
 *   This elements defaults to:
 *   <p>
 *    %7.7{priority} %5.5{time}   [%8.8{category}] (%{context}): %{message}\\n%{throwable}
 *   </p>
 *  </dd>
 * </dl>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public final class ServletTargetFactory
    extends AbstractTargetFactory
{

    /**
     * create a LogTarget based on a Configuration
     */
    public final LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        if( m_context == null )
        {
            throw new ConfigurationException( "Context not available." );
        }
        final String contextkey =
            m_configuration.getAttribute( "context-key", "servlet-context" );
        final ServletContext sctx;

        final Configuration confFormat = configuration.getChild( "format" );
        final Formatter formatter = getFormatter( confFormat );

        try
        {
            sctx = (ServletContext)m_context.get( contextkey );
        }
        catch( final ContextException ce )
        {
            throw new ConfigurationException( "Cannot find ServletContext object in " +
                                              "application context", ce );
        }

        return new ServletOutputLogTarget( sctx, formatter );
    }

    protected Formatter getFormatter( final Configuration conf )
    {
        Formatter formatter = null;

        if( null != conf )
        {
            final FormatterFactory formatterFactory = new FormatterFactory();
            formatter = formatterFactory.createFormatter( conf );
        }

        return formatter;
    }
}
