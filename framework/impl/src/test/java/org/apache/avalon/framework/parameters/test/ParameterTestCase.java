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
package org.apache.avalon.framework.parameters.test;

import java.io.*;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * TestCase for Parameter.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ParameterTestCase
    extends TestCase
{
    private static final String EOL = "\n";

    public ParameterTestCase( final String name )
    {
        super( name );
    }

    public void testRemoveParameter()
    {
        final Parameters parameters = new Parameters();
        parameters.setParameter( "key1", "value1" );
        assertEquals("Should only have one parameter", 1, parameters.getNames().length );
        parameters.setParameter( "key1", null );
        assertTrue( "key1 should no longer be a parameter", ! parameters.isParameter( "key1" ) );
        assertEquals( 0, parameters.getNames().length );
    }

    public void testIsParameter()
    {
        final Parameters parameters = new Parameters();
        parameters.setParameter( "key1", "value1" );
        assertTrue( "key1 should be a parameter", parameters.isParameter( "key1" ) );
        assertTrue( "key2 should not be a parameter", ! parameters.isParameter( "key2" ) );
    }

    public void testGetParameter()
    {
        final Parameters parameters = new Parameters();
        parameters.setParameter( "key1", "value1" );

        try
        {
            assertEquals( "key1 should equal value1", "value1", parameters.getParameter( "key1" ) );
        }
        catch ( final ParameterException pe )
        {
            fail( pe.getMessage() );
        }

        try
        {
            parameters.getParameter( "key2" );
            fail( "Not inserted parameter 'key2' exists" );
        }
        catch( final ParameterException pe )
        {
            //OK
        }

        assertEquals( "key1 should use correct value1", "value1", parameters.getParameter( "key1", "value1-1" ) );

        assertEquals( "key2 should use default value2", "value2", parameters.getParameter( "key2", "value2" ) );
    }

    public void testFromConfiguration()
    {
        final ByteArrayInputStream confInput = new ByteArrayInputStream( (
            "<?xml version=\"1.0\"?>" + EOL +
            "<test>" + EOL +
            "<parameter name=\"key1\" value=\"value1\"/>" + EOL +
            "<parameter name=\"key2\" value=\"value2\"/>" + EOL +
            "<parameter name=\"key3\" value=\"value3\"/>" + EOL +
            "</test>" ).getBytes() );

        try
        {
            final DefaultConfigurationBuilder builder =
                new DefaultConfigurationBuilder();
            final Configuration configuration = builder.build( confInput );

            final Parameters parameters =
                Parameters.fromConfiguration( configuration );

            assertEquals( "key1 should be value1", "value1", parameters.getParameter( "key1" ) );
            assertEquals( "key2 should be value2", "value2", parameters.getParameter( "key2" ) );
            assertEquals( "key3 should be value3", "value3", parameters.getParameter( "key3" ) );
        }
        catch ( final ConfigurationException ce )
        {
            fail( "Converting failed: " + ce.getMessage() );
        }
        catch ( final Exception e )
        {
            fail( e.getMessage() );
        }
    }

    public void testFromProperties()
    {
        final Properties properties = new Properties();
        properties.put( "key1", "value1" );
        properties.put( "key2", "value2" );
        properties.put( "key3", "value3" );

        final Parameters parameters = Parameters.fromProperties( properties );

        try
        {
            assertEquals( "key1 should be value1", "value1", parameters.getParameter( "key1" ) );
            assertEquals( "key2 should be value2", "value2", parameters.getParameter( "key2" ) );
            assertEquals( "key3 should be value3", "value3", parameters.getParameter( "key3" ) );
        }
        catch ( final ParameterException pe )
        {
            fail( pe.getMessage() );
        }
    }

    public void testSerialization() 
        throws Exception
    {
        Parameters p = Parameters.EMPTY_PARAMETERS;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( p );
        oos.close();
        byte[] ba = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream( ba );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Parameters serialized = (Parameters) ois.readObject();
        ois.close();

        assertEquals( "equality", p, serialized );
        assertEquals( "hashcode", p.hashCode(), serialized.hashCode() );
    }
}
