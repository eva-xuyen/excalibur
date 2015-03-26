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

package org.apache.avalon.fortress;

import org.apache.avalon.framework.CascadingException;

/**
 * This exception is used to indicate something went horribly wrong in the
 * ContainerManager, and it is unable to create a new instance of your
 * Container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/28 15:16:24 $
 */
public final class InitializationException extends CascadingException
{
    /**
     * Create the InitializationException with the supplied message.
     *
     * @param message  The message for the exception
     */
    public InitializationException( final String message )
    {
        super( message );
    }

    /**
     * Create the InitializationException with the supplied message and source
     * exception.
     *
     * @param message  The message for the exception
     * @param source   The source exception
     */
    public InitializationException( final String message, final Throwable source )
    {
        super( message, source );
    }
}

