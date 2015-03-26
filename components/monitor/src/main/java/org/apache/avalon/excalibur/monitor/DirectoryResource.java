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
package org.apache.avalon.excalibur.monitor;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This is a Resource that monitors a directory. If any files
 * are added, removed or modified in directory then it will
 * send an event indicating the change.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 11:47:32 $
 */
public class DirectoryResource
    extends Resource
{
    public static final String ADDED = "AddedFiles";
    public static final String REMOVED = "DeletedFiles";
    public static final String MODIFIED = "ModifiedFiles";

    private final File m_dir;
    private Set m_files;
    private Map m_times;

    public DirectoryResource( final String resourceKey )
        throws Exception
    {
        super( resourceKey );
        m_dir = new File( resourceKey );
        if( !m_dir.isDirectory() )
        {
            final String message = m_dir + " is not a directory.";
            throw new IllegalArgumentException( message );
        }

        m_files = new HashSet();
        m_times = new HashMap();

        final File[] files = m_dir.listFiles();
        for( int i = 0; i < files.length; i++ )
        {
            final File file = files[ i ];
            m_files.add( file );
            m_times.put( file, new Long( file.lastModified() ) );
        }
        setPreviousModified( System.currentTimeMillis() );
    }

    /**
     * Test whether this has been modified since time X
     */
    public void testModifiedAfter( final long time )
    {
        if( getPreviousModified() > time )
        {
            return;
        }

        final HashSet existingFiles = new HashSet();
        final HashSet modifiedFiles = new HashSet();
        final HashSet addedFiles = new HashSet();

        final File[] files = m_dir.listFiles();

        int fileCount = 0;
        if( null != files )
        {
            fileCount = files.length;
            for( int i = 0; i < files.length; i++ )
            {
                final File file = files[ i ];
                final long newModTime = file.lastModified();
                if( m_files.contains( file ) )
                {
                    existingFiles.add( file );

                    final Long oldModTime = (Long)m_times.get( file );
                    if( oldModTime.longValue() != newModTime )
                    {
                        modifiedFiles.add( file );
                    }
                }
                else
                {
                    addedFiles.add( file );
                }
                m_times.put( file, new Long( newModTime ) );
            }
        }

        final int lastCount = m_files.size();
        final int addedCount = addedFiles.size();
        final int modifiedCount = modifiedFiles.size();

        //If no new files have been added and
        //none deleted then nothing to do
        if( fileCount == lastCount &&
            0 == addedCount &&
            0 == modifiedCount )
        {
            return;
        }

        final HashSet deletedFiles = new HashSet();

        //If only new files were added and none were changed then
        //we don't have to scan for deleted files
        if( fileCount != lastCount + addedCount )
        {
            //Looks like we do have to scan for deleted files
            final Iterator iterator = m_files.iterator();
            while( iterator.hasNext() )
            {
                final File file = (File)iterator.next();
                if( !existingFiles.contains( file ) )
                {
                    deletedFiles.add( file );
                    m_times.remove( file );
                }
            }
        }

        final int deletedCount = deletedFiles.size();
        if( 0 != deletedCount )
        {
            getEventSupport().firePropertyChange( REMOVED,
                                                  Collections.EMPTY_SET,
                                                  deletedFiles );
        }
        if( 0 != addedCount )
        {
            getEventSupport().firePropertyChange( ADDED,
                                                  Collections.EMPTY_SET,
                                                  addedFiles );
        }

        if( 0 != modifiedCount )
        {
            getEventSupport().firePropertyChange( MODIFIED,
                                                  Collections.EMPTY_SET,
                                                  modifiedFiles );
        }

        existingFiles.addAll( addedFiles );
        m_files = existingFiles;
    }

    public long lastModified()
    {
        return getPreviousModified();
    }
}
