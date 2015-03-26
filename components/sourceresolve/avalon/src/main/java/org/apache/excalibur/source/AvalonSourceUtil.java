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
package org.apache.excalibur.source;

import java.util.Iterator;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * This is a helper class for using the Avalon framework together with the
 * source resolver package.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AvalonSourceUtil.java 587589 2007-10-23 18:24:48Z cziegeler $
 */
public abstract class AvalonSourceUtil
{
    /**
     * Create a new parameters object from the
     * children of the configuration.
     * If no children are available <code>null</code>
     * is returned.
     */
    public static SourceParameters createSourceParameters( Configuration conf )
    {
        Configuration[] children = conf.getChildren();
        if( children != null && children.length > 0 )
        {
            SourceParameters pars = new SourceParameters();
            String name;
            String value;
            for( int i = 0; i < children.length; i++ )
            {
                name = children[ i ].getName();
                try
                {
                    value = children[ i ].getValue();
                }
                catch( ConfigurationException local )
                {
                    value = ""; // ignore exception
                }
                pars.setParameter( name, value );
            }
            return pars;
        }
        return null;
    }

    /**
     * Create a Parameters object.
     * The first value of each parameter is added to the Parameters object.
     * @return An Parameters object - if no parameters are defined this is an
     *         empty object.
     */
    public static Parameters getFirstParameters(SourceParameters params)
    {
        Parameters result = new Parameters();
        Iterator iter = params.getParameterNames();
        String parName;
        while( iter.hasNext() )
        {
            parName = (String)iter.next();
            result.setParameter( parName, params.getParameter( parName ) );
        }
        return result;
    }

    /**
     * Append parameters to the uri.
     * Each parameter is appended to the uri with "parameter=value",
     * the parameters are separated by "&".
     */
    public static String appendParameters( String uri,
                                           Parameters parameters )
    {
        if( parameters != null )
        {
            StringBuffer buffer = new StringBuffer( uri );
            String[] keys = parameters.getNames();
            String current;
            char separator = ( uri.indexOf( "?" ) == -1 ? '?' : '&' );

            if( keys != null )
            {
                for( int i = 0; i < keys.length; i++ )
                {
                    current = keys[ i ];
                    buffer.append( separator )
                        .append( current )
                        .append( '=' )
                        .append( SourceUtil.encode( parameters.getParameter( current, null ) ) );
                    separator = '&';
                }
            }
            return buffer.toString();
        }

        return uri;
    }
}
