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

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;

/**
 * Test suite for the RevolvingFileStrategy.
 *
 * @author Peter Donald
 */
public final class RevolvingFileStrategyTestCase
    extends TestCase
{
    private static final int OLD_AGE = 100000000;
    private static final int YOUNG_AGE = -100000000;

    private final File m_baseFile;
    private final long m_now = System.currentTimeMillis();

    public RevolvingFileStrategyTestCase( final String name )
        throws IOException
    {
        super( name );

        File basedir = getWorkDir();
        m_baseFile = ( new File( basedir, "build/testdata/log" ) ).getCanonicalFile();
        m_baseFile.getParentFile().mkdirs();
    }

    private File getWorkDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            path = System.getProperty( "basedir" );
            File root = new File( path );
            return new File( root, "target/test-classes" );
        }
    }

    private void deleteFiles( final int maxRotation )
        throws IOException
    {
        for( int i = 0; i <= maxRotation; i++ )
        {
            final File file = new File( getFilename( i ) );
            if( file.exists() && !file.delete() )
            {
                throw new IOException( "Failed to delete file " + file );
            }
        }
    }

    private String getFilename( int i )
    {
        return m_baseFile.toString() + ".00000" + i;
    }

    private void createFile( final int rotation, final long age )
        throws IOException
    {
        final File file = new File( getFilename( rotation ) );
        if( !file.createNewFile() )
        {
            throw new IOException( "Failed to create file " + file );
        }
        final long time = m_now - age;
        file.setLastModified( time );
    }

    public void testNew()
        throws Exception
    {
        deleteFiles( 9 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 0, strategy.getCurrentRotation() );
    }

    public void testRotationExisting()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 1, strategy.getCurrentRotation() );
    }

    public void testRotationExisting2()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );
        createFile( 1, 0 );
        createFile( 2, 0 );
        createFile( 3, 0 );
        createFile( 4, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 5, strategy.getCurrentRotation() );
    }

    public void testRotationExistingWithMissing()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );
        createFile( 4, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 5, strategy.getCurrentRotation() );
    }

    public void testRotationExistingWithOlderLower()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, OLD_AGE ); //Note this is oldest
        createFile( 4, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 5, strategy.getCurrentRotation() );
    }

    public void testRotationExistingWithOlderHigher()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );
        createFile( 4, OLD_AGE );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 5, strategy.getCurrentRotation() );
    }

    public void testFullRotation()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );
        createFile( 1, 0 );
        createFile( 2, 0 );
        createFile( 3, 0 );
        createFile( 4, 0 );
        createFile( 5, 0 );
        createFile( 6, 0 );
        createFile( 7, 0 );
        createFile( 8, 0 );
        createFile( 9, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 0, strategy.getCurrentRotation() );
    }

    public void testFullRotationWithOlder()
        throws Exception
    {
        deleteFiles( 9 );
        createFile( 0, 0 );
        createFile( 1, 0 );
        createFile( 2, 0 );
        createFile( 3, 0 );
        createFile( 4, 0 );
        createFile( 5, 0 );
        createFile( 6, 0 );
        createFile( 7, OLD_AGE );
        createFile( 8, 0 );
        createFile( 9, 0 );

        final RevolvingFileStrategy strategy =
            new RevolvingFileStrategy( m_baseFile, 9 );

        assertEquals( "rotation", 7, strategy.getCurrentRotation() );
    }
}
