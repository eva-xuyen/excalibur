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
package org.apache.avalon.excalibur.logger.decorator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.logger.util.LoggerSwitch;

/**
 *
 * This class intercepts the class passed to us via
 * <code>enableLogging()</code> and substibutes it
 * by <code>LoggerSwitch.get()</code> logger.
 * <p>
 * Later on at the <code>start()</code> stage
 * we assume that our wrapped LoggerManager has already
 * completely initialized itself and extract
 * a <code>Logger</code> from it.
 * <p>
 * <code>LoggerSwitch</code> allowes us to supply this
 * logger to it via <code>LoggerSwitch.setPreferred()</code>.
 * This has the effect of all the log messages directed
 * to <code>LoggerSwitch.get()</code> obtained logger
 * being directed to the new <code>Logger</code> unless
 * a recursion error happens.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public class LogToSelfDecorator extends LoggerManagerDecorator
{
    /* The category to switch our logging to. */
    private final String m_switchTo;
    /* The LoggerSwitch object controlling our substitute Logger. */
    private LoggerSwitch m_switch;
    /** 
     * Our substitute logger obtained from m_switch. 
     * Used for our own logging.
     */
    private Logger m_logger;

    /**
     * Creates a LogToSelfDecorator instance.
     * @param switchTo the name of the category we should extract
     *         a Logger and switch our logging to at the 
     *         <code>start()</code> stage; can not be null;
     *         empty value causes logging to be switched to the
     *         "" category.
     */
    public LogToSelfDecorator( final LoggerManager loggerManager, final String switchTo )
    {
        super( loggerManager );
        if ( switchTo == null ) throw new NullPointerException( "switchTo" );
        m_switchTo = switchTo;
    }

    /**
     * Substitutes the supplied logger by <code>m_switch.get()</code>.
     * The substiting logger is used both for our own logging and
     * passed onto our decorated <code>LoggerManager</code>.
     * @param logger the logger supplied for us and our wrapped
     *        LoggerManager; we chould survive with a null logger
     *        (LoggerSwitch create a NullLogger in this case), but 
     *        for better error detection we shall rather blow up.
     */
    public void enableLogging( final Logger logger )
    {
        if ( m_switch != null )
        {
            throw new IllegalStateException( "enableLogging() already called" );
        }

        if ( logger == null )
        {
            throw new NullPointerException( "logger" );
        }

        m_switch = new LoggerSwitch( logger );
        m_logger = m_switch.get();
        ContainerUtil.enableLogging( m_loggerManager, m_logger );
    }

    /**
     * Invokes <code>start()</code> on our wrapped
     * <code>LoggerManager</code> and swithces the
     * logger used by us and all objects that we
     * decorate for a logger extracted from our
     * wrapped <code>LoggerManager</code>.
     */
    public void start() throws Exception
    {
        /** 
         * If our LoggerManager is <code>Startable</code>
         * its <code>start()</code> will be invoked now.
         */    
        super.start();

        final Logger preferred = m_loggerManager.getLoggerForCategory( m_switchTo );
        if ( m_logger.isDebugEnabled() )
        {
            /**
             * We have to identify ourselves now via 'LogToSelfDecorator:'
             * because we are likely to be logging to a shared bootstrap
             * logger, not to a dedicated category Logger.
             */
            final String message = "LogToSelfDecorator: switching logging to '" + 
                    m_switchTo + "'";
            m_logger.debug( message );
        }
        
        m_switch.setPreferred( preferred );

        if ( m_logger.isDebugEnabled() )
        {
            /**
             * We do not have to identify ourselves now, we're already logging
             * to a proper category.
             */
            final String message = "Have switched logging to '" + 
                    m_switchTo + "'";
            m_logger.debug( message );
        }
    }
}
