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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * The TableIdGenerator requests blocks of ids from a Database table.  The table consists of two
 *  columns one called <code>table_name</code> of type CHAR or VARCHAR, and the second called
 *  <code>next_id</code> of an integer type large enough to hold your largest ids.
 * <p>
 * The Configuration to use a TableIdGenerator looks like the following:
 * <pre>
 *   &lt;id-generators&gt;
 *       &lt;table name="user-ids" big-decimals="true" block-size="1" table="ids"
 *           key-table="event-type" logger="cm.ids"&gt;
 *           &lt;dbpool&gt;user-db&lt;/dbpool&gt;
 *       &lt;/table&gt;
 *   &lt;/id-generators&gt;
 * </pre>
 * Where user-db is the name of a DataSource configured in a datasources element, block-size is
 *  the number if ids that are allocated with each query to the databse (defaults to "10"),
 *  table is the name of the table which contains the ids (defaults to "ids"), and key-table is
 *  the table_name of the row from which the block of ids are allocated (defaults to "id").
 * <p>
 *
 * With the following roles declaration:
 * <pre>
 *   &lt;role name="org.apache.avalon.excalibur.datasource.ids.IdGeneratorSelector"
 *         shorthand="id-generators"
 *         default-class="org.apache.avalon.excalibur.component.ExcaliburComponentSelector"&gt;
 *       &lt;hint shorthand="table"
 *             class="org.apache.avalon.excalibur.datasource.ids.TableIdGenerator"/&gt;
 *   &lt;/role&gt;
 * </pre>
 *
 * To configure your component to use the IdGenerator declared above, its configuration should look
 *  something like the following:
 * <pre>
 *   &lt;user-service logger="cm"&gt;
 *       &lt;dbpool&gt;user-db&lt;/dbpool&gt;
 *       &lt;id-generator&gt;user-ids&lt;/id-generator&gt;
 *   &lt;/user-service&gt;
 * </pre>
 *
 * Your component obtains a reference to an IdGenerator using the same method as it obtains a
 *  DataSource, by making use of a ComponentSelector.
 * <p>
 * Depending on your database, the ids table should look something like the following:
 * <pre>
 *   CREATE TABLE ids (
 *       table_name varchar(16) NOT NULL,
 *       next_id INTEGER NOT NULL,
 *       PRIMARY KEY (table_name)
 *   );
 * </pre>
 * 
 * @avalon.component
 * @avalon.service type=org.apache.avalon.excalibur.datasource.ids.IdGenerator
 * @x-avalon.info name=table-id-generator
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 * @since 4.1
 */
public class TableIdGenerator
    extends AbstractDataSourceBlockIdGenerator
{
    /**
     * The name of the table containing the ids.
     */
    private String m_table;

    /**
     * TableName used to reference which ids to allocate.
     */
    private String m_tableName;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public TableIdGenerator()
    {
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Allocates a block of ids of the given size and returns the first id.
     *
     * @param blockSize number of ids to allocate.
     * @param useBigDecimals returns the first id as a BigDecimal if true, otherwise as a Long.
     *
     * @return either a Long or a BigDecimal depending on the value of useBigDecimals
     *
     * @throws IdException if a block of ids can not be allocated.
     */
    private Object allocateIdBlock( int blockSize, boolean useBigDecimals )
        throws IdException
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Allocating a new block of " + blockSize +
                               " ids for key_table " + m_tableName + "." );
        }

        try
        {
            Connection conn = getConnection();
            try
            {
                boolean autoCommit = conn.getAutoCommit();
                
                Statement stmt = conn.createStatement();
                try
                {
                    // Try to get a block without using transactions.  This makes this code
                    //  portable, but works on the assumption that requesting blocks of ids
                    //  is a fairly rare thing.
                    int tries = 0;
                    while( tries < 50 )
                    {
                        // Find out what the next available id is.
                        String query = "SELECT next_id FROM " + m_table + " WHERE table_name = '"
                            + m_tableName + "'";
                        ResultSet rs = stmt.executeQuery( query );
                        if ( !rs.next() )
                        {
                            // The row does not exist.
                            String msg =
                                "Unable to allocate a block of Ids, no row with table_name='"
                                + m_tableName + "' exists in the " + m_table + " table.";
                            getLogger().error( msg );
                            if ( !autoCommit )
                            {
                                conn.rollback();
                            }

                            throw new IdException( msg );
                        }
                        
                        // Get the next_id using the appropriate data type.
                        Object nextId;
                        Object newNextId;
                        if( useBigDecimals )
                        {
                            BigDecimal oldNextId = rs.getBigDecimal( 1 );
                            newNextId = oldNextId.add( new BigDecimal( blockSize ) );
                            nextId = oldNextId;
                        }
                        else
                        {
                            long oldNextId = rs.getLong( 1 );
                            newNextId = new Long( oldNextId + blockSize );
                            nextId = new Long( oldNextId );
                        }
                        
                        // Update the value of next_id in the database so it reflects the full block
                        //  being allocated.  If another process has done the same thing, then this
                        //  will either throw an exception due to transaction isolation or return
                        //  an update count of 0.  In either case, we will need to try again.
                        try
                        {
                            // Need to quote next_id values so that MySQL handles large BigDecimals
                            //  correctly.
                            query = "UPDATE " + m_table
                                + " SET next_id = '" + newNextId + "' "
                                + " WHERE table_name = '" + m_tableName + "' "
                                + "   AND next_id = '" + nextId + "'";
                            int updated = stmt.executeUpdate( query );
                            if( updated >= 1 )
                            {
                                // Update was successful.
                                if ( !autoCommit )
                                {
                                    conn.commit();
                                }

                                // Return the next id obtained above.
                                return nextId;
                            }
                            else
                            {
                                // May have been a transaction confict. Try again.
                                if( getLogger().isDebugEnabled() )
                                {
                                    getLogger().debug(
                                        "Update resulted in no rows being changed." );
                                }
                            }
                        }
                        catch ( SQLException e )
                        {
                            // Assume that this was caused by a transaction conflict.  Try again.
                            if( getLogger().isDebugEnabled() )
                            {
                                // Just show the exception message to keep the output small.
                                getLogger().debug(
                                    "Encountered an exception attempting to update the "
                                    + m_table + " table.  May be a transaction confict.  "
                                    + "Trying again: " + e.getMessage() );
                            }
                        }
                        
                        // If we got here, then we failed, roll back the connection so we can
                        //  try again.
                        if ( !autoCommit )
                        {
                            conn.rollback();
                        }

                        tries++;
                    }
                    
                    // If we got here then we ran out of tries.
                    getLogger().error( "Unable to allocate a block of Ids.  Too many retries." );
                    return null;
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                conn.close();
            }
        }
        catch ( SQLException e )
        {
            String msg = "Unable to allocate a block of Ids.";
            getLogger().error( msg, e );
            throw new IdException( msg, e );
        }
    }

    /*---------------------------------------------------------------
     * AbstractDataSourceBlockIdGenerator Methods
     *-------------------------------------------------------------*/
    /**
     * Allocates a block, of the given size, of ids from the database.
     *
     * @param blockSize number of Ids which are to be allocated.
     *
     * @return The first id in the allocated block.
     *
     * @throws IdException if there it was not possible to allocate a block of ids.
     */
    protected BigDecimal allocateBigDecimalIdBlock( int blockSize )
        throws IdException
    {
        return (BigDecimal)allocateIdBlock( blockSize, true );
    }

    /**
     * Allocates a block, of the given size, of ids from the database.
     *
     * @param blockSize number of Ids which are to be allocated.
     *
     * @return The first id in the allocated block.
     *
     * @throws IdException if there it was not possible to allocate a block of ids.
     */
    protected long allocateLongIdBlock( int blockSize )
        throws IdException
    {
        Long id = (Long)allocateIdBlock( blockSize, false );

        return id.longValue();
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to configure the component.
     *
     * @param configuration configuration info used to setup the component.
     *
     * @throws ConfigurationException if there are any problems with the configuration.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        super.configure( configuration );

        // Obtain the table name.
        m_table = configuration.getAttribute( "table", "ids" );

        // Obtain the key-table.
        m_tableName = configuration.getAttribute( "key-table", "id" );
    }
}

