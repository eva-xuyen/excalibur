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

package org.apache.avalon.fortress.util.test;

import org.apache.avalon.lifecycle.Creator;
import org.apache.avalon.framework.context.Context;
import junit.framework.Assert;

/**
 * TestCreator does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class TestCreator extends Assert implements Creator
{
    public final int m_id;

    public TestCreator(int id)
    {
        m_id = id;
    }

    /**
     * Create stage handler.
     *
     * @param object the object that is being created
     * @param context the context instance required by the create handler
     *    implementation
     * @exception Exception if an error occurs
     */
    public void create( Object object, Context context ) throws Exception
    {
        assertNotNull(object);
        assertNotNull(context);
    }

    /**
     * Destroy stage handler.
     *
     * @param object the object that is being destroyed
     * @param context the context instance required by the handler
     *    implementation
     */
    public void destroy( Object object, Context context )
    {
        assertNotNull( object );
        assertNotNull( context );
    }
}
