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

package org.apache.excalibur.instrument.client;

import java.io.File;

import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
public class Main
{
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    private static void showUsage()
    {
        System.out.println( "Usage:");
        System.out.println( "java -classpath {classpath} org.apache.excalibur.instrument.client.Main [-debug] [state file]" );
        System.out.println();
        System.out.println( "    -debug     - Enables debug output." );
        System.out.println( "    state file - Name of a state file to read at startup.  Defaults to: ../conf/default.desktop" );
        System.out.println();
    }
    
    
    /*---------------------------------------------------------------
     * Main Method
     *-------------------------------------------------------------*/
    /**
     * Main method used to lauch an InstrumentClient application.
     */
    public static void main( String args[] )
    {
        // Parse the command line.  Want to replace this with something more powerful later.
        boolean debug = false;
        String defaultStateFileName = "../conf/default.desktop";
        switch( args.length )
        {
        case 0:
            break;
            
        case 1:
            if ( args[0].equalsIgnoreCase( "-debug" ) )
            {
                debug = true;
            }
            else
            {
                defaultStateFileName = args[0];
            }
            break;
            
        case 2:
            if ( args[0].equalsIgnoreCase( "-debug" ) )
            {
                debug = true;
            }
            else
            {
                showUsage();
                System.exit( 1 );
            }
            defaultStateFileName = args[1];
            break;
            
        default:
            showUsage();
            System.exit( 1 );
        }
        
        File defaultStateFile = new File( defaultStateFileName );
        
        InstrumentClientFrame client = new InstrumentClientFrame( "Instrument Client" );
        int logLevel = ( debug ? ConsoleLogger.LEVEL_DEBUG : ConsoleLogger.LEVEL_INFO );
        client.enableLogging( new ConsoleLogger( logLevel ) );
        client.initialize();
        client.setDefaultStateFile( defaultStateFile );
        client.show();
    }
}

