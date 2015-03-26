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

package org.apache.avalon.fortress.util.test;

import org.apache.avalon.lifecycle.Accessor;
import org.apache.avalon.framework.context.Context;
import junit.framework.Assert;

/**
 * TestAccessor does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class TestAccessor extends Assert implements Accessor
{
    public final int m_id;

    public TestAccessor( int id )
    {
        m_id = id;
    }

    public void access( Object object, Context context ) throws Exception
    {
        assertNotNull( object );
        assertNotNull( context );
    }

    public void release( Object object, Context context )
    {
        assertNotNull( object );
        assertNotNull( context );
    }
}
