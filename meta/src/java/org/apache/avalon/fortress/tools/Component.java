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

package org.apache.avalon.fortress.tools;

import com.thoughtworks.qdox.model.*;
import org.apache.avalon.fortress.MetaInfoEntry;
import org.apache.avalon.fortress.attributes.qdox.QDoxSerializer;
import org.apache.avalon.fortress.util.dag.Vertex;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents a component, and output the meta information.
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/02 08:29:44 $
 */
public final class Component
{
    /** The repository of components. */
    static final Set m_repository = new HashSet();
    
    private static final String SINGLE_THREADED = "org.apache.avalon.framework.thread.SingleThreaded";
    private static final String THREAD_SAFE = "org.apache.avalon.framework.thread.ThreadSafe";
    private static final String POOLABLE = "org.apache.avalon.excalibur.pool.Poolable";
    private static final String RECYCLABLE = "org.apache.avalon.excalibur.pool.Recyclable";
    private static final String SERVICE_MANAGER = "org.apache.avalon.framework.service.ServiceManager";

    static final String ATTR_TYPE = "type";
    static final String ATTR_NAME = "name";

    private static final String TAG_SERVICE = "avalon.service";
    private static final String TAG_DEPENDENCY = "avalon.dependency";
    private static final String TAG_LIFESTYLE = "x-avalon.lifestyle";
    private static final String TAG_HANDLER = "fortress.handler";
    private static final String TAG_INFO = "x-avalon.info";
    private static final String TAG_NAME = "fortress.name";

    private static final String META_NAME = "x-avalon.name";

    private static final String METH_SERVICE = "service";

    private final JavaClass m_javaClass;
    private final Properties m_attributes;
    private final List m_dependencies;
    private final Vertex m_vertex;
    private final List m_dependencyNames;
    private final List m_serviceNames;

    /**
     * Initialize a service with the type name.
     *
     * @param javaClass
     */
    public Component( final JavaClass javaClass )
    {
        if ( javaClass == null ) 
        {
            throw new NullPointerException( "javaClass" );
        } 

        m_javaClass = javaClass;
        m_attributes = new Properties();
        m_dependencies = new ArrayList( 10 );
        m_vertex = new Vertex( this );
        m_dependencyNames = new ArrayList( 10 );
        m_serviceNames = new ArrayList( 10 );
        
        final DocletTag[] tags = javaClass.getTagsByName( TAG_SERVICE );
        for ( int t = 0; t < tags.length; t++ )
        {
            if ( tags[t].getNamedParameter( Component.ATTR_TYPE ) == null )
            {
                throw new BuildException( "The \"type\" tag is missing from the "
                    + "\"@" + TAG_SERVICE + "\" meta tag in "
                    + javaClass.getName() );
            }

            final String serviceName = resolveClassName( m_javaClass.getParentSource(),
                    tags[t].getNamedParameter( Component.ATTR_TYPE ) );
            m_serviceNames.add( serviceName );
        }

        discoverLifecycleType();
        discoverNameInfo();
        discoverDependencies();

        m_repository.add( this );
    }

    /**
     * Recursively discover dependencies from the local class hierarchy.  This does not, and cannot
     * discover dependencies from classes from other JARs.
     */
    private void discoverDependencies()
    {
        JavaClass currClass = m_javaClass;

        while ( currClass != null && discoverDependencies( currClass ) )
        {
            currClass = currClass.getSuperJavaClass();
        }
    }

    /**
     * Discover the dependencies that this component class requires.
     *
     * @param fromClass  The JavaClass object to gather the dependency set from.
     */
    private boolean discoverDependencies( final JavaClass fromClass )
    {
        boolean isSuccessful = true;

        JavaMethod[] methods = fromClass.getMethods();
        for ( int i = 0; i < methods.length; i++ )
        {
            if ( methods[i].getName().equals( METH_SERVICE ) )
            {
                if ( methods[i].getParameters().length == 1 && methods[i].getParameters()[0].getType().getValue().equals( SERVICE_MANAGER ) )
                {
                    DocletTag[] dependencies = methods[i].getTagsByName( TAG_DEPENDENCY );
                    for ( int d = 0; d < dependencies.length; d++ )
                    {
                        if ( dependencies[d].getNamedParameter( ATTR_TYPE ) == null )
                        {
                            throw new BuildException( "The \"type\" tag is missing from a "
                                + "\"@" + TAG_DEPENDENCY + "\" meta tag of the " + METH_SERVICE
                                + " method in " + fromClass.getName() );
                        }

                        String type = resolveClassName( fromClass.getParentSource(),
                                dependencies[d].getNamedParameter( ATTR_TYPE ) );
                        //String optional = dependencies[d].getNamedParameter("optional");

                        if ( null == type )
                        {
                            isSuccessful = false;
                        }
                        else
                        {
                            m_dependencyNames.add( type );
                        }
                    }
                }
            }
        }

        return isSuccessful;
    }

    private void discoverNameInfo()
    {
        DocletTag avalonConfigName = m_javaClass.getTagByName( TAG_INFO );
        if ( null == avalonConfigName ) avalonConfigName = m_javaClass.getTagByName( TAG_NAME );

        String name = MetaInfoEntry.createShortName(m_javaClass.getName());

        if ( avalonConfigName != null )
        {
            name = avalonConfigName.getNamedParameter( ATTR_NAME );
        }

        setAttribute( META_NAME, name );
    }

    private void discoverLifecycleType()
    {
        final DocletTag avalonLifecycle = m_javaClass.getTagByName( TAG_LIFESTYLE );
        final DocletTag fortressHandler = m_javaClass.getTagByName( TAG_HANDLER );
        String lifecycle = null;
        String handler = null;

        if ( avalonLifecycle == null && fortressHandler == null )
        {
            final Type[] interfaces = m_javaClass.getImplements();
            for ( int i = 0; i < interfaces.length && handler != null; i++ )
            {
                if ( interfaces[i].getClass().getName().equals( THREAD_SAFE ) )
                {
                    handler = MetaInfoEntry.THREADSAFE_HANDLER;
                }
                else if ( interfaces[i].getClass().getName().equals( POOLABLE ) ||
                        interfaces[i].getClass().getName().equals( RECYCLABLE ) )
                {
                    handler = MetaInfoEntry.POOLABLE_HANDLER;
                }
                else if ( interfaces[i].getClass().getName().equals( SINGLE_THREADED ) )
                {
                    handler = MetaInfoEntry.FACTORY_HANDLER;
                }
            }
        }

        if ( null != avalonLifecycle )       // lifecycle specified directly
        {
            lifecycle = stripQuotes( avalonLifecycle.getNamedParameter( ATTR_TYPE ) );
        }
        else if ( null != fortressHandler )  // handler specified directly
        {
            handler = stripQuotes( fortressHandler.getNamedParameter( ATTR_TYPE ) );
        }
        else // no lifecycle or handler specified, and no inspection match 
        {
            handler = MetaInfoEntry.PER_THREAD_HANDLER;
        }

        if ( null != lifecycle ) setAttribute( TAG_LIFESTYLE, lifecycle );
        if ( null != handler ) setAttribute( TAG_HANDLER, handler );
    }

    /**
     * Get the type name.
     *
     * @return String
     */
    public String getType()
    {
        return m_javaClass.getFullyQualifiedName();
    }

    public Iterator getDependencyNames()
    {
        return m_dependencyNames.iterator();
    }

    public Iterator getServiceNames()
    {
        return m_serviceNames.iterator();
    }

    /**
     * Add a dependency to this type.
     *
     * @param service  The name of the service that depends on this.
     */
    public void addDependency( Service service )
    {
        if ( !m_dependencies.contains( service ) )
        {
            m_dependencies.add( service );
        }
    }

    public Vertex getVertex()
    {
        if ( m_vertex.getDependencies().size() != 0 )
        {
            Iterator it = m_dependencies.iterator();
            while ( it.hasNext() )
            {
                Service service = (Service) it.next();

                Iterator cit = service.getComponents();
                while ( cit.hasNext() )
                {
                    Component component = (Component) cit.next();
                    m_vertex.addDependency( component.getVertex() );
                }
            }
        }
        return m_vertex;
    }

    /**
     * Set the component attribute.
     *
     * @param name   The name of the attribute
     * @param value  The attribute value
     */
    public void setAttribute( final String name, final String value )
    {
        m_attributes.setProperty( name, value );
    }

    /**
     * Output the meta information.
     *
     * @param rootDir
     * @throws IOException
     */
    public void serialize( final File rootDir ) throws IOException
    {
        final String type = getType();
        final String className   = type.substring( type.lastIndexOf('.') + 1 );
        final String typePackage = type.substring( 0, type.lastIndexOf('.') );
        final String typePackageDirFormat = typePackage.replace( '.', '/' );
        
        final File packageDir = new File( rootDir, typePackageDirFormat );
        packageDir.mkdirs();
        
        writeMetaFile( packageDir, className );
        writeDepsFile( packageDir, className );
        writeAttrFile( packageDir, className );
    }

    private void writeMetaFile( final File packageDir, final String className ) throws IOException
    {
        final File output = new File( packageDir, className + ".meta" );

        FileOutputStream writer = null;
        
        try
        {
            writer = new FileOutputStream( output );
            m_attributes.store( writer, "Meta information for " + getType() );
        }
        finally
        {
            if ( null != writer )
            {
                writer.close();
            }
        }
    }

    private void writeDepsFile( final File packageDir, final String className ) throws IOException
    {
        if ( m_dependencies.size() == 0 )
        {
            return;
        }
        
        final File output = new File( packageDir, className + ".deps" );

        FileOutputStream writer = null;
        
        try
        {
            writer = new FileOutputStream( output );

            Iterator it = m_dependencies.iterator();
            while ( it.hasNext() )
            {
                Service service = (Service) it.next();
                String name = service.getType() + "\n";
                writer.write( name.getBytes() );
            }
        }
        finally
        {
            if ( null != writer )
            {
                writer.close();
            }
        }
    }

    private void writeAttrFile( final File packageDir, final String className ) throws IOException
    {
        final File output = new File( packageDir, className + ".attrs" );

        FileOutputStream outStream = new FileOutputStream( output );
        
        try
        {
            QDoxSerializer.instance().serialize( outStream, m_javaClass );
        }
        finally
        {
            outStream.flush();
            outStream.close();
        }
    }
    
    private String stripQuotes( final String value )
    {
        if ( null == value ) return null;
        if ( value.length() < 2 ) return value;

        String retVal = value.trim();

        if ( retVal.startsWith( "\"" ) && retVal.endsWith( "\"" ) )
        {
            retVal = retVal.substring( 1, retVal.length() - 1 );
        }

        return retVal;
    }

    /**
     * Resolve the classname from the "@avalon.service" javadoc tags.
     *
     * @param serviceName  The service type name
     * @return  The fully qualified class name
     */
    protected String resolveClassName( final JavaSource sourceCode, final String serviceName )
    {
        if ( null == sourceCode )
        {
            return null;
        }
        if ( null == serviceName )
        {
            // This should be checked by the caller so that a message which better
            //  describes the problem in a given context can be given.
            throw new IllegalStateException( "The serviceName parameter was null." );
        }

        final String className = stripQuotes( serviceName );

        if ( className != null || className.length() > 0 )
        {
            if ( className.indexOf( '.' ) < 0 )
            {
                String checkName = checkPackage(sourceCode, sourceCode.getPackage(), className);
                if ( ! checkName.equals(className) ) return checkName;

                String[] imports = sourceCode.getImports();
                for ( int t = 0; t < imports.length; t++ )
                {
                    checkName = checkImport( sourceCode, imports[t], className);
                    if ( ! checkName.equals( className ) ) return checkName;
                }
            }
        }

        return className;
    }

    private String checkImport( final JavaSource sourceCode, final String type, final String className)
    {
        final String tail = type.substring( type.lastIndexOf( '.' ) + 1 );

        if ( tail.equals( className ) )
        {
            return type;
        }
        else if ( tail.equals( "*" ) )
        {
            final String pack = type.substring( 0, type.lastIndexOf( '.' ) );

            String checkName = checkPackage( sourceCode, pack, className );
            if ( !checkName.equals( className ) ) return checkName;
        }

        return className;
    }

    private String checkPackage( final JavaSource sourceCode, final String pack, final String serviceName )
    {
        String className = serviceName;
        final JavaClass klass = sourceCode.getClassLibrary().getClassByName( pack + "." + serviceName );
        if ( null !=  klass )
            className = klass.getFullyQualifiedName();
        return className;
    }
}
