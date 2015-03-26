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

package org.apache.avalon.fortress.impl;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * A class holding metadata about a component handler.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.12 $ $Date: 2004/02/28 15:16:24 $
 */
public final class ComponentHandlerMetaData
{
    /** Component activation should be performed during container initialization. */
    public static final int ACTIVATION_INLINE     = 0;
    
    /** Component activation should be initiated during container
     *   initialization, but can be done asynchronously in a background
     *   thread. */
    public static final int ACTIVATION_BACKGROUND = 1;
    
    /** Component activation will be delayed until the first time the
     *   component is looked up. */
    public static final int ACTIVATION_LAZY       = 2;
    
    private final String m_name;
    private final String m_classname;
    private final Configuration m_configuration;
    private final int m_activation;

    /**
     * Creation of a new impl handler meta data instance.
     *
     * @param name the handler name
     * @param classname the handler classname
     * @param configuration the handler configuration
     * @param activation the activation policy, one of
     *                   ComponentHandlerMetaData.ACTIVATION_BACKGROUND,
     *                   ComponentHandlerMetaData.ACTIVATION_INLINE,
     *                   ComponentHandlerMetaData.ACTIVATION_LAZY.
     */
    public ComponentHandlerMetaData( final String name,
                                     final String classname,
                                     final Configuration configuration,
                                     final int activation )
    {
        if ( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if ( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if ( null == configuration )
        {
            throw new NullPointerException( "configuration" );
        }

        m_name = name;
        m_classname = classname;
        m_configuration = configuration;
        m_activation = activation;
    }
    
    /**
     * Creation of a new impl handler meta data instance.
     *
     * @param name the handler name
     * @param classname the handler classname
     * @param configuration the handler configuration
     * @param lazyActivation the activation policy, true implies
     *                       ACTIVATION_LAZY, and false implies
     *                       ACTIVATION_BACKGROUND
     *
     * @deprecated in favor of construction which takes an integer activation.
     */
    public ComponentHandlerMetaData( final String name,
                                     final String classname,
                                     final Configuration configuration,
                                     final boolean lazyActivation )
    {
        this( name, classname, configuration,
            ( lazyActivation ? ACTIVATION_LAZY : ACTIVATION_BACKGROUND ) );
    }

    /**
     * Returns the handler name
     * @return the handler name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the handler classname
     * @return the classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Returns the handler configuration
     * @return the configuration
     */
    public Configuration getConfiguration()
    {
        return m_configuration;
    }
    
    /**
     * Returns the handler activation policy
     *
     * @return the activation policy
     */
    public int getActivation()
    {
        return m_activation;
    }

    /**
     * Returns the handler activation policy
     * @return the activation policy
     *
     * @deprecated in favor of getActivation()
     */
    public boolean isLazyActivation()
    {
        return m_activation == ACTIVATION_LAZY;
    }
}
