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

package org.apache.avalon.fortress.attributes.qdox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.fortress.ExtendedMetaInfo;
import org.apache.avalon.fortress.attributes.AttributeInfo;
import org.apache.avalon.fortress.attributes.AttributeLevel;
import org.codehaus.metaclass.io.MetaClassIOBinary;
import org.codehaus.metaclass.model.Attribute;
import org.codehaus.metaclass.model.ClassDescriptor;
import org.codehaus.metaclass.model.MethodDescriptor;
import org.codehaus.metaclass.model.ParameterDescriptor;
import org.codehaus.metaclass.tools.qdox.QDoxDescriptorParser;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * Implements a serialization and deserialization 
 * mechanism for QDox
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class QDoxSerializer
{
    private static final QDoxSerializer m_instance = new QDoxSerializer();

    private MetaClassIOBinary binary = new MetaClassIOBinary();
    private QDoxDescriptorParser parser = new QDoxDescriptorParser();
    
    private QDoxSerializer()
    {
    }
    
    public static QDoxSerializer instance()
    {
        return m_instance;
    }

    public void serialize( final OutputStream stream, final JavaClass clazz )
        throws IOException
    {
        binary.serializeClass( stream, parser.buildClassDescriptor( clazz ) );
    }

    public ExtendedMetaInfo deserialize( final InputStream stream, final Class target )
        throws IOException
    {
        ClassDescriptor descriptor = binary.deserializeClass( stream );
        return new ExtendedMetaInfoAdapter( descriptor );
    }
    
    public static class ExtendedMetaInfoAdapter implements ExtendedMetaInfo
    {
        private static final AttributeInfo[] EMPTY = new AttributeInfo[0];
        private final ClassDescriptor m_descriptor;
        private final AttributeInfo[] m_classAttributes;
        private final Map m_method2Attributes = new HashMap(); 
        
        /**
         * @param descriptor
         */
        public ExtendedMetaInfoAdapter( final ClassDescriptor descriptor )
        {
            m_descriptor = descriptor;
            m_classAttributes = buildAttributeInfoArray( descriptor.getAttributes() );
        }

        /**
         * Document me!
         *  
         * @see org.apache.avalon.fortress.ExtendedMetaInfo#getClassAttributes()
         */
        public AttributeInfo[] getClassAttributes()
        {
            return m_classAttributes;
        }

        /**
         * Document me!
         *  
         * @see org.apache.avalon.fortress.ExtendedMetaInfo#getAttributesForMethod(java.lang.reflect.Method)
         */
        public AttributeInfo[] getAttributesForMethod( final Method method )
        {
            AttributeInfo[] attributes = (AttributeInfo[]) m_method2Attributes.get( method );
            
            if (attributes != null)
            {
                return attributes;
            }
            
            attributes = EMPTY;
            
            final MethodDescriptor[] descriptors = m_descriptor.getMethods();
            
            for (int i = 0; i < descriptors.length; i++)
            {
                MethodDescriptor descriptor = descriptors[i];
                if (!(method.getName().equals(descriptor.getName())))
                {
                    continue;
                }
                if (!(method.getReturnType().getName().equals(descriptor.getReturnType())))
                {
                    continue;
                }
                
                Class[] parameters = method.getParameterTypes();
                ParameterDescriptor[] paramsDesc = descriptor.getParameters();
                
                if ( parameters.length != paramsDesc.length )
                {
                    continue;
                }
                
                for (int j = 0; j < parameters.length; j++)
                {
                    Class paramClass = parameters[j];
                    if (!(paramsDesc[j].getName().equals( paramClass.getName() )))
                    {
                        break;
                    }
                }
                
                attributes = buildAttributeInfoArray( descriptor.getAttributes() );
                break;
            }
            
            m_method2Attributes.put( method, attributes );
            
            return attributes;
        }

        /**
         * Document me!
         *  
         * @see org.apache.avalon.fortress.ExtendedMetaInfo#getAttributeForMethod(java.lang.String, java.lang.reflect.Method)
         */
        public AttributeInfo getAttributeForMethod( final String name, final Method method )
        {
            final AttributeInfo[] attributes = getAttributesForMethod( method );
            AttributeInfo attribute = null;
            
            if (attributes != EMPTY)
            {
                for (int i = 0; i < attributes.length; i++)
                {
                    final AttributeInfo info = attributes[i];
                    
                    if (name.equalsIgnoreCase( info.getName() ))
                    {
                        attribute = info;
                        break;
                    }
                }
            }
            
            return attribute;
        }
        
        private AttributeInfo[] buildAttributeInfoArray( final Attribute[] attributes )
        {
            final AttributeInfo[] newArray = new AttributeInfo[ attributes.length ];
            
            for (int i = 0; i < attributes.length; i++)
            {
                Attribute attribute = attributes[i];
                newArray[i] = buildAttributeInfo( attribute );
            }
            
            return newArray;
        }
        
        private AttributeInfo buildAttributeInfo( final Attribute attribute )
        {
            return new AttributeInfo( 
                attribute.getName(), 
                buildAttributes( attribute ), 
                AttributeLevel.ClassLevel );
        }

        private Map buildAttributes( final Attribute attribute )
        {
            Map parameters = Collections.EMPTY_MAP;
            
            final String[] paramNames = attribute.getParameterNames();
            
            if (paramNames.length != 0)
            {
                parameters = new TreeMap( String.CASE_INSENSITIVE_ORDER );
                
                for (int i = 0; i < paramNames.length; i++)
                {
                    final String key = paramNames[i];
                    final String value = attribute.getParameter( key );
                    
                    parameters.put( key, value );
                }
            }
            
            return parameters;
        }    
    }
}
