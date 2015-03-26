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

package org.apache.excalibur.instrument.manager.impl;

/**
 * Utility classes useful for working with XML.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:25 $
 * @since 4.1
 */
final class XMLUtil
{
    /*---------------------------------------------------------------
     * Static Methods
     *-------------------------------------------------------------*/
    /**
     * Replaces one token with another in a string.
     *
     * @param str String with tokens to be replaced.
     * @param oldToken The token to be replaced.
     * @param newToken The new token value.
     *
     * @return A new String that has had its tokens replaced.
     */
    static String replaceToken( String str, String oldToken, String newToken )
    {
        int len = str.length();
        int oldLen = oldToken.length();
        if ( oldLen == 0 )
        {
            // Can't replace nothing.
            return str;
        }
        int newLen = newToken.length();
        int start = 0;
        int pos;
        while ( ( pos = str.indexOf( oldToken, start ) ) >= 0 )
        {
            String left;
            String right;
            int leftLen;
            int rightLen;
            
            // Get the left side of the string
            leftLen = pos;
            if ( leftLen == 0 )
            {
                left = "";
            }
            else
            {
                left = str.substring( 0, pos );
            }
            
            // Get the right side of the string
            rightLen = len - pos - oldLen;
            if ( len - pos - oldLen <= 0 )
            {
                right = "";
            }
            else
            {
                right = str.substring( pos + oldLen );
            }
            
            // Rebuild the str variable
            str = left + newToken + right;
            len = leftLen + newLen + rightLen;
            start = leftLen + newLen;
        }
        return str;
    }

    /**
     * Given an arbitrary text String, generate a new String which can be
     *  safely included in XML content.
     *
     * @param value The original String.
     *
     * @return A new XML-safe String.
     */
    static final String getXMLSafeString( String value )
    {
        value = replaceToken( value, "&", "&amp;" ); // Must be done first.
        value = replaceToken( value, "<", "&lt;" );
        value = replaceToken( value, ">", "&gt;" );
        value = replaceToken( value, "\"", "&quot;" );
        
        return value;
    }
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Not instantiable.
     */
    private XMLUtil()
    {
    }
}
