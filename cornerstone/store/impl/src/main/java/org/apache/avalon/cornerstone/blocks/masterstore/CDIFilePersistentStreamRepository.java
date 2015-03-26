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

package org.apache.avalon.cornerstone.blocks.masterstore;

import org.apache.avalon.cornerstone.services.store.StreamRepository;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of a StreamRepository to a File.
 * TODO: -retieve(String key) should return a FilterInputStream to allow
 * mark and reset methods. (working not like BufferedInputStream!!!)
 *
 * @author  Federico Barbieri <fede@apache.org>
 */
public class CDIFilePersistentStreamRepository
    extends AbstractFilePersistentStreamRepository
    implements StreamRepository
{

    public CDIFilePersistentStreamRepository(FileRepositoryMonitor monitor, ObjectRespositoryConfig config) throws IOException {
        m_baseDirectory = config.getBaseDirectory();
        this.monitor = monitor;

        setDestination( config.getURL() );

        monitor.initialized(CDIFilePersistentStreamRepository.class);

        m_name = RepositoryManager.getName();
        m_extension = "." + m_name + getExtensionDecorator();
        m_filter = new ExtensionFileFilter( m_extension );

        final File directory = new File( m_path );
        directory.mkdirs();
        monitor.pathOpened(CDIFilePersistentStreamRepository.class, m_path);

    }

    protected void initializeChild(AbstractFileRepository child) throws Exception {
    }

}


