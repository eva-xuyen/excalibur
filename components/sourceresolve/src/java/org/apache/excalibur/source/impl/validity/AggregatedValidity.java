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
package org.apache.excalibur.source.impl.validity;

import java.util.Iterator;
import java.util.List;

import org.apache.excalibur.source.SourceValidity;

/**
 * A validation object using a List.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 */
public final class AggregatedValidity
    extends AbstractAggregatedValidity
    implements SourceValidity
{
    /**
     * Check if the component is still valid.
     * If <code>0</code> is returned the isValid(SourceValidity) must be
     * called afterwards!
     * If -1 is returned, the component is not valid anymore and if +1
     * is returnd, the component is valid.
     */
    public int isValid()
    {
        for( final Iterator i = m_list.iterator(); i.hasNext(); )
        {
            final int v = ( (SourceValidity)i.next() ).isValid();
            if( v < 1 )
            {
                return v;
            }
        }
        return 1;
    }

    public int isValid( final SourceValidity validity )
    {
        if( validity instanceof AggregatedValidity )
        {
            final AggregatedValidity other = (AggregatedValidity)validity;
            final List otherList = other.m_list;
            if( m_list.size() != otherList.size() )
            {
                return -1;
            }

            for( final Iterator i = m_list.iterator(), j = otherList.iterator(); i.hasNext(); )
            {
                final SourceValidity srcA = (SourceValidity)i.next();
                final SourceValidity srcB = (SourceValidity)j.next();
                int result = srcA.isValid();
                if ( result == -1) 
                {
                    return -1;
                }
                if ( result == 0 )
                {
                    result = srcA.isValid( srcB );
                    if ( result < 1)
                    {
                        return result;
                    }
                }
            }
            return 1;
        }
        return -1;
    }

}

