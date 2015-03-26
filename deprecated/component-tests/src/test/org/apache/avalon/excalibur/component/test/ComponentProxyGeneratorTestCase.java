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
package org.apache.avalon.excalibur.component.test;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.component.ComponentProxyGenerator;
import org.apache.avalon.framework.component.Component;

/**
 * Create a Component proxy.  Requires JDK 1.3+
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class ComponentProxyGeneratorTestCase
    extends TestCase
{
    public ComponentProxyGeneratorTestCase( String name )
    {
        super( name );
    }

    public void testGenerateComponent()
        throws Exception
    {
        Integer testInt = new Integer( 7 );
        ComponentProxyGenerator proxyGen = new ComponentProxyGenerator();

        final Component component =
            proxyGen.getProxy( "java.lang.Comparable", testInt );
        assertTrue( component != null );
        assertTrue( component instanceof Comparable );

        Comparable comp = (Comparable)component;
        assertEquals( 0, comp.compareTo( testInt ) );

        /* Please note one important limitation of using the Proxy on final
         * classes like Integer.  I cannot create a proxy on Integer, but I
         * can on interfaces it implements like Comparable.  I can safely
         * compare the proxied class against the original Integer, but I
         * cannot compare the original Integer against the proxied class.
         * there ends up a class cast exception within the Integer.compareTo
         * method.
         */
    }
}
