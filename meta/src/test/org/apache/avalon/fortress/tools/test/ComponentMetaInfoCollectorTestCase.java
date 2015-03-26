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

import org.apache.avalon.fortress.tools.ComponentMetaInfoCollector;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import junit.framework.TestCase;

/**
 * Pending
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Team</a>
 */
public class ComponentMetaInfoCollectorTestCase extends TestCase
{
    private File m_dest;
    private File m_source;
    private Project m_antProject;
    private LogBuildListener m_log;

    /**
     * Constructor for ComponentMetaInfoCollectorTestCase.
     * @param name
     */
    public ComponentMetaInfoCollectorTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_log = new LogBuildListener();
        
        m_antProject = new Project();
        m_antProject.addBuildListener( m_log );

        m_dest = new File("./tempfiles");
        m_source = new File("./src/testdata");

        if (!m_dest.exists())
        {
            fail("Directory must exists: " + m_dest.getCanonicalPath() );
        }
        
        if (!m_source.exists())
        {
            fail("Directory must exists: " + m_source.getCanonicalPath() );
        }
    }

    public void testExecute()
    {
        ComponentMetaInfoCollector collector = new ComponentMetaInfoCollector();
        
        collector.setProject( m_antProject );
        collector.addFileset( new DummyFileSet() );
        collector.setDestDir( m_dest );
        collector.execute();
        
        File metaFile = new File( m_dest, "org/apache/excalibur/DefaultMailService.meta" );
        File attsFile = new File( m_dest, "org/apache/excalibur/DefaultMailService.attrs" );
        
        assertTrue( metaFile.exists() );
        assertTrue( attsFile.exists() );

        String content = m_log.getLogContent();
        
        final String messageExpected = "Writing Info descriptors as property files (.meta).\r\n" +            "Collecting service information.\r\n" +            "Processing service MailService\r\n";
        
        assertEquals( messageExpected, content );
    }

    private class DummyFileSet extends FileSet
    {
        public DummyFileSet( )
        {
        }

        public File getDir(Project project)
        {
            return m_source;
        }

        public DirectoryScanner getDirectoryScanner( Project project )
        {
            return new DirectoryScanner()
            {
                public String[] getIncludedDirectories()
                {
                    return new String[] { "org/apache/excalibur" };
                }

                public String[] getIncludedFiles()
                {
                    return new String[] { "org/apache/excalibur/DefaultMailService.java" };
                }
            };
        }
    }
    
    private class LogBuildListener implements BuildListener
    {
        StringBuffer m_log = new StringBuffer(); 
        
        public String getLogContent()
        {
            return m_log.toString();
        }
        
        ///
        /// BuildListener implementation
        /// 
        
        public void buildStarted(BuildEvent event)
        {
        }

        public void buildFinished(BuildEvent event)
        {
        }

        public void targetStarted(BuildEvent event)
        {
        }

        public void targetFinished(BuildEvent event)
        {
        }

        public void taskStarted(BuildEvent event)
        {
        }

        public void taskFinished(BuildEvent event)
        {
        }

        public void messageLogged(BuildEvent event)
        {
            m_log.append( event.getMessage() );
            m_log.append( "\r\n" );
        }
    }
}
