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

import org.apache.excalibur.event.impl.DefaultQueue;
import org.apache.excalibur.event.impl.FixedSizeQueue;
import org.apache.excalibur.event.impl.ThresholdEnqueuePredicate;

/**
 * The default queue implementation is a variabl size queue.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class FixedSizeQueueTestCase extends AbstractQueueTestCase
{
    public FixedSizeQueueTestCase( String name )
    {
        super( name );
    }

    public void testFixedSizeQueue()
        throws Exception
    {
        this.performQueue( new FixedSizeQueue( 32 ) );
    }

    public void testThresholdDefaultQueue()
        throws Exception
    {
        this.performQueue( new DefaultQueue( new ThresholdEnqueuePredicate( 32 ) ) );
    }
}
