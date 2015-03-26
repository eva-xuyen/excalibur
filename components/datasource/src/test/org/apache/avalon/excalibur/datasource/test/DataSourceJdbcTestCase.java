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

package org.apache.avalon.excalibur.datasource.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.excalibur.testcase.CascadingAssertionFailedError;
import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

/**
 * Test the DataSource Component.  I don't know how to make this generic,
 * so I'll throw some bones out there, and hope someone can set this up
 * better.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DataSourceJdbcTestCase
    extends ExcaliburTestCase
{
    protected boolean m_isSuccessful;
    protected CyclicBarrier m_barrier;
    protected int m_connectionCount;

    public DataSourceJdbcTestCase( String name )
    {
        super( name );
    }

    public void testOverAllocation()
    {
        DataSourceComponent ds = null;
        m_isSuccessful = false;
        LinkedList connectionList = new LinkedList();

        try
        {
            ds = (DataSourceComponent)manager.lookup( DataSourceComponent.ROLE );

            for( int i = 0; i < 10; i++ )
            {
                connectionList.add( ds.getConnection() );
            }
            getLogger().info( "Testing overallocation of connections.  Should see a warning next." );
            connectionList.add( ds.getConnection() );
        }
        catch( SQLException se )
        {
            this.m_isSuccessful = true;
            getLogger().info( "The test was successful" );
        }
        catch( ComponentException ce )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "There was an error in the OverAllocation test", ce );
            }

            throw new CascadingAssertionFailedError( "There was an error in the OverAllocation test", ce );
        }
        finally
        {
            assertTrue( "The DataSourceComponent could not be retrieved.", null != ds );

            Iterator connections = connectionList.iterator();

            while( connections.hasNext() )
            {
                try
                {
                    ( (Connection)connections.next() ).close();
                }
                catch( SQLException se )
                {
                    // ignore
                }
            }

            connectionList.clear();

            manager.release( (Component)ds );
        }

        assertTrue( "Exception was not thrown when too many datasource components were retrieved.", this.m_isSuccessful );
    }

    public void testNormalUse()
    {
        DataSourceComponent ds = null;
        m_isSuccessful = true;

        try
        {
            ds = (DataSourceComponent)manager.lookup( DataSourceComponent.ROLE );

            m_connectionCount = 0;
            m_barrier = new CyclicBarrier( 11 );

            for( int i = 0; i < 10; i++ )
            {
                ( new Thread( new ConnectionThread( this, ds ) ) ).start();
            }

            try
            {
                m_barrier.barrier();
            }
            catch( Exception ie )
            {
                // Ignore
            }

            getLogger().info( "The normal use test passed with " + this.m_connectionCount + " requests and 10 concurrent threads running" );
        }
        catch( ComponentException ce )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "There was an error in the normal use test", ce );
            }

            throw new CascadingAssertionFailedError( "There was an error in the normal use test", ce );
        }
        finally
        {
            assertTrue( "The DataSourceComponent could not be retrieved.", null != ds );

            manager.release( (Component)ds );
        }

        assertTrue( "Normal use test failed", this.m_isSuccessful );
    }

    static class ConnectionThread
        implements Runnable
    {
        protected DataSourceComponent m_datasource;
        protected DataSourceJdbcTestCase m_testcase;

        ConnectionThread( DataSourceJdbcTestCase testcase,
                          final DataSourceComponent datasource )
        {
            m_datasource = datasource;
            m_testcase = testcase;
        }

        public void run()
        {
            long end = System.currentTimeMillis() + 5000; // run for 5 seconds
            Random rnd = new Random();

            while( System.currentTimeMillis() < end && m_testcase.m_isSuccessful )
            {
                try
                {
                    Connection con = this.m_datasource.getConnection();
                    Thread.sleep( (long)rnd.nextInt( 100 ) ); // sleep for up to 100ms
                    con.close();
                    this.m_testcase.m_connectionCount++;
                }
                catch( final SQLException se )
                {
                    m_testcase.m_isSuccessful = false;
                    m_testcase.getLogger().info( "Failed to get Connection, test failed", se );
                }
                catch( final InterruptedException ie )
                {
                    // Ignore
                }
            }

            try
            {
                m_testcase.m_barrier.barrier();
            }
            catch( final InterruptedException ie )
            {
                // Ignore
            }
        }
    }
}

