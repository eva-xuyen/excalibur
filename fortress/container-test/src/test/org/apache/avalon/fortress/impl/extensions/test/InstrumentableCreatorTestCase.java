/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.impl.extensions.test;

import junit.framework.TestCase;
import org.apache.avalon.fortress.impl.extensions.InstrumentableCreator;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.lifecycle.Creator;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * InstrumentableCreatorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class InstrumentableCreatorTestCase extends TestCase
{
    private Instrumentable m_instrumentable;
    private DefaultContext m_context;
    private InstrumentManager m_instrumentManager;
    private boolean m_isActive = false;

    public InstrumentableCreatorTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_instrumentable = new TestInstrumentable();
        m_instrumentManager = new TestInstrumentManager();
        m_context = new DefaultContext();
        m_context.put( "component.name", "component1" );
        m_context.makeReadOnly();
    }

    public void testNoInstrumentManager() throws Exception
    {
        Creator creator = new InstrumentableCreator( null );

        creator.create( m_instrumentable, m_context );
        creator.destroy( m_instrumentable, m_context );
    }

    public void testInstrumentManager() throws Exception
    {
        Creator creator = new InstrumentableCreator( m_instrumentManager );
        m_isActive = true;

        creator.create( m_instrumentable, m_context );
        creator.destroy( m_instrumentable, m_context );
    }

    class TestInstrumentable implements Instrumentable, InstrumentManageable
    {
        private static final String DEFAULT_NAME = "test";
        private String m_name = DEFAULT_NAME;
        private String m_assigned;

        public void setInstrumentableName( String name )
        {
            assertTrue( m_isActive );
            assertNotNull( name );
            m_name = name;
            m_assigned = m_name;
        }

        public String getInstrumentableName()
        {
            assertTrue( m_isActive );
            assertNotNull( m_name );

            if ( null == m_assigned )
            {
                assertEquals( DEFAULT_NAME, m_name );
            }
            else
            {
                assertEquals( m_assigned, m_name );
            }

            return m_name;
        }

        public Instrument[] getInstruments()
        {
            assertTrue( m_isActive );
            return new Instrument[0];
        }

        public Instrumentable[] getChildInstrumentables()
        {
            assertTrue( m_isActive );
            return new Instrumentable[0];
        }

        public void setInstrumentManager( InstrumentManager instrumentManager )
        {
            assertTrue( m_isActive );
        }
    }
}
