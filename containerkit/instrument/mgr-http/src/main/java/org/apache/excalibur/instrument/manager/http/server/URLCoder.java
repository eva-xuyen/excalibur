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

package org.apache.excalibur.instrument.manager.http.server;

import java.io.UnsupportedEncodingException;

/**
 * The java.net.URLDecoder and java.net.URLEncoder classes that ship with Java
 *  do not support encoding until Java 1.4.  But this tool needs to be able to
 *  be used with Java 1.3.  This class makes that possible.
 */
public class URLCoder
{
    /** Not instantiable. */
    private URLCoder()
    {
    }
    
    /**
     * Returns a single upper case hex digit given a 0..15 number.
     */
    private static char getDigit( int d )
    {
        char c = Character.forDigit( d, 16 );
        if ( Character.isLetter( c ) )
        {
            c = Character.toUpperCase( c );
        }
        return c;
    }
    
    /**
     * Adds a string of characters in %xx encoded form to the specified
     *  StringBuffer.
     */
    private static void encodeChars( String value, String encoding, StringBuffer sb )
        throws UnsupportedEncodingException
    {
        byte[] bytes;
        if ( encoding == null )
        {
            bytes = value.getBytes();
        }
        else
        {
            bytes = value.getBytes( encoding );
        }
        
        for ( int i = 0; i < bytes.length; i++ )
        {
            sb.append( '%' );
            int b = (int)bytes[i];
            sb.append( getDigit( ( b & 0xf0 ) >> 4 ) );
            sb.append( getDigit( b & 0xf ) );
        }
    }
    
    private static String decodeChars( char[] chars, int start, int count, String encoding )
        throws UnsupportedEncodingException
    {
        byte[] bytes = new byte[count / 3];
        
        int pos = start;
        int bPos = 0;
        boolean bad = false;
        while ( pos < start + count )
        {
            char c = chars[pos];
            if ( c != '%' )
            {
                bad = true;
                break;
            }
            pos++;
            
            int b = 0;
            
            if ( pos >= chars.length )
            {
                bad = true;
                break;
            }
            c = chars[pos];
            if ( ( c >= 'A' ) && ( c <= 'F' ) )
            {
                b = b + 10 + ( c - 'A' );
            }
            else if ( ( c >= '0' ) && ( c <= '9' ) )
            {
                b = b + c - '0';
            }
            else
            {
                bad = true;
                break;
            }
            b = b << 4;
            pos++;
            
            if ( pos >= chars.length )
            {
                bad = true;
                break;
            }
            c = chars[pos];
            if ( ( c >= 'A' ) && ( c <= 'F' ) )
            {
                b = b + 10 + ( c - 'A' );
            }
            else if ( ( c >= '0' ) && ( c <= '9' ) )
            {
                b = b + c - '0';
            }
            else
            {
                bad = true;
                break;
            }
            pos++;
            
            bytes[bPos++] = (byte)( b & 0xff );
        }
        
        if ( bad )
        {
            throw new IllegalArgumentException( "Unexpected character at position " + pos );
        }
        
        if ( encoding == null )
        {
            return new String( bytes );
        }
        else
        {
            return new String( bytes, encoding );
        }
    }
    
    public static String encode( String value, String encoding )
        throws UnsupportedEncodingException
    {
        boolean changed = false;
        StringBuffer sb = new StringBuffer();
        
        char[] chars = value.toCharArray();
        
        int firstEncodeIndex = -1;
        int encodeCount = 0;
        StringBuffer encodeSb = new StringBuffer();
        for ( int i = 0; i < chars.length; i++ )
        {
            char c = chars[i];
            boolean encode;
            if ( c == ' ' )
            {
                // Special character.
                c = '+';
                changed = true;
                encode = false;
            }
            else if ( ( ( c >= 'a' ) && ( c <= 'z' ) )
                || ( ( c >= 'A' ) && ( c <= 'Z' ) )
                || ( ( c >= '0' ) && ( c <= '9' ) )
                || ( c == '-' ) || ( c == '_' ) || ( c == '.' ) || ( c == '*' ) )
            {
                // No encoding required.
                encode = false;
            }
            else
            {
                // All other characters must be encoded.
                changed = true;
                encode = true;
            }
            
            if ( encode )
            {
                // This character needs to be encoded.
                if ( firstEncodeIndex < 0 )
                {
                    firstEncodeIndex = i;
                }
                encodeCount++;
            }
            else
            {
                // No encoding needed.
                if ( firstEncodeIndex >= 0 )
                {
                    // The last run of chars needs to be encoded.
                    encodeChars( new String( chars, firstEncodeIndex, encodeCount ), encoding, sb );
                    firstEncodeIndex = -1;
                    encodeCount = 0;
                }
                
                sb.append( c );
            }
        }
        
        // If we ended in an encoding block then handle it.
        if ( firstEncodeIndex >= 0 )
        {
            // The last run of chars needs to be encoded.
            encodeChars( new String( chars, firstEncodeIndex, encodeCount ), encoding, sb );
            firstEncodeIndex = -1;
            encodeCount = 0;
        }
        
        if ( changed )
        {
            return sb.toString();
        }
        else
        {
            return value;
        }
    }
    
    public static String decode( String value, String encoding )
        throws UnsupportedEncodingException
    {
        boolean changed = false;
        StringBuffer sb = new StringBuffer();
        
        char[] chars = value.toCharArray();
        
        int firstDecodeIndex = -1;
        int decodeCount = 0;
        for ( int i = 0; i < chars.length; i++ )
        {
            char c = chars[i];
            boolean decode;
            if ( c == '+' )
            {
                c = ' ';
                decode = false;
                changed = true;
            }
            else if ( c == '%' )
            {
                decode = true;
                changed = true;
            }
            else
            {
                decode = false;
            }
            
            if ( decode )
            {
                // This is the first character needing to be decoded.
                if ( firstDecodeIndex < 0 )
                {
                    firstDecodeIndex = i;
                }
                decodeCount += 3;  // Always assume 3 characters for now.
                i += 2;
            }
            else
            {
                // This character needs no decoding.
                if ( firstDecodeIndex >= 0 )
                {
                    sb.append( decodeChars( chars, firstDecodeIndex, decodeCount, encoding ) );
                    firstDecodeIndex = -1;
                    decodeCount = 0;
                }
                
                sb.append( c );
            }
        }
        
        // If we ended while decoding a block then handle it.
        if ( firstDecodeIndex >= 0 )
        {
            sb.append( decodeChars( chars, firstDecodeIndex, decodeCount, encoding ) );
            firstDecodeIndex = -1;
            decodeCount = 0;
        }
        
        if ( changed )
        {
            return sb.toString();
        }
        else
        {
            return value;
        }
    }
}