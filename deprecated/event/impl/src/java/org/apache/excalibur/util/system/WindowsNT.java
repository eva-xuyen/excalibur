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
package org.apache.excalibur.util.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.excalibur.util.CPUParser;

/**
 * Parses the Windows 2000 environment--the same class should work for other
 * Windows versions, but I only have one to test.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:29 $
 */
public final class WindowsNT implements CPUParser
{
    private final int m_processors;
    private final String m_cpuInfo;

    public WindowsNT()
    {
        int procs = 1;
        String info = "";

        try
        {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec( "cmd.exe /C echo %NUMBER_OF_PROCESSORS%" );
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                proc.getInputStream() ) );
            String numProcs = reader.readLine();

            proc = rt.exec( "cmd.exe /C echo %PROCESSOR_IDENTIFIER%" );
            reader = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            info = reader.readLine();

            procs = Integer.parseInt( numProcs );
        }
        catch( Exception e )
        {
        }

        m_processors = procs;
        m_cpuInfo = info;
    }

    /**
     * Return the number of processors available on the machine
     */
    public int numProcessors()
    {
        return m_processors;
    }

    /**
     * Return the cpu info for the processors (assuming symetric multiprocessing
     * which means that all CPUs are identical).  The format is:
     *
     * ${arch} family ${family} Model ${model} Stepping ${stepping}, ${identifier}
     */
    public String cpuInfo()
    {
        return m_cpuInfo;
    }
}

