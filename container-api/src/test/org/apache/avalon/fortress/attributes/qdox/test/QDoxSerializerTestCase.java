/* 
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.attributes.qdox.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.attributes.AttributeInfo;
import org.apache.avalon.fortress.attributes.qdox.QDoxSerializer;
import org.apache.avalon.fortress.attributes.qdox.test.data.Component;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;

import junit.framework.TestCase;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class QDoxSerializerTestCase extends TestCase
{
    /**
     * Constructor for QDoxSerializerTestCase.
     * @param name
     */
    public QDoxSerializerTestCase(String name)
    {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testInstance()
    {
        assertNotNull( QDoxSerializer.instance() );
    }

    /*
     * Test for void serialize(ObjectOutputStream, JavaClass)
     */
    public void testSerializeDeserialize()
        throws Exception
    {
        JavaClass clazz = createJavaClass();
        assertNotNull( clazz );
        
        /// Serialization
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        
        QDoxSerializer.instance().serialize( byteOutStream, clazz );
        byteOutStream.close();
        
        assertTrue( byteOutStream.size() != 0 );
        
        /// Deserialization
        ByteArrayInputStream byteInStream = new ByteArrayInputStream( byteOutStream.toByteArray() );

        ExtendedMetaInfo extendedInfo = QDoxSerializer.instance().deserialize( byteInStream, Component.class );
        assertNotNull( extendedInfo );
        
        /// Assertions
        assertExtendedInfo(extendedInfo);
    }

    public void testSerializeDeserializeWithFile()
        throws Exception
    {
        JavaClass clazz = createJavaClass();
        assertNotNull( clazz );

        /// Serialization
        File targetFile = new File("./data.attrs");
        targetFile.deleteOnExit();
        
        FileOutputStream fsOutStream = new FileOutputStream( targetFile ); 
        
        QDoxSerializer.instance().serialize( fsOutStream, clazz );
        fsOutStream.close();
        
        /// Deserialization
        FileInputStream fsInStream = new FileInputStream( targetFile );

        ExtendedMetaInfo extendedInfo = QDoxSerializer.instance().deserialize( fsInStream, Component.class );
        assertNotNull( extendedInfo );
        
        /// Assertions
        assertExtendedInfo(extendedInfo);
    }

    private void assertExtendedInfo(ExtendedMetaInfo extendedInfo) throws SecurityException
    {
        AttributeInfo[] classAttrs = extendedInfo.getClassAttributes();
        assertNotNull( classAttrs );
        assertEquals( 3, classAttrs.length );
        
        assertEquals( "attribute1", classAttrs[0].getName() );
        assertEquals( 0, classAttrs[0].getProperties().size() );
        
        assertEquals( "attribute2", classAttrs[1].getName() );
        assertEquals( 1, classAttrs[1].getProperties().size() );

        assertEquals( "attribute3", classAttrs[2].getName() );
        assertEquals( 2, classAttrs[2].getProperties().size() );
        
        Method[] methods = Component.class.getDeclaredMethods();
        
        for (int i = 0; i < methods.length; i++)
        {
            Method method = methods[i];
            AttributeInfo[] methodAttrs = extendedInfo.getAttributesForMethod( method );
            assertNotNull( methodAttrs );
            assertEquals( 1, methodAttrs.length );
        }
    }

    private JavaClass createJavaClass() throws Exception
    {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSource(new File("src/test/org/apache/avalon/fortress/attributes/qdox/test/data/Component.java"));
        JavaClass clazz = builder.getClassByName( Component.class.getName() );
        return clazz;
    }
}
