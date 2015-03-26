/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.framework.configuration.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;

/**
 * Test the basic public methods of DefaultConfigurationSerializer.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public final class DefaultConfigurationSerializerTestCase extends TestCase
{
    private File testDirectory;
    private File testDirectory2;
    
    public DefaultConfigurationSerializerTestCase()
    {
        this("DefaultConfigurationSerializer Test Case ");
    }

    public DefaultConfigurationSerializerTestCase( final String name )
    {
        super( name );
    }
    
    public void setUp() {
        File basedir = getWorkDir();
        testDirectory = (new File( basedir, "io" ) ).getAbsoluteFile();
        testDirectory2 = 
          new File( 
            basedir,  
            "DefaultConfigurationSerializerTestCase" ).getAbsoluteFile();

        if( !testDirectory.exists() )
        {
            testDirectory.mkdirs();
        }
        
        assertTrue ( !testDirectory2.exists() );
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

    /**
     * Checks that the <code>serializeToFile</code> method closes the output stream
     * when it is done.
     */
    public void testSerializeToFile() throws Exception 
    {
        DefaultConfiguration config = new DefaultConfiguration("root", "");
        config.setAttribute( "attribute", "value" );
        
        File file = new File( testDirectory, "DefaultConfigurationSerializerTestCase.xml" );
        
        DefaultConfigurationSerializer serializer = new DefaultConfigurationSerializer();
        serializer.serializeToFile( file, config );
        
        //
        // This will not work if the serializeToFile method keeps the stream open.
        //
        assertTrue( testDirectory.renameTo( testDirectory2 ) );
        assertTrue( testDirectory2.renameTo( testDirectory ) );
        
        file.delete();
    }
}





