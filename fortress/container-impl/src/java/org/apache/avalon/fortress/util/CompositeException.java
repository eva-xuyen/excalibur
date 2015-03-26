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

package org.apache.avalon.fortress.util;

import org.apache.avalon.framework.CascadingException;

/**
 * This is an exception made up of one or more subexceptions.
 */
public final class CompositeException extends CascadingException
{
    private final Exception[] m_ex;
    private final String m_message;

    public CompositeException( final Exception[] ex )
    {
        this( ex, null );
    }

    public CompositeException( final Exception[] ex, final String message )
    {
        super( message, null );
        m_ex = ex;
        if ( ex == null || ex.length < 1 )
        {
            throw new IllegalArgumentException( "you must specify a contained exception!" );
        }
        if ( message == null )
        {
            final StringBuffer msg = new StringBuffer();
            for ( int i = 0; i < ex.length; i++ )
            {
                if (i > 0) msg.append('\n');
                msg.append( ex[i].getMessage() );
            }
            m_message = msg.toString();
        }
        else
            m_message = message;
    }

    public String getMessage()
    {
        return m_message;
    }

    public Exception[] getExceptions()
    {
        return m_ex;
    }
}
