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

package org.apache.avalon.fortress.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * This class handles looking up service providers on the class path.
 * It implements the system described in:
 *
 * <a href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service Provider">
 * File Specification Under Service Provider</a>.  Note that this interface is
 * very similar to the one they describe whiehc seems to be missing in the JDK.
 *
 * This class adapted from <code>org.apache.batik.util.Service</code>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version 1.0
 */
public final class Service
{
    private final static Class POOLABLE;

    static
    {
        Class klass = null;
        try
        {
            klass = Thread.currentThread().getContextClassLoader().loadClass( "org.apache.avalon.excalibur.pool.Poolable" );
        }
        catch (Exception e)
        {
            // couldn't find the class, ignore
        }

        POOLABLE = klass;
    }

    private static final String SERVICES = "META-INF/services/";
    private static final HashMap providers = new HashMap();

    /**
     * Private constructor to keep from instantiating this class
     */
    private Service()
    {
    }

    /**
     * Get all the providers for the specified services.
     *
     * @param klass  the interface <code>Class</code>
     * @param loader  the <code>ClassLoader to be used.</code>
     *
     * @return an <code>Iterator</code> for the providers.
     */
    public static synchronized Iterator providers( final Class klass, ClassLoader loader )
    {
        final String serviceFile = SERVICES + klass.getName();

        if ( null == loader )
        {
            loader = klass.getClassLoader();
        }

        Set providerSet = (Set) providers.get( serviceFile );

        if ( null == providerSet )
        {
            providerSet = new HashSet();
            Enumeration enum = null;
            boolean errorOccurred = false;

            providers.put( serviceFile, providerSet );

            try
            {
                enum = loader.getResources( serviceFile );
            }
            catch ( IOException ioe )
            {
                errorOccurred = true;
            }

            if ( !errorOccurred )
            {
                while ( enum.hasMoreElements() )
                {
                    try
                    {
                        final URL url = (URL) enum.nextElement();
                        final InputStream is = url.openStream();
                        final BufferedReader reader = new BufferedReader(
                            new InputStreamReader( is,
                                "UTF-8" ) );

                        String line = reader.readLine();
                        while ( null != line )
                        {
                            try
                            {
                                final int comment = line.indexOf( '#' );

                                if ( comment > -1 )
                                {
                                    line = line.substring( 0, comment );
                                }

                                line.trim();

                                if ( line.length() > 0 )
                                {
                                    // We just want the types, not the instances
                                    providerSet.add( loader.loadClass( line ) );
                                }
                            }
                            catch ( Exception e )
                            {
                                // try the next line
                            }

                            line = reader.readLine();
                        }
                    }
                    catch ( Exception e )
                    {
                        // try the next file
                    }
                }
            }
        }

        return providerSet.iterator();
    }

    /**
     * Get all the providers for the specified services.
     *
     * @param klass  the interface <code>Class</code>
     *
     * @return an <code>Iterator</code> for the providers.
     */
    public static synchronized Iterator providers( final Class klass )
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        return providers( klass, loader );
    }

    /**
     * Provide a way to determine if a Class implements Poolable without
     * requiring it to be in the classpath.
     *
     * @param clazz  the class to test
     * @return <code>true</code> if Poolable is in the classpath and the class implements Poolable
     */
    public static boolean isClassPoolable( Class clazz )
    {
        boolean isPoolable = false;

        if (POOLABLE != null)
        {
            isPoolable = POOLABLE.isAssignableFrom( clazz );
        }

        return isPoolable;
    }
}
