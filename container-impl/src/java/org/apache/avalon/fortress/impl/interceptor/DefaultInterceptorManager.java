/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl.interceptor;

import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.ContainerListener;
import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.fortress.attributes.AttributeInfo;
import org.apache.avalon.fortress.impl.interceptor.strategies.simple.SimpleInterceptableFactory;
import org.apache.avalon.fortress.interceptor.Interceptor;
import org.apache.avalon.fortress.interceptor.InterceptorManager;
import org.apache.avalon.fortress.interceptor.InterceptorManagerException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

/**
 * Default (and simple) implementation of InterceptorManager capability.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class DefaultInterceptorManager
    implements InterceptorManager, Configurable, Contextualizable, Initializable, ContainerListener
{
    protected static final String INTERCEPTABLE_TAGNAME = "excalibur.interceptable";
    protected static final String FAMILY_ATT_NAME = "family";
    
    ///
    /// Instance fields
    ///

    protected final Map m_families;

    protected Container m_container;

    protected MetaInfoManager m_metaManager;
    
    protected InterceptableFactory m_interFactory = new SimpleInterceptableFactory();

    ///
    /// Constructors
    ///
    
    public DefaultInterceptorManager()
    {
        m_families = new TreeMap( String.CASE_INSENSITIVE_ORDER );
    }

    public DefaultInterceptorManager( Context context ) throws ContextException
    {
        this();
        contextualize( context );
    }

    public DefaultInterceptorManager( Context context, Configuration config ) throws ContextException, ConfigurationException
    {
        this( context );
        configure( config );
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException
    {
        m_container = (Container) context.get("container");
        m_metaManager = (MetaInfoManager) context.get("metamanager");
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration config) throws ConfigurationException
    {
        configureFactory( config.getAttribute("factory", "") );
        configureInterceptors(config);
    }
    
    /**
     * Pending
     * 
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception
    {
        m_container.getEventManager().addListener(this);
    }

    ///
    /// ContainerListener implementation
    /// 

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.ContainerListener#componentCreated(org.apache.avalon.fortress.MetaInfoEntry, java.lang.Object)
     */
    public Object componentCreated( final MetaInfoEntry entry, final Object instance )
    {
        Object newInstance = instance;
        
        final ExtendedMetaInfo metaInfo = 
            m_metaManager.getExtendedMetaInfo( entry.getComponentClass().getName() );
        final String family = obtainComponentFamily( metaInfo );
        
        if ( family != null )
        {
            try
            {
                Interceptor chain = buildChain( family );
                
                Class[] interfaces = entry.getComponentClass().getInterfaces();
                
                newInstance = m_interFactory.createInterceptableInstance( 
                    instance, metaInfo, interfaces, chain );
            }
            catch(IllegalAccessException ex)
            {
            }
            catch(InstantiationException ex)
            {
            }
        }

        return newInstance;
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.ContainerListener#componentDestroyed(org.apache.avalon.fortress.MetaInfoEntry, java.lang.Object)
     */
    public void componentDestroyed( final MetaInfoEntry entry, final Object instance )
    {
    }
    
    ///
    /// InterceptorManager implementation
    ///
    
    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.InterceptorManager#add(java.lang.String, java.lang.String, java.lang.String)
     */
    public void add( final String family, final String name, final String interceptorClass ) 
        throws InterceptorManagerException
    {
        try
        {
            ComponentFamily componentFamily = obtainFamily( family );
            componentFamily.add( name, interceptorClass );
        
            m_families.put( family, componentFamily );
        }
        catch(ClassNotFoundException ex)
        {
            throw new InterceptorManagerException( "Could not find class specified.", ex );
        }
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.InterceptorManager#remove(java.lang.String, java.lang.String)
     */
    public void remove( final String family, final String name )
    {
        ComponentFamily componentFamily = obtainFamily( family );
        componentFamily.remove( name );
        removeIfEmpty( componentFamily );
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.InterceptorManager#buildChain(java.lang.String)
     */
    public Interceptor buildChain( final String family ) throws IllegalAccessException, InstantiationException
    {
        ComponentFamily componentFamily = obtainFamily( family );
        Interceptor[] interceptors = componentFamily.buildOrderedChain();
        return assemble( interceptors );
    }

    /**
     * Pending
     * 
     * @see org.apache.avalon.fortress.interceptor.InterceptorManager#getFamilies()
     */
    public String[] getFamilies()
    {
        return (String[]) m_families.keySet().toArray( new String[0] );
    }
    
    ///
    /// Overrideable Implementation
    ///
    
    /**
     * Connects the interceptors throught the init(next), adds
     * the tail interceptor to execute the real invocation 
     * and returns the first interceptor.
     * 
     * @param interceptors raw array of interceptor instances.
     */
    protected Interceptor assemble( final Interceptor[] interceptors )
    {
        Interceptor tail = createTailInterceptor( interceptors );
        
        for (int i = interceptors.length - 1; i >= 0; i--)
        {
            Interceptor last = interceptors[i];
            last.init( tail );
            tail = last;
        }
        
        return tail;
    }
    
    /**
     * Returns the last interceptor on the chain. The interceptor manager
     * must provide the last interceptor to guarantee the real execution of the method, 
     * although it doesn't necessarily needs to be execute. The chain can 
     * determine when the actual execution will be performed using its own criteria. 
     * 
     * @param interceptors raw array of interceptor instances.
     * @return
     */
    protected Interceptor createTailInterceptor( final Interceptor[] interceptors )
    {
        return new TailInterceptor();
    }
    
    protected String obtainComponentFamily( final ExtendedMetaInfo metaInfo )
    {
        // TODO: There are plenty space here for optimizations.
        
        AttributeInfo[] attributes = metaInfo.getClassAttributes();
        
        for (int i = 0; i < attributes.length; i++)
        {
            AttributeInfo info = attributes[i];
            if (INTERCEPTABLE_TAGNAME.equalsIgnoreCase( info.getName() ))
            {
                return (String) info.getProperties().get( FAMILY_ATT_NAME );
            }
        }
        
        return null;
    }
    
    protected void configureFactory( final String factoryClass ) throws ConfigurationException
    {
        if ( factoryClass.equals("") )
        {
            // Use the default factory
            return;
        }
        
        try
        {
            m_interFactory = (InterceptableFactory) 
                Thread.currentThread().getContextClassLoader().loadClass( factoryClass ).newInstance();
        }
        catch(Exception ex)
        {
            throw new ConfigurationException("Custom InterceptableFactory specified could not be created.");
        }
    }

    protected void configureInterceptors(final Configuration config) throws ConfigurationException
    {
        final Configuration[] sets = config.getChildren("set");
        
        for (int i = 0; i < sets.length; i++)
        {
            final Configuration set = sets[i];
            final String familyName = set.getAttribute(FAMILY_ATT_NAME, "");
            
            if ( "".equals(familyName) )
            {
                throw new ConfigurationException("Element 'set' must " +
                    "specify a valid 'family' attribute.");
            }
            
            Configuration[] interceptors = set.getChildren("interceptor");
            
            for (int j = 0; j < interceptors.length; j++)
            {
                final Configuration interceptor = interceptors[j];
                final String key = interceptor.getAttribute("name", "");
                final String clazz = interceptor.getAttribute("class", "");
                
                if ( "".equals(key) || "".equals(clazz) )
                {
                    throw new ConfigurationException("Element 'interceptor' must " +
                        "specify 'name' and 'class' attributes.");
                }
                
                try
                {
                    add( familyName, key, clazz );
                }
                catch(InterceptorManagerException ex)
                {
                    throw new ConfigurationException("Invalid interceptor entry", ex);
                }
            }
        }
    }

    ///
    /// Private implementation
    ///

    private void removeIfEmpty( final ComponentFamily componentFamily )
    {
        synchronized( m_families )
        {
            if (componentFamily.interceptorsCount() == 0)
            {
                m_families.remove( componentFamily.getFamilyName() );
            }
        }
    }
    
    private ComponentFamily obtainFamily( final String family )
    {
        synchronized( m_families )
        {
            ComponentFamily componentFamily = (ComponentFamily) m_families.get( family );
            
            if (componentFamily == null)
            {
                componentFamily = new ComponentFamily( family );
                m_families.put( family, componentFamily );
            }

            return componentFamily;
        }
    }
}
