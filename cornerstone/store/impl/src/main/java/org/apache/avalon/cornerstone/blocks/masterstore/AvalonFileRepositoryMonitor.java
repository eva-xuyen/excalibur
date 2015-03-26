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

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.io.File;
import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public class AvalonFileRepositoryMonitor extends AbstractLogEnabled implements FileRepositoryMonitor {
    public void repositoryCreated(Class aClass, String m_name, String m_destination, String childName) {
        getLogger().debug( "Child repository of " + m_name + " created in " +
                           m_destination + File.pathSeparatorChar +
                           childName + File.pathSeparator );
    }

    public void keyRemoved(Class aClass, String key) {
        getLogger().debug( "removed key " + key );
    }

    public void checkingKey(Class aClass, String key) {
        getLogger().debug( "checking key " + key );
    }

    public void returningKey(Class aClass, String key) {
        getLogger().debug( "returning key " + key );

    }

    public void returningObjectForKey(Class aClass, Object object, String key) {
        getLogger().debug( "returning object " + object + " for key " + key );
    }


    public void storingObjectForKey(Class xmlFilePersistentObjectRepository, Object value, String key) {
        getLogger().debug( "storing object " + value + " for key " + key );
    }

    public void initialized(Class aClass) {
        getLogger().info( "Init " + aClass.getName() + " Store" );
    }

    public void pathOpened(Class aClass, String m_path) {
        getLogger().info( aClass.getName() + " opened in " + m_path );
    }

    public void unExpectedIOException(Class aClass, String message, IOException ioe) {
        getLogger().warn( message, ioe );
    }
}
