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
package org.apache.avalon.excalibur.component.example_im;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

/**
 * This example application creates a component which registers several
 *  Instruments for the example.
 *
 * Note, this code ignores exceptions to keep the code simple.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/17 13:22:37 $
 * @since 4.1
 */
public class DefaultExampleInstrumentable
    extends AbstractLogEnabled
    implements ExampleInstrumentable, Startable, Runnable, Instrumentable
{
    public static final String INSTRUMENT_RANDOM_QUICK_NAME = "random-quick";
    public static final String INSTRUMENT_RANDOM_SLOW_NAME = "random-slow";
    public static final String INSTRUMENT_RANDOM_RANDOM_NAME = "random-random";
    public static final String INSTRUMENT_COUNTER_QUICK_NAME = "counter-quick";
    public static final String INSTRUMENT_COUNTER_SLOW_NAME = "counter-slow";
    public static final String INSTRUMENT_COUNTER_RANDOM_NAME = "counter-random";
    public static final String INSTRUMENT_DOACTION_NAME = "doaction-counter";

    /** Instrumentable Name assigned to this Instrumentable */
    private String m_instrumentableName;

    /** Instrument used to profile random values with lots of updates. */
    private ValueInstrument m_randomQuickInstrument;

    /** Instrument used to profile random values with few of updates. */
    private ValueInstrument m_randomSlowInstrument;

    /** Instrument used to profile random values with updates at a random rate. */
    private ValueInstrument m_randomRandomInstrument;

    /** Instrument used to profile random actions with lots of updates. */
    private CounterInstrument m_counterQuickInstrument;

    /** Instrument used to profile random actions with few of updates. */
    private CounterInstrument m_counterSlowInstrument;

    /** Instrument used to profile random actions with updates at a random rate. */
    private CounterInstrument m_counterRandomInstrument;

    /** Instrument used to count the number of times that doAction is called. */
    private CounterInstrument m_doActionInstrument;

    /** Thread which is used to send profile data to the random instruments. */
    private Thread m_runner;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public DefaultExampleInstrumentable()
    {
        // Initialize the Instrumentable elements.
        m_randomQuickInstrument = new ValueInstrument( INSTRUMENT_RANDOM_QUICK_NAME );
        m_randomSlowInstrument = new ValueInstrument( INSTRUMENT_RANDOM_SLOW_NAME );
        m_randomRandomInstrument = new ValueInstrument( INSTRUMENT_RANDOM_RANDOM_NAME );
        m_counterQuickInstrument = new CounterInstrument( INSTRUMENT_COUNTER_QUICK_NAME );
        m_counterSlowInstrument = new CounterInstrument( INSTRUMENT_COUNTER_SLOW_NAME );
        m_counterRandomInstrument = new CounterInstrument( INSTRUMENT_COUNTER_RANDOM_NAME );
        m_doActionInstrument = new CounterInstrument( INSTRUMENT_DOACTION_NAME );
    }

    /*---------------------------------------------------------------
     * ExampleInstrumentable Methods
     *-------------------------------------------------------------*/
    /**
     * Example action method.
     */
    public void doAction()
    {
        getLogger().debug( "ExampleInstrumentable.doAction() called." );

        // Notify the profiler.
        m_doActionInstrument.increment();
    }

    /*---------------------------------------------------------------
     * Startable Methods
     *-------------------------------------------------------------*/
    /**
     * Start the component.
     */
    public void start()
    {
        if( m_runner == null )
        {
            m_runner = new Thread( this, "ExampleInstrumentableRunner" );
            m_runner.start();
        }
    }

    /**
     * Stop the component.
     */
    public void stop()
    {
        if( m_runner != null )
        {
            m_runner.interrupt();
            m_runner = null;
        }
    }

    /*---------------------------------------------------------------
     * Runnable Methods
     *-------------------------------------------------------------*/
    /**
     * Runner thread which is responsible for sending data to the Profiler via
     *  the various random Profile Points.
     */
    public void run()
    {
        int counter = 0;
        while( m_runner != null )
        {
            // Add some delay to the loop.
            try
            {
                Thread.sleep( 100 );
            }
            catch( InterruptedException e )
            {
                if( m_runner == null )
                {
                    return;
                }
            }

            // Handle the quick Profile Points
            m_randomQuickInstrument.setValue( (int)( Math.random() * 100 ) );
            m_counterQuickInstrument.increment();

            // Handle the slow Profile Points
            counter++;
            if( counter >= 20 )
            {
                m_randomSlowInstrument.setValue( (int)( Math.random() * 100 ) );
                m_counterSlowInstrument.increment();
                counter = 0;
            }

            // Handle the random Profile Points.  Fire 10% of the time.
            if( 100 * Math.random() < 10 )
            {
                m_randomRandomInstrument.setValue( (int)( Math.random() * 100 ) );
                m_counterRandomInstrument.increment();
            }
        }
    }

    /*---------------------------------------------------------------
     * Instrumentable Methods
     *-------------------------------------------------------------*/
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
    public void setInstrumentableName( String name )
    {
        m_instrumentableName = name;
    }

    /**
     * Gets the name of the Instrumentable.
     *
     * @return The name used to identify a Instrumentable.
     */
    public String getInstrumentableName()
    {
        return m_instrumentableName;
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
    public Instrument[] getInstruments()
    {
        return new Instrument[]
        {
            m_randomQuickInstrument,
            m_randomSlowInstrument,
            m_randomRandomInstrument,
            m_counterQuickInstrument,
            m_counterSlowInstrument,
            m_counterRandomInstrument,
            m_doActionInstrument
        };
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
    public Instrumentable[] getChildInstrumentables()
    {
        // This instrumentable does not have any children.
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }
}

