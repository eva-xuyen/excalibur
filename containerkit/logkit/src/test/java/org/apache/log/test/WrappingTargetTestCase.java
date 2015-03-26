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
package org.apache.log.test;

import junit.framework.TestCase;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.output.AbstractWrappingTarget;
import org.apache.log.util.Closeable;

/**
 * Test suite for wrapping targets.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class WrappingTargetTestCase
    extends TestCase
{
    public WrappingTargetTestCase(String name)
    {
        super(name);
    }
    
    static private class DummyTarget implements LogTarget
    {
        private boolean closed = false;
        
        public void close()
        {
            closed = true;
        }
        
        public boolean isClosed()
        {
            return closed;
        }
        
        public void processEvent( LogEvent event )
        {
            // Do nothing
        }
    }
    
    static private class CloseableDummyTarget extends DummyTarget implements Closeable
    {
    }
    
    static private class DummyTargetWrapper extends AbstractWrappingTarget
    {
        public DummyTargetWrapper( final LogTarget logTarget )
        {
            super( logTarget );
        }
        
        public DummyTargetWrapper( final LogTarget logTarget, final boolean closeWrappedTarget )
        {
            super( logTarget, closeWrappedTarget );
        }
        
        public void doProcessEvent( LogEvent event )
        {
            // Do nothing
        }
    }
    
    public void testNonCloseable()
    {
        DummyTarget dummyTargetNonClose = new DummyTarget();
        DummyTarget dummyTargetNonClose2 = new DummyTarget();
        DummyTarget dummyTargetClose = new DummyTarget();
        
        DummyTargetWrapper wrapperNonClose = new DummyTargetWrapper(dummyTargetNonClose, false);
        DummyTargetWrapper wrapperNonClose2 = new DummyTargetWrapper(dummyTargetNonClose2); // should default to false
        DummyTargetWrapper wrapperClose = new DummyTargetWrapper(dummyTargetClose, true);
        
        assertTrue( !dummyTargetNonClose.isClosed() );
        assertTrue( !dummyTargetNonClose2.isClosed() );
        assertTrue( !dummyTargetClose.isClosed() );
        
        wrapperNonClose.close();
        wrapperNonClose2.close();
        wrapperClose.close();
        
        // The close() should have no effect, since neither target implements closeable.
        
        assertTrue( !dummyTargetNonClose.isClosed() );
        assertTrue( !dummyTargetNonClose2.isClosed() );
        assertTrue( !dummyTargetClose.isClosed() );        
    }
    
    public void testCloseable()
    {
        DummyTarget dummyTargetNonClose = new CloseableDummyTarget();
        DummyTarget dummyTargetNonClose2 = new CloseableDummyTarget();
        DummyTarget dummyTargetClose = new CloseableDummyTarget();
        
        DummyTargetWrapper wrapperNonClose = new DummyTargetWrapper(dummyTargetNonClose, false);
        DummyTargetWrapper wrapperNonClose2 = new DummyTargetWrapper(dummyTargetNonClose2); // should default to false
        DummyTargetWrapper wrapperClose = new DummyTargetWrapper(dummyTargetClose, true);
        
        assertTrue( !dummyTargetNonClose.isClosed() );
        assertTrue( !dummyTargetNonClose2.isClosed() );
        assertTrue( !dummyTargetClose.isClosed() );
        
        wrapperNonClose.close();
        wrapperNonClose2.close();
        wrapperClose.close();
        
        // Only the target that was wrapped with the closeWrapped parameter
        // set to true should be closed.
        
        assertTrue( !dummyTargetNonClose.isClosed() );
        assertTrue( !dummyTargetNonClose2.isClosed() );
        assertTrue( dummyTargetClose.isClosed() );        
    }
}
