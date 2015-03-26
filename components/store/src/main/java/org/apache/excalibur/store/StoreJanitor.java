/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.store;

import java.util.Iterator;

/**
 * Interface for the StoreJanitors
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: StoreJanitor.java,v 1.4 2004/02/28 11:47:35 cziegeler Exp $
 */
public interface StoreJanitor
{

    String ROLE = StoreJanitor.class.getName();

    /** register method for the stores */
    void register(Store store);

    /** unregister method for the stores */
    void unregister(Store store);

    /** get an iterator to list registered stores */
    Iterator iterator();
}
