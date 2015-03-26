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

import org.apache.avalon.fortress.tools.Component;
import org.apache.avalon.fortress.tools.Service;

import com.thoughtworks.qdox.model.JavaClass;

import junit.framework.TestCase;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Team</a>
 */
public class ServiceTestCase extends TestCase
{
    private static final String TYPE_NAME = "org.apache.avalon.fortress.tools.Service";
    private static final String COMPONENT_TYPE_NAME = "org.apache.avalon.fortress.tools.ServiceImpl";
    
    private Service m_service;
    private File m_root;

    public ServiceTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_service = new Service( TYPE_NAME );
        
        m_root = new File("./tempfiles");
        m_root.mkdirs();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        m_root.delete();
    }

    public void testGetType()
    {
        assertEquals( TYPE_NAME, m_service.getType() );
    }

    public void testAddComponent()
    {
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
        
        Component component = new Component( model );
        m_service.addComponent( component );
        
        assertNotNull( m_service.getComponents() );
        assertTrue( m_service.getComponents().hasNext() );
    }

    public void testSerialize() throws Exception
    {
        testAddComponent();
        
        m_service.serialize( m_root );
        
        File file = new File( m_root, "META-INF/services/" + TYPE_NAME );
       
        assertTrue( file.exists() );
    }
}
