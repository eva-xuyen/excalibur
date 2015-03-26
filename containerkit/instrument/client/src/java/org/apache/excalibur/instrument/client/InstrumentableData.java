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

package org.apache.excalibur.instrument.client;

public interface InstrumentableData
    extends ElementData
{
    /**
     * Returns the registered flag of the remote object.
     *
     * @return The registered flag of the remote object.
     */
    boolean isRegistered();
    
    /**
     * Gets a thread-safe snapshot of the child instrumentable list.
     *
     * @return A thread-safe snapshot of the child instrumentable list.
     */
    InstrumentableData[] getInstrumentables();
    
    /**
     * Gets a thread-safe snapshot of the instrument list.
     *
     * @return A thread-safe snapshot of the instrument list.
     */
    InstrumentData[] getInstruments();
}
