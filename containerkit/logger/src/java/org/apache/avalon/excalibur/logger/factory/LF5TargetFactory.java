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

import org.apache.avalon.excalibur.logger.LogTargetFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.output.lf5.LF5LogTarget;

/**
 * A factory for the <a href="http://jakarta.apache.org/log4j/docs/lf5/features.html">LogFactor5</a>
 * Swing GUI.
 * <p>
 * Configuration :
 * <pre>
 * &lt;lf5 id="target-id"&gt;
 *   &lt;NDC-format type="raw|pattern|extended"&gt;pattern to be used&lt;/NDC-format&gt;
 * &lt;/lf5&gt;
 * </pre>
 *
 * The optional "NDC-pattern" configuration defines the pattern that will be used to
 * format the log event for display on the "NDC" line in the Swing GUI.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/03/10 13:54:50 $
 */

public class LF5TargetFactory implements LogTargetFactory
{
    public LogTarget createTarget( final Configuration configuration )
        throws ConfigurationException
    {
        LF5LogTarget result = new LF5LogTarget();

        Configuration child = configuration.getChild( "NDC-pattern", false );
        if( null != child )
        {
            result.setNDCFormatter( new FormatterFactory().createFormatter( child ) );
        }

        return result;
    }
}
