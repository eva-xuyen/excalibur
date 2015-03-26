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
package org.apache.avalon.excalibur.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.logger.util.LoggerSwitch;
import org.apache.avalon.excalibur.logger.util.LoggerUtil;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 *
 * This abstract class implements LogEnabled.
 * A derived class is expected to obtain a logger via
 * <code>getLogger()</code> and live with it.
 * The <code>Logger</code> supplied via <code>enableLogging</code>
 * will be used both as the "initial" and as the "fallback" logger.
 * <ul><li>
 * "initial" means that until a call to 
 * <code>start()</code> the messages logger via
 * <code>getLogger().xxx()</code> will go to this logger</li><li>
 * "fallback" means that if after a successfull <code>start</code>
 * a recursive invocation of <code>getLogger().xxx()</code> will be detected
 * the message will be logged via the initial logger as a fallback.</li></ul>
 * See {@link org.apache.avalon.excalibur.logger.util.LoggerSwitch} for
 * more details.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/10 13:54:49 $
 * @since 4.0
 */
public abstract class AbstractLoggerManager
    implements LogEnabled, LoggerManager
{
    /** 
     * Map for name to logger mapping.
     * This instance variable is protected (not privated)
     * so that it may be pre-filled at configuration stage.
     */
    final protected Map m_loggers = new HashMap();

    /** The root logger to configure */
    protected String m_prefix;

    /** 
     * The object that wraps a swithing logger.
     * The switching logger itself for security reasons
     * has no methods of controlling it, but its wrapping
     * object has.
     */
    private LoggerSwitch m_switch;

    /** Always equals to m_switch.get() */
    private Logger m_logger;

    /** 
     * category we should switch our own loggin to 
     * on <code>start()</code>.
     */
    private String m_switchTo;

    /** safeguards against double <code>enableLogging()</code> invocation. */
    private boolean m_enableLoggingInvoked = false;

    /** safeguards against double <code>start()</code> invocation. */
    private boolean m_startInvoked = false;

    /** 
     * The logger used to be returned from <code>getDefaultLogger()</code>
     * and <code>getLoggerForCategory("")</code>,
     * if one has been forcibly set via a constructor. 
     */
    final private Logger m_defaultLoggerOverride;

    /**
     * Derived LoggerManager implementations should obtain
     * a logger to log their own messages via this call.
     * It is also safe to log messages about logging failures
     * via this logger as it safeguards internally gainst
     * recursion.
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Initializes AbstractLoggerManager.
     * @param prefix the prefix to prepended to the category name
     *         on each invocation of getLoggerForCategory before
     *         passing the category name on to the underlying logging
     *         system (currently LogKit or Log4J).
     * @param switchTo fuel for the <code>start()</code> method; 
     *         if null <code>start()</code> will do nothing; 
     *         if empty <code>start()</code> will switch to
     *         <code>getLoggerForCategory("")</code>.
     */
    public AbstractLoggerManager( final String prefix, final String switchTo, 
            Logger defaultLoggerOverride )
    {
        m_prefix = prefix;
        m_switchTo = switchTo;

        m_switch = new LoggerSwitch( null, null );
        m_logger = m_switch.get();

        m_defaultLoggerOverride = defaultLoggerOverride;
    }

    /**
     * Accept the logger we shall use as the initial and the fallback logger.
     */
    public void enableLogging( final Logger fallbackLogger )
    {
        if ( m_enableLoggingInvoked )
        {
            throw new IllegalStateException( "enableLogging() already called" );
        }
        m_switch.setFallback( fallbackLogger );
        m_enableLoggingInvoked = true;
    }

    /**
     * Get a logger from ourselves and pass it to <code>m_switch</code>.
     */
    public void start()
    {
        if ( m_startInvoked )
        {
            throw new IllegalStateException( "start() already invoked" );
        }

        if ( m_switchTo != null )
        {
            if ( m_logger.isDebugEnabled() )
            {
                final String message = "LoggerManager: switching logging to " + 
                        "this.getLoggerForCategory('" +
                        LoggerUtil.getFullCategoryName( m_prefix, m_switchTo) + "').";
                m_logger.debug( message );
            }

            final Logger ourOwn = this.getLoggerForCategory( m_switchTo );
        
            if ( ourOwn == null )
            {
                throw new NullPointerException( "ourOwn" );
            }
            
            m_switch.setPreferred( ourOwn );

            if ( m_logger.isDebugEnabled() )
            {
                final String message = "LoggerManager: have switched logging to " + 
                        "this.getLoggerForCategory('" +
                        LoggerUtil.getFullCategoryName( m_prefix, m_switchTo) + "').";
                m_logger.debug( message );
            }
        }
        else
        {
            if ( m_logger.isDebugEnabled() )
            {
                final String message = "LoggerManager: switchTo is null, " +
                        "no switch of our own logging.";
                m_logger.debug( message );
            }
        }
        m_startInvoked = true;
    }

    /** Startable.stop() empty implementation. */
    public void stop(){}

    /**
     * Retruns the logger for the <code>""</code> category.
     */
    public final Logger getDefaultLogger()
    {
        return getLoggerForCategory( null );
    }

    /**
     * Actually create a logger wrapping underlying logger
     * backed implementation for a give category. Bypasses the caching.
     * Derived LoggerManager implementations should provide an implementation
     * of this method.
     */
    protected abstract Logger doGetLoggerForCategory( final String fullCategoryName );

    /**
     * Retrieves a Logger from a category name. Usually
     * the category name refers to a configuration attribute name.  If
     * this LogKitManager does not have the match the default Logger will
     * be returned and a warning is issued.
     */
    public final Logger getLoggerForCategory( final String categoryName )
    {
        if ( m_defaultLoggerOverride != null &&
                ( categoryName == null || categoryName.length() == 0 ) )
        {
            return m_defaultLoggerOverride;
        }

        final String fullCategoryName = 
                LoggerUtil.getFullCategoryName( m_prefix, categoryName );

        final Logger logger;
        final Logger newLogger;

        synchronized( m_loggers )
        {
            logger = (Logger)m_loggers.get( fullCategoryName );
            
            if ( logger == null )
            {
                newLogger = doGetLoggerForCategory( fullCategoryName );
                m_loggers.put( fullCategoryName, newLogger );
            }
            else
            {
                /* Let's have no "variable might not have been initialized". */
                newLogger = null;
            }
        }

        if( null != logger )
        {
            if( m_logger.isDebugEnabled() )
            {
                m_logger.debug( "Logger for category " + fullCategoryName + " returned" );
            }
            return logger;
        }

        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Logger for category " + fullCategoryName + " not defined in "
                            + "configuration. New Logger created and returned" );
        }

        return newLogger;
    }
}
