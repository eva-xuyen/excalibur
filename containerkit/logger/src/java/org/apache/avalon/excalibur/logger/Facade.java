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
package org.apache.avalon.excalibur.logger;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.logger.logkit.LogKitAdapter;
import org.apache.avalon.excalibur.logger.logkit.LogKitLoggerHelper;
import org.apache.avalon.excalibur.logger.logkit.LogKitConfHelper;
import org.apache.avalon.excalibur.logger.log4j.Log4JConfAdapter;
import org.apache.avalon.excalibur.logger.decorator.LogToSelfDecorator;
import org.apache.avalon.excalibur.logger.decorator.PrefixDecorator;
import org.apache.avalon.excalibur.logger.decorator.CachingDecorator;
import org.apache.avalon.excalibur.logger.util.LoggerManagerTee;
import org.apache.log.Hierarchy;

/**
 * A facade to the modularized *LoggerManager building system.
 * Add methods here to create LoggerManagers to your preference.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */

public class Facade
{
    /**
     * Assemble a new LoggerManager running on top of LogKit
     * configured from a configuration file logging to a supplied
     * logger as a fallback.
     * Use this method as a sample showing how to assemble your
     * own LoggerManager running on top of LogKit flavour.
     */
    public static LoggerManager createLogKitConfigurable( 
            final String prefix, final String switchTo )
    {
        final org.apache.log.Hierarchy hierarchy = new Hierarchy();

        final LoggerManager bare = new LogKitAdapter( hierarchy );
        final LoggerManager decorated = applyDecorators( bare, prefix, switchTo );
        final LoggerManagerTee tee = new LoggerManagerTee( decorated );

        tee.addTee( new LogKitLoggerHelper( hierarchy ) );
        tee.addTee( new LogKitConfHelper( hierarchy ) );
        tee.makeReadOnly();

        return tee;
    }

    /**
     * Assemble LoggerManager for Log4J system configured
     * via a configuration file. All the logging errors
     * will go to System.err however.
     */
    public static LoggerManager createLog4JConfigurable(
            final String prefix, final String switchTo )
    {
        final LoggerManager bare = new Log4JConfAdapter();
        final LoggerManager decorated = applyDecorators( bare, prefix, switchTo );
        return decorated;
    }

    private static LoggerManager applyDecorators( LoggerManager target,
            final String prefix, final String switchTo )
    {
        if ( switchTo != null )
        {
            target = new LogToSelfDecorator( target, switchTo );
        }
        if ( prefix != null && prefix.length() > 0 )
        {
            target = new PrefixDecorator( target, prefix );
        }
        target = new CachingDecorator( target );
        return target;
    }
}
