/* 
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.excalibur.util;

/**
 * A set of utility operations that provide necessary information about the
 * architecture of the machine that the system is running on.  The values
 * provided are automatically determined at JVM startup.  The SystemUtils uses
 * a plugin architecture so that it can be extended for more than just Linux/
 * Windows support.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:35 $
 */
public final class SystemUtil
{
    private static final int m_processors;
    private static final String m_cpuInfo;
    private static final String m_architecture;
    private static final String m_osName;
    private static final String m_osVersion;

    static
    {
        m_architecture = System.getProperty( "os.arch" );
        m_osName = System.getProperty( "os.name" );
        m_osVersion = System.getProperty( "os.version" );
        int procs = 0;
        String info = "";

        try
        {
            String name = "org.apache.excalibur.util.system." +
                stripWhitespace( m_osName );
            Class klass = Class.forName( name );
            CPUParser parser = (CPUParser)klass.newInstance();

            procs = parser.numProcessors();
            info = parser.cpuInfo();
        }
        catch( Exception e )
        {
            String proc = System.getProperty( "os.arch.cpus", "1" );
            info = System.getProperty(
                "os.arch.info",
                m_architecture +
                " Family n, Model n, Stepping n, Undeterminable"
            );

            procs = Integer.parseInt( proc );
        }

        m_processors = procs;
        m_cpuInfo = info;
    }

    /**
     * Utility method to strip whitespace from specified name.
     *
     * @param mosname the name
     * @return the whitespace stripped version
     */
    private static String stripWhitespace( String mosname )
    {
        final StringBuffer sb = new StringBuffer();

        final int size = mosname.length();
        for( int i = 0; i < size; i++ )
        {
            final char ch = mosname.charAt( i );
            if( ch != '\t' && ch != '\r' &&
                ch != '\n' && ch != '\b' )
            {
                sb.append( ch );
            }
        }

        return sb.toString();
    }

    /** keep utility from being instantiated */
    private SystemUtil()
    {
    }

    /**
     * Return the number of processors available on this machine.  This is useful
     * in classes like Thread/Processor thread pool models.
     */
    public static final int numProcessors()
    {
        return m_processors;
    }

    public static final String cpuInfo()
    {
        return m_cpuInfo;
    }

    /**
     * Return the architecture name
     */
    public static final String architecture()
    {
        return m_architecture;
    }

    /**
     * Return the Operating System name
     */
    public static final String operatingSystem()
    {
        return m_osName;
    }

    /**
     * Return the Operating System version
     */
    public static final String osVersion()
    {
        return m_osVersion;
    }
}

