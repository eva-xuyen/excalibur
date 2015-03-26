/* 
 * Copyright 2002-2004 The Apache Software Foundation
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

package org.apache.avalon.examples.jdbcdatasource;

import org.apache.avalon.framework.component.Component;

/**
 * This example application creates a conmponent which makes use of a JdbcDataSource to
 *  connect to a Hypersonic SQL database.  It then adds a row to a table that it creates
 *  displaying a list of all the rows in the table.
 *
 * Note, this code ignores exceptions to keep the code simple.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:28 $
 * @since 4.1
 */
public interface HelloDBService
    extends Component
{
    /** The lookup key for the HelloDBService */
    String ROLE = "org.apache.avalon.examples.jdbcdatasource.HelloDBService";
    
    /**
     * Adds a single row to the database.
     *
     * @param title  The title for the row.
     */
    void addRow( String title );
    
    /**
     * Ask the component to delete all rows in the database.
     */
    void deleteRows();
    
    /**
     * Ask the component to log all of the rows in the database to the logger
     *  with the info log level.
     */
    void logRows();
}

