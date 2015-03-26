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

package org.apache.avalon.fortress.impl.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.LogKit2AvalonLoggerAdapter;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.excalibur.instrument.AbstractLogEnabledInstrumentable;
import org.apache.excalibur.instrument.CounterInstrument;
import org.d_haven.mpool.ObjectFactory;

/**
 * Factory for Avalon components.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.31 $ $Date: 2004/04/13 13:17:54 $
 */
public final class ComponentFactory extends AbstractLogEnabledInstrumentable implements ObjectFactory
{
    private final CounterInstrument m_newInstance;
    private final CounterInstrument m_dispose;

    /** The class which this <code>ComponentFactory</code>
     * should create.
     */
    private final Class m_componentClass;

    /** The Context for the component
     */
    private final Context m_context;

    /** The component manager for this component.
     */
    private final ServiceManager m_serviceManager;

    /** The configuration for this component.
     */
    private final Configuration m_configuration;

    /** The LogKitManager for child ComponentSelectors
     */
    private final LoggerManager m_loggerManager;

    /** Lifecycle extensions manager
     */
    private final LifecycleExtensionManager m_extManager;

    /** The component's logger
     */
    private final Logger m_componentLogger;

	/** The container
	 */
	private final Container m_container;
    
    /** Component info - used only for raising events
     */
    private final MetaInfoEntry m_metaEntry;

    /**
     * The class info for creating the component
     */
    private ClassInfo m_classinfo;

    /** Use the dynamic configuration */
    private final boolean m_useDynamicCreation = false;

    /**
     * Construct a new component factory for the specified component.
     *
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     * @param serviceManager the service manager to pass to <code>Serviceable</code>s.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     * @param loggerManager the loggerManager manager instance.
     */
    public ComponentFactory(
        final Class componentClass,
        final Configuration configuration,
        final ServiceManager serviceManager,
        final Context context,
        final LoggerManager loggerManager,
        final LifecycleExtensionManager extManager,
        final MetaInfoEntry metaEntry, 
        final Container container)
    {
        m_componentClass = componentClass;
        m_configuration = configuration;
        m_serviceManager = serviceManager;
        m_context = new DefaultContext(context);
        m_metaEntry = metaEntry;
		m_container = container;
        final String name = configuration.getAttribute("id", componentClass.getName());
        ((DefaultContext) m_context).put("component.name", name);
        ((DefaultContext) m_context).put("component.logger", configuration.getAttribute("logger", name));
        // Take each configuration attribute, and make a context entry of form "component.<attribName>"
        String[] attribNames = configuration.getAttributeNames();

        for (int index = 0; index < attribNames.length; index++)
        {
            String oneName = attribNames[index];
            ((DefaultContext) m_context).put("component." + oneName, configuration.getAttribute(oneName, ""));
        }

        ((DefaultContext) m_context).put("component.configuration", configuration);
        ((DefaultContext) m_context).makeReadOnly();
        m_loggerManager = loggerManager;
        m_extManager = extManager;
        enableLogging(m_loggerManager.getLoggerForCategory("system.factory"));
        m_componentLogger = aquireLogger();

        m_newInstance = new CounterInstrument("creates");
        m_dispose = new CounterInstrument("destroys");

        setInstrumentableName("factory");

        addInstrument(m_newInstance);
        addInstrument(m_dispose);
    }

    /**
     * Returns a new instance of a component and optionally applies a logging channel,
     * instrumentation, context, a component or service manager, configuration, parameters,
     * lifecycle extensions, initialization, and execution phases based on the interfaces
     * implemented by the component class.
     *
     * @return the new instance
     */
    public Object newInstance() throws Exception
    {
        final Object component;

        try
        {
            component = this.createComponent();

            if (getLogger().isDebugEnabled())
            {
                final String message = "ComponentFactory creating new instance of " + m_componentClass.getName() + ".";
                getLogger().debug(message);
            }

            this.enabledComponentLogging(component);

            this.contextualizeComponent(component);

            this.serviceComponent(component);

            this.configureComponent(component);

            m_extManager.executeCreationExtensions(component, m_context);

            ContainerUtil.initialize(component);

            ContainerUtil.start(component);

            if (m_newInstance.isActive())
            {
                m_newInstance.increment();
            }
        }
        catch (LinkageError le)
        {
            throw new CascadingException("Could not load component", le);
        }

        return component;
    }

    private Logger aquireLogger()
    {
        Logger logger;

        try
        {
            final String name = (String) m_context.get("component.logger");
            if (getLogger().isDebugEnabled())
            {
                final String message = "logger name is " + name;
                getLogger().debug(message);
            }
            logger = m_loggerManager.getLoggerForCategory(name);
        }
        catch (ContextException ce)
        {
            if (getLogger().isDebugEnabled())
            {
                final String message = "no logger name available, using standard name";
                getLogger().debug(message);
            }
            logger = m_loggerManager.getDefaultLogger();
        }
        return logger;
    }

    /**
     * Returns the component class.
     * @return the class
     */
    public final Class getCreatedClass()
    {
        return m_componentClass;
    }

    /**
     * Disposal of the supplied component instance.
     * @param component the component to dispose of
     * @exception Exception if a disposal error occurs
     */
    public final void dispose(final Object component) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            final String message = "ComponentFactory decommissioning instance of " + getCreatedClass().getName() + ".";
            getLogger().debug(message);
        }

        if ( component.getClass().getName().startsWith( "$Proxy" ) || getCreatedClass().equals(component.getClass()))
        {
            ContainerUtil.shutdown(component);

            m_extManager.executeDestructionExtensions(component, m_context);
            raiseComponentDestroyedEvent( component );

            if (m_dispose.isActive())
            {
                m_dispose.increment();
            }
        }
        else
        {
            final String message = "The object given to be disposed does " + "not come from this ObjectFactory";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Create a new component and fires the 
     * associated event
     */
    protected Object createComponent() throws Exception
    {
        Object newInstance = null;
        
        if (!m_useDynamicCreation)
        {
            newInstance = m_componentClass.newInstance();
        }
        else
        {
            if (m_classinfo == null)
            {
                m_classinfo = new ClassInfo();
            }
            
            newInstance = m_classinfo.m_constructor.newInstance( m_classinfo.m_constructorArguments );
        }

        return raiseComponentCreatedEvent( newInstance );
    }

    /**
     * Enable logging for the component
     */
    protected void enabledComponentLogging(Object component) throws Exception
    {
        ContainerUtil.enableLogging(component, m_componentLogger);

        if (component instanceof Loggable)
        {
            final org.apache.log.Logger logkitLogger = LogKit2AvalonLoggerAdapter.createLogger(m_componentLogger);
            ((Loggable) component).setLogger(logkitLogger);
        }

        if (m_useDynamicCreation)
        {

            if (m_classinfo.m_setLoggerMethod != null)
            {
                m_classinfo.m_setLoggerMethod.invoke(component, new Object[] { m_componentLogger });
            }
        }
    }

    /**
     * Contextualize the component
     */
    protected void contextualizeComponent(Object component) throws Exception
    {
        ContainerUtil.contextualize(component, m_context);
        if (!(component instanceof Contextualizable))
        {
            try
            {
                MethodUtils.invokeMethod(component, "setContext", m_context);
            }
            catch (Exception ignore)
            {
            }
        }

        if (m_useDynamicCreation)
        {
            if (m_classinfo.m_setContextMethod != null)
            {
                m_classinfo.m_setContextMethod.invoke(component, new Object[] { m_context });
            }
        }
    }

    /**
     * Service the component
     */
    protected void serviceComponent(Object component) throws Exception
    {
        if (component instanceof Composable)
        {
            ContainerUtil.compose(component, new WrapperComponentManager(m_serviceManager));
        }
        ContainerUtil.service(component, m_serviceManager);

        if (m_useDynamicCreation)
        {
            if (m_classinfo.m_setServiceManagerMethod != null)
            {
                m_classinfo.m_setServiceManagerMethod.invoke(component, new Object[] { m_serviceManager });
            }
        }
    }

    /**
     * Configure the component
     */
    protected void configureComponent(Object component) throws Exception
    {
        ContainerUtil.configure(component, m_configuration);

        if (component instanceof Parameterizable)
        {
            ContainerUtil.parameterize(component, Parameters.fromConfiguration(m_configuration));
        }

        if (m_useDynamicCreation)
        {
            if (m_classinfo.m_setParametersMethod != null)
            {
                m_classinfo.m_setParametersMethod.invoke(
                    component,
                    new Object[] { Parameters.fromConfiguration(m_configuration)});
            }
            if (m_classinfo.m_setConfigurationMethod != null)
            {
                m_classinfo.m_setConfigurationMethod.invoke(component, new Object[] { m_configuration });
            }

            // if the component has a configuration, but does not implement the
            // interfaces, try to set the parameters using reflection
            if (m_classinfo.m_dynamicConfiguration)
            {
                if (m_configuration != null && m_configuration.getChildren().length > 0)
                {
                    final Parameters p = Parameters.fromConfiguration(m_configuration);
                    String[] names = p.getNames();
                    for (int i = 0; i < names.length; i++)
                    {
                        try
                        {
                            BeanUtils.setProperty(component, names[i], p.getParameter(names[i]));
                        }
                        catch (Exception ignore)
                        {
                            if (this.getLogger() != null && this.getLogger().isWarnEnabled())
                            {
                                this.getLogger().warn(
                                    "Error while trying to configure " + component + " with parameter: " + names[i],
                                    ignore);
                            }
                        }
                    }
                }
            }
        }
    }

    protected Object raiseComponentCreatedEvent( final Object newInstance )
    {
        return m_container.getEventManager().fireComponentCreated( m_metaEntry, newInstance );
    }

    protected void raiseComponentDestroyedEvent( final Object newInstance )
    {
        m_container.getEventManager().fireComponentDestroyed( m_metaEntry, newInstance );
    }

    /**
     * This class collects all information about the components class:
     * - the constructor to use
     * - the parameters to pass into the constructor
     * - Additional infos about implemented methods
     */
    protected class ClassInfo
    {
        public Constructor m_constructor;
        public Object[] m_constructorArguments;
        public Method m_setLoggerMethod;
        public Method m_setConfigurationMethod;
        public Method m_setParametersMethod;
        public Method m_setContextMethod;
        public boolean m_dynamicConfiguration = false;
        public Method m_setServiceManagerMethod;

        /** Constructor */
        public ClassInfo() throws Exception
        {
            // let's see which constructors are available
            Constructor[] constructors = m_componentClass.getConstructors();

            if (constructors.length < 1)
            {
                throw new Exception("Class " + m_componentClass + " does not have a public constructor.");
            }

            if (constructors.length > 1)
            {
                // if we have more than one constructor, we first search for
                // an empty argument constructor
                // if that is not available we simply take the first constructor
                // we find
                try
                {
                    m_constructor = m_componentClass.getConstructor(new Class[0]);
                }
                catch (NoSuchMethodException e)
                {
                    // we ignore the exception and take the first one
                    m_constructor = constructors[0];
                }
            }
            else
            {
                m_constructor = constructors[0];
            }

            // now test the parameters for the constructor
            final Class[] classes = m_constructor.getParameterTypes();
            m_constructorArguments = new Object[classes.length];
            for (int i = 0; i < classes.length; i++)
            {
                final Class current = classes[i];
                if (Logger.class.isAssignableFrom(current))
                {
                    if (m_componentLogger == null)
                    {
                        throw new IllegalArgumentException("Logger is null.");
                    }
                    m_constructorArguments[i] = m_componentLogger;
                }
                else if (Context.class.isAssignableFrom(current))
                {
                    if (m_context == null)
                    {
                        throw new IllegalArgumentException("Context is null.");
                    }
                    m_constructorArguments[i] = m_context;
                }
                else if (Configuration.class.isAssignableFrom(current))
                {
                    if (m_configuration == null)
                    {
                        throw new IllegalArgumentException("Configuration is null.");
                    }
                    m_constructorArguments[i] = m_configuration;
                }
                else if (Parameters.class.isAssignableFrom(current))
                {
                    if (m_configuration == null)
                    {
                        throw new IllegalArgumentException("Configuration is null.");
                    }
                    m_constructorArguments[i] = Parameters.fromConfiguration(m_configuration);
                }
                else if (ServiceManager.class.isAssignableFrom(current))
                {
                    m_constructorArguments[i] = m_serviceManager;
                }
                else
                {
                    // now test if this is a reference to a component!
                    // FIXME - This works only for thread safe components!
                    final String role = current.getName();
                    try
                    {
                        final Object component = m_serviceManager.lookup(role);
                        m_constructorArguments[i] = component;
                        m_serviceManager.release(component);
                    }
                    catch (Exception e)
                    {
                        throw new ServiceException(
                            "ComponentFactory",
                            "Unknown parameter type for constructor of component: " + current,
                            e);
                    }
                }
            }

            // now test for some setter methods
            if (!Loggable.class.isAssignableFrom(m_componentClass)
                && !LogEnabled.class.isAssignableFrom(m_componentClass))
            {
                m_setLoggerMethod = this.getMethod("setLogger", Logger.class);
            }

            if (!Contextualizable.class.isAssignableFrom(m_componentClass))
            {
                m_setContextMethod = this.getMethod("setContext", Context.class);
            }

            if (!Parameterizable.class.isAssignableFrom(m_componentClass)
                && !Configurable.class.isAssignableFrom(m_componentClass))
            {
                m_setConfigurationMethod = this.getMethod("setConfiguration", Configuration.class);
                m_setParametersMethod = this.getMethod("setParameters", Parameters.class);
                if (m_setConfigurationMethod == null && m_setParametersMethod == null)
                {
                    m_dynamicConfiguration = true;
                }
            }

            if (!Composable.class.isAssignableFrom(m_componentClass)
                && !Serviceable.class.isAssignableFrom(m_componentClass))
            {
                m_setServiceManagerMethod = this.getMethod("setServiceManager", ServiceManager.class);
            }
        }

        /**
         * Helper method for getting a named method that has one parameter of the given type.
         */
        protected Method getMethod(String name, Class clazz) throws Exception
        {
            try
            {
                return m_componentClass.getMethod(name, new Class[] { clazz });
            }
            catch (NoSuchMethodException ignore)
            {
            }
            return null;
        }
    }
}
