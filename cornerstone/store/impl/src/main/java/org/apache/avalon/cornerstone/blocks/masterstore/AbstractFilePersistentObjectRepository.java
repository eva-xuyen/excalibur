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
package org.apache.avalon.cornerstone.blocks.masterstore;

import org.apache.avalon.cornerstone.services.store.ObjectRepository;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public abstract class AbstractFilePersistentObjectRepository extends AbstractFileRepository
        implements ObjectRepository {
    /**
     * Get the object associated to the given unique key.
     */
    public synchronized Object get( final String key )
    {
        try
        {
            final InputStream inputStream = getInputStream( key );

            if( inputStream == null )
                throw new NullPointerException( "Null input stream returned for key: " + key );
            try
            {
                final ObjectInputStream stream = new ObjectInputStream( inputStream );

                if( stream == null )
                    throw new NullPointerException( "Null stream returned for key: " + key );

                final Object object = stream.readObject();
                if( DEBUG )
                {
                    monitor.returningObjectForKey(File_Persistent_Object_Repository.class, object, key);
                }
                return object;
            }
            finally
            {
                inputStream.close();
            }
        }
        catch( final Throwable e )
        {
            throw new RuntimeException(
                "Exception caught while retrieving an object, cause: " + e.toString() );
        }
    }

    public synchronized Object get( final String key, final ClassLoader classLoader )
    {
        try
        {
            final InputStream inputStream = getInputStream( key );

            if( inputStream == null )
                throw new NullPointerException( "Null input stream returned for key: " + key );

            try
            {
                final ObjectInputStream stream = new ClassLoaderObjectInputStream( classLoader, inputStream );

                if( stream == null )
                    throw new NullPointerException( "Null stream returned for key: " + key );

                final Object object = stream.readObject();

                if( DEBUG )
                {
                    monitor.returningObjectForKey(File_Persistent_Object_Repository.class, object, key);
                }
                return object;
            }
            finally
            {
                inputStream.close();
            }
        }
        catch( final Throwable e )
        {
            throw new RuntimeException( "Exception caught while retrieving an object: " + e );
        }

    }

    /**
     * Store the given object and associates it to the given key
     */
    public synchronized void put( final String key, final Object value )
    {
        try
        {
            final OutputStream outputStream = getOutputStream( key );

            try
            {
                final ObjectOutputStream stream = new ObjectOutputStream( outputStream );
                stream.writeObject( value );
                if( DEBUG ) monitor.storingObjectForKey(File_Persistent_Object_Repository.class, value, key);
            }
            finally
            {
                outputStream.close();
            }
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Exception caught while storing an object: " + e );
        }
    }

    protected String getExtensionDecorator()
    {
        return ".FileObjectStore";
    }
}
