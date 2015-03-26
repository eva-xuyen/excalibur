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

package org.apache.avalon.excalibur.datasource.ids.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.excalibur.datasource.ids.IdGenerator;
import org.apache.avalon.excalibur.testcase.CascadingAssertionFailedError;
import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.excalibur.testcase.LatchedThreadGroup;
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * Test the TableIdGenerator Component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class TableIdGeneratorMultithreadedJdbcTestCase
    extends ExcaliburTestCase
{
    private static final String TABLE_KEY = "test";
    private static final int ID_COUNT = 1000;
    private static final int THREAD_COUNT = 50;

    private ComponentSelector m_dbSelector;
    private DataSourceComponent m_dataSource;

    private ComponentSelector m_idGeneratorSelector;

    protected Object m_semaphore = new Object();
    protected IdGenerator m_idGenerator;
    protected int m_perThreadGets;
    protected HashMap m_ids;
    protected Throwable m_throwable;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public TableIdGeneratorMultithreadedJdbcTestCase( String name )
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
                getLogEnabledLogger().debug( "Attempting to drop old ids table" );
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
                getLogEnabledLogger().debug( "Create new ids table" );
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
            getLogEnabledLogger().error( "Unable to initialize database for test.", e );
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
                getLogEnabledLogger().debug( "Drop ids table" );
                statement.executeUpdate( "DROP TABLE ids" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogEnabledLogger().error( "Unable to cleanup database after test.", e );
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
    public void testSimpleRequestIdsSize1() throws Exception
    {
        getLogEnabledLogger().info( "testSimpleRequestIdsSize1" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize1" );
        try
        {
            long firstId = 1;
            int idCount = ID_COUNT;
            int threadCount = THREAD_COUNT;

            // Initialize the counter in the database.
            initializeNextLongId( TABLE_KEY, firstId );

            generalTestCase( idGenerator, firstId, idCount, threadCount );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testSimpleRequestIdsSize10() throws Exception
    {
        getLogEnabledLogger().info( "testSimpleRequestIdsSize10" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize10" );
        try
        {
            long firstId = 1;
            int idCount = ID_COUNT;
            int threadCount = THREAD_COUNT;

            // Initialize the counter in the database.
            initializeNextLongId( TABLE_KEY, firstId );

            generalTestCase( idGenerator, firstId, idCount, threadCount );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testSimpleRequestIdsSize100() throws Exception
    {
        getLogEnabledLogger().info( "testSimpleRequestIdsSize100" );

        IdGenerator idGenerator =
            (IdGenerator)m_idGeneratorSelector.select( "ids-testSimpleRequestIdsSize100" );
        try
        {
            long firstId = 1;
            int idCount = ID_COUNT;
            int threadCount = THREAD_COUNT;

            // Initialize the counter in the database.
            initializeNextLongId( TABLE_KEY, firstId );

            generalTestCase( idGenerator, firstId, idCount, threadCount );
        }
        finally
        {
            m_idGeneratorSelector.release( idGenerator );
        }
    }

    public void testBigDecimalRequestIdsSize10() throws Exception
    {
        getLogEnabledLogger().info( "testBigDecimalRequestIdsSize10" );

        if( isBigDecimalImplemented() )
        {
            IdGenerator idGenerator =
                (IdGenerator)m_idGeneratorSelector.select( "ids-testBigDecimalRequestIdsSize10" );
            try
            {
                long firstId = 1;
                int idCount = ID_COUNT;
                int threadCount = THREAD_COUNT;

                // Initialize the counter in the database.
                initializeNextLongId( TABLE_KEY, firstId );

                generalTestCase( idGenerator, firstId, idCount, threadCount );
            }
            finally
            {
                m_idGeneratorSelector.release( idGenerator );
            }
        }
        else
        {
            getLogEnabledLogger().warn( "Test Skipped because BigDecimals are not implemented in current driver." );
        }
    }

    /*---------------------------------------------------------------
     * Utilitity Methods
     *-------------------------------------------------------------*/
    /**
     * General multithreaded test of an IdGenerator
     *
     * @param idGenerator the Id Generator to test.
     * @param firstId the first Id that is expected to be returned by the Id Generator.
     * @param idCount the number of ids to request in the test.
     * @param threadCount the number of threads to use to test the Id Generator.
     */
    private void generalTestCase( final IdGenerator idGenerator,
                                  final long firstId,
                                  final int idCount,
                                  final int threadCount )
    {
        if( idCount % threadCount != 0 )
        {
            fail( "idCount must be evenly divisible by threadCount" );
        }

        m_idGenerator = idGenerator;
        m_perThreadGets = idCount / threadCount;
        m_ids = new HashMap();

        // Create the runnable which will be used by the test.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                boolean duplicatesFound = false;

                for( int i = 0; i < m_perThreadGets; i++ )
                {
                    try
                    {
                        long id = m_idGenerator.getNextLongId();

                        synchronized( m_semaphore )
                        {
                            Long lId = new Long( id );

                            // Make sure this id has not already been seen
                            if( m_ids.get( lId ) != null )
                            {
                                getLogEnabledLogger().error( "Obtained a duplicate id: " + id );
                                duplicatesFound = true;
                            }
                            else
                            {
                                // Store a reference to this id
                                m_ids.put( lId, lId );
                            }
                        }
                    }
                    catch( Throwable t )
                    {
                        synchronized( m_semaphore )
                        {
                            if( m_throwable == null )
                            {
                                m_throwable = t;
                            }
                        }
                        return;
                    }
                }

                if( duplicatesFound )
                {
                    fail( "IdGenerator returned duplicate ids." );
                }
            }
        };

        LatchedThreadGroup group = new LatchedThreadGroup( runnable, threadCount );
        group.enableLogging( getLogEnabledLogger() );

        // Run the test.
        long duration;
        try
        {
            duration = group.go();
        }
        catch( Throwable t )
        {
            // Throwable could have been thrown by one of the tests.
            if( m_throwable == null )
            {
                m_throwable = t;
            }
            duration = 0;
        }

        if( m_throwable != null )
        {
            throw new CascadingAssertionFailedError( "Exception in test thread.", m_throwable );
        }

        // Make sure that all of the expected ids were obtained
        for( int i = 0; i < idCount; i++ )
        {
            Long id = new Long( firstId + i );
            assertTrue( "The IdGenerator did not return an expected id (" + id + ")",
                        m_ids.get( id ) != null );
        }

        getLogEnabledLogger().info( "It took " + duration + "ms. for " + threadCount +
                                    " threads to allocate " + idCount + " ids." );

        assertEquals( "The next_id column in the database did not have the expected value.",
                      firstId + idCount, peekNextLongId( TABLE_KEY ) );
    }

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

/*  Never used code
    private void initializeNextBigDecimalId( String tableName, BigDecimal nextId )
    {
        try
        {
            Connection conn = m_dataSource.getConnection();
            try
            {
                Statement statement = conn.createStatement();

                statement.executeUpdate( "INSERT INTO ids (table_name, next_id) VALUES ('" +
                                         tableName + "', " + nextId.toString() + ")" );
            }
            finally
            {
                conn.close();
            }
        }
        catch( SQLException e )
        {
            getLogEnabledLogger().error( "Unable to initialize next_id.", e );
            fail( "Unable to initialize next_id. " + e );
        }
    }
*/
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
            getLogEnabledLogger().error( "Unable to initialize next_id.", e );
            fail( "Unable to initialize next_id. " + e );
        }
    }

/* Never used code
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
            getLogEnabledLogger().error( "Unable to peek next_id.", e );
            fail( "Unable to peek next_id. " + e );
            return null; // for compiler
        }
    }
*/
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
            getLogEnabledLogger().error( "Unable to peek next_id.", e );
            fail( "Unable to peek next_id. " + e );
            return -1; // for compiler
        }
    }
}

