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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.StreamTarget;

/**
 * ConsoleTargetFactory class.
 *
 * This factory is able to create a console target which can be used for 
 * debugging components.
 * 
 * <pre>
 * &lt;console id="console"&gt;
 *  &lt;format type="avalon|raw|pattern|extended"&gt;pattern to be used if needed&lt;/format&gt;
 * &lt;/console&gt;
 * </pre>
 *
 * <p>Some explanations about the Elements used in the configuration:</p>
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
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/16 09:07:57 $
 * @since 1.2
 */
public class ConsoleTargetFactory
    extends AbstractTargetFactory
{

    /**
     * Create a LogTarget based on a Configuration
     */
    public final LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration confFormat = configuration.getChild( "format" );
        final Formatter formatter = getFormatter( confFormat );

        final LogTarget logtarget = new StreamTarget(System.out, formatter);

        return logtarget;
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
