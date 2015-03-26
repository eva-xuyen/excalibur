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
package org.apache.excalibur.mpool;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.StaticBucketMap;
import org.apache.excalibur.event.Sink;
import org.apache.excalibur.event.command.RepeatedCommand;

/**
 * This interface is for a PoolManager that creates pools that are managed
 * asynchronously.  The contract is that the controller type is specified in
 * the constructor.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:34 $
 * @since 4.1
 */
public class DefaultPoolManager implements PoolManager
{
    private final Random m_keyGenerator;
    private final Map m_keyMap = new StaticBucketMap();
    private final Map m_factoryMap = new StaticBucketMap();

    public DefaultPoolManager()
    {
        this( null );
    }

    public DefaultPoolManager( final Sink commandSink )
    {
        m_keyGenerator = new Random();

        if( null != commandSink )
        {
            try
            {
                commandSink.enqueue( new PoolManagerCommand( m_keyMap ) );
            }
            catch( Exception e )
            {
                // ignore silently for now
            }
        }
    }

    /**
     * Return a managed pool that has a controller.
     */
    public Pool getManagedPool( ObjectFactory factory, int initialEntries )
        throws Exception
    {
        ManagablePool pool = (ManagablePool)m_factoryMap.get( factory );

        if( null == pool )
        {
            final long poolKey = getKey();
            pool = new VariableSizePool( factory, initialEntries, poolKey );
            m_keyMap.put( pool, new Long( poolKey ) );
            m_factoryMap.put( factory, pool );
        }

        return pool;
    }

    /**
     * Return a new key for the pool and controller.
     */
    private final long getKey()
    {
        return m_keyGenerator.nextLong();
    }

    private static final class PoolManagerCommand implements RepeatedCommand
    {
        private final Map m_map;
        private final int m_min = 4;
        private final int m_max = 256;
        private final int m_grow = 4;

        protected PoolManagerCommand( Map map )
        {
            m_map = map;
        }

        public long getDelayInterval()
        {
            return 10 * 1000L;
        }

        public long getRepeatInterval()
        {
            return 10 * 1000L;
        }

        public int getNumberOfRepeats()
        {
            return 0;
        }

        public void execute()
            throws Exception
        {
            Iterator i = m_map.keySet().iterator();

            while( i.hasNext() )
            {
                ManagablePool pool = (ManagablePool)i.next();
                long key = ( (Long)m_map.get( pool ) ).longValue();
                int size = pool.size( key );

                if( size < m_min )
                {
                    pool.grow( m_grow, key );
                }

                if( size > m_max )
                {
                    pool.shrink( m_grow, key );
                }
            }
        }
    }
}
