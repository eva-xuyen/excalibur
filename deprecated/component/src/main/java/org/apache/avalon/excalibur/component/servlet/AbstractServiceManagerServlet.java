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
package org.apache.avalon.excalibur.component.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 * Abstract Servlet which can be used with the ExcaliburServiceManagerServlet
 *  to enable servlets to have access to a ServiceManager as well as logging
 *  and instrumentation features.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
public abstract class AbstractServiceManagerServlet
    extends HttpServlet
    implements Instrumentable
{
    private String m_referenceName;
    private ServiceManager m_serviceManager;
    private Logger m_logger;

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName;

    /** Stores the instruments during initialization. */
    private ArrayList m_instrumentList;

    /** Stores the child instrumentables during initialization. */
    private ArrayList m_childList;

    /** Flag which is to used to keep track of when the Instrumentable has been registered. */
    private boolean m_registered;

    /** Counts the number of times the service is requested. */
    private CounterInstrument m_instrumentRequests;

    /** Records the amount of time execute takes to be processed. */
    private ValueInstrument m_instrumentTime;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new AbstractServiceManagerServlet.
     *
     * @param referenceName A name which does not include any spaces or periods
     *                      that will be used to name the logger category and
     *                      instrumentable which represents this servlet.
     */
    public AbstractServiceManagerServlet( String referenceName )
    {
        //System.out.println( "AbstractServiceManagerServlet( " + referenceName + " )" );
        m_referenceName = referenceName;

        // Set up Instrumentable like AbstractInstrumentable
        m_registered = false;
        m_instrumentList = new ArrayList();
        m_childList = new ArrayList();

        // Create the instruments
        setInstrumentableName( referenceName );
        addInstrument( m_instrumentRequests = new CounterInstrument( "requests" ) );
        addInstrument( m_instrumentTime = new ValueInstrument( "time" ) );
    }

    /*---------------------------------------------------------------
     * HttpServlet Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the servlet container to initialize a servlet before it is
     *  put into service.
     *
     * @param config ServletConfig object for the servlet.
     *
     * @throws ServletException If there are any initialization problems.
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        ServletContext context = config.getServletContext();

        // Initialize logging for the servlet.
        LoggerManager loggerManager =
            (LoggerManager)context.getAttribute( LoggerManager.class.getName() );
        if ( loggerManager == null )
        {
            throw new IllegalStateException(
                "The ExcaliburComponentManagerServlet servlet was not correctly initialized." );
        }
        Logger logger = loggerManager.getLoggerForCategory( "servlet" );
        m_logger = logger.getChildLogger( m_referenceName );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "servlet.init( config )" );
        }

        // Obtain a reference to the ServiceManager
        m_serviceManager =
            (ServiceManager)context.getAttribute( ServiceManager.class.getName() );
        if ( m_serviceManager == null )
        {
            throw new IllegalStateException(
                "The ExcaliburComponentManagerServlet servlet was not correctly initialized." );
        }

        // Register this servlet with the InstrumentManager if it exists.
        InstrumentManager instrumentManager =
            (InstrumentManager)context.getAttribute( InstrumentManager.class.getName() );
        if ( instrumentManager != null )
        {
            try
            {
                instrumentManager.registerInstrumentable(
                    this, "servlets." + getInstrumentableName() );
            }
            catch ( Exception e )
            {
                throw new ServletException(
                    "Unable to register the servlet with the instrument manager.", e );
            }
        }

        // Do this last so the subclasses will be able to access these objects in their
        //  init method.
        super.init( config );
    }

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     *  is being taken out of service.
     */
    public void destroy()
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "servlet.destroy()" );
        }

        // Release the ServiceManager by removing its reference.
        m_serviceManager = null;

        super.destroy();

        // Make sure that the component manager gets collected.
        System.gc();

        // Give the system time for the Gc to complete.  This is necessary to make sure that
        //  the ECMServlet has time to dispose all of its managers before the Tomcat server
        //  invalidates the current class loader.
        try
        {
            Thread.sleep(250);
        }
        catch ( InterruptedException e )
        {
        }
    }

    /**
     * Receives standard HTTP requests from the public service method and dispatches
     *  them to the doXXX methods defined in this class.
     * Overrides the default method to allow for instrumentation.
     *
     * @param request The HttpServletRequest object that contains the request the
     *                client made of the servlet.
     * @param response The HttpServletResponse object that contains the response
     *                 the servlet returns to the client.
     */
    public void service( HttpServletRequest request, HttpServletResponse response )
         throws ServletException, IOException
    {
        if ( getLogger().isDebugEnabled() )
        {
            StringBuffer sb = new StringBuffer( request.getRequestURI() );
            String query = request.getQueryString();
            if ( query != null )
            {
                sb.append( "?" );
                sb.append( query );
            }

            getLogger().debug( "Request: " + sb.toString() );
        }

        long start = System.currentTimeMillis();

        // Notify the Instrument Manager
        m_instrumentRequests.increment();

        super.service( request, response );

        // Notify the Instrument Manager how long the service took.
        if ( m_instrumentTime.isActive() )
        {
            m_instrumentTime.setValue( (int)( System.currentTimeMillis() - start ) );
        }
    }

    /*---------------------------------------------------------------
     * Instrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Gets the name of the Instrumentable.
     *
     * @return The name used to identify a Instrumentable.
     */
    public final String getInstrumentableName()
    {
        return m_instrumentableName;
    }

    /**
     * Sets the name for the Instrumentable.  The Instrumentable Name is used
     *  to uniquely identify the Instrumentable during the configuration of
     *  the InstrumentManager and to gain access to an InstrumentableDescriptor
     *  through the InstrumentManager.  The value should be a string which does
     *  not contain spaces or periods.
     * <p>
     * This value may be set by a parent Instrumentable, or by the
     *  InstrumentManager using the value of the 'instrumentable' attribute in
     *  the configuration of the component.
     *
     * @param name The name used to identify a Instrumentable.
     */
    public final void setInstrumentableName( String name )
    {
        m_instrumentableName = name;
    }

    /**
     * Any Object which implements Instrumentable can also make use of other
     *  Instrumentable child objects.  This method is used to tell the
     *  InstrumentManager about them.
     *
     * @return An array of child Instrumentables.  This method should never
     *         return null.  If there are no child Instrumentables, then
     *         EMPTY_INSTRUMENTABLE_ARRAY can be returned.
     */
    public final Instrumentable[] getChildInstrumentables()
    {
        m_registered = true;
        if( m_childList.size() == 0 )
        {
            return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
        }
        else
        {
            Instrumentable[] children = new Instrumentable[ m_childList.size() ];
            m_childList.toArray( children );
            return children;
        }
    }

    /**
     * Obtain a reference to all the Instruments that the Instrumentable object
     *  wishes to expose.  All sampling is done directly through the
     *  Instruments as opposed to the Instrumentable interface.
     *
     * @return An array of the Instruments available for profiling.  Should
     *         never be null.  If there are no Instruments, then
     *         EMPTY_INSTRUMENT_ARRAY can be returned.  This should never be
     *         the case though unless there are child Instrumentables with
     *         Instruments.
     */
    public final Instrument[] getInstruments()
    {
        m_registered = true;
        if( m_instrumentList.size() == 0 )
        {
            return Instrumentable.EMPTY_INSTRUMENT_ARRAY;
        }
        else
        {
            Instrument[] instruments = new Instrument[ m_instrumentList.size() ];
            m_instrumentList.toArray( instruments );
            return instruments;
        }
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Adds an Instrument to the list of Instruments published by the component.
     *  This method may not be called after the Instrumentable has been
     *  registered with the InstrumentManager.
     *
     * @param instrument Instrument to publish.
     */
    protected void addInstrument( Instrument instrument )
    {
        if( m_registered )
        {
            throw new IllegalStateException( "Instruments can not be added after the "
                + "Instrumentable is registered with the InstrumentManager." );
        }
        m_instrumentList.add( instrument );
    }

    /**
     * Adds a child Instrumentable to the list of child Instrumentables
     *  published by the component.  This method may not be called after the
     *  Instrumentable has been registered with the InstrumentManager.
     * <p>
     * Note that Child Instrumentables must be named by the caller using the
     *  setInstrumentableName method.
     *
     * @param child Child Instrumentable to publish.
     */
    protected void addChildInstrumentable( Instrumentable child )
    {
        if( m_registered )
        {
            throw new IllegalStateException( "Child Instrumentables can not be added after the "
                + "Instrumentable is registered with the InstrumentManager." );
        }
        m_childList.add( child );
    }

    /**
     * Obtain a reference to the servlet's logger.
     *
     * @return The servlet's logger.
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Returns the current ServiceManager.
     *
     * @return The current ServiceManager.
     */
    public ServiceManager getServiceManager()
    {
        return m_serviceManager;
    }
}
