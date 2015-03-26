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

/**
 * Common Reference Proxy implementation.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:16 $
 * @since 4.2
 */
abstract class AbstractReferenceProxy
    implements ReferenceProxy
{
    /** AbstractReferenceProxyLatch which owns the proxy. */
    private AbstractReferenceProxyLatch m_latch;

    /** Name of the proxy. */
    private String m_name;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Create a new AbstractReferenceProxy around a given object.
     *
     * @param object The object to protect with the proxy.
     * @param latch ReferenceProxyLatch which owns the proxy.
     */
    AbstractReferenceProxy( AbstractReferenceProxyLatch latch, String name )
    {
        m_latch = latch;
        m_name = name;
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the name of the proxy.
     *
     * @return The name of the proxy.
     */
    String getName()
    {
        return m_name;
    }

    /**
     * Called when all references to the ReferenceProxy have been removed.
     */
    protected void finalize()
    {
        m_latch.notifyFinalized( this );
    }
}
