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

import org.apache.avalon.framework.logger.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Log4JLoggerManager implementation.  This is the interface used to get instances of
 * a Logger for your system.  This manager does not set up the categories--it
 * leaves that as an excercise for Log4J's construction.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.21 $ $Date: 2004/05/04 13:08:00 $
 * @since 4.1
 */
public class Log4JLoggerManager extends AbstractLoggerManager
    implements LoggerManager
{
    /** The hierarchy private to Log4JManager */
    private final LoggerRepository m_hierarchy;

    /**
     * Creates a new <code>DefaultLog4JManager</code>. It will use a new <code>Hierarchy</code>.
     */
    public Log4JLoggerManager()
    {
        this( LogManager.getLoggerRepository() );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> with an existing <code>Hierarchy</code>.
     */
    public Log4JLoggerManager( final LoggerRepository hierarchy )
    {
        this( (String) null, hierarchy, (String) null, (Logger) null, (Logger) null );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix )
    {
        this( prefix, (LoggerRepository) null, (String) null, (Logger) null, (Logger) null );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> with an existing <code>Hierarchy</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix,
                               final LoggerRepository hierarchy )
    {
        this( prefix, hierarchy, (String) null, (Logger) null, (Logger) null );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix, final String switchToCategory )
    {
        this( prefix, (LoggerRepository) null, switchToCategory, (Logger) null, (Logger) null );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> with an existing <code>Hierarchy</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix,
                               final LoggerRepository hierarchy,
                               final String switchToCategory )
    {
        this( prefix, hierarchy, switchToCategory, (Logger) null, (Logger) null );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> with an existing <code>Hierarchy</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix,
                               final LoggerRepository hierarchy,
                               final Logger defaultLogger )
    {
        this( prefix, hierarchy, (String) null, defaultLogger, defaultLogger );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code> with an existing <code>Hierarchy</code> using
     * specified logger name as root logger.
     */
    public Log4JLoggerManager( final String prefix,
                               final LoggerRepository hierarchy,
                               final Logger defaultLogger,
                               final Logger logger )
    {
        this( prefix, hierarchy, (String) null, defaultLogger, logger );
    }

    /**
     * Creates a new <code>DefaultLog4JManager</code>.
     * @param prefix to prepend to every category name on 
     *         <code>getLoggerForCategory()</code>
     * @param hierarchy a Log4J LoggerRepository to run with
     * @param switchToCategory if this parameter is not null
     *         after <code>start()</code>
     *         <code>LogKitLoggerManager</code> will start
     *         to log its own debug and error messages to
     *         a logger obtained via
     *         <code>this.getLoggerForCategory( switchToCategory )</code>.
     *         Note that prefix will be prepended to
     *         the value of <code>switchToCategory</code> also.
     * @param defaultLogger the logger to override the default
     *         logger configured by Log4J; probably should be
     *         null to allow users set up whatever logger they
     *         like as the root logger via Log4J configuration
     * @param logger the logger to log our own initialization
     *         messages (currently we have none) and to log
     *         errors (currently this functionality is not used
     *         either)
     */
    public Log4JLoggerManager( final String prefix,
                               final LoggerRepository hierarchy,
                               final String switchToCategory,
                               final Logger defaultLogger,
                               final Logger logger )
    {
        super( prefix, switchToCategory, defaultLogger );

        if ( hierarchy == null )
        {
            // is this an analog of new Hierarchy() or an
            // analog of Hierarchy.getDefaultHierarchy()?
            // we should have an analog of new Hierarchy() here
            // I guess - Anton Tagunov
            m_hierarchy = LogManager.getLoggerRepository();
        }
        else
        {
            m_hierarchy = hierarchy;
        }

        if ( logger != null )
        {
            this.enableLogging( logger );
        }
    }

    /* Actaully create the Logger */
    protected Logger doGetLoggerForCategory( final String fullCategoryName )
    {
        return new Log4JLogger( m_hierarchy.getLogger( fullCategoryName ) );
    }
}
