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
package org.apache.avalon.excalibur.monitor.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/*
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class MonitorTCListener
    extends AbstractLogEnabled
    implements PropertyChangeListener
{
    private volatile boolean m_hasChanged = false;

    public boolean hasBeenModified()
    {
        return m_hasChanged;
    }

    public void reset()
    {
        m_hasChanged = false;
    }

    public void propertyChange( final PropertyChangeEvent propertyChangeEvent )
    {
        m_hasChanged = true;

        if( getLogger().isInfoEnabled() )
        {
            getLogger().info( "NOTIFICATION LATENCY: " + ( System.currentTimeMillis() -
                                                           ( (Long)propertyChangeEvent.getNewValue() ).longValue() ) +
                              "ms" );
            getLogger().info( "Received notification for " +
                              ( (MockResource)propertyChangeEvent.getSource() ).getResourceKey() );
            getLogger().info( propertyChangeEvent.getPropertyName() +
                              "\n  IS::" + (Long)propertyChangeEvent.getNewValue() +
                              "\n  WAS::" + (Long)propertyChangeEvent.getOldValue() +
                              "\n  TIME SINCE LAST MOD::" +
                              ( ( (Long)propertyChangeEvent.getNewValue() ).longValue() -
                                ( (Long)propertyChangeEvent.getOldValue() ).longValue() ) );
        }
    }
}
