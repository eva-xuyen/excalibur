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

package org.apache.avalon.fortress.examples.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Fortress based servlet example. Presents a simple page to the user
 * displaying the possible languages they can see the text 'hello world'
 * written in.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.8 $ $Date: 2004/02/24 22:31:22 $
 */
public final class ServletContainer extends org.apache.avalon.fortress.impl.DefaultContainer
{
    public static final String KEY = "hello-world";

    private org.apache.avalon.fortress.examples.components.Translator m_translator;

    /**
     * Initializes this component.
     *
     * @exception java.lang.Exception if an error occurs
     */
    public void initialize()
        throws Exception
    {
        super.initialize();

        m_translator = (org.apache.avalon.fortress.examples.components.Translator)m_serviceManager.lookup( org.apache.avalon.fortress.examples.components.Translator.ROLE );
    }

    /**
     * Simple method to handle requests sent to the container from the
     * controlling servlet. This container simply displays a page containing
     * a list of possible languages the user can see the text 'hello world'
     * written in.
     *
     * @param request a <code>ServletRequest</code> instance
     * @param response a <code>ServletResponse</code> instance
     * @exception ServletException if a servlet error occurs
     * @exception java.io.IOException if an IO error occurs
     */
    public void handleRequest( ServletRequest request, ServletResponse response )
        throws ServletException, IOException
    {
        java.io.PrintWriter out = response.getWriter();
        String selected = request.getParameter( "language" );
        String[] languages = m_translator.getSupportedLanguages( KEY );

        out.println( "<html>" );
        out.println( "<head><title>Hello World!</title></head>" );
        out.println( "<body>" );
        out.println( "<hr>" );

        out.println( "<h1>" );

        if( selected == null )
        {
            out.println( "Please select your language" );
        }
        else
        {
            out.println( m_translator.getTranslation( KEY, selected ) );
        }

        out.println( "</h1>" );
        out.println( "<hr>" );

        out.println( "Available languages:" );

        out.println( "<form action='' name='languagelist'>" );
        out.println( "<select size='1' name='language'>" );

        for( int i = 0; i < languages.length; ++i )
        {
            String lang = languages[ i ];
            out.print( "<option value='" + lang + "'" );

            // preselect chosen language

            if( lang.equals( selected ) )
            {
                out.print( " selected" );
            }

            out.println( ">" + lang + "</option>" );
        }

        out.println( "</select>" );
        out.println( "<input value='OK' type='submit'>" );
        out.println( "</form>" );

        out.println( "</body>" );
        out.println( "</html>" );

        out.close();
    }

    /**
     * Release resources
     */
    public void dispose()
    {
        if( m_translator != null )
            m_serviceManager.release( m_translator );

        super.dispose();
    }
}

