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

package org.apache.avalon.fortress.impl.factory;

import org.d_haven.mpool.ObjectFactory;

/**
 * NoopObjectFactory is used in situations where no proxied objects are desired.  You are offered
 * no protection with this approach, so caveat emptor.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class NoopObjectFactory extends AbstractObjectFactory
{
    public NoopObjectFactory( final ObjectFactory objectFactory )
    {
        super(objectFactory);
    }

    public Object newInstance() throws Exception
    {
        return m_delegateFactory.newInstance();
    }

    public void dispose( Object object ) throws Exception
    {
        m_delegateFactory.dispose( object );
    }
}
