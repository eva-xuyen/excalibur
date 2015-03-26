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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.format.SyslogFormatter;
import org.apache.log.format.XMLFormatter;

/**
 * Factory for Formatters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class FormatterFactory
{
    //Format of default formatter
    private static final String FORMAT =
        "%7.7{priority} %5.5{time}   [%8.8{category}] (%{context}): %{message}\\n%{throwable}";

    public Formatter createFormatter( final Configuration conf )
    {
        final String type = conf.getAttribute( "type", "pattern" );
        final String format = conf.getValue( FORMAT );
        if( "avalon".equals( type ) )
        {
            final int depth = conf.getAttributeAsInteger( "depth", AvalonFormatter.DEFAULT_STACK_DEPTH );
            final boolean printCascading = conf.getAttributeAsBoolean( "cascading", AvalonFormatter.DEFAULT_PRINT_CASCADING );
            return new AvalonFormatter( format, depth, printCascading );
        }
        else if( "extended".equals( type ) )
        {
            /*Normally ExtendPatternFormatter would look for callers
             *of Logger.class.  But when Excalibur Logger provides a
             *facade, the user class/method is actually one-level deeper.
             *We therefore create the pattern-formatter with an
             *additional depth-offset of 1.
             */
            return new ExtendedPatternFormatter( format, 1 );
        }
        else if( "raw".equals( type ) )
        {
            return new RawFormatter();
        }
        else if( "xml".equals( type ) )
        {
            return new XMLFormatter();
        }
        else if( "syslog".equals( type ) )
        {
            return new SyslogFormatter();
        }
        else if( "pattern".equals( type ) )
        {
            return new PatternFormatter( format );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown formatter type " + type );
        }
    }
}
