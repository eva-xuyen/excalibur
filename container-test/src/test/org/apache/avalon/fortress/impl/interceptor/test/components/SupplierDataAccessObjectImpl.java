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
 
package org.apache.avalon.fortress.impl.interceptor.test.components;

import org.apache.avalon.fortress.impl.interceptor.test.examples.FakeTransactionManager;

/**
 * @avalon.component
 * @avalon.service type=SupplierDataAccessObject
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=supplierDAO
 *
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class SupplierDataAccessObjectImpl implements SupplierDataAccessObject
{
    /**
     * @excalibur.transaction required
     */
    public void save( final Object data )
    {
        FakeTransactionManager.instance().startTransaction();
        
        // Some fake work
        
        FakeTransactionManager.instance().endTransaction();
    }

}
