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

import org.d_haven.event.Sink;
import org.d_haven.event.SinkException;
import org.d_haven.event.PreparedEnqueue;
import org.d_haven.event.command.CommandManager;
import org.d_haven.event.command.Command;

/**
 * Created by IntelliJ IDEA. User: bloritsch Date: Jun 24, 2004 Time:
 * 2:59:37 PM To change this template use File | Settings | File
 * Templates.
 */
public class CommandSink implements Sink
{
    private final CommandManager m_manager;

    public CommandSink(CommandManager manager)
    {
        if (manager == null) throw new IllegalArgumentException("manager");

        m_manager = manager;
    }

    public void enqueue( Object o ) throws SinkException
    {
        checkValid( o );
        m_manager.enqueueCommand( (Command)o );
    }

    private void checkValid( Object o )
            throws SinkException
    {
        if ( ! (o instanceof Command) ) throw new SinkException("The object must be a command");
    }

    public void enqueue( Object[] objects ) throws SinkException
    {
        for (int i = 0; i < objects.length; i++)
        {
            checkValid( objects[i] );
        }

        for (int i = 0; i < objects.length; i++)
        {
            m_manager.enqueueCommand( (Command) objects[i] );
        }
    }

    public boolean tryEnqueue( Object o )
    {
        boolean didEnqueue = true;

        try
        {
            enqueue(o);
        }
        catch (SinkException se)
        {
            didEnqueue = false;
        }

        return didEnqueue;
    }

    public PreparedEnqueue prepareEnqueue( Object[] objects )
            throws SinkException
    {
        throw new SinkException("Method not supported");
    }

    public int size()
    {
        return -1;
    }
}
