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
package org.apache.excalibur.event.test;

import junit.framework.TestCase;

import org.apache.excalibur.event.PreparedEnqueue;
import org.apache.excalibur.event.Queue;

/**
 * The default queue implementation is a variabl size queue.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public abstract class AbstractQueueTestCase extends TestCase
{
    Object element = new TestQueueElement();
    Object[] elements = new TestQueueElement[ 10 ];

    private static final class TestQueueElement
    {
    }

    public AbstractQueueTestCase( String name )
    {
        super( name );

        for( int i = 0; i < 10; i++ )
        {
            elements[ i ] = new TestQueueElement();
        }
    }

    protected final void performMillionIterationOneElement( Queue queue )
        throws Exception
    {
        assertEquals( 0, queue.size() );

        if( queue.maxSize() > 0 )
        {
            for( int j = 0; j < 1000000; j++ )
            {
                queue.enqueue( element );
                assertEquals( 1, queue.size() );

                assertNotNull( queue.dequeue() );
                assertEquals( 0, queue.size() );
            }
        }
        else
        {
            for( int i = 0; i < 1000; i++ )
            {
                for( int j = 0; j < 1000; j++ )
                {
                    queue.enqueue( element );
                    assertEquals( "Queue Size: " + queue.size(), j + 1, queue.size() );
                }

                for( int j = 0; j < 1000; j++ )
                {
                    assertNotNull( "Queue Size: " + queue.size(), queue.dequeue() );
                    assertEquals( "Queue Size: " + queue.size(), 999 - j, queue.size() );
                }
            }
        }
    }

    protected final void performMillionIterationTenElements( Queue queue )
        throws Exception
    {
        assertEquals( 0, queue.size() );

        if( queue.maxSize() > 0 )
        {
            for( int j = 0; j < 1000000; j++ )
            {
                queue.enqueue( elements );
                assertEquals( 10, queue.size() );

                Object[] results = queue.dequeueAll();
                assertEquals( 10, results.length );
                assertEquals( 0, queue.size() );
            }
        }
        else
        {
            for( int i = 0; i < 1000; i++ )
            {
                for( int j = 0; j < 1000; j++ )
                {
                    queue.enqueue( elements );
                    assertEquals( "Queue Size: " + queue.size(), 10 * ( j + 1 ), queue.size() );
                }

                Object[] results = queue.dequeueAll();
                assertEquals( "Queue Size: " + queue.size(), 10 * 1000, results.length );
                assertEquals( "Queue Size: " + queue.size(), 0, queue.size() );
            }
        }
    }

    protected final void performQueue( Queue queue )
        throws Exception
    {
        assertEquals( 0, queue.size() );

        queue.enqueue( new TestQueueElement() );
        assertEquals( 1, queue.size() );

        assertNotNull( queue.dequeue() );
        assertEquals( 0, queue.size() );

        queue.enqueue( elements );
        assertEquals( 10, queue.size() );

        Object[] results = queue.dequeue( 3 );
        assertEquals( 3, results.length );
        assertEquals( 7, queue.size() );

        results = queue.dequeueAll();
        assertEquals( 7, results.length );
        assertEquals( 0, queue.size() );

        PreparedEnqueue prep = queue.prepareEnqueue( elements );
        assertEquals( 10, queue.size() );
        prep.abort();
        assertEquals( 0, queue.size() );

        prep = queue.prepareEnqueue( elements );
        assertEquals( 10, queue.size() );
        prep.commit();
        assertEquals( 10, queue.size() );

        results = queue.dequeue( queue.size() );
        assertEquals( 0, queue.size() );
    }
}
