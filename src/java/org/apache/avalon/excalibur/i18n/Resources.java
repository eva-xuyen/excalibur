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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * A class to simplify extracting localized strings, icons
 * and other common resources from a ResourceBundle.
 *
 * Reworked to mirror behaviour of StringManager from Tomcat (format() to getString()).
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class Resources
{
    private static final Random RANDOM = new Random();

    ///Local of Resources
    private final Locale m_locale;

    ///Resource bundle referenced by manager
    private ResourceBundle m_bundle;

    ///Base name of resource bundle
    private String m_baseName;

    ///ClassLoader from which to load resources
    private ClassLoader m_classLoader;

    /**
     * Constructor that builds a manager in default locale.
     *
     * @param baseName the base name of ResourceBundle
     */
    public Resources( final String baseName )
    {
        this( baseName, Locale.getDefault(), null );
    }

    /**
     * Constructor that builds a manager in default locale
     * using specified ClassLoader.
     *
     * @param baseName the base name of ResourceBundle
     * @param classLoader the classLoader to load ResourceBundle from
     */
    public Resources( final String baseName, final ClassLoader classLoader )
    {
        this( baseName, Locale.getDefault(), classLoader );
    }

    /**
     * Constructor that builds a manager in specified locale.
     *
     * @param baseName the base name of ResourceBundle
     * @param locale the Locale for resource bundle
     */
    public Resources( final String baseName, final Locale locale )
    {
        this( baseName, locale, null );
    }

    /**
     * Constructor that builds a manager in specified locale.
     *
     * @param baseName the base name of ResourceBundle
     * @param locale the Locale for resource bundle
     * @param classLoader the classLoader to load ResourceBundle from
     */
    public Resources( final String baseName,
                      final Locale locale,
                      final ClassLoader classLoader )
    {
        if( null == baseName )
        {
            throw new NullPointerException( "baseName property is null" );
        }
        if( null == locale )
        {
            throw new NullPointerException( "locale property is null" );
        }
        m_baseName = baseName;
        m_locale = locale;
        m_classLoader = classLoader;
    }

    /**
     * Retrieve a boolean from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource boolean
     */
    public boolean getBoolean( final String key, final boolean defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getBoolean( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a boolean from bundle.
     *
     * @param key the key of resource
     * @return the resource boolean
     */
    public boolean getBoolean( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        return value.equalsIgnoreCase( "true" );
    }

    /**
     * Retrieve a byte from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource byte
     */
    public byte getByte( final String key, final byte defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getByte( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a byte from bundle.
     *
     * @param key the key of resource
     * @return the resource byte
     */
    public byte getByte( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Byte.parseByte( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a byte value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a char from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource char
     */
    public char getChar( final String key, final char defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getChar( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a char from bundle.
     *
     * @param key the key of resource
     * @return the resource char
     */
    public char getChar( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );

        if( 1 == value.length() )
        {
            return value.charAt( 0 );
        }
        else
        {
            throw new MissingResourceException( "Expecting a char value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a short from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource short
     */
    public short getShort( final String key, final short defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getShort( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a short from bundle.
     *
     * @param key the key of resource
     * @return the resource short
     */
    public short getShort( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Short.parseShort( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a short value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a integer from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource integer
     */
    public int getInteger( final String key, final int defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getInteger( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a integer from bundle.
     *
     * @param key the key of resource
     * @return the resource integer
     */
    public int getInteger( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Integer.parseInt( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a integer value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a long from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource long
     */
    public long getLong( final String key, final long defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getLong( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a long from bundle.
     *
     * @param key the key of resource
     * @return the resource long
     */
    public long getLong( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Long.parseLong( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a long value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a float from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource float
     */
    public float getFloat( final String key, final float defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getFloat( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a float from bundle.
     *
     * @param key the key of resource
     * @return the resource float
     */
    public float getFloat( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Float.parseFloat( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a float value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a double from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource double
     */
    public double getDouble( final String key, final double defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getDouble( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a double from bundle.
     *
     * @param key the key of resource
     * @return the resource double
     */
    public double getDouble( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            return Double.parseDouble( value );
        }
        catch( final NumberFormatException nfe )
        {
            throw new MissingResourceException( "Expecting a double value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a date from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource date
     */
    public Date getDate( final String key, final Date defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getDate( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a date from bundle.
     *
     * @param key the key of resource
     * @return the resource date
     */
    public Date getDate( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            final DateFormat format =
                DateFormat.getDateInstance( DateFormat.DEFAULT, m_locale );
            return format.parse( value );
        }
        catch( final ParseException pe )
        {
            throw new MissingResourceException( "Expecting a date value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a time from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource time
     */
    public Date getTime( final String key, final Date defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getTime( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a time from bundle.
     *
     * @param key the key of resource
     * @return the resource time
     */
    public Date getTime( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            final DateFormat format =
                DateFormat.getTimeInstance( DateFormat.DEFAULT, m_locale );
            return format.parse( value );
        }
        catch( final ParseException pe )
        {
            throw new MissingResourceException( "Expecting a time value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a time from bundle.
     *
     * @param key the key of resource
     * @param defaultValue the default value if key is missing
     * @return the resource time
     */
    public Date getDateTime( final String key, final Date defaultValue )
        throws MissingResourceException
    {
        try
        {
            return getDateTime( key );
        }
        catch( final MissingResourceException mre )
        {
            return defaultValue;
        }
    }

    /**
     * Retrieve a date + time from bundle.
     *
     * @param key the key of resource
     * @return the resource date + time
     */
    public Date getDateTime( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final String value = bundle.getString( key );
        try
        {
            final DateFormat format =
                DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT, m_locale );
            return format.parse( value );
        }
        catch( final ParseException pe )
        {
            throw new MissingResourceException( "Expecting a time value but got " + value,
                                                "java.lang.String",
                                                key );
        }
    }

    /**
     * Retrieve a raw string from bundle.
     *
     * @param key the key of resource
     * @return the resource string
     */
    public String getString( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        return bundle.getString( key );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @return the formatted string
     */
    public String getString( final String key, final Object arg1 )
    {
        final Object[] args = new Object[]{arg1};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @return the formatted string
     */
    public String getString( final String key, final Object arg1, final Object arg2 )
    {
        final Object[] args = new Object[]{arg1, arg2};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @param arg3 an arg
     * @return the formatted string
     */
    public String getString( final String key,
                             final Object arg1,
                             final Object arg2,
                             final Object arg3 )
    {
        final Object[] args = new Object[]{arg1, arg2, arg3};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @param arg3 an arg
     * @param arg4 an arg
     * @return the formatted string
     */
    public String getString( final String key,
                             final Object arg1,
                             final Object arg2,
                             final Object arg3,
                             final Object arg4 )
    {
        final Object[] args = new Object[]{arg1, arg2, arg3, arg4};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @param arg3 an arg
     * @param arg4 an arg
     * @param arg5 an arg
     * @return the formatted string
     */
    public String getString( final String key,
                             final Object arg1,
                             final Object arg2,
                             final Object arg3,
                             final Object arg4,
                             final Object arg5 )
    {
        final Object[] args = new Object[]{arg1, arg2, arg3, arg4, arg5};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @param arg3 an arg
     * @param arg4 an arg
     * @param arg5 an arg
     * @param arg6 an arg
     * @return the formatted string
     */
    public String getString( final String key,
                             final Object arg1,
                             final Object arg2,
                             final Object arg3,
                             final Object arg4,
                             final Object arg5,
                             final Object arg6 )
    {
        final Object[] args = new Object[]{arg1, arg2, arg3, arg4, arg5, arg6};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param arg1 an arg
     * @param arg2 an arg
     * @param arg3 an arg
     * @param arg4 an arg
     * @param arg5 an arg
     * @param arg6 an arg
     * @param arg7 an arg
     * @return the formatted string
     */
    public String getString( final String key,
                             final Object arg1,
                             final Object arg2,
                             final Object arg3,
                             final Object arg4,
                             final Object arg5,
                             final Object arg6,
                             final Object arg7 )
    {
        final Object[] args = new Object[]{arg1, arg2, arg3, arg4, arg5, arg6, arg7};
        return format( key, args );
    }

    /**
     * Retrieve a string from resource bundle and format it with specified args.
     *
     * @param key the key for resource
     * @param args an array of args
     * @return the formatted string
     */
    public String format( final String key, final Object[] args )
    {
        try
        {
            final String pattern = getPatternString( key );
            return MessageFormat.format( pattern, args );
        }
        catch( final MissingResourceException mre )
        {
            final StringBuffer sb = new StringBuffer();
            sb.append( "Unknown resource. Bundle: '" );
            sb.append( m_baseName );
            sb.append( "' Key: '" );
            sb.append( key );
            sb.append( "' Args: '" );

            for( int i = 0; i < args.length; i++ )
            {
                if( 0 != i ) sb.append( "', '" );
                sb.append( args[ i ] );
            }

            sb.append( "' Reason: " );
            sb.append( mre );

            return sb.toString();
        }
    }

    /**
     * Retrieve underlying ResourceBundle.
     * If bundle has not been loaded it will be loaded by this method.
     * Access is given in case other resources need to be extracted
     * that this Manager does not provide simplified access to.
     *
     * @return the ResourceBundle
     * @throws MissingResourceException if an error occurs
     */
    public final ResourceBundle getBundle()
        throws MissingResourceException
    {
        if( null == m_bundle )
        {
            // bundle wasn't cached, so load it, cache it, and return it.
            ClassLoader classLoader = m_classLoader;
            if( null == classLoader )
            {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if( null != classLoader )
            {
                m_bundle = ResourceBundle.getBundle( m_baseName, m_locale, classLoader );
            }
            else
            {
                m_bundle = ResourceBundle.getBundle( m_baseName, m_locale );
            }
        }
        return m_bundle;
    }

    /**
     * Utility method to retrieve a string from ResourceBundle.
     * If the key is a single string then that will be returned.
     * If key refers to string array then a random string will be chosen.
     * Other types cause an exception.
     *
     * @param key the key to resource
     * @return the string resource
     * @throws MissingResourceException if an error occurs
     */
    private String getPatternString( final String key )
        throws MissingResourceException
    {
        final ResourceBundle bundle = getBundle();
        final Object object = bundle.getObject( key );

        // is the resource a single string
        if( object instanceof String )
        {
            return (String)object;
        }
        else if( object instanceof String[] )
        {
            //if string array then randomly pick one
            final String[] strings = (String[])object;
            return strings[ RANDOM.nextInt( strings.length ) ];
        }
        else
        {
            throw new MissingResourceException( "Unable to find resource of appropriate type.",
                                                "java.lang.String",
                                                key );
        }
    }
}
