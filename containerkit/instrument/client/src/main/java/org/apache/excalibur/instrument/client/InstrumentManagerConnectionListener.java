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
package org.apache.excalibur.instrument.client;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public interface InstrumentManagerConnectionListener
{
    /**
     * Called when the connection is opened.  May be called more than once if 
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was opened.
     */
    void opened( InstrumentManagerConnection connection );
    
    /**
     * Called when the connection is closed.  May be called more than once if 
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was closed.
     */
    void closed( InstrumentManagerConnection connection );
    
    /**
     * Called when the connection is deleted.  All references should be removed.
     *
     * @param connection Connection which was deleted.
     */
    void deleted( InstrumentManagerConnection connection );
}

