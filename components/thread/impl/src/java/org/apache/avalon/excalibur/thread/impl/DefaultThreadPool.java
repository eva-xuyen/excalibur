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
package org.apache.avalon.excalibur.thread.impl;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.excalibur.pool.SoftResourceLimitingPool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.thread.ThreadControl;
import org.apache.excalibur.thread.ThreadPool;

/**
 * This class is the public frontend for the thread pool code.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultThreadPool
    extends ThreadGroup
    implements ObjectFactory, LogEnabled, Disposable, ThreadPool
{
    private final BasicThreadPool m_pool;
    private SoftResourceLimitingPool m_underlyingPool;

    public DefaultThreadPool( final int capacity )
        throws Exception
    {
        this( "Worker Pool", capacity );
    }

    public DefaultThreadPool( final String name,
                              final int capacity )
        throws Exception
    {
        super( name );
        m_underlyingPool = new SoftResourceLimitingPool( this, capacity );
        m_pool = new BasicThreadPool( this, name, m_underlyingPool );
    }

    public DefaultThreadPool( final String name,
                              final int min,
                              final int max )
        throws Exception
    {
        super( name );
        m_underlyingPool = new SoftResourceLimitingPool( this, min, max );
        m_pool = new BasicThreadPool( this, name, m_underlyingPool );
    }

    public void enableLogging( final Logger logger )
    {
        ContainerUtil.enableLogging( m_pool, logger );
    }

    public void dispose()
    {
        m_pool.dispose();
    }

    public Object newInstance()
    {
        return m_pool.newInstance();
    }

    public void decommission( final Object object )
    {
        m_pool.decommission( object );
    }

    public Class getCreatedClass()
    {
        return m_pool.getCreatedClass();
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final Executable work )
    {
        return m_pool.execute( work );
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final Runnable work )
    {
        return m_pool.execute( work );
    }

    /**
     * Run work in separate thread.
     * Return a valid ThreadControl to control work thread.
     *
     * @param work the work to be executed.
     * @return the ThreadControl
     */
    public ThreadControl execute( final org.apache.excalibur.thread.Executable work )
    {
        return m_pool.execute( work );
    }
}
