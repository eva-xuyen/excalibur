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
package org.apache.avalon.excalibur.pool.test;

import java.util.HashMap;

import org.apache.avalon.excalibur.pool.ObjectFactory;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.1
 */
public class ClassInstanceObjectFactory
    implements ObjectFactory
{
    private HashMap m_instances = new HashMap();
    private Logger m_logger;
    private Class m_clazz;
    private int m_id;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a reproducable log of activity in the provided StringBuffer
     */
    public ClassInstanceObjectFactory( Class clazz, Logger logger )
    {
        m_clazz = clazz;
        m_logger = logger;
        m_id = 1;
    }

    /*---------------------------------------------------------------
     * ObjectFactory Methods
     *-------------------------------------------------------------*/
    public Object newInstance() throws Exception
    {
        Object object = m_clazz.newInstance();
        Integer id = new Integer( m_id++ );

        m_instances.put( object, id );

        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "ClassInstanceObjectFactory.newInstance()  id:" + id );
        }

        return object;
    }

    public Class getCreatedClass()
    {
        return m_clazz;
    }

    public void decommission( Object object ) throws Exception
    {
        if( object instanceof Disposable )
        {
            ( (Disposable)object ).dispose();
        }
        Integer id = (Integer)m_instances.remove( object );

        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "ClassInstanceObjectFactory.decommission(a "
                            + object.getClass().getName() + ")  id:" + id );
        }
    }
}

