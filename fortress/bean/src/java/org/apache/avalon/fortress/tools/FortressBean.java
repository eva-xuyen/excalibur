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

package org.apache.avalon.fortress.tools;

import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Bean for making it easier to run Fortress, for example as Ant task.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/05/06 11:00:44 $
 */
public class FortressBean implements Initializable, LogEnabled, Serviceable, Disposable {

    private static final String COMMONS_LOG_PROPERTY = "org.apache.commons.logging.Log";
    private static final String COMMONS_AVALON_LOGGER = "org.apache.commons.logging.impl.AvalonLogger";

    private final FortressConfig config = new FortressConfig();
    private Logger logger = null;
    private DefaultContainerManager cm;
    private DefaultContainer container;
    private ServiceManager sm;

    private String lookupComponentRole = null;
    private String invokeMethod = null;

    private boolean systemExitOnDispose = true;

    private Properties properties = null;

    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    protected final Logger getLogger() {
        if (this.logger == null) this.logger = new ConsoleLogger();
        return this.logger;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        //only initialize if we do not already have a servicemanager passed in from outside
        if (this.sm == null) {
            ClassLoader cl = null;
            if (this.properties != null) initialize(this.properties);
            if (Thread.currentThread().getContextClassLoader() == null) {
                if (this.getClass().getClassLoader() != null) {
                    cl = this.getClass().getClassLoader();
                    config.setContextClassLoader(cl);
                    Thread.currentThread().setContextClassLoader(cl);
                } else {
                    getLogger().warn("context classloader not set and class classloader is null!");
                }
            }
            // Get the root container initialized
            this.cm = new DefaultContainerManager(config.getContext());
            ContainerUtil.initialize(cm);
            initializeCommonsLogging(cl);
            this.container = (DefaultContainer) cm.getContainer();
            this.sm = container.getServiceManager();
        }
    }

    /**
     * Use reflection to set up commons logging. If commons logging is available, it will be set up;
     * if it is not available, this section is ignored. This needs version 1.0.4 (or later) of commons
     * logging, earlier versions do not have avalon support.
     */
    private void initializeCommonsLogging(ClassLoader cl) {
        try {
            //if commons logging is available, set the static logger for commons logging
            Class commonsLoggerClass;
            if (cl != null) {
                commonsLoggerClass = cl.loadClass(COMMONS_AVALON_LOGGER);
            } else {
                commonsLoggerClass = Class.forName(COMMONS_AVALON_LOGGER);
            }
            Method setDefaultLoggerMethod = commonsLoggerClass.getMethod("setDefaultLogger", new Class[] {Logger.class});
            setDefaultLoggerMethod.invoke(null, new Object[] {cm.getLogger()});
            //set the system property to use avalon logger
            System.setProperty(COMMONS_LOG_PROPERTY, COMMONS_AVALON_LOGGER);
            if (getLogger().isInfoEnabled()) getLogger().info("AvalonLogger found, commons logging redirected to Avalon logs");
        } catch (ClassNotFoundException e) {
            if (getLogger().isInfoEnabled()) getLogger().info("AvalonLogger not found, commons logging not redirected");
        } catch (Exception e) {
            if (getLogger().isDebugEnabled()) getLogger().debug("error while initializing commons logging: " + e.getClass().getName() + ", " + e.getMessage());
        }
    }

    private void disposeCommonsLogging() {
        System.getProperties().remove(COMMONS_LOG_PROPERTY);
    }

    public static final String PROPERTY_CONTAINER_CLASS = "container.class";
    public static final String PROPERTY_CONTAINER_CONFIGURATION = "container.configuration";
    public static final String PROPERTY_CONTEXT_DIRECTORY = "context.directory";
    public static final String PROPERTY_INSTRUMENT_MANAGER_CONFIGURATION =
                                          "instrument.manager.configuration";
    public static final String PROPERTY_INVOKE_METHOD = "invoke.method";
    public static final String PROPERTY_LOGGER_MANAGER_CONFIGURATION = "logger.manager.configuration";
    public static final String PROPERTY_LOOKUP_COMPONENT_ROLE = "lookup.component.role";
    public static final String PROPERTY_ROLE_MANAGER_CONFIGURATION = "role.manager.configuration";
    public static final String PROPERTY_SYSTEM_EXIT_ON_DISPOSE = "system.exit.on.dispose";
    public static final String PROPERTY_WORK_DIRECTORY = "work.directory";

    public void initialize(Properties p) throws Exception {
        if (p.getProperty(PROPERTY_CONTAINER_CLASS) != null)
            this.setContainerClass(p.getProperty(PROPERTY_CONTAINER_CLASS));
        if (p.getProperty(PROPERTY_CONTAINER_CONFIGURATION) != null)
            this.setContainerConfiguration(p.getProperty(PROPERTY_CONTAINER_CONFIGURATION));
        if (p.getProperty(PROPERTY_CONTEXT_DIRECTORY) != null)
            this.setContextDirectory(p.getProperty(PROPERTY_CONTEXT_DIRECTORY));
        if (p.getProperty(PROPERTY_INSTRUMENT_MANAGER_CONFIGURATION) != null)
            this.setInstrumentManagerConfiguration(p.getProperty(PROPERTY_INSTRUMENT_MANAGER_CONFIGURATION));
        if (p.getProperty(PROPERTY_INVOKE_METHOD) != null)
            this.setInvokeMethod(p.getProperty(PROPERTY_INVOKE_METHOD));
        if (p.getProperty(PROPERTY_LOGGER_MANAGER_CONFIGURATION) != null)
            this.setLoggerManagerConfiguration(p.getProperty(PROPERTY_LOGGER_MANAGER_CONFIGURATION));
        if (p.getProperty(PROPERTY_LOOKUP_COMPONENT_ROLE) != null)
            this.setLookupComponentRole(p.getProperty(PROPERTY_LOOKUP_COMPONENT_ROLE));
        if (p.getProperty(PROPERTY_ROLE_MANAGER_CONFIGURATION) != null)
            this.setRoleManagerConfiguration(p.getProperty(PROPERTY_ROLE_MANAGER_CONFIGURATION));
        if (p.getProperty(PROPERTY_SYSTEM_EXIT_ON_DISPOSE) != null)
            this.setSystemExitOnDispose(Boolean.valueOf(p.getProperty(PROPERTY_SYSTEM_EXIT_ON_DISPOSE)).booleanValue());
        if (p.getProperty(PROPERTY_WORK_DIRECTORY) != null)
            this.setWorkDirectory(p.getProperty(PROPERTY_WORK_DIRECTORY));
    }

    public void run() throws Exception {
        Object component = getServiceManager().lookup(lookupComponentRole);
        Method method = component.getClass().getMethod(invokeMethod, null);
        method.invoke(component, null);
    }

    /**
     * Implementation execute() method for Ant compatability.
     */
    public void execute() {
        try {
            initialize();
            try {
                run();
            } catch (Exception e) {
                getLogger().error("error while running", e);
            }
        } catch (Exception e) {
            getLogger().error("error while initializing", e);
        }
        dispose();
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager sm) throws ServiceException {
        if (this.sm == null) this.sm = sm;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // Properly clean up when we are done
        org.apache.avalon.framework.container.ContainerUtil.dispose( cm );
        disposeCommonsLogging();
        //system exit, in case we were running some GUI and some thread is still active
        if (this.systemExitOnDispose) {
            Thread.yield();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
            
            System.exit(0);
        }
    }

    protected ServiceManager getServiceManager() {
        return this.sm;
    }

    /**
     * The container implementation has to be a subclass of
     * <code>org.apache.avalon.fortress.impl.DefaultContainer</code>.
     *
     * @param containerClass fully qualified class name of the container implementation class.
     */
    public void setContainerClass(String containerClass) throws Exception {
        config.setContextClassLoader(getClass().getClassLoader());
        config.setContainerClass(containerClass);
    }

    public void setContainerConfiguration(String containerConfiguration) {
        config.setContainerConfiguration(containerConfiguration);
    }

    public void setContextDirectory(String contextDirectory) {
        config.setContextDirectory(contextDirectory);
    }

    public void setInstrumentManagerConfiguration(String instrumentManagerConfiguration) {
        config.setInstrumentManagerConfiguration(instrumentManagerConfiguration);
    }

    public void setLoggerManagerConfiguration(String loggerManagerConfiguration) {
        config.setLoggerManagerConfiguration(loggerManagerConfiguration);
    }

    public void setRoleManagerConfiguration(String roleManagerConfiguration) {
        config.setRoleManagerConfiguration(roleManagerConfiguration);
    }

    public void setWorkDirectory(String workDirectory) {
        config.setWorkDirectory(workDirectory);
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public void setLookupComponentRole(String lookupComponentRole) {
        this.lookupComponentRole = lookupComponentRole;
    }

    /**
     * Should we call System.exit(0) after we are finished with processing.
     * Useful if the components have a GUI and there are some threads running that
     * do not allow the JVM to exit.
     */
    public void setSystemExitOnDispose(boolean systemExitOnDispose) {
        this.systemExitOnDispose = systemExitOnDispose;
    }

    public static void main(String[] args) {
        FortressBean bean = new FortressBean();
        bean.setProperties(System.getProperties());
        if (args.length > 0) bean.setLookupComponentRole(args[0]);
        if (args.length > 1) bean.setInvokeMethod(args[1]);
        bean.execute();
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
