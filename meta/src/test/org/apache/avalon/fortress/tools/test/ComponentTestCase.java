/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.tools.test;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.avalon.fortress.tools.Component;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Team</a>
 */
public class ComponentTestCase extends TestCase
{
    private static final String COMPONENT_TYPE_NAME = "org.apache.avalon.fortress.tools.ServiceImpl";
    
    private Component m_component;
    private final File m_root = new File("./tempfiles");

    public ComponentTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        
        JavaClass model = new JavaClass(null)
        {
            public String getFullyQualifiedName()
            {
                return getName();
            }

            public JavaClass getSuperJavaClass()
            {
                return null;
            }
        };
        model.setName( COMPONENT_TYPE_NAME );
        model.setTags( Collections.EMPTY_LIST );
        
        m_component = new Component( model );
        
        m_root.mkdirs();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        m_root.delete();
    }

    public void testGetType()
    {
        assertEquals( COMPONENT_TYPE_NAME, m_component.getType() );
    }

    public void testSerialize() throws Exception
    {
        m_component.serialize( m_root );
        
        String fileName = COMPONENT_TYPE_NAME.replace( '.', '/' ).concat( ".meta" );
        
        File file = new File( m_root, fileName );
        
        assertTrue( file.exists() );
    }
}