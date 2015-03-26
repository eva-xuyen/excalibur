/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.fortress.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.util.Service;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.LEAwareComponentHandler;
import org.apache.avalon.fortress.impl.lookup.FortressServiceSelector;
import org.apache.avalon.fortress.impl.role.ECMMetaInfoManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.instrument.Instrumentable;
import org.d_haven.mpool.ObjectFactory;

/**
 * Customize the Fortress container to handle ECM compatibility
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class DefaultECMContainer extends DefaultContainer {

    /**
     * Retrieve the role for the component.
     *
     * @param config the component configuration
     * @return the class name
     */
    private String getRole( final Configuration config )
    throws ConfigurationException {
        final String className;

        if ( "component".equals( config.getName() ) )
        {
            className = config.getAttribute( "role" );
        }
        else
        {
            final MetaInfoEntry roleEntry = m_metaManager.getMetaInfoForShortName( config.getName() );
            if ( null == roleEntry )
            {

                final String message = "No class found matching configuration name " +
                        "[name: " + config.getName() + ", location: " + config.getLocation() + "]";
                throw new ConfigurationException( message );
            }

            Iterator roleIterator = roleEntry.getRoles();
            if ( roleIterator.hasNext() )
            {
                className = (String)roleIterator.next();
            }
            else
            {
                className = roleEntry.getComponentClass().getName();
            }
        }

        return className;
    }

    /**
     * Retrieve the classname (impl) for the component.
     *
     * @param config the component configuration
     * @return the class name
     */
    private String getClassname( final Configuration config )
    throws ConfigurationException {
        final String className;

        if ( "component".equals( config.getName() ) )
        {
            className = config.getAttribute( "class" );
        }
        else
        {
            if ( config.getAttribute("class", null) != null )
            {
                className = config.getAttribute("class");
            }
            else
            {
                final MetaInfoEntry roleEntry = m_metaManager.getMetaInfoForShortName( config.getName() );
                if ( null == roleEntry )
                {

                    final String message = "No class found matching configuration name " +
                        "[name: " + config.getName() + ", location: " + config.getLocation() + "]";
                    throw new ConfigurationException( message );
                }

                className = roleEntry.getComponentClass().getName();
            }
        }

        return className;
    }

    /**
     * Provide some validation for the core Cocoon components
     *
     * @param conf The configuration
     * @throws ConfigurationException if the coniguration is invalid
     */
    public void configure( Configuration conf )
    throws ConfigurationException {
        this.interpretProxy( conf.getAttribute("proxy-type", this.getDefaultProxyType()) );

        final Configuration[] elements = conf.getChildren();
        for ( int i = 0; i < elements.length; i++ )
        {
            final Configuration element = elements[i];

            // figure out Role
            String role = getRole( element );
            if ( role.endsWith("Selector") )
            {
                processSelector(role.substring(0, role.length()-8), element );
            }
            else
            {

                // get the implementation
                final String className = getClassname( element );

                final int pos = role.indexOf('/');
                final String hint;
                if ( pos != -1 )
                {
                    hint = role.substring(pos+1);
                    role = role.substring(0, pos);
                }
                else
                {
                    hint = null;
                }

                final String shortName;
                if ( "component".equals( element.getName() ))
                {
                    shortName = null;
                }
                else
                {
                    shortName = element.getName();
                }

                this.addComponent(role, hint, shortName, className, element );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Configuration processed for: " + role );
            }
        }
    }

    /**
     * Get a ComponentHandler with the default constructor for the component class passed in.
     *
     * @param classname     the name of the component's class
     * @param handlerClass  the class used to handle the component
     * @param metaData      the information needed to construct a ComponentHandler for the component
     * @return the component handler
     * @throws Exception if unable to provide a componenthandler
     */
    private ComponentHandler getComponentHandler( final String classname,
                                                  final Class  handlerClass,
                                                  final ComponentHandlerMetaData metaData)
            throws Exception
    {
        final Configuration configuration = metaData.getConfiguration();

        // get info from params
        final ComponentHandler handler;

        try
        {
            final ObjectFactory factory =
                    createObjectFactory( classname, configuration );

            // create the appropriate handler instance
            final ComponentHandler targetHandler =
                    (ComponentHandler) handlerClass.newInstance();

            // do the handler lifecycle
            ContainerUtil.enableLogging( targetHandler, getLogger() );
            ContainerUtil.contextualize( targetHandler, m_context );
            final DefaultServiceManager serviceManager =
                    new DefaultServiceManager( getServiceManager() );
            serviceManager.put( ObjectFactory.class.getName(), factory );
            serviceManager.makeReadOnly();

            ContainerUtil.service( targetHandler, serviceManager );
            ContainerUtil.configure( targetHandler, configuration );
            ContainerUtil.initialize( targetHandler );

            if ( targetHandler instanceof Instrumentable )
            {
                final Instrumentable instrumentable = (Instrumentable) targetHandler;
                final String name = instrumentable.getInstrumentableName();
                m_instrumentManager.registerInstrumentable( instrumentable, name );
            }

            // no other lifecycle stages supported for ComponentHandler;
            // ComponentHandler is not a "true" avalon component

            handler = new LEAwareComponentHandler( targetHandler, m_extManager, m_context );
        }
        catch ( final Exception e )
        {
            // if anything went wrong, the component cannot be worked with
            // and it cannot be added into the impl, so don't provide
            // a handler
            if ( getLogger().isDebugEnabled() )
            {
                final String message =
                        "Could not create the handler for the '" +
                        classname + "' component.";
                getLogger().debug( message, e );
            }
            throw e;
        }

        if ( getLogger().isDebugEnabled() )
        {
            final String message =
                    "Component " + classname +
                    " uses handler " + handlerClass.getName();
            getLogger().debug( message );
        }

        // we're still here, so everything went smooth. Register the handler
        // and return it
        final ComponentHandlerEntry entry =
                new ComponentHandlerEntry( handler, metaData );
        m_components.add( entry );

        return handler;
    }

    protected Class getComponentHandlerClass(final String defaultClassName, final String shortName )
        throws Exception
    {
        if ( shortName == null )
        {
            String handlerClassName = null;

            Class clazz;
            try
            {
                clazz = m_classLoader.loadClass( defaultClassName );

                if ( ThreadSafe.class.isAssignableFrom( clazz ) )
                {
                    handlerClassName = MetaInfoEntry.THREADSAFE_HANDLER;
                }
                else if ( Service.isClassPoolable(clazz) )
                {
                    handlerClassName = MetaInfoEntry.POOLABLE_HANDLER;
                }
                else if ( SingleThreaded.class.isAssignableFrom( clazz) )
                {
                    handlerClassName = MetaInfoEntry.FACTORY_HANDLER;
                }
            }
            catch ( final Exception e )
            {
                final String message =
                    "Unable to load class " + defaultClassName + ". Using dfault component handler.";
                getLogger().warn( message );
            }

            // Don't know, use default
            if ( handlerClassName == null )
            {
                handlerClassName = MetaInfoEntry.THREADSAFE_HANDLER;
            }
            return m_classLoader.loadClass( handlerClassName ) ;
        }
        else
        {
            final MetaInfoEntry roleEntry = m_metaManager.getMetaInfoForShortName( shortName );
            if ( null == roleEntry )
            {

                final String message = "No class found matching configuration name " +
                        "[name: " + shortName + "]";
                throw new ConfigurationException( message );
            }

            return roleEntry.getHandlerClass();
        }
    }

    protected void processSelector(String role, Configuration config)
        throws ConfigurationException
    {
        final String selectorRole = role + "Selector";
        FortressServiceSelector fss = new FortressServiceSelector(this, selectorRole);
        Map hintMap = createHintMap();
        hintMap.put( DEFAULT_ENTRY, fss );
        hintMap.put( SELECTOR_ENTRY,
                    new FortressServiceSelector( this, selectorRole ) );
        m_mapper.put( selectorRole, hintMap );

        final Configuration[] children = config.getChildren();
        if ( children != null )
        {
            for(int i=0; i<children.length; i++)
            {
                final Configuration element = children[i];
                final String hint = element.getAttribute("name");
                final String className = element.getAttribute("class");

                if ( m_metaManager instanceof ECMMetaInfoManager )
                {
                    try
                    {
                        ((ECMMetaInfoManager)m_metaManager).addSelectorComponent(role, hint, className, getComponentHandlerClass(className, null).getName());
                    }
                    catch (ConfigurationException ce )
                    {
                        throw ce;
                    }
                    catch (Exception e)
                    {
                        throw new ConfigurationException("Unable to add selector component.", e);
                    }
                }
                addComponent(role, hint, null, className, element );
            }
        }
    }

    protected void addComponent(final String role,
                                String hint,
                                String shortName,
                                final String className,
                                final Configuration element)
        throws ConfigurationException
    {
        final int activation = ComponentHandlerMetaData.ACTIVATION_BACKGROUND;

        // Fortress requires a hint, so we just give it one :) (if missing)
        final String metaDataHint = element.getAttribute( "id", element.getLocation() );

        if ( hint == null )
        {
            hint = metaDataHint;
        }

        final ComponentHandlerMetaData metaData =
            new ComponentHandlerMetaData( metaDataHint, className, element, activation );

        try
        {

            if ( DEFAULT_ENTRY.equals( metaData.getName() ) ||
                    SELECTOR_ENTRY.equals( metaData.getName() ) )
            {
                throw new IllegalArgumentException( "Using a reserved id name" + metaData.getName() );
            }

            // create a handler for the combo of Role+MetaData
            final ComponentHandler handler =
                    getComponentHandler( className,
                                         getComponentHandlerClass( className, shortName),
                                         metaData );

            // put the role into our role mapper. If the role doesn't exist
            // yet, just stuff it in as DEFAULT_ENTRY. If it does, we create a
            // ServiceSelector and put that in as SELECTOR_ENTRY.
            Map hintMap = (Map) m_mapper.get( role );

            // Initialize the hintMap if it doesn't exist yet.
            if ( null == hintMap )
            {
                hintMap = createHintMap();
                hintMap.put( DEFAULT_ENTRY, handler );
                hintMap.put( SELECTOR_ENTRY,
                        new FortressServiceSelector( this, role ) );
                m_mapper.put( role, hintMap );
            }

            hintMap.put( hint, handler );

            if ( element.getAttributeAsBoolean( "default", false ) )
            {
                hintMap.put( DEFAULT_ENTRY, handler );
            }
        }
        catch ( ConfigurationException ce )
        {
            throw ce;
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( "Could not add component", e );
        }
    }

    /**
     * Return the default proxy type.
     * This method can be overwritten in subclasses to provide a different
     * default proxy type.
     */
    protected String getDefaultProxyType() {
        return "none";
    }
}
