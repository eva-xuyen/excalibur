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
package org.apache.excalibur.event.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.UnboundedFifoBuffer;
import org.apache.excalibur.event.*;
import org.apache.excalibur.event.impl.DefaultQueue;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * The CommandManager handles asynchronous commands from the rest of the
 * system.  The only exposed piece is the Queue that other components use to
 * give Commands to this system.  You <strong>must</strong> register this
 * with a ThreadManager for it to work.
 *
 * <p><strong>Source Example</strong></p>
 * <pre>

 //
 // Set up the ThreadManager that the CommandManager will use
 //

 ThreadManager threadManager = new TPCThreadManager();
 threadManager.enableLogging( getLogger().getChildLogger("threadmanager") );
 Parameters params = new Parameters();
 params.setParameter( "threads-per-processor", "2" );
 params.setParameter( "sleep-time", "1000" );
 params.setParameter( "block-timeout", "250" );
 threadManager.parameterize( params );
 threadManager.initialize();

 //
 // Set up the CommandManager
 //

 CommandManager commandManager = new CommandManager();
 commandManager.enableLogging( getLogger().getChildLogger("commandmanager") );
 threadManager.register( commandManager );
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class CommandManager extends AbstractLogEnabled
        implements EventPipeline, Disposable, EnqueuePredicate {
    private final Queue m_queue;
    private final HashMap m_signalHandlers;
    private final ReentrantLock m_mutex;
    private final EventHandler m_eventHandler;
    private final Source[] m_sources;
    private CommandFailureHandler m_failureHandler;
    private boolean m_isAccepting;

    /**
     * Create the CommandManager
     */
    public CommandManager() {
        m_queue = new DefaultQueue();
        m_signalHandlers = new HashMap();
        m_mutex = new ReentrantLock();
        m_eventHandler = new CommandEventHandler(Collections.unmodifiableMap(m_signalHandlers));
        m_sources = new Source[]{m_queue};
        m_failureHandler = NullCommandFailureHandler.SHARED_INSTANCE;
        m_queue.setEnqueuePredicate(this);
        m_isAccepting = true;

        // if no logger is set ensure a valid one is there.
        enableLogging(new NullLogger());
    }

    /**
     * Set the failure handler that the application can use to override what happens when a command
     * failure happens.
     *
     * @param handler  the new Handler
     * @throws NullPointerException if "handler" is null.
     */
    public void setCommandFailureHandler(final CommandFailureHandler handler) {
        if (null == handler) throw new NullPointerException("handler");
        m_failureHandler = handler;
    }

    /**
     * Get the failure handler so that CommandManager can use it when a problem happens.
     *
     * @return the failure handler.
     */
    protected CommandFailureHandler getCommandFailureHandler() {
        return m_failureHandler;
    }

    /**
     * Get the Command Sink so that you can enqueue new commands.
     *
     * @return the Sink that feeds the CommandManager
     */
    public final Sink getCommandSink() {
        return m_queue;
    }

    /**
     * Register a Signal with an EventHandler.  The Signal is a special object
     * that implements the {@link Signal} interface.  When CommandManager recieves
     * events that match the Signal, it will send a copy of it to all the
     * {@link EventHandler}s attached to it.
     *
     * @param signal   The signal we are listening for.
     * @param handler  The handler that wants to be notified
     */
    public final void registerSignalHandler(Signal signal, EventHandler handler) {
        try {
            m_mutex.acquire();
            ArrayList handlers = (ArrayList) m_signalHandlers.get(signal.getClass());

            if (null == handlers) {
                handlers = new ArrayList();
            }

            if (!handlers.contains(handler)) {
                handlers.add(handler);

                m_signalHandlers.put(signal.getClass(), handlers);
            }
        } catch (InterruptedException ie) {
            // ignore for now
        } finally {
            m_mutex.release();
        }
    }

    /**
     * Deregister a Signal with an EventHandler.  Stop notifying the particular
     * EventHandler that is passed in about the associated Signal.
     *
     * @param signal   The signal we are listening for.
     * @param handler  The handler that wants to be notified
     */
    public final void deregisterSignalHandler(Signal signal, EventHandler handler) {
        try {
            m_mutex.acquire();
            ArrayList handlers = (ArrayList) m_signalHandlers.get(signal.getClass());

            if (null != handlers) {
                if (handlers.remove(handler)) {
                    m_signalHandlers.put(signal.getClass(), handlers);
                }

                if (0 == handlers.size()) {
                    m_signalHandlers.remove(signal.getClass());
                }
            }
        } catch (InterruptedException ie) {
            // ignore for now
        } finally {
            m_mutex.release();
        }
    }

    /**
     * When you are done with CommandManager, call this and it will
     * clean up all its resources.
     */
    public void dispose() {
        m_isAccepting = false;
        Object[] remainingElements = m_queue.dequeueAll();
        for (int i = 0; i < remainingElements.length; i++) {
            getEventHandler().handleEvent(remainingElements[i]);
        }
    }

    /**
     * Used by the Threadmanager to get the sources that are feeding
     * the CommandManager.
     *
     * @return the Array of one Source
     */
    public final Source[] getSources() {
        return m_sources;
    }

    /**
     * Used by the ThreadManager to get the EventHandler for the
     * CommandManager.
     *
     * @return the EventHandler
     */
    public final EventHandler getEventHandler() {
        return m_eventHandler;
    }

    private final class CommandEventHandler implements EventHandler {
        private final Map m_signalHandlers;
        private final Buffer m_delayedCommands = new UnboundedFifoBuffer();

        protected CommandEventHandler(Map signalHandlers) {
            m_signalHandlers = signalHandlers;
        }

        public final void handleEvents(Object[] elements) {
            for (int i = 0; i < elements.length; i++) {
                handleEvent(elements[i]);
            }

            int size = m_delayedCommands.size();
            for (int i = 0; i < size; i++) {
                DelayedCommandInfo command = (DelayedCommandInfo) m_delayedCommands.remove();

                if (System.currentTimeMillis() >= command.m_nextRunTime) {
                    try {
                        command.m_command.execute();
                    } catch (Exception e) {
                        if (getLogger().isWarnEnabled()) {
                            getLogger().warn("Exception during Command.execute()", e);
                        }
                    }

                    command.m_numExecutions++;

                    if (command.m_repeatable) {
                        RepeatedCommand cmd = (RepeatedCommand) command.m_command;
                        int numRepeats = cmd.getNumberOfRepeats();

                        if ((numRepeats < 1) || (command.m_numExecutions < numRepeats)) {
                            command.m_nextRunTime = System.currentTimeMillis() +
                                    cmd.getRepeatInterval();
                            m_delayedCommands.add(command);
                        }
                    }
                } else {
                    // not yet executed, wait some more
                    m_delayedCommands.add(command);
                }
            }
        }

        public final void handleEvent(Object element) {
            if (!(element instanceof Signal)) {
                return;
            }

            if (!(element instanceof Command)) {
                ArrayList handlers = (ArrayList) m_signalHandlers.get(element.getClass());

                if (null != handlers) {
                    Iterator i = handlers.iterator();

                    while (i.hasNext()) {
                        EventHandler handler = (EventHandler) i.next();
                        handler.handleEvent(element);
                    }
                }

                return;
            }

            if (element instanceof DelayedCommand) {
                DelayedCommandInfo commandInfo = new DelayedCommandInfo();
                commandInfo.m_command = (DelayedCommand) element;
                commandInfo.m_nextRunTime = System.currentTimeMillis() +
                        commandInfo.m_command.getDelayInterval();
                commandInfo.m_numExecutions = 0;
                commandInfo.m_repeatable = element instanceof RepeatedCommand;

                m_delayedCommands.add(commandInfo);
                return;
            }

            try {
                ((Command) element).execute();
            } catch (Exception e) {
                boolean stopProcessing =
                        getCommandFailureHandler().handleCommandFailure((Command) element, e);

                /* If we are no longer processing, then we clear out the Queue and refuse to accept
                 * any more commands.  Essentially the CommandManager is closed.
                 */
                if (stopProcessing) {
                    m_isAccepting = false;
                    m_queue.dequeueAll();
                }
            }
        }
    }

    private static final class DelayedCommandInfo {
        protected DelayedCommand m_command;
        protected long m_nextRunTime;
        protected int m_numExecutions;
        protected boolean m_repeatable;
    }

    /**
     * Tests the given element for acceptance onto the m_sink.
     * @since Feb 10, 2003
     *
     * @param  element  The element to enqueue
     * @param  modifyingSink  The sink that is used for this predicate
     * @return
     *  <code>true</code> if the sink accepts the element;
     *  <code>false</code> otherwise.
     */
    public boolean accept(Object element, Sink modifyingSink) {
        return m_isAccepting;
    }

    /**
     * Tests the given element for acceptance onto the m_sink.
     * @since Feb 10, 2003
     *
     * @param  elements  The array of elements to enqueue
     * @param  modifyingSink  The sink that is used for this predicate
     * @return
     *  <code>true</code> if the sink accepts all the elements;
     *  <code>false</code> otherwise.
     */
    public boolean accept(Object elements[], Sink modifyingSink) {
        return m_isAccepting;
    }
}
