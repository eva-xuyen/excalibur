/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.avalon.excalibur.i18n;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

/**
 * Manager for resources.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ResourceManager
{
    /**
     * Permission needed to clear complete cache.
     */
    private static final RuntimePermission CLEAR_CACHE_PERMISSION =
        new RuntimePermission( "i18n.clearCompleteCache" );
    private static final HashMap c_resources = new HashMap();

    /**
     * Retrieve resource with specified basename.
     *
     * @param baseName the basename
     * @return the Resources
     */
    public static final Resources getBaseResources( final String baseName )
    {
        return getBaseResources( baseName, null );
    }

    /**
     * Retrieve resource with specified basename.
     *
     * @param baseName the basename
     * @param locale Locale of the requested Resources, null implies default locale
     * @param classLoader the classLoader to load resources from
     *
     * @return the Resources
     */
    public synchronized static final Resources getBaseResources( final String baseName,
                                                                 final Locale locale,
                                                                 final ClassLoader classLoader )
    {
        String cacheKey;
        if ( locale == null )
        {
            cacheKey = baseName;
        }
        else
        {
            cacheKey = baseName + "-" + locale.toString();
        }
        
        Resources resources = getCachedResource( cacheKey );
        if( null == resources )
        {
            if ( locale == null )
            {
                resources = new Resources( baseName, classLoader );
            }
            else
            {
                resources = new Resources( baseName, locale, classLoader );
            }
            
            putCachedResource( cacheKey, resources );
        }

        return resources;
    }

    /**
     * Retrieve resource with specified basename.
     *
     * @param baseName the basename
     * @param classLoader the classLoader to load resources from
     *
     * @return the Resources
     */
    public synchronized static final Resources getBaseResources( final String baseName,
                                                                 final ClassLoader classLoader )
    {
        return getBaseResources( baseName, null, classLoader );
    }

    /**
     * Clear the cache of all resources currently loaded into the
     * system. This method is useful if you need to dump the complete
     * cache and because part of the application is reloading and
     * thus the resources may need to be reloaded.
     *
     * <p>Note that the caller must have been granted the
     * "i18n.clearCompleteCache" {@link RuntimePermission} or
     * else a security exception will be thrown.</p>
     *
     * @throws SecurityException if the caller does not have
     *                           permission to clear cache
     */
    public synchronized static final void clearResourceCache()
        throws SecurityException
    {
        final SecurityManager sm = System.getSecurityManager();
        if( null != sm )
        {
            sm.checkPermission( CLEAR_CACHE_PERMISSION );
        }

        c_resources.clear();
    }

    /**
     * Cache specified resource in weak reference.
     *
     * @param cacheKey the key used to reference the resource in the cache
     * @param resources the resources object
     */
    private synchronized static final void putCachedResource( final String cacheKey,
                                                              final Resources resources )
    {
        c_resources.put( cacheKey, new WeakReference( resources ) );
    }

    /**
     * Retrieve cached resource.
     *
     * @param cacheKey the key used to reference the resource in the cache
     *
     * @return resources the resources object
     */
    private synchronized static final Resources getCachedResource( final String cacheKey )
    {
        final WeakReference weakReference = (WeakReference)c_resources.get( cacheKey );
        if( null == weakReference )
        {
            return null;
        }
        else
        {
            return (Resources)weakReference.get();
        }
    }

    /**
     * Retrieve resource for specified name.
     * The basename is determined by name postfixed with ".Resources".
     *
     * @param name the name to use when looking up resources
     * @param locale Locale of the requested Resources, null implies default locale
     *
     * @return the Resources
     */
    public static final Resources getResources( final String name, final Locale locale )
    {
        return getBaseResources( name + ".Resources", locale, null );
    }

    /**
     * Retrieve resource for specified name.
     * The basename is determined by name postfixed with ".Resources".
     *
     * @param name the name to use when looking up resources
     *
     * @return the Resources
     */
    public static final Resources getResources( final String name )
    {
        return getResources( name, null );
    }

    /**
     * Retrieve resource for specified Classes package.
     * The basename is determined by name of classes package
     * postfixed with ".Resources".
     *
     * @param clazz the Class
     * @param locale Locale of the requested Resources, null implies default locale
     *
     * @return the Resources
     */
    public static final Resources getPackageResources( final Class clazz, final Locale locale )
    {
        return getBaseResources(
            getPackageResourcesBaseName( clazz ), locale, clazz.getClassLoader() );
    }

    /**
     * Retrieve resource for specified Classes package.
     * The basename is determined by name of classes package
     * postfixed with ".Resources".
     *
     * @param clazz the Class
     *
     * @return the Resources
     */
    public static final Resources getPackageResources( final Class clazz )
    {
        return getPackageResources( clazz, null );
    }

    /**
     * Retrieve resource for specified Class.
     * The basename is determined by name of Class
     * postfixed with "Resources".
     *
     * @param clazz the Class
     * @param locale Locale of the requested Resources, null implies default locale
     *
     * @return the Resources
     */
    public static final Resources getClassResources( final Class clazz, final Locale locale )
    {
        return getBaseResources(
            getClassResourcesBaseName( clazz ), locale, clazz.getClassLoader() );
    }

    /**
     * Retrieve resource for specified Class.
     * The basename is determined by name of Class
     * postfixed with "Resources".
     *
     * @param clazz the Class
     *
     * @return the Resources
     */
    public static final Resources getClassResources( final Class clazz )
    {
        return getClassResources( clazz, null );
    }

    /**
     * Retrieve resource basename for specified Classes package.
     * The basename is determined by name of classes package
     * postfixed with ".Resources".
     *
     * @param clazz the Class
     *
     * @return the resource basename
     */
    public static final String getPackageResourcesBaseName( final Class clazz )
    {
        final Package pkg = clazz.getPackage();

        String baseName;
        if( null == pkg )
        {
            final String name = clazz.getName();
            if( -1 == name.lastIndexOf( "." ) )
            {
                baseName = "Resources";
            }
            else
            {
                baseName = name.substring( 0, name.lastIndexOf( "." ) ) + ".Resources";
            }
        }
        else
        {
            baseName = pkg.getName() + ".Resources";
        }

        return baseName;
    }

    /**
     * Retrieve resource basename for specified Class.
     * The basename is determined by name of Class
     * postfixed with "Resources".
     *
     * @param clazz the Class
     *
     * @return the resource basename
     */
    public static final String getClassResourcesBaseName( final Class clazz )
    {
        return clazz.getName() + "Resources";
    }

    /**
     * Private Constructor to block instantiation.
     */
    private ResourceManager()
    {
    }
}
