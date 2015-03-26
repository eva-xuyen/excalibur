/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.avalon.excalibur.monitor.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.monitor.Monitor;
import org.apache.avalon.excalibur.monitor.Resource;

/**
 * The AbstractMonitor class is a useful base class which all Monitors
 * can extend. The particular monitoring policy is defined by the particular
 * implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractMonitor.java,v 1.5 2004/02/28 11:47:35 cziegeler Exp $
 */
public abstract class AbstractMonitor
    implements Monitor
{
    /**
     * The set of resources that the monitor is monitoring.
     */
    private Map m_resources = new HashMap();

    /**
     * Add an array of resources to monitor.
     *
     * @param resources the resources to monitor
     */
    public final void addResources( final Resource[] resources )
    {
        for( int i = 0; i < resources.length; i++ )
        {
            addResource( resources[ i ] );
        }
    }

    /**
     * Add a resource to monitor.  The resource key referenced in the other
     * interfaces is derived from the resource object.
     */
    public final void addResource( final Resource resource )
    {
        synchronized( m_resources )
        {
            final String resourceKey = resource.getResourceKey();
            if( m_resources.containsKey( resourceKey ) )
            {
                final Resource original =
                    (Resource)m_resources.get( resourceKey );
                original.addPropertyChangeListenersFrom( resource );
            }
            else
            {
                m_resources.put( resourceKey, resource );
            }
        }
    }

    /**
     * Find a monitored resource.  If no resource is available, return null
     */
    public Resource getResource( final String key )
    {
        synchronized( m_resources )
        {
            return (Resource)m_resources.get( key );
        }
    }

    /**
     * Remove a monitored resource by key. Will throw NPE, if no resource with given key.
     */
    public final void removeResource( final String key )
    {
        synchronized( m_resources )
        {
            final Resource resource =
                (Resource)m_resources.remove( key );
            resource.removeAllPropertyChangeListeners();
        }
    }

    /**
     * Remove a monitored resource by reference. Will throw NPE, if resource has been removed from monitor.
     */
    public final void removeResource( final Resource resource )
    {
        removeResource( resource.getResourceKey() );
    }

    /**
     * Return an array containing all the resources currently monitored.
     *
     * @return an array containing all the resources currently monitored.
     */
    protected Resource[] getResources()
    {
        synchronized( m_resources )
        {
            final Collection collection = m_resources.values();
            return (Resource[])collection.toArray( new Resource[ collection.size() ] );
        }
    }

    /**
     * Scan through all resources to determine if they have changed.
     */
    protected void scanAllResources()
    {
        final long currentTestTime = System.currentTimeMillis();
        final Resource[] resources = getResources();
        for( int i = 0; i < resources.length; i++ )
        {
            resources[ i ].testModifiedAfter( currentTestTime );
        }
    }
}
