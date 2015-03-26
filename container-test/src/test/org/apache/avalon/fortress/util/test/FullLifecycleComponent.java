/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.util.test;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
 * This test class is used to test the AbstractComponent facilities for you.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/29 17:04:15 $
 */
public final class FullLifecycleComponent
    implements LogEnabled, Contextualizable, Parameterizable, Configurable,
    Serviceable, Initializable, Startable, Suspendable, Disposable,
    ThreadSafe
{
    private org.apache.avalon.fortress.util.test.ComponentStateValidator m_validator = new org.apache.avalon.fortress.util.test.ComponentStateValidator( this );
    private Logger m_logger;
    private Context m_context;
    private Parameters m_parameters;
    private Configuration m_configuration;
    private ServiceManager m_componentManager;

    public void enableLogging( Logger logger )
    {
        m_validator.checkNotAssigned( m_logger );
        m_validator.checkLogEnabled();

        m_logger = logger;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        m_validator.checkNotAssigned( m_context );
        m_validator.checkContextualized();

        m_context = context;
    }

    public void parameterize( Parameters params )
        throws ParameterException
    {
        m_validator.checkNotAssigned( m_parameters );
        m_validator.checkParameterized();

        m_parameters = params;
    }

    public void configure( Configuration config )
        throws ConfigurationException
    {
        m_validator.checkNotAssigned( m_configuration );
        m_validator.checkConfigured();

        m_configuration = config;
    }

    public void service( final ServiceManager manager )
        throws ServiceException
    {
        m_validator.checkNotAssigned( m_componentManager );
        m_validator.checkServiced();
    }

    public void initialize()
        throws Exception
    {
        m_validator.checkInitialized();
    }

    public void start()
        throws Exception
    {
        m_validator.checkStarted();
    }

    public void suspend()
    {
        m_validator.checkSuspended();
    }

    public void resume()
    {
        m_validator.checkResumed();
    }

    public void stop()
        throws Exception
    {
        m_validator.checkStopped();
    }

    public void dispose()
    {
        m_validator.checkDisposed();

        m_logger = null;
        m_context = null;
        m_parameters = null;
        m_configuration = null;
        m_componentManager = null;
    }
}
