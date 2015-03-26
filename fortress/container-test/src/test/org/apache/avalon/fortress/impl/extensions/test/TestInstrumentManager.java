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

package org.apache.avalon.fortress.impl.extensions.test;

import junit.framework.Assert;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.Instrumentable;

/**
 * TestInstrumentManager does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class TestInstrumentManager extends Assert implements InstrumentManager
{
    public void registerInstrumentable( Instrumentable instrumentable, String instrumentableName ) throws Exception
    {
        String name = instrumentable.getInstrumentableName();
        assertNotNull( name );

        name = "registered:" + instrumentableName;
        instrumentable.setInstrumentableName( name );
        assertEquals( name, instrumentable.getInstrumentableName() );

        assertNotNull( instrumentable.getChildInstrumentables() );
        assertNotNull( instrumentable.getInstruments() );
    }
}
