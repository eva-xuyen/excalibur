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
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.io.rotate.FileStrategy;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;
import org.apache.log.output.io.rotate.RotateStrategy;
import org.apache.log.output.io.rotate.RotateStrategyBySize;
import org.apache.log.output.io.rotate.RotateStrategyByTime;
import org.apache.log.output.io.rotate.RotatingFileTarget;
import org.apache.log.output.io.rotate.UniqueFileStrategy;

/**
 *
 * @author <a href="mailto:bh22351@i-one.at">Bernhard Huber</a>
 */
public class TestRotatingFileOutputLogTarget
{
    private RawFormatter m_formatter = new RawFormatter();

    /** test file rotation by size, using unique filenames
     */
    public void testSizeUnique()
        throws Exception
    {
        final File file = new File( "test/size-unique.log" );
        final FileStrategy fileStrategy = new UniqueFileStrategy( file );
        final RotateStrategy rotateStrategy = new RotateStrategyBySize( 128 * 1024 );
        final Logger logger = getLogger( fileStrategy, rotateStrategy );

        doTest( logger );
    }

    /** test file rotation by size, using revolving filenames
     */
    public void testSizeRevoling()
        throws Exception
    {
        final File file = new File( "test/size-revolve.log" );
        final FileStrategy fileStrategy = new RevolvingFileStrategy( file, 20 );
        final RotateStrategy rotateStrategy = new RotateStrategyBySize( 128 * 1024 );
        final Logger logger = getLogger( fileStrategy, rotateStrategy );

        doTest( logger );
    }

    /** test file rotation by time, using unique filenames
     */
    public void testTimeUnique()
        throws Exception
    {
        final File file = new File( "test/time-unique.log" );
        final FileStrategy fileStrategy = new UniqueFileStrategy( file );
        final RotateStrategy rotateStrategy = new RotateStrategyByTime( 3 * 1000 );
        final Logger logger = getLogger( fileStrategy, rotateStrategy );

        doTest( logger );
    }

    /** test file rotation by time, using revolving filenames
     */
    public void testTimeRevolving()
        throws Exception
    {
        final File file = new File( "test/time-revolve.log" );
        final FileStrategy fileStrategy = new RevolvingFileStrategy( file, 5 );
        final RotateStrategy rotateStrategy = new RotateStrategyByTime( 3 * 1000 );
        final Logger logger = getLogger( fileStrategy, rotateStrategy );

        doTest( logger );
    }

    private void doTest( final Logger logger )
    {
        final long startTime = System.currentTimeMillis();
        final long diffTime = 10 * 1000;
        long endTime = startTime;

        int size = 0;
        for( int i = 0; ( endTime - startTime ) < diffTime; i++ )
        {
            size += generateMessages( logger, i, size, ( endTime - startTime ) );
            endTime = System.currentTimeMillis();
        }
    }

    /** just generate some logger messages
     */
    private int generateMessages( final Logger logger,
                                  final int i,
                                  final long totalSize,
                                  final long diffTime )
    {
        final String message =
            "Message " + i + ": total size " + totalSize + " diff time " + diffTime;
        logger.debug( message );
        logger.info( message );
        logger.warn( message );
        logger.error( message );
        logger.fatalError( message );

        return message.length();
    }

    private Logger getLogger( final FileStrategy fileStrategy,
                              final RotateStrategy rotateStrategy )
        throws Exception
    {
        final RotatingFileTarget target =
            new RotatingFileTarget( m_formatter, rotateStrategy, fileStrategy );
        final Hierarchy hierarchy = new Hierarchy();
        final Logger logger = hierarchy.getLoggerFor( "myCat" );

        logger.setLogTargets( new LogTarget[]{target} );

        return logger;
    }

    public static void main( final String args[] )
        throws Exception
    {
        TestRotatingFileOutputLogTarget trfolt = new TestRotatingFileOutputLogTarget();
        trfolt.testSizeUnique();
        trfolt.testSizeRevoling();
        trfolt.testTimeUnique();
        trfolt.testTimeRevolving();
    }
}
