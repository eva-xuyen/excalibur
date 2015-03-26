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

package org.apache.avalon.cornerstone.blocks.masterstore.xml;

import org.apache.avalon.cornerstone.blocks.masterstore.AbstractFileRepository;
import org.apache.avalon.cornerstone.blocks.masterstore.ExtensionFileFilter;
import org.apache.avalon.cornerstone.blocks.masterstore.FileRepositoryMonitor;
import org.apache.avalon.cornerstone.blocks.masterstore.RepositoryManager;
import org.apache.avalon.cornerstone.blocks.masterstore.ObjectRespositoryConfig;
import org.apache.avalon.cornerstone.services.store.ObjectRepository;

import java.io.File;
import java.io.IOException;

/**
 * This is a simple implementation of persistent object store using
 * XML serialization from JDK 1.4 to a file system.
 *
 * This implementation of ObjectRepository comes with the following warning:
 * "XMLEncoder provides suitable persistence delegates
 * for all public subclasses of java.awt.Component in J2SE and the types of
 * all of their properties, recursively. All other classes will be handled
 * with the default persistence delegate which assumes the class follows
 * the beans conventions" (snipped from the BugParade)
 *
 * Basically, don't use this block for anything other than Swing component
 * serialization.  Sun will have to do a lot of work writing a
 * "PersistenceDelegate" to handle other JDK types let alone custom classes.
 *
 * @author <a href="mailto:paul_hammant@yahoo.com">Paul Hammant</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 */
public class CDIXMLFilePersistentObjectRepository
    extends AbstractXMLFilePersistentObjectRepository
    implements ObjectRepository
{

    public CDIXMLFilePersistentObjectRepository(FileRepositoryMonitor monitor, ObjectRespositoryConfig config) throws IOException {
        m_baseDirectory = config.getBaseDirectory();
        this.monitor = monitor;

        setDestination( config.getURL() );

        monitor.initialized(CDIXMLFilePersistentObjectRepository.class);

        m_name = RepositoryManager.getName();
        m_extension = "." + m_name + getExtensionDecorator();
        m_filter = new ExtensionFileFilter( m_extension );

        final File directory = new File( m_path );
        directory.mkdirs();

        monitor.pathOpened(CDIXMLFilePersistentObjectRepository.class, m_path);

    }

    protected void initializeChild(AbstractFileRepository child) throws Exception {
    }

}
