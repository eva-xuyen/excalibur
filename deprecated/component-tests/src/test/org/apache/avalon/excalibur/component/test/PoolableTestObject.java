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
package org.apache.avalon.excalibur.component.test;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.Logger;

/**
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/17 13:22:38 $
 */
public class PoolableTestObject
    implements PoolableTestObjectInterface, Initializable, Recyclable, Disposable, Poolable
{
    /** Semaphore used to synchronize access to m_instanceCounter */
    private static Object m_semaphore = new Object();

    /** Number of instances created since the last call to resetInstanceCounter() */
    private static int m_instanceCounter = 0;

    private static Logger m_logger;

    /** Instance Id */
    private int m_instanceId;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public PoolableTestObject()
    {
        synchronized( m_semaphore )
        {
            m_instanceCounter++;
            m_instanceId = m_instanceCounter;
        }
    }

    /*---------------------------------------------------------------
     * Static Methods
     *-------------------------------------------------------------*/
    /**
     * Resets the instance counter so that the next Poolable will get an instance Id of 1.
     */
    public static void resetInstanceCounter()
    {
        synchronized( m_semaphore )
        {
            m_instanceCounter = 0;
        }
    }

    /**
     * Used by tests to change the current logger object.
     */
    public static void setStaticLoggger( Logger logger )
    {
        m_logger = logger;
    }

    /*---------------------------------------------------------------
     * Initializable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to initialize the component.
     */
    public void initialize()
    {
        m_logger.debug( "PoolableTestObject #" + m_instanceId + " initialized." );
    }

    /*---------------------------------------------------------------
     * Recyclable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container when the component is recycled.
     */
    public void recycle()
    {
        m_logger.debug( "PoolableTestObject #" + m_instanceId + " recycled." );
    }

    /*---------------------------------------------------------------
     * Disposable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to dispose the component.
     */
    public void dispose()
    {
        m_logger.debug( "PoolableTestObject #" + m_instanceId + " disposed." );
    }
}

