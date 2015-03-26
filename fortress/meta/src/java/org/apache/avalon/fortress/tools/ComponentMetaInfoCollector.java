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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.fortress.util.dag.CyclicDependencyException;
import org.apache.avalon.fortress.util.dag.DirectedAcyclicGraphVerifier;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * QDox-based engine to collect all the meta information for the components.
 * This common class is utilized by both Ant and Maven plgins, and possibly
 * others in the future.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/02 08:29:44 $
 */
public final class ComponentMetaInfoCollector
{
    /**
     * To log messages - varies by build system used
     */
    protected BuildLogger m_logger = null;

    /**
     * The list of classes to extract metadata from
     */
    protected ArrayList m_allClasses;

    /**
     * The destination directory for metadata files.
     */
    private File m_destDir;

    /**
     * The services to write the meta info for.
     */
    private final Map m_services = new HashMap();

    private static final String TAG_COMPONENT = "avalon.component";

//    private ComponentMetaInfoCollector() {}

    public ComponentMetaInfoCollector(BuildLogger logger) {
        m_logger = logger;
    }

    /**
     * Execute generator task.
     *
     * @throws BuildException if there was a problem collecting the info
     */
    public void execute()
            throws Exception
    {
        try
        {
            collectInfoMetaData();
            writeComponents();

            writeServiceList( m_services.values().iterator() );

            if (m_logger != null) {
                m_logger.info( "Collecting service information." );
            }
            writeServices();
        }
        catch ( final Exception e )
        {
            throw new Exception( e.getMessage(), e );
        }
        finally
        {
            Component.m_repository.clear();
        }
    }

    /**
     * Write the component meta information to the associated files.
     *
     * @throws IOException if there is a problem.
     */
    private void writeComponents() throws IOException, CyclicDependencyException
    {
        final List dagVerifyList = new ArrayList( Component.m_repository.size() );
        final Iterator it = Component.m_repository.iterator();
        while ( it.hasNext() )
        {
            final Component comp = (Component) it.next();
            comp.serialize( m_destDir );
            dagVerifyList.add( comp.getVertex() );
        }

        DirectedAcyclicGraphVerifier.verify( dagVerifyList );
    }

    /**
     * Write the service list to the "/service.list" file.
     *
     * @param it  The iterator for the services
     * @throws IOException if there is a problem writing the file
     */
    public void writeServiceList( final Iterator it ) throws IOException
    {
        int numServices = 0;

        File m_serviceFile = new File( m_destDir, "services.list" );
        final PrintWriter writer = new PrintWriter(
            new OutputStreamWriter( new ChangedFileOutputStream( m_serviceFile ), "UTF-8" ) );
        try
        {
            while ( it.hasNext() )
            {
                writer.println( ( (Service) it.next() ).getType() );
                numServices++;
            }
        }
        finally
        {
            writer.close();
        }

        if ( numServices == 0 )
        {
            m_serviceFile.delete();
        }
    }

    /**
     * Output the metadata files.
     */
    private void collectInfoMetaData()
    {
        final Iterator it = m_allClasses.iterator();
        while ( it.hasNext() )
        {
            final JavaClass javaClass = (JavaClass) it.next();
            final DocletTag tag = javaClass.getTagByName( TAG_COMPONENT );

            if ( null != tag )
            {
                final Component comp = new Component( javaClass );

                Iterator sit = comp.getServiceNames();
                while ( sit.hasNext() )
                {
                    String servName = (String) sit.next();
                    Service service = getService( servName );
                    service.addComponent( comp );
                }

                Iterator dit = comp.getDependencyNames();
                while ( dit.hasNext() )
                {
                    String depName = (String) dit.next();
                    Service service = getService( depName );
                    comp.addDependency( service );
                }
            }
        }
    }

    /**
     * Get the unique Service object for the specified type.
     *
     * @param type  The service type name
     * @return the Service object
     */
    protected Service getService( final String type )
    {
        Service service = (Service) m_services.get( type );

        if ( null == service )
        {
            service = new Service( type );
            m_services.put( service.getType(), service );
        }

        return service;
    }

    /**
     * Collect all the services and write out the implementations.
     */
    private void writeServices()
    {
        final File baseDir = new File( m_destDir, "META-INF/services/" );
        baseDir.mkdirs();

        final Iterator services = m_services.values().iterator();

        while ( services.hasNext() )
        {
            final Service service = (Service) services.next();
            if (m_logger != null) {
                m_logger.info( "Processing service " + service.getType());
            }
            try
            {
                service.serialize( m_destDir );
            }
            catch ( Exception e )
            {
                if (m_logger != null) {
                    m_logger.warn("Could not save information for service " + service.getType() );
                }
            }
        }
    }

    /**
     * Set the list of classes to extract metadata from
     *
     * @param allClasses The list of classes
     */
    public void setAllClasses(ArrayList allClasses) {
        m_allClasses = allClasses;
    }

    /**
     * Set the destination directory for the meta information.
     *
     * @param destDir  The destination directory
     */
    public void setDestDir( final File destDir )
    {
        m_destDir = destDir;
    }
}
