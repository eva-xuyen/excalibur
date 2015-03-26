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
package org.apache.avalon.fortress.tools;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;

/**
 * The Ant plugin to extract Fortress meta-data from Java sources.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">The Excalibur Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2005/12/25 08:29:44 $
 */
public class ComponentMetaAntTask extends AbstractQdoxTask
{

    /**
     * Wrap the Ant logger
     */
    private class AntBuildLogger implements BuildLogger
    {
        private Task m_task;

        private AntBuildLogger()
        {
            //Disallow use, mst use constructor below that supplies Ant data
        }

        public AntBuildLogger( Task task )
        {
            m_task = task;
        }

        public void debug( String message )
        {
            m_task.log( message, Project.MSG_DEBUG );
        }

        public void error( String message )
        {
            m_task.log( message, Project.MSG_ERR );
        }

        public void info( String message )
        {
            m_task.log( message, Project.MSG_INFO );
        }

        public void warn( String message )
        {
            m_task.log( message, Project.MSG_WARN );
        }
    }

    /**
     * The destination directory for metadata files.
     */
    private File m_destDir;

    public void execute() throws BuildException
    {

        validate();

        log( "Writing Info descriptors as property files (.meta)." );
        super.execute();

        ComponentMetaInfoCollector collector = new ComponentMetaInfoCollector( new AntBuildLogger( this ) );
        collector.setAllClasses( allClasses );
        collector.setDestDir( m_destDir );
        try
        {
            collector.execute();
        }
        catch ( Exception e )
        {
            throw new BuildException( e.getMessage(), e );
        }
    }

    /**
     * Validate that the parameters are valid.
     */
    private void validate()
    {
        if ( null == m_destDir )
        {
            final String message = "DestDir (" + m_destDir + ") not specified";
            throw new BuildException( message );
        }

        if ( !m_destDir.isDirectory() )
        {
            final String message = "DestDir (" + m_destDir + ") is not a directory.";
            throw new BuildException( message );
        }

        if ( !m_destDir.exists() && !m_destDir.mkdirs() )
        {
            final String message = "DestDir (" + m_destDir + ") could not be created.";
            throw new BuildException( message );
        }
    }

    /**
     * Set the destination directory for the meta information.
     * 
     * @param destDir The destination directory
     */
    public void setDestDir( final File destDir )
    {
        m_destDir = destDir;
    }

}
