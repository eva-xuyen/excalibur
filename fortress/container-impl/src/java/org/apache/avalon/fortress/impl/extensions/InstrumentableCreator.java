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

package org.apache.avalon.fortress.impl.extensions;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.lifecycle.AbstractCreator;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * The InstrumentableCreator is used as a standard lifecycle
 * extension for containers that support it.
 */
public final class InstrumentableCreator extends AbstractCreator
{
    private final InstrumentManager m_instrumentManager;
    private final boolean m_instrumentEnabled;

    public InstrumentableCreator( final InstrumentManager instrumentManager )
    {
        m_instrumentManager = instrumentManager;
        m_instrumentEnabled = instrumentManager != null;
    }

    /**
     * Assign the instrumentables and InstrumentManageables
     */
    public void create( final Object object, final Context context ) throws Exception
    {
        if ( m_instrumentEnabled && object instanceof Instrumentable )
        {
            final String instrumentableName = (String) context.get( "component.name" );
            final Instrumentable instrumentable = (Instrumentable) object;
            instrumentable.setInstrumentableName( instrumentableName );

            // Get the name from the instrumentable in case it was changed since being set above.
            m_instrumentManager.registerInstrumentable(
                instrumentable, instrumentable.getInstrumentableName() );

        }

        if ( m_instrumentEnabled && object instanceof InstrumentManageable )
        {
            ( (InstrumentManageable) object ).setInstrumentManager( m_instrumentManager );
        }

    }
}
