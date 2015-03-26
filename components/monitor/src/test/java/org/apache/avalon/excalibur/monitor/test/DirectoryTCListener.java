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
package org.apache.avalon.excalibur.monitor.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.apache.avalon.excalibur.monitor.DirectoryResource;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class DirectoryTCListener
    extends AbstractLogEnabled
    implements PropertyChangeListener
{
    private int m_changeCount;
    private Set m_added = Collections.EMPTY_SET;
    private Set m_removed = Collections.EMPTY_SET;
    private Set m_modified = Collections.EMPTY_SET;

    void reset()
    {
        m_added = Collections.EMPTY_SET;
        m_removed = Collections.EMPTY_SET;
        m_modified = Collections.EMPTY_SET;
    }

    public Set getAdded()
    {
        return m_added;
    }

    public Set getRemoved()
    {
        return m_removed;
    }

    public Set getModified()
    {
        return m_modified;
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }

    public void propertyChange( final PropertyChangeEvent event )
    {
        m_changeCount++;
        final String name = event.getPropertyName();
        final Set newValue = (Set)event.getNewValue();
        if( name.equals( DirectoryResource.ADDED ) )
        {
            m_added = newValue;
        }
        else if( name.equals( DirectoryResource.REMOVED ) )
        {
            m_removed = newValue;
        }
        else
        {
            m_modified = newValue;
        }
    }
}
