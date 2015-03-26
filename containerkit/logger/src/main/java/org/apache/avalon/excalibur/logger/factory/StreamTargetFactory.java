/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.OutputStream;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.StreamTarget;

/**
 * TargetFactory for {@link org.apache.log.output.io.StreamTarget}.
 *
 * This factory is able to create different StreamTargets according to the following
 * configuration syntax:
 * <pre>
 * &lt;stream id="foo"&gt;
 *  &lt;stream&gt;<i>stream-context-name</i>&lt;/stream&gt;
 *  &lt;format type="<i>raw|pattern|extended</i>"&gt;<i>pattern to be used if needed</i>&lt;/format&gt;
 * &lt;/stream&gt;
 * </pre>
 *
 * <p>The "stream-context-name" is the name of an <code>java.io.OutputStream</code> that
 * is fetched in the context. This context contains two predefined streams :
 * <li>"<code>System.out</code>" for the system output stream,</li>
 * <li>"<code>System.err</code>" for the system error stream.</li>
 * </p>
 *
 * <p>The syntax of "format" is the same as in <code>FileTargetFactory</code>.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/03/10 13:54:50 $
 */
public class StreamTargetFactory
    extends AbstractTargetFactory
    implements Contextualizable
{
    /**
     * Create a LogTarget based on a Configuration
     */
    public LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        OutputStream stream;

        final Configuration streamConfig = configuration.getChild( "stream", false );
        if( null == streamConfig )
        {
            stream = System.err;
        }
        else
        {
            final String streamName = streamConfig.getValue();
            try
            {
                stream = (OutputStream)m_context.get( streamName );
            }
            catch( Exception e )
            {
                throw new ConfigurationException( "Error resolving stream '" +
                                                  streamName + "' at " +
                                                  streamConfig.getLocation(), e );
            }
        }

        final Configuration formatterConf = configuration.getChild( "format" );
        final Formatter formatter = getFormatter( formatterConf );

        return new StreamTarget( stream, formatter );
    }

    public void contextualize( final Context context )
        throws ContextException
    {
        // Add System output streams
        final DefaultContext newContext = new DefaultContext( context );

        newContext.put( "System.out", System.out );
        newContext.put( "System.err", System.err );

        super.contextualize( newContext );
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

