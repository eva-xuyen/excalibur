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
package org.apache.avalon.framework.container;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
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

/**
 * Utility class that makes it easier to transfer
 * a component throught it's lifecycle stages.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContainerUtil.java 506231 2007-02-12 02:36:54Z crossley $
 */
public final class ContainerUtil
{
    /**
     * Private constructor to block instantiation.
     */
    private ContainerUtil()
    {
    }

    /**
     * Run specified object through shutdown lifecycle stages
     * (Stop and Dispose).
     *
     * @param object the object to shutdown
     * @throws Exception if there is a problem stoppping object
     */
    public static void shutdown( final Object object )
        throws Exception
    {
        stop( object );
        dispose( object );
    }

    /**
     * Supply specified object with Logger if it implements the
     * {@link LogEnabled} interface.
     *
     * @param object the object to Start
     * @param logger the logger to enable component with. May be null
     *        in which case the specified object must not implement LogEnabled.
     * @throws IllegalArgumentException if the object is LogEnabled but Logger is null
     */
    public static void enableLogging( final Object object,
                                      final Logger logger )
    {
        if( object instanceof LogEnabled )
        {
            if( null == logger )
            {
                final String message = "logger is null";
                throw new IllegalArgumentException( message );
            }
            ( (LogEnabled)object ).enableLogging( logger );
        }
    }

    /**
     * Supply specified object with a Context object if it implements the
     * {@link Contextualizable} interface.
     *
     * @param object the object to contextualize
     * @param context the context object to use for object.
     *        May be null in which case the specified object must not
     *        implement Contextualizable.
     * @throws ContextException if there is a problem contextualizing object
     * @throws IllegalArgumentException if the object is Contextualizable but
     *         context is null
     */
    public static void contextualize( final Object object,
                                      final Context context )
        throws ContextException
    {
        if( object instanceof Contextualizable )
        {
            if( null == context )
            {
                final String message = "context is null";
                throw new IllegalArgumentException( message );
            }
            ( (Contextualizable)object ).contextualize( context );
        }
    }

    /**
     * Supply specified object with ServiceManager if it implements the
     * {@link Serviceable} interface.
     *
     * @param object the object to service
     * @param serviceManager the serviceManager object to use for object.
     *        May be null in which case the specified object must not
     *        implement Serviceable.
     * @throws ServiceException if there is a problem servicing object
     * @throws IllegalArgumentException if the object is Servicable but
     *         ServiceManager is null
     */
    public static void service( final Object object,
                                final ServiceManager serviceManager )
        throws ServiceException
    {
        if( object instanceof Serviceable )
        {
            if( null == serviceManager )
            {
                final String message = "ServiceManager is null";
                throw new IllegalArgumentException( message );
            }
            ( (Serviceable)object ).service( serviceManager );
        }
    }

    /**
     * Supply specified object with ComponentManager if it implements the
     * {@link Composable} interface.
     *
     * @param object the object to compose
     * @param componentManager the ComponentManager object to use for object.
     *        May be null in which case the specified object must not
     *        implement Composable.
     * @throws ComponentException if there is a problem composing object
     * @deprecated compose() is no longer the preferred method via
     *             which components will be supplied with Components. Please
     *             Use service() from Composable instead.
     * @throws IllegalArgumentException if the object is Composable but
     *         ComponentManager is null
     */
    public static void compose( final Object object,
                                final ComponentManager componentManager )
        throws ComponentException
    {
        if( object instanceof Composable )
        {
            if( null == componentManager )
            {
                final String message = "componentManager is null";
                throw new IllegalArgumentException( message );
            }
            ( (Composable)object ).compose( componentManager );
        }
    }

    /**
     * Configure specified object if it implements the
     * {@link Configurable} interface.
     *
     * @param object the object to Start
     * @param configuration the configuration object to use during
     *        configuration. May be null in which case the specified object
     *        must not implement Configurable
     * @throws ConfigurationException if there is a problem Configuring object,
     *         or the object is Configurable but Configuration is null
     * @throws IllegalArgumentException if the object is Configurable but
     *         Configuration is null
     */
    public static void configure( final Object object,
                                  final Configuration configuration )
        throws ConfigurationException
    {
        if( object instanceof Configurable )
        {
            if( null == configuration )
            {
                final String message = "configuration is null";
                throw new IllegalArgumentException( message );
            }
            ( (Configurable)object ).configure( configuration );
        }
    }

    /**
     * Parameterize specified object if it implements the
     * {@link Parameterizable} interface.
     *
     * @param object the object to Parameterize.
     * @param parameters the parameters object to use during Parameterization.
     *        May be null in which case the specified object must not
     *        implement Parameterizable.
     * @throws ParameterException if there is a problem Parameterizing object
     * @throws IllegalArgumentException if the object is Parameterizable but
     *         parameters is null
     */
    public static void parameterize( final Object object,
                                     final Parameters parameters )
        throws ParameterException
    {
        if( object instanceof Parameterizable )
        {
            if( null == parameters )
            {
                final String message = "parameters is null";
                throw new IllegalArgumentException( message );
            }
            ( (Parameterizable)object ).parameterize( parameters );
        }
    }

    /**
     * Initialize specified object if it implements the
     * {@link Initializable} interface.
     *
     * @param object the object to Initialize
     * @throws Exception if there is a problem Initializing object
     */
    public static void initialize( final Object object )
        throws Exception
    {
        if( object instanceof Initializable )
        {
            ( (Initializable)object ).initialize();
        }
    }

    /**
     * Start specified object if it implements the
     * {@link Startable} interface.
     *
     * @param object the object to Start
     * @throws Exception if there is a problem Starting object
     */
    public static void start( final Object object )
        throws Exception
    {
        if( object instanceof Startable )
        {
            ( (Startable)object ).start();
        }
    }

    /**
     * Execute the specified object if it implements the
     * {@link Executable} interface.
     *
     * @param object the object to execute
     * @throws Exception if there is a problem executing object
     */
    public static void execute( final Object object )
        throws Exception
    {
        if( object instanceof Executable )
        {
            ( (Executable)object ).execute();
        }
    }

    /**
     * Stop specified object if it implements the
     * {@link Startable} interface.
     *
     * @param object the object to stop
     * @throws Exception if there is a problem stoppping object
     */
    public static void stop( final Object object )
        throws Exception
    {
        if( object instanceof Startable )
        {
            ( (Startable)object ).stop();
        }
    }

    /**
     * Dispose specified object if it implements the
     * {@link Disposable} interface.
     *
     * @param object the object to dispose
     */
    public static void dispose( final Object object )
    {
        if( object instanceof Disposable )
        {
            ( (Disposable)object ).dispose();
        }
    }
}
