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
package org.apache.avalon.excalibur.monitor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;

/**
 * A class that contains a few utility methods for working
 * creating resource sets from Avalons configuration objects.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:32 $
 */
class MonitorUtil
{
    private static final Class[] c_constructorParams =
        new Class[]{String.class};

    public static Resource[] configureResources( final Configuration[] resources,
                                                 final Logger logger )
    {
        final ArrayList results = new ArrayList();
        for( int i = 0; i < resources.length; i++ )
        {
            final Configuration initialResource = resources[ i ];
            final String key =
                initialResource.getAttribute( "key", "** Unspecified key **" );
            final String className =
                initialResource.getAttribute( "class", "** Unspecified class **" );

            try
            {
                final Resource resource = createResource( className, key );
                results.add( resource );

                if( logger.isDebugEnabled() )
                {
                    final String message =
                        "Initial Resource: \"" + key + "\" Initialized.";
                    logger.debug( message );
                }
            }
            catch( final Exception e )
            {
                if( logger.isWarnEnabled() )
                {
                    final String message =
                        "Initial Resource: \"" + key +
                        "\" Failed (" + className + ").";
                    logger.warn( message, e );
                }
            }
        }

        return (Resource[])results.toArray( new Resource[ results.size() ] );
    }

    private static Resource createResource( final String className,
                                            final String key )
        throws Exception
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Class clazz = loader.loadClass( className );
        final Constructor initializer =
            clazz.getConstructor( c_constructorParams );
        return (Resource)initializer.newInstance( new Object[]{key} );
    }
}
