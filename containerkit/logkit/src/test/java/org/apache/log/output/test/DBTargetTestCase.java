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
package org.apache.log.output.test;

import junit.framework.TestCase;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.output.db.ColumnInfo;
import org.apache.log.output.db.ColumnType;
import org.apache.log.output.db.DefaultDataSource;
import org.apache.log.output.db.DefaultJDBCTarget;
import org.apache.log.output.db.NormalizedJDBCTarget;

/**
 * Test suite for the DB output target.
 *
 * @author Peter Donald
 */
public final class DBTargetTestCase
    extends TestCase
{
    private String m_connectString;
    private String m_userName;
    private String m_userPassword;
    private boolean m_doDBTest;

    public DBTargetTestCase( final String name )
        throws Exception
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        String driverName = System.getProperty("test.db.driver");
        m_connectString = System.getProperty("test.db.jdbc", "");
        m_userName = System.getProperty( "test.db.user", "" );
        m_userPassword = System.getProperty( "test.db.pword", "" );
        m_doDBTest = System.getProperty( "test.db.run", "false" ).equalsIgnoreCase("true");

        if (m_doDBTest)
        {
            Class.forName(driverName);
        }
        else
        {
            System.out.println("[WARNING] Database Testing is not being done");
            System.out.println();
            System.out.println("To enable database testing, please provide the");
            System.out.println("following properties:");
            System.out.println();
            System.out.println( "test.db.driver -> Class name for the JDBC driver" );
            System.out.println( "test.db.jdbc   -> JDBC connect string" );
            System.out.println( "test.db.user   -> User ID" );
            System.out.println( "test.db.pword  -> User password" );
            System.out.println( "test.db.run    -> \"true\"" );
        }
    }

    public void testBasicTarget()
        throws Exception
    {
        if (! m_doDBTest) return;

        final DefaultDataSource dataSource =
            new DefaultDataSource( m_connectString, m_userName, m_userPassword );

        final ColumnInfo[] columns =
            {
                new ColumnInfo( "TIME", ColumnType.TIME, null ),
                new ColumnInfo( "PRIORITY", ColumnType.PRIORITY, null ),
                new ColumnInfo( "CATEGORY", ColumnType.CATEGORY, null ),
                new ColumnInfo( "HOSTNAME", ColumnType.STATIC, "helm.realityforge.net" ),
                new ColumnInfo( "MESSAGE", ColumnType.MESSAGE, null )
            };

        final DefaultJDBCTarget target =
            new DefaultJDBCTarget( dataSource, "log_entrys", columns );

        final Logger logger = getNewLogger( target );
        logger.debug( "Hello" );
    }

    public void testNumericConstants()
        throws Exception
    {
        if ( !m_doDBTest ) return;

        final DefaultDataSource dataSource =
                new DefaultDataSource( m_connectString, m_userName, m_userPassword );

        final ColumnInfo[] columns =
            {
                new ColumnInfo( "TIME", ColumnType.TIME, null ),
                new ColumnInfo( "PRIORITY", ColumnType.PRIORITY, null ),
                new ColumnInfo( "CATEGORY", ColumnType.CATEGORY, null ),
                new ColumnInfo( "HOSTNAME", ColumnType.STATIC, "helm.realityforge.net" ),
                new ColumnInfo( "MESSAGE", ColumnType.MESSAGE, null )
            };

        final NormalizedJDBCTarget target =
            new NormalizedJDBCTarget( dataSource, "log_entrys2", columns );

        final Logger logger = getNewLogger( target );
        logger.debug( "Hello" );
        logger.info( "Hello info" );
        logger.error( "Hello error" );
        logger.fatalError( "Hello fatalError" );
    }

    private Logger getNewLogger( final LogTarget target )
    {
        final Hierarchy hierarchy = new Hierarchy();
        final Logger logger = hierarchy.getLoggerFor( "myCategory" );
        logger.setLogTargets( new LogTarget[]{target} );
        return logger;
    }
}
