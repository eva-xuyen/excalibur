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
package org.apache.log.output.jms;

/**
 * A descriptor for each message property.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author <a href="mailto:mirceatoma@home.com">Mircea Toma</a>
 */
public class PropertyInfo
{
    ///Name of property
    private final String m_name;

    ///Type/Source of property
    private final int m_type;

    ///Auxilliary parameters (ie constant or sub-format)
    private final String m_aux; //may be null

    /**
     * Creation of a new property info instance.
     * @param name the property
     * @param type the property type
     * @param aux auxillary property value
     */
    public PropertyInfo( final String name, final int type, final String aux )
    {
        m_type = type;
        m_aux = aux;
        m_name = name;
    }

    /**
     * Return the property name
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the property type
     * @return the type
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * Return the property auxilliary information
     * @return the information
     */
    public String getAux()
    {
        return m_aux;
    }
}

