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

package org.apache.avalon.excalibur.datasource.ids;

import org.apache.avalon.framework.CascadingException;

/**
 * Thrown when it was not possible to allocate an Id.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 * @since 4.1
 */
public class IdException
    extends CascadingException
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Construct a new IdException instance.
     *
     * @param message The detail message for this exception.
     */
    public IdException( String message )
    {
        super( message );
    }

    /**
     * Construct a new IdException instance.
     *
     * @param message The detail message for this exception.
     * @param throwable The root cause of the exception.
     */
    public IdException( String message, Throwable throwable )
    {
        super( message, throwable );
    }
}

