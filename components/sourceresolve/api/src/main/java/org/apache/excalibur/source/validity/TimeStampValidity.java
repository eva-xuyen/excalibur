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
package org.apache.excalibur.source.validity;

import org.apache.excalibur.source.SourceValidity;

/**
 * A validation object for time-stamps.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TimeStampValidity.java 641953 2008-03-27 19:09:20Z cziegeler $
 */
public final class TimeStampValidity
    implements SourceValidity
{
    private long m_timeStamp;

    public TimeStampValidity( final long timeStamp )
    {
        m_timeStamp = timeStamp;
    }

    /**
     * Check if the component is still valid.
     * If <code>0</code> is returned the isValid(SourceValidity) must be
     * called afterwards!
     * If -1 is returned, the component is not valid anymore and if +1
     * is returnd, the component is valid.
     */
    public int isValid()
    {
        return 0;
    }

    public int isValid( SourceValidity newValidity )
    {
        if( newValidity instanceof TimeStampValidity )
        {
            final long timeStamp =
                ( (TimeStampValidity)newValidity ).getTimeStamp();
            return (m_timeStamp == timeStamp ? +1 : -1);
        }
        return -1;
    }

    public long getTimeStamp()
    {
        return m_timeStamp;
    }

    public String toString()
    {
        return "TimeStampValidity: " + m_timeStamp;
    }
}
