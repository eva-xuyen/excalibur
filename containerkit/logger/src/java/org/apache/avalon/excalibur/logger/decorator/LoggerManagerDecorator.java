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
package org.apache.avalon.excalibur.logger.decorator;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * This is the base class to create LoggerManager decorators.
 * It passes all lifecycle and LoggerManagerc
 * calls onto the wrapped object.
 *
 * <p> Decorators are expected to be slim - be able to run
 * for instance both with and without having their 
 * enableLogging() method called.
 *
 * <p> This constraint is imposed to allow decorators to
 * be applicable to an object both at its burth, like
 *
 * <pre>
 * C c = new C();
 * DecoratorX d = new DecoratorX( c );
 * x.enableLogging( logger );
 * </pre>
 *
 * and after the object has been completely configured
 *
 * <pre>
 * C c = (C)manager.lookup( C.ROLE );
 * DecoratorX d = new DecoratorX( c );
 * </pre>
 * 
 * If this constrianed is not obeyed this should be clearly
 * stated in the javadocs. For instance, LogToSelfDecorator
 * _only_ makes sense if it passes the <code>enableLogging</code>
 * call through it.
 *
 * <p>
 * This implementation is incomplete, 
 * it passes only those calls that are needed in
 *
 * <code>org.apache.avalon.excalibur.logger.decorator.*</code> and
 * <code>org.apache.avalon.excalibur.logger.adapter.*</code>:
 * <pre>
 *    LogEnabled
 *    Contextualizable
 *    Configurable
 *    Startable
 *    Disposable
 * </pre>
 *
 * This object differes from LoggerManagerTee by being abstract,
 * by absence of addTee() public method and by implementation.
 * LoggerManagerTee might be used instead of this but maintaining
 * it as a separate class seemed cleaner.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/03/10 13:54:50 $
 * @since 4.0
 */
public abstract class LoggerManagerDecorator implements
        LoggerManager, 
        LogEnabled, 
        Contextualizable, 
        Configurable, 
        Startable, 
        Disposable
{
    /**
     * The wrapped-in LoggerManager.
     */
    protected final LoggerManager m_loggerManager;

    public LoggerManagerDecorator( final LoggerManager loggerManager )
    {
        if ( loggerManager == null ) throw new NullPointerException( "loggerManager" );
        m_loggerManager = loggerManager;
    }

    public void enableLogging( final Logger logger )
    {
        ContainerUtil.enableLogging( m_loggerManager, logger );
    }

    public void contextualize( final Context context ) throws ContextException
    {
        ContainerUtil.contextualize( m_loggerManager, context );
    }
    
    public void configure( final Configuration configuration ) throws ConfigurationException
    {
        ContainerUtil.configure( m_loggerManager, configuration );
    }

    public void start() throws Exception
    {
        ContainerUtil.start( m_loggerManager );
    }

    public void stop() throws Exception
    {
        ContainerUtil.stop( m_loggerManager );
    }

    public void dispose()
    {
        ContainerUtil.dispose( m_loggerManager );
    }

    /**
     * Return the Logger for the specified category.
     */
    public Logger getLoggerForCategory( final String categoryName )
    {
        return m_loggerManager.getLoggerForCategory( categoryName );
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public Logger getDefaultLogger()
    {
        return m_loggerManager.getDefaultLogger();
    }
}
