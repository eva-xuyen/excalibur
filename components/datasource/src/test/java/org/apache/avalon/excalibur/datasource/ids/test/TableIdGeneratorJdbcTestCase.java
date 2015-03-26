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

package org.apache.avalon.excalibur.datasource.ids.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.excalibur.datasource.ids.IdException;
import org.apache.avalon.excalibur.datasource.ids.IdGenerator;
import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * Test the TableIdGenerator Component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class TableIdGeneratorJdbcTestCase
    extends ExcaliburTestCase
{
    private ComponentSelector m_dbSelector;
    private DataSourceComponent m_dataSource;

    private ComponentSelector m_idGeneratorSelector;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public TableIdGeneratorJdbcTestCase( String name )
    {
        super( name );
    }

    /*---------------------------------------------------------------
     * TestCase Methods
     *-------------------------------------------------------------*/
    public void setUp() throws Exception
    {
        super.setUp();
        // Get a reference to a data source
        m_dbSelector = (ComponentSelector)manager.lookup( DataSourceComponent.ROLE + "Selector" );
        m_dataSource = (DataSourceComponent)m_dbSelector.select( "test-db" );

        // We need to initialize an ids table in the database for these tests.
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                // Try to drop the table.  It may not exist and throw an exception.
                getLogger().debug( "Attempting to drop old ids table" );
                try
                {
                    statement.executeUpdate( "DROP TABLE ids" );
                }
                catch( SQLException e )
                {
                    // The table was probably just not there.  Ignore this.
                }

                // Create the table that we will use in this test.
                // Different depending on the db. Please add new statements as new databases are
                //  tested.
                getLogger().debug( "Create new ids table" );
                statement.executeUpdate(
                    "CREATE TABLE ids ( " +
                    "table_name varchar(16) NOT NULL, " +
                    "next_id DECIMAL(30) NOT NULL, " +
                    "PRIMARY KEY (table_name))" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to initialize database for test.", e );
            fail( "Unable to initialize database for test. " + e );
        }

        // Get a reference to an IdGenerator Selector.
        // Individual IdGenerators are obtained in the tests.
        m_idGeneratorSelector = (ComponentSelector)manager.lookup( IdGenerator.ROLE + "Selector" );

    }

    public void tearDown() throws Exception
    {
        // Free up the IdGenerator Selector
        if( m_idGeneratorSelector != null )
        {
            manager.release( m_idGeneratorSelector );

            m_dbSelector = null;
        }

        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                // Delete the table that we will use in this test.
                getLogger().debug( "Drop ids table" );
                statement.executeUpdate( "DROP TABLE ids" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to cleanup database after test.", e );
            // Want to continue
        }

        // Free up the data source
        if( m_dbSelector != null )
        {
            if( m_dataSource != null )
            {
                m_dbSelector.release( m_dataSource );

                m_dataSource = null;
            }

            manager.release( m_dbSelector );

            m_dbSelector = null;
        }

        super.tearDown();
    }

    /*---------------------------------------------------------------
     * Test Cases
     *-------------------------------------------------------------*/
    public void testNonExistingTableName() throws Exception
    {
        getLogger().info( "testNonExistingTableName" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testNonExistingTableName" );
        try
        {
            try
            {
                idGenerator.getNextIntegerId();
                fail( "Should not have gotten an id" );
            }
            catch( IdException e )
            {
                // Got the expected error.
            }
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testSimpleRequestIdsSize1() throws Exception
    {
        getLogger().info( "testSimpleRequestIdsSize1" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize1" );
        try
        {
            int testCount = 100;

            // Initialize the counter in the database.
            initializeNextLongId( "test", 1 );

            for( int i = 1; i <= testCount; i++ )
            {
                int id = idGenerator.getNextIntegerId();
                assertEquals( "The returned id was not what was expected.", i, id );
            }

            assertEquals( "The next_id column in the database did not have the expected value.",
                          testCount + 1, peekNextLongId( "test" ) );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testSimpleRequestIdsSize10() throws Exception
    {
        getLogger().info( "testSimpleRequestIdsSize10" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize10" );
        try
        {
            int testCount = 100;

            // Initialize the counter in the database.
            initializeNextLongId( "test", 1 );

            for( int i = 1; i <= testCount; i++ )
            {
                int id = idGenerator.getNextIntegerId();
                assertEquals( "The returned id was not what was expected.", i, id );
            }

            assertEquals( "The next_id column in the database did not have the expected value.",
                          testCount + 1, peekNextLongId( "test" ) );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testSimpleRequestIdsSize100() throws Exception
    {
        getLogger().info( "testSimpleRequestIdsSize100" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize100" );
        try
        {
            int testCount = 100;

            // Initialize the counter in the database.
            initializeNextLongId( "test", 1 );

            for( int i = 1; i <= testCount; i++ )
            {
                int id = idGenerator.getNextIntegerId();
                assertEquals( "The returned id was not what was expected.", i, id );
            }

            assertEquals( "The next_id column in the database did not have the expected value.",
                          testCount + 1, peekNextLongId( "test" ) );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testBigDecimalRequestIdsSize10() throws Exception
    {
        getLogger().info( "testBigDecimalRequestIdsSize10" );

        if( isBigDecimalImplemented() )
        {
            IdGenerator idGenerator =
                (IdGenerator)m_idGeneratorSelector.select( "ids-testBigDecimalRequestIdsSize10" );
            try
            {
                int testCount = 100;
                BigDecimal initial = new BigDecimal( Long.MAX_VALUE + "00" );

                // Initialize the counter in the database.
                initializeNextBigDecimalId( "test", initial );

                for( int i = 0; i < testCount; i++ )
                {
                    BigDecimal id = idGenerator.getNextBigDecimalId();
                    assertEquals( "The returned id was not what was expected.",
                                  initial.add( new BigDecimal( i ) ), id );
                }

                assertEquals( "The next_id column in the database did not have the expected value.",
                              initial.add( new BigDecimal( testCount ) ), peekNextBigDecimalId( "test" ) );
            }
            finally
            {
                m_idGeneratorSelector.release( idGenerator );
            }
        }
        else
        {
            getLogger().warn( "Test Skipped because BigDecimals are not implemented in current driver." );
        }
    }

    public void testMaxByteIds() throws Exception
    {
        getLogger().info( "testMaxByteIds" );

        IdGenerator idGenerator = (IdGenerator)m_idGeneratorSelector.select( "ids-testMaxByteIds" );
        try
        {
            int testCount = 100;
            long max = Byte.MAX_VALUE;
            long initial = max - testCount;

            // Initialize the counter in the database.
            initializeNextLongId( "test", initial );

            for( int i = 0; i <= testCount; i++ )
            {
                byte id = idGenerator.getNextByteId();
                assertEquals( "The returned id was not what was expected.", i + initial, id );
            }

            // Next one should throw an exception
            try
            {
                byte id = idGenerator.getNextByteId();
                fail( "Should not have gotten an id: " + id );
            }
            catch( IdException e )
            {
                // Good.  Got the exception.
            }
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testMaxShortIds() throws Exception
    {
        getLogger().info( "testMaxShortIds" );

        IdGenerator idGenerator = (IdGenerator)m_idGeneratorSelector.select( "ids-testMaxShortIds" );
        try
        {
            int testCount = 100;
            long max = Short.MAX_VALUE;
            long initial = max - testCount;

            // Initialize the counter in the database.
            initializeNextLongId( "test", initial );

            for( int i = 0; i <= testCount; i++ )
            {
                short id = idGenerator.getNextShortId();
                assertEquals( "The returned id was not what was expected.", i + initial, id );
            }

            // Next one should throw an exception
            try
            {
                short id = idGenerator.getNextShortId();
                fail( "Should not have gotten an id: " + id );
            }
            catch( IdException e )
            {
                // Good.  Got the exception.
            }
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testMaxIntegerIds() throws Exception
    {
        getLogger().info( "testMaxIntegerIds" );

        IdGenerator idGenerator = (IdGenerator)m_idGeneratorSelector.select( "ids-testMaxIntegerIds" );
        try
        {
            int testCount = 100;
            long max = Integer.MAX_VALUE;
            long initial = max - testCount;

            // Initialize the counter in the database.
            initializeNextLongId( "test", initial );

            for( int i = 0; i <= testCount; i++ )
            {
                int id = idGenerator.getNextIntegerId();
                assertEquals( "The returned id was not what was expected.", i + initial, id );
            }

            // Next one should throw an exception
            try
            {
                int id = idGenerator.getNextIntegerId();
                fail( "Should not have gotten an id: " + id );
            }
            catch( IdException e )
            {
                // Good.  Got the exception.
            }
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testMaxLongIds() throws Exception
    {
        getLogger().info( "testMaxLongIds" );

        IdGenerator idGenerator = (IdGenerator)m_idGeneratorSelector.select( "ids-testMaxLongIds" );
        try
        {
            int testCount = 100;
            long max = Long.MAX_VALUE;
            long initial = max - testCount;

            // Initialize the counter in the database.
            initializeNextLongId( "test", initial );

            for( int i = 0; i <= testCount; i++ )
            {
                long id = idGenerator.getNextLongId();
                assertEquals( "The returned id was not what was expected.", i + initial, id );
            }

            // Next one should throw an exception
            try
            {
                long id = idGenerator.getNextLongId();
                fail( "Should not have gotten an id: " + id );
            }
            catch( IdException e )
            {
                // Good.  Got the exception.
            }
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    /*---------------------------------------------------------------
     * Utilitity Methods
     *-------------------------------------------------------------*/
    /**
     * Tests to see whether or not the current DataSource supports BigDecimal
     */
    private boolean isBigDecimalImplemented()
    {
        String tableName = "foorbar_table";

        // Add a row that can be selected.
        initializeNextLongId( tableName, 1 );

        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                ResultSet rs = statement.executeQuery( "SELECT next_id FROM ids " +
                                                       "WHERE table_name = '" + tableName + "'" );
                if( rs.next() )
                {
                    rs.getBigDecimal( 1 );
                }
                else
                {
                    fail( tableName + " row not in ids table." );
                    return false; // for compiler
                }
            }
            finally
            {
                conn.close();
            }

            // Implemented
            return true;
        }
        catch( SQLException e )
        {
            if( e.toString().toLowerCase().indexOf( "implemented" ) > 0 )
            {
                // Not implemented
                return false;
            }
            getLogEnabledLogger().error( "Unable to test for BigDecimal support.", e );
            fail( "Unable to test for BigDecimal support. " + e );
            return false; // for compiler
        }
    }

    private void initializeNextBigDecimalId( String tableName, BigDecimal nextId )
    {
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                // Need to quote the BigDecimal as it is larger than normal numbers can be.
                //  Was causing problems with MySQL
                statement.executeUpdate( "INSERT INTO ids (table_name, next_id) VALUES ('" +
                                         tableName + "', '" + nextId.toString() + "')" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to initialize next_id.", e );
            fail( "Unable to initialize next_id. " + e );
        }
    }

    private void initializeNextLongId( String tableName, long nextId )
    {
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                statement.executeUpdate( "INSERT INTO ids (table_name, next_id) VALUES ('" +
                                         tableName + "', " + nextId + ")" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to initialize next_id.", e );
            fail( "Unable to initialize next_id. " + e );
        }
    }

    private BigDecimal peekNextBigDecimalId( String tableName )
    {
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                ResultSet rs = statement.executeQuery( "SELECT next_id FROM ids " +
                                                       "WHERE table_name = '" + tableName + "'" );
                if( rs.next() )
                {
                    return rs.getBigDecimal( 1 );
                }
                else
                {
                    fail( tableName + " row not in ids table." );
                    return null; // for compiler
                }
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to peek next_id.", e );
            fail( "Unable to peek next_id. " + e );
            return null; // for compiler
        }
    }

    private long peekNextLongId( String tableName )
    {
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                ResultSet rs = statement.executeQuery( "SELECT next_id FROM ids " +
                                                       "WHERE table_name = '" + tableName + "'" );
                if( rs.next() )
                {
                    return rs.getLong( 1 );
                }
                else
                {
                    fail( tableName + " row not in ids table." );
                    return -1; // for compiler
                }
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogger().error( "Unable to peek next_id.", e );
            fail( "Unable to peek next_id. " + e );
            return -1; // for compiler
        }
    }
}

