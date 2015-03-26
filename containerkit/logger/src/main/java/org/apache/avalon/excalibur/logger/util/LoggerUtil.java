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
package org.apache.avalon.excalibur.logger.util;

/**
 * This class implements utility methods for building LoggerManager-s.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class LoggerUtil
{
    /**
     * Generates a full category name given a prefix and category.  Either may be
     *  null.
     *
     * @param prefix Prefix or parent category.
     * @param category Child category name.
     */
    public static String getFullCategoryName( final String prefix, final String category )
    {
        if( ( null == prefix ) || ( prefix.length() == 0 ) )
        {
            if( category == null )
            {
                return "";
            }
            else
            {
                return category;
            }
        }
        else
        {
            if( ( null == category ) || ( category.length() == 0 ) )
            {
                return prefix;
            }
            else
            {
                return prefix + org.apache.log.Logger.CATEGORY_SEPARATOR + category;
            }
        }
    }
}
