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
package org.apache.avalon.framework.logger;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.log.LogEvent;
import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.util.StackIntrospector;

/**
 * This formatter extends ExtendedPatternFormatter so that
 * CascadingExceptions are formatted with all nested exceptions.
 *
 * <ul>
 * <li><code>class</code> : outputs the name of the class that has logged the
 *     message. The optional <code>short</code> subformat removes the
 *     package name. Warning : this pattern works only if formatting occurs in
 *     the same thread as the call to Logger, i.e. it won't work with
 *     <code>AsyncLogTarget</code>.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AvalonFormatter.java 506231 2007-02-12 02:36:54Z crossley $
 */
public class AvalonFormatter
    extends ExtendedPatternFormatter
{
    private static final int TYPE_CLASS = MAX_TYPE + 1;

    private static final String TYPE_CLASS_STR = "class";
    private static final String TYPE_CLASS_SHORT_STR = "short";

    /**
     * The constant defining the default stack depth when
     * none other is specified.
     *
     * @since 4.1.2
     */
    public static final int DEFAULT_STACK_DEPTH = 8;

    /**
     * The constant defining the default behaviour for printing
     * nested exceptions.
     *
     * @since 4.1.2
     */
    public static final boolean DEFAULT_PRINT_CASCADING = true;

    //The depth to which stacktraces are printed out
    private final int m_stackDepth;

    //Determines if nested exceptions should be logged
    private final boolean m_printCascading;

    /**
     * Construct the formatter with the specified pattern
     * and which which prints out exceptions to stackDepth of 8.
     *
     * @param pattern The pattern to use to format the log entries
     * @since 4.1
     */
    public AvalonFormatter( final String pattern )
    {
        this( pattern, DEFAULT_STACK_DEPTH, DEFAULT_PRINT_CASCADING );
    }

    /**
     * Construct the formatter with the specified pattern
     * and which which prints out exceptions to stackDepth specified.
     *
     * @param pattern The pattern to use to format the log entries
     * @param stackDepth The depth to which stacktraces are printed out
     * @param printCascading true enables printing of nested exceptions,
     *   false only prints out the outermost exception
     * @since 4.1.2
     */
    public AvalonFormatter( final String pattern, final int stackDepth,
                            final boolean printCascading )
    {
        super( pattern );
        m_stackDepth = stackDepth;
        m_printCascading = printCascading;
    }

    /**
     * Utility method to format stack trace.
     *
     * @param throwable the throwable instance
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getStackTrace( final Throwable throwable, final String format )
    {
        if( null == throwable )
        {
            return "";
        }
        return ExceptionUtil.printStackTrace( throwable, m_stackDepth, m_printCascading );
    }

    /**
     * Retrieve the type-id for a particular string.
     *
     * @param type the string
     * @return the type-id
     */
    protected int getTypeIdFor( final String type )
    {
        if( type.equalsIgnoreCase( TYPE_CLASS_STR ) )
        {
            return TYPE_CLASS;
        }
        else
        {
            return super.getTypeIdFor( type );
        }
    }

   /**
    * Return the result of formaltting a pattern run.
    * @param event the log event
    * @param run the patter formatter pattern run
    * @return the formatted string
    */
    protected String formatPatternRun( LogEvent event, PatternFormatter.PatternRun run )
    {
        switch( run.m_type )
        {
            case TYPE_CLASS:
                return getClass( run.m_format );
            default:
                return super.formatPatternRun( event, run );
        }
    }

    /**
     * Finds the class that has called Logger.
     */
    private String getClass( String format )
    {
        final Class clazz = StackIntrospector.getCallerClass( Logger.class );

        if( null == clazz )
        {
            return "Unknown-class";
        }
        else
        {
            // Found : the caller is the previous stack element
            String className = clazz.getName();

            // Handle optional format
            if( TYPE_CLASS_SHORT_STR.equalsIgnoreCase( format ) )
            {
                int pos = className.lastIndexOf( '.' );

                if( pos >= 0 )
                {
                    className = className.substring( pos + 1 );
                }
            }

            return className;
        }
    }
}
