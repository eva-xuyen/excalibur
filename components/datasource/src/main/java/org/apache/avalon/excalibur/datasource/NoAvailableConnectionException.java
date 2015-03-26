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

package org.apache.avalon.excalibur.datasource;

import java.sql.SQLException;

/**
 * Exception that is thrown when there are no more Connection objects available
 * in the pool.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:14 $
 * @since 4.1
 */
public class NoAvailableConnectionException extends SQLException
{

    public NoAvailableConnectionException()
    {
        super();
    }

    public NoAvailableConnectionException( String message )
    {
        super( message );
    }

    public NoAvailableConnectionException( String message, String SQLState )
    {
        super( message, SQLState );
    }

    public NoAvailableConnectionException( String message, String SQLState, int vendorCode )
    {
        super( message, SQLState, vendorCode );
    }
}
