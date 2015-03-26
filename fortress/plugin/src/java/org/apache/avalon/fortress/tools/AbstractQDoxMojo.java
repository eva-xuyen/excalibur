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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DefaultDocletTagFactory;
import com.thoughtworks.qdox.model.DocletTagFactory;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * An abstract class that provides the common logic of getting directory and
 * file-set configuration information from Maven before creating the list of classes
 * and source files that will be parsed by QDox.
 * 
 * Classes extending this class must call the <code>execute()</code> method, after which
 * the <code>allSources</code> and <code>allClasses</code> fields are filled in.
 * 
 * @author <a href="mailto:dev@excalibur.apache.org">The Excalibur Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2005/12/25 08:29:44 $
 */
public class AbstractQDoxMojo extends AbstractMojo
{

    /**
     * The directory where the generated directories and files are placed
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File        destDir;

    /**
     * Define the set of directory and file name inclusion/exclusion patterns.  If ommitted, 
     * a default fileset is constructed using the baseDir and defaultIncludes configuration
     * @parameter
     */
    private FileSet[]   filesets;

    /**
     * If filesets are not defined, a default one is created using this directory
     * @parameter expression="${basedir}"
     */
    private String        baseDir;

    /**
     * If filesets are not defined, a default one is created using this inclusion pattern
     * @parameter expression="**\/*.java"
     */
    private String      defaultIncludes;

    protected HashMap   fileMap    = new HashMap();

    protected ArrayList allSources = new ArrayList();

    protected ArrayList allClasses = new ArrayList();

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        validateAttributes();
        buildFileMap();
        JavaDocBuilder builder = new JavaDocBuilder( createDocletTagFactory() );

        // Add a classloader that has the taskdef's classpath.
        builder.getClassLibrary().addClassLoader( getClass().getClassLoader() );
        mergeBuilderSources( builder );
        JavaSource[] sources = builder.getSources();
        processSources( sources );
    }

    protected void buildFileMap()
    {
        for ( int i = 0; i < filesets.length; i++ )
        {
            FileSet fs = filesets[ i ];
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir( fs.getDirectory() );
            if ( fs.getIncludes().size() > 0 )
            {
                ds.setIncludes( (String[]) fs.getIncludes().toArray( new String[0] ) );
            }
            if ( fs.getExcludes().size() > 0 )
            {
                ds.setExcludes( (String[]) fs.getExcludes().toArray( new String[0] ) );
            }
            ds.scan();
            String[] srcFiles = ds.getIncludedFiles();
            buildFileMap( new File( fs.getDirectory() ), srcFiles );
        }
    }

    protected void buildFileMap( File directory, String[] sourceFiles )
    {
        for ( int i = 0; i < sourceFiles.length; i++ )
        {
            File src = new File( directory, sourceFiles[ i ] );
            fileMap.put( src.getAbsolutePath(), src );
        }
    }

    protected DocletTagFactory createDocletTagFactory()
    {
        return new DefaultDocletTagFactory();
    }

    private void mergeBuilderSources( JavaDocBuilder builder )
    {
        for ( Iterator iterator = fileMap.keySet().iterator(); iterator.hasNext(); )
        {
            String sourceFile = (String) iterator.next();
            builder.addSourceTree( (File) fileMap.get( sourceFile ) );

        }
    }

    protected void processSources( JavaSource[] sources )
    {
        for ( int i = 0; i < sources.length; i++ )
        {
            JavaSource source = sources[ i ];
            allSources.add( source );
            JavaClass[] classes = source.getClasses();
            processClasses( classes );
        }
    }

    protected void processClasses( JavaClass[] classes )
    {
        for ( int j = 0; j < classes.length; j++ )
        {
            JavaClass clazz = classes[ j ];
            allClasses.add( clazz );
        }
    }

    protected void validateAttributes() throws MojoExecutionException
    {
        if ( filesets == null || filesets.length == 0 )
        {
            filesets = createDefaultFileset();
        }
    }

    protected FileSet[] createDefaultFileset()
    {
        FileSet[] defaultFileSet = new FileSet[] {new FileSet()};
        List defaultPatternList = new ArrayList();
        
        defaultPatternList.add(defaultIncludes);
        defaultFileSet[0].setIncludes(defaultPatternList);
        defaultFileSet[0].setDirectory(baseDir);
        
        return defaultFileSet;
    }

    public File getDestDir()
    {
        return destDir;
    }
}
