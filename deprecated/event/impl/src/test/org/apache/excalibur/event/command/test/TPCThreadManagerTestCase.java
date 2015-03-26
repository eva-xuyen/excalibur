/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.excalibur.event.command.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.event.EventHandler;
import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.Sink;
import org.apache.excalibur.event.SinkException;
import org.apache.excalibur.event.Source;
import org.apache.excalibur.event.command.EventPipeline;
import org.apache.excalibur.event.command.TPCThreadManager;
import org.apache.excalibur.event.impl.DefaultQueue;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class TPCThreadManagerTestCase extends TestCase
{
    /**
     * Constructor for JUnit
     *
     * @param name  The name of the test
     */
    public TPCThreadManagerTestCase( String name )
    {
        super( name );
    }

    // number of milliseconds it reasonably takes the JVM to switch threads
    private final static int SCHEDULING_TIMEOUT = 1000; // ms

    // number of times the handler should be called
    private final static int MINIMAL_NUMBER_INVOCATIONS = 2;

    private Parameters createParameters( int threadsPerProcessor, long sleep )
    {
        final Parameters parameters = new Parameters();

        parameters.setParameter( "threads-per-processor", String.valueOf( threadsPerProcessor ) );
        parameters.setParameter( "sleep-time", String.valueOf( sleep ) );

        return parameters;
    }

    /**
     * Checks TPCThreadManager ability to survive the situation when
     * it tries to schedule more tasks than it has threads. Originally
     * it was dying due to hitting Pool limit and not catching the
     * resulting runtime exception.
     * <p>
     * The test is not foolproof, it probably depends on preemtive
     * threads management.
     *
     * @throws Exception on error
     */
    public void testThreadContention() throws Exception
    {
        // enforces only 1 thread and no timeout which makes it
        // fail quickly
        final TPCThreadManager threadManager = new TPCThreadManager();

        threadManager.parameterize( createParameters( 1, 0 ) );
        threadManager.initialize();

        // an obviously syncronized component
        final StringBuffer result = new StringBuffer();
        final StringWriter exceptionBuffer = new StringWriter();
        final PrintWriter errorOut = new PrintWriter( exceptionBuffer );

        threadManager.register( new Pipeline( result, errorOut ) );

        // sleeps for 1 more scheduling timeout to surely go over limit
        Thread.sleep( SCHEDULING_TIMEOUT * ( MINIMAL_NUMBER_INVOCATIONS + 1 ) );

        int numberCalls = result.length();

        String msg =
            "Number of calls to handler (" + numberCalls +
            ") is less than the expected number of calls (" +
            MINIMAL_NUMBER_INVOCATIONS + ")";

        assertTrue( msg, numberCalls >= MINIMAL_NUMBER_INVOCATIONS );

        errorOut.flush(); // why not?

        String stackTrace = exceptionBuffer.toString();

        assertEquals( "Exceptions while running the test",
                      "",
                      stackTrace );
    }

    private static class Pipeline implements EventPipeline, EventHandler
    {
        private final Queue m_queue = new DefaultQueue();
        private final Source[] m_sources = new Source[]{m_queue};
        private final StringBuffer m_result;
        private final PrintWriter m_errorOut;

        Pipeline( StringBuffer resultAccumulator, PrintWriter errorOut )
            throws SinkException
        {
            m_result = resultAccumulator;
            m_errorOut = errorOut;
            // even though TPCThreadManager currently calls event handlers
            // when there is nothing to do, that may change
            m_queue.enqueue( new Object()
            {
            } );
        }

        public EventHandler getEventHandler()
        {
            return this;
        }

        public final Source[] getSources()
        {
            return m_sources;
        }

        public final Sink getSink()
        {
            return m_queue;
        }

        public void handleEvent( Object element )
        {
            handleEvents( new Object[]{element} );
        }

        public void handleEvents( Object[] elements )
        {
            // records the fact that the handler was called
            m_result.append( 'a' );
            try
            {
                // sleeps to occupy the thread and let thread manager try to reschedule
                Thread.sleep( SCHEDULING_TIMEOUT );
                // enqueues another element to be called again
                m_queue.enqueue( new Object()
                {
                } );
            }
            catch( Exception e )
            {
                // fails the test, no exceptions are expected
                e.printStackTrace( m_errorOut );

            }
        }
    }
}
