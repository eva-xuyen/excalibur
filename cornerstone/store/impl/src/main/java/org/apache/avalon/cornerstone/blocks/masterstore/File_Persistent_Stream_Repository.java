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
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of a StreamRepository to a File.
 * TODO: -retieve(String key) should return a FilterInputStream to allow
 * mark and reset methods. (working not like BufferedInputStream!!!)
 *
 * @author  Federico Barbieri <fede@apache.org>
 */
public class File_Persistent_Stream_Repository
    extends AbstractFilePersistentStreamRepository
    implements StreamRepository, Contextualizable, Initializable, Configurable, LogEnabled
{

    public void enableLogging(Logger logger) {
        AvalonFileRepositoryMonitor avMonitor = new AvalonFileRepositoryMonitor();
        avMonitor.enableLogging(logger);
    }

    protected void initializeChild(AbstractFileRepository child) throws Exception {
        ((Initializable) child).initialize();
    }

    /**
     * Contextualization of the component by the container during
     * which the working home directory will be provided.
     *
     * @param context the supplied context object
     * @avalon.entry key="urn:avalon:home" type="java.io.File"
     */
     public void contextualize( final Context context ) throws ContextException
     {
         try
         {
             m_baseDirectory = (File)context.get( "urn:avalon:home" );
         }
         catch( ContextException e )
         {
             m_baseDirectory = (File)context.get( "app.home" );
         }
     }

    /**
     * Initialization of the component by the container.
     * @exception Exception if a initialization stage error occurs
     */
     public void initialize()
         throws Exception
     {
        monitor.initialized(File_Persistent_Stream_Repository.class);

         m_name = RepositoryManager.getName();
         m_extension = "." + m_name + getExtensionDecorator();
         m_filter = new ExtensionFileFilter( m_extension );

         final File directory = new File( m_path );
         directory.mkdirs();
        monitor.pathOpened(File_Persistent_Stream_Repository.class, m_path);

     }

    /**
     * Configuration of the component by the container.
     * @param configuration the configuration
     * @exception org.apache.avalon.framework.configuration.ConfigurationException if a configuration error occurs
     */
     public void configure( final Configuration configuration )
         throws ConfigurationException
     {
         if( null == m_destination )
         {
             final String destination = configuration.getAttribute( "destinationURL" );
             try {
                 setDestination( destination );
             } catch (IOException ioe) {
                 throw new ConfigurationException("Unexpected IOException " + ioe.getMessage(), ioe);
             }
         }
     }
}


