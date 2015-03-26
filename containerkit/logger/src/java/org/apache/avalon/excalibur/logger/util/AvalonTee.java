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
package org.apache.avalon.excalibur.logger.util;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import java.util.ArrayList;

/**
 * This class broadcasts Avalon lifestyle events to several
 * destination objects, somewhat like Unix 'tee' command
 * directing its input both to file and to its output.
 *
 * The current implementation is incomplete and handles
 * only LogEnabled, Contextutalizable, Configurable and Disposable
 * interfaces.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */

public class AvalonTee implements
        LogEnabled,
        Contextualizable,
        Configurable,
        Startable, 
        Disposable
{
    /* The objects we direct our events to */
    private ArrayList m_listeners = new ArrayList( 10 );
    /**
     * The number of these objects. This variable is here
     * is to save a line of code in every reactor method
     * rather then to optimize speed.
     */
    private int m_len = 0;

    /* Has adding new tees been prohibited? */
    private boolean m_readOnly = false;

    /**
     * Disallow adding more tees.
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Adds an object to the list of objects receiving events.
     * @param obj the object to add; can not be null.
     */
    public void addTee( final Object obj )
    {
        if ( m_readOnly )
        {
            throw new IllegalStateException( "makeReadOnly() already invoked" );
        }

        if ( obj == null ) throw new NullPointerException( "obj" );
        if ( m_listeners.contains( obj ) )
        {
            // should we complain? better not, probably 
        }
        else
        {
            // adds to the end of the array
            m_listeners.add( obj );
            m_len = m_listeners.size();
        }
    }

    public void enableLogging( final Logger logger )
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.enableLogging( m_listeners.get( i ), logger );
        }
    }

    public void contextualize( final Context context ) throws ContextException
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.contextualize( m_listeners.get( i ), context );
        }
    }

    public void configure( final Configuration config ) throws ConfigurationException
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.configure( m_listeners.get( i ), config );
        }
    }

    public void start() throws Exception
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.start( m_listeners.get( i ) );
        }
    }

    public void stop() throws Exception
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.stop( m_listeners.get( i ) );
        }
    }

    public void dispose()
    {
        for( int i = 0; i < m_len; ++i )
        {
            ContainerUtil.dispose( m_listeners.get( i ) );
        }
    }
}
