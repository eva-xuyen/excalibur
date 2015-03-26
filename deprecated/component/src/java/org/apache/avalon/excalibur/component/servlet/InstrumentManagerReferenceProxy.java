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
package org.apache.avalon.excalibur.component.servlet;

import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * Reference Proxy to an InstrumentManager
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
final class InstrumentManagerReferenceProxy
    extends AbstractReferenceProxy
    implements InstrumentManager
{
    private InstrumentManager m_instrumentManager;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new proxy.
     *
     * @param instrumentManager InstrumentManager being proxied.
     * @param latch Latch wich will be notified when this proxy is finalized.
     * @param name Name of the proxy.
     */
    InstrumentManagerReferenceProxy( InstrumentManager instrumentManager,
                                     AbstractReferenceProxyLatch latch,
                                     String name )
    {
        super( latch, name );
        m_instrumentManager = instrumentManager;
    }

    /*---------------------------------------------------------------
     * InstrumentManager Methods
     *-------------------------------------------------------------*/
    /**
     * Instrumentable to be registered with the instrument manager.  Should be
     *  called whenever an Instrumentable is created.  The '.' character is
     *  used to denote a child Instrumentable and can be used to register the
     *  instrumentable at a specific point in an instrumentable hierarchy.
     *
     * @param instrumentable Instrumentable to register with the InstrumentManager.
     * @param instrumentableName The name to use when registering the Instrumentable.
     *
     * @throws Exception If there were any problems registering the Instrumentable.
     */
    public void registerInstrumentable( Instrumentable instrumentable, String instrumentableName )
        throws Exception
    {
        m_instrumentManager.registerInstrumentable( instrumentable, instrumentableName );
    }
}
