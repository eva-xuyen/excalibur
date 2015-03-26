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

package org.apache.excalibur.instrument.manager.http.server;

import java.io.InterruptedIOException;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.6 $
 */
abstract class AbstractSocketServer
    extends AbstractLogEnabledInstrumentableStartable
{
    /** Semaphore used to synchronize actions contained in this class. This is to
     *   avoid unintended problems with synchronization performed by subclasses
     *   this is important here because this is a server and actions from one
     *   thread can cause a call out through a socket into another thread and
     *   cause deadlocks. */
    private Object m_semaphore = new Object();
    
    /** The port to listen on for connections. */
    private int m_port;
    
    /** The backlog to assign to the server socket. */
    private int m_backlog = 50;
    
    /** The address to bind the port server to.  Null for any address. */
    private InetAddress m_bindAddr;
    
    /** The SO_TIMEOUT to use for client sockets. */
    private int m_soTimeout = 30000; /* 30 seconds. */
    
    /** The time in ms after the component starts to shutdown that sockets
     *   will have to shutdown on their own before they are closed. */
    private long m_shutdownTimeout = 5000; /* 5 seconds. */
    
    /** Flag which keeps track of when the server has been started. */
    private boolean m_started;
    
    /** Reference to the ServerSocket. */
    private ServerSocket m_serverSocket;
    
    /** Used to track the number of open sockets. */
    private List m_openSockets = new ArrayList();
    
    /** Number of times that the server socket is connected to. */
    private CounterInstrument m_instrumentConnects;
    
    /** Number of sockets that are connected at any given time. */
    private ValueInstrument m_instrumentOpenSockets;
    
    /** Number of times that a connection socket disconnects. */
    private CounterInstrument m_instrumentDisconnects;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractSocketServer.
     *
     * @param port The port on which the server will listen.
     * @param bindAddress The address on which the server will listen for
     *                    connections.
     */
    public AbstractSocketServer( int port, InetAddress bindAddress )
    {
        super();
        m_port = port;
        m_bindAddr = bindAddress;
        
        // Create instruments
        m_instrumentConnects = new CounterInstrument( "connects" );
        m_instrumentOpenSockets = new ValueInstrument( "open-sockets" );
        m_instrumentDisconnects = new CounterInstrument( "disconnects" );
        addInstrument( m_instrumentConnects );
        addInstrument( m_instrumentOpenSockets );
        addInstrument( m_instrumentDisconnects );
    }
    
    /*---------------------------------------------------------------
     * AbstractLogEnabledInstrumentableStartable Methods
     *-------------------------------------------------------------*/
    /**
     * Starts the runner thread.
     *
     * @throws Exception If there are any problems.
     */
    public void start()
        throws Exception
    {
        // Create the server socket.
        try
        {
            m_serverSocket = new ServerSocket( m_port, m_backlog, m_bindAddr );
        }
        catch ( IOException e )
        {
            String msg = "Unable to bind to port " + m_port + ": " + e.getMessage();
            throw new BindException( msg );
        }
        
        super.start();
    }
    
    /**
     * Called when the component is being stopped, the isStopping method will
     *  always return true when this method is called.
     *
     * @throws Exception If there are any problems
     */
    protected void stopRunner()
        throws Exception
    {
        // Close the server socket so it stops blocking for new connections.
        ServerSocket serverSocket = m_serverSocket;
        if ( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch ( IOException e )
            {
                getLogger().debug( "Unable to close the server socket.", e );
            }
        }
        
        // Wait for any sockets that are still open to complete.
        synchronized ( m_semaphore )
        {
            long start = System.currentTimeMillis();
            boolean closed = false;
            int lastSize = 0;
            int size;
            while ( ( size = m_openSockets.size() ) > 0 )
            {
                // Only display the remaining queue size when the number changes.
                if ( lastSize != size )
                {
                    getLogger().debug(
                        "Waiting until " + size + " open sockets have been closed." );
                    lastSize = size;
                }
                
                try
                {
                    // Will be notified whenever a socket is removed from the openSockets list.
                    m_semaphore.wait( 250 );
                }
                catch ( InterruptedException e )
                {
                    // Ignore.
                }
                
                // If we have already waited too long, then try closing the connections.
                long now = System.currentTimeMillis();
                if ( ( !closed )
                    && ( ( m_shutdownTimeout > 0 ) && ( now - start >= m_shutdownTimeout ) ) )
                {
                    getLogger().debug( "Closing " + m_openSockets.size() + " open sockets that did "
                        + "not exit on their own." );
                    
                    for ( Iterator iter = m_openSockets.iterator(); iter.hasNext(); )
                    {
                        Socket socket = (Socket)iter.next();
                        try
                        {
                            socket.close();
                        }
                        catch ( IOException e )
                        {
                            getLogger().debug( "Problem closing socket.", e );
                        }
                    }
                    
                    closed = true;
                }
            }
        }
    }
    
    /**
     * Runner method that will be called when the component is started.
     *  The method must monitor the isStopping() method and make sure
     *  that it returns in a timely manner when the isStopping() method
     *  returns true.
     */
    protected void runner()
    {
        // Set the started flag
        m_started = true;
        
        int workerId = 1;
        try
        {
            // Loop until we are asked to stop.
            while ( !isStopping() )
            {
                try
                {
                    getLogger().debug( "Listen for a connection..." );
                    final Socket socket = m_serverSocket.accept();
                    
                    // Set the SO_TIMEOUT for the socket.
                    socket.setSoTimeout( m_soTimeout );
                    
                    // Set the TCP_NO_DELAY flag for the socket to improve performance.
                    socket.setTcpNoDelay( true );
                    
                    if ( getLogger().isDebugEnabled() )
                    {
                        String remoteIP = socket.getInetAddress().getHostAddress();
                        getLogger().debug( "Accepted a connection from " + remoteIP );
                    }
                    
                    // Increment the number of open sockets.  This is done here rather than in
                    //  handleSocketInner so that it will be incremented before we request a
                    //  worker from the thread pool.  This is necessary to avoid timing problems
                    //  during shutdown of the component.
                    int openSockets;
                    synchronized ( m_semaphore )
                    {
                        m_openSockets.add( socket );
                        openSockets = m_openSockets.size();
                        getLogger().debug( "Open sockets: " + openSockets );
                    }
                    
                    // Notify the instrument manager
                    m_instrumentConnects.increment();
                    m_instrumentOpenSockets.setValue( openSockets );
                    
                    // Handle the socket in a new thread.  May want to use pooling here later.
                    Thread worker = new Thread( "socketWorker." + workerId++ )
                    {
                        public void run()
                        {
                            handleSocketInner( socket );
                        }
                    };
                    worker.start();
                }
                catch ( Throwable t )
                {
                    // Check for throwable type this way rather than with seperate catches
                    //  to work around a problem where InterruptedException can be thrown
                    //  when the compiler gives an error saying that it can't.
                    if ( isStopping()
                        && ( ( t instanceof InterruptedException )
                        || ( t instanceof SocketException )
                        || ( t instanceof InterruptedIOException ) ) )
                    {
                        // This is expected, the service is being stopped.
                    }
                    else
                    {
                        getLogger().error( "Encountered an unexpected error, continuing.", t );
                        
                        // Avoid tight thrashing
                        try
                        {
                            Thread.sleep( 5000 );
                        }
                        catch ( InterruptedException e )
                        {
                            // Ignore
                        }
                    }
                }
            }
        }
        finally
        {
            // Always make sure the server socket is closed.
            try
            {
                m_serverSocket.close();
            }
            catch ( IOException e )
            {
                getLogger().debug( "Unable to close the server socket.", e );
            }
        }
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Handle a newly connected socket.  The implementation need not
     *  worry about closing the socket.
     *
     * @param socket Newly connected Socket to be handled.
     */
    protected abstract void handleSocket( Socket socket );
    
    /**
     * Keeps track of instrumentation related to the life of a socket as well
     *  as handles any errors encountered while handling the socket in the
     *  user code.
     *
     * @param socket The socket to be handled.
     */
    private void handleSocketInner( Socket socket )
    {
        try
        {
            try
            {
                handleSocket( socket );
            }
            finally
            {
                int openSockets;
                synchronized ( m_semaphore )
                {
                    m_openSockets.remove( socket );
                    openSockets = m_openSockets.size();
                    getLogger().debug( "Open sockets: " + openSockets );
                    
                    // Notify the stop method if it is waiting for this socket to complete.
                    m_semaphore.notify();
                }
                
                // Notify the instrument manager
                m_instrumentOpenSockets.setValue( openSockets );
                m_instrumentDisconnects.increment();
                
                // Always close the socket.
                socket.close();
            }
        }
        catch ( Throwable t )
        {
            getLogger().error( "Encountered an error while handling socket: " + socket, t );
        }
    }
    
    protected int getSoTimeout()
    {
        return m_soTimeout;
    }
}
