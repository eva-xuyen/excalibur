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
package org.apache.excalibur.source.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * This is the default implemenation of a {@link SourceResolver}.
 *
 * The source resolving is done relative to a base directory/URI (if
 * the given location is relative). This implementation looks for the
 * base URI in the {@link Context} object of the "container" for the
 * "context-root" information. This information can either be a
 * {@link File} object or a {@link URL} object.
 * If the entry does not exist, the system property "user.dir" is used
 * as the base URI instead.
 *
 * @see org.apache.excalibur.source.SourceResolver
 *
 * @avalon.component
 * @avalon.service type=SourceResolver
 * @x-avalon.info name=resolver
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SourceResolverImpl.java,v 1.4 2004/02/28 11:47:24 cziegeler Exp $
 */
public class SourceResolverImpl
    extends AbstractSourceResolver
    implements Serviceable,
    Contextualizable,
    Disposable,
    LogEnabled,
    ThreadSafe
{

    /** The component m_manager */
    protected ServiceManager m_manager;

    /** The special Source factories */
    protected ServiceSelector m_factorySelector;

    /** Our logger. */
    private Logger m_logger;

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#getSourceFactory(java.lang.String)
     */
    protected SourceFactory getSourceFactory(String protocol)
    {
        try
        {
            return (SourceFactory) this.m_factorySelector.select(protocol);
        }
        catch (ServiceException e)
        {
            // we go back to the default factory selector
            return null;
        }
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#releaseSourceFactory(org.apache.excalibur.source.SourceFactory)
     */
    protected void releaseSourceFactory(SourceFactory factory)
    {
        this.m_factorySelector.release(factory);
    }

    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger)
    {
        this.m_logger = logger;
    }

    protected final Logger getLogger()
    {
        return this.m_logger;
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#debug(java.lang.String)
     */
    protected final void debug(String text)
    {
        this.m_logger.debug(text);
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#isDebugEnabled()
     */
    protected final boolean isDebugEnabled()
    {
        return this.m_logger.isDebugEnabled();
    }

    /**
     * Get the context
     */
    public void contextualize( Context context )
        throws ContextException
    {
        try
        {
            if( context.get( "context-root" ) instanceof URL )
            {
                this.m_baseURL = (URL)context.get( "context-root" );
            }
            else
            {
                this.m_baseURL = ( (File)context.get( "context-root" ) ).toURL();
            }
        }
        catch( ContextException ce )
        {
            // set the base URL to the current directory
            try
            {
                this.m_baseURL = new File( System.getProperty( "user.dir" ) ).toURL();
                if( this.getLogger().isDebugEnabled() )
                {
                    this.getLogger().debug( "SourceResolver: Using base URL: " + this.m_baseURL );
                }
            }
            catch( MalformedURLException mue )
            {
                this.getLogger().warn( "Malformed URL for user.dir, and no container.rootDir exists", mue );
                throw new ContextException( "Malformed URL for user.dir, and no container.rootDir exists", mue );
            }
        }
        catch( MalformedURLException mue )
        {
            this.getLogger().warn( "Malformed URL for container.rootDir", mue );
            throw new ContextException( "Malformed URL for container.rootDir", mue );
        }
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @avalon.dependency type="org.apache.excalibur.source.SourceFactory"
     */
    public void service( final ServiceManager manager )
        throws ServiceException
    {
        this.m_manager = manager;
        this.m_factorySelector = (ServiceSelector) this.m_manager.lookup( SourceFactory.ROLE + "Selector" );
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose()
    {
        if( null != this.m_manager )
        {
            this.m_manager.release( this.m_factorySelector );
            this.m_factorySelector = null;
        }
    }
}
