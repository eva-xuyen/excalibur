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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * This logger manager is a wrapper around all other "real" logger managers.
 * The idea is to have one single configuration file where you can
 * define, which logger manager (Log4J, LogKit etc.) you want to use, so
 * you don't have to hard-code this.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/10 13:54:49 $
 */

public final class DefaultLoggerManager
    implements LoggerManager,
    ThreadSafe,
    LogEnabled,
    Contextualizable,
    Configurable,
    Serviceable,
    Disposable
{
    /** The used LoggerManager */
    private LoggerManager m_loggermanager;

    /** The context object */
    private Context m_context;

    /** The logger used to log output from the logger manager. */
    private Logger m_logger;

    /** The prefix */
    private String m_prefix;

    /** The service manager */
    private ServiceManager m_manager;

    /** Do we have to dispose the manager */
    private boolean m_disposeManager = false;

    /**
     * Creates a new <code>DefaultLoggerManager</code>. .
     */
    public DefaultLoggerManager()
    {
    }

    /**
     * Creates a new <code>DefaultLoggerManager</code>. .
     */
    public DefaultLoggerManager( String prefix )
    {
        m_prefix = prefix;
    }

    /**
     * Provide a logger.
     *
     * @param logger the logger
     **/
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
    }

    /**
     * Return the Logger for the specified category.
     */
    public final Logger getLoggerForCategory( final String categoryName )
    {
        return m_loggermanager.getLoggerForCategory( categoryName );
    }

    /**
     * Return the default Logger.  This is basically the same
     * as getting the Logger for the "" category.
     */
    public final Logger getDefaultLogger()
    {
        return m_loggermanager.getDefaultLogger();
    }

    /**
     * Reads a context object that will be supplied to the logger manager.
     *
     * @param context The context object.
     * @throws ContextException if the context is malformed
     */
    public final void contextualize( final Context context )
        throws ContextException
    {
        m_context = context;
    }

    /**
     * Reads a configuration object and creates the category mapping.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    public final void configure( final Configuration configuration )
        throws ConfigurationException
    {
        // first we test for the class name to use
        final String className = configuration.getAttribute( "manager-class", null );

        if( null != className )
        {
            // is a prefix available?
            final String prefix = configuration.getAttribute( "prefix", m_prefix );

            // create logger manager
            try
            {
                if( null == prefix )
                {
                    m_loggermanager = (LoggerManager)Class.forName( className ).newInstance();
                }
                else
                {
                    m_loggermanager = (LoggerManager)Class.forName( className )
                        .getConstructor( new Class[]{String.class} )
                        .newInstance( new Object[]{prefix} );
                }
            }
            catch( Exception e )
            {
                throw new ConfigurationException( "Unable to create new logger manager for class " + className, e );
            }

            // now test for some lifecycle interfaces
            ContainerUtil.enableLogging(m_loggermanager, m_logger );

            try
            {
                ContainerUtil.contextualize( m_loggermanager, m_context );
            }
            catch( ContextException ce )
            {
                throw new ConfigurationException( "Unable to contextualize new logger manager.", ce );
            }

            try 
            {
                ContainerUtil.service( m_loggermanager, m_manager );
            }
            catch (ServiceException se ) 
            {            
                throw new ConfigurationException("Unable to service new logger manager.", se);
            }
            
            if( m_loggermanager instanceof Configurable )
            {
                ( (Configurable)m_loggermanager ).configure( configuration.getChildren()[ 0 ] );
            }
            else if( m_loggermanager instanceof Parameterizable )
            {
                try
                {
                    ( (Parameterizable)m_loggermanager ).parameterize( Parameters.fromConfiguration( configuration.getChildren()[ 0 ] ) );
                }
                catch( ParameterException pe )
                {
                    throw new ConfigurationException( "Unable to parameterize new logger manager.", pe );
                }
            }

            try
            {
                ContainerUtil.initialize( m_loggermanager );
            }
            catch (Exception e ) 
            {
                throw new ConfigurationException("Unable to initialize new logger manager.");
            }
        }
        else
        {
            // now test for role name
            final String roleName = configuration.getAttribute( "manager-role", null );
            if( null == roleName )
            {
                throw new ConfigurationException( "The LoggerManager needs either a manager-role or a manager-class" );
            }

            try
            {
                m_loggermanager = (LoggerManager)m_manager.lookup( roleName );
                m_disposeManager = true;
            }
            catch( ServiceException e )
            {
                throw new ConfigurationException( "Unable to lookup logger manager with role " + roleName );
            }
        }
    }

    public void service( ServiceManager manager )
        throws ServiceException
    {
        m_manager = manager;
    }

    public void dispose()
    {
        if( m_disposeManager && null != m_manager )
        {
            m_manager.release( m_loggermanager );
        }
        m_manager = null;
        m_loggermanager = null;
        m_disposeManager = false;
    }

}
