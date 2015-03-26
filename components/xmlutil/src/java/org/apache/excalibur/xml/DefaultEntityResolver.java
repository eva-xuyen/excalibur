/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.excalibur.xml;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * A component that uses catalogs for resolving entities.
 * This implementation uses the XML Entity and URI Resolvers from
 * http://xml.apache.org/commons/
 * published by Norman Walsh. More information on the catalogs can be
 * found at
 * http://xml.apache.org/cocoon/userdocs/concepts/catalog.html
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: DefaultEntityResolver.java,v 1.4 2004/02/28 11:47:36 cziegeler Exp $
 */
public class DefaultEntityResolver extends AbstractLogEnabled
  implements EntityResolver,
             Serviceable,
             Parameterizable,
             ThreadSafe,
             Disposable,
             Component {

    /** The catalog manager */
    protected CatalogManager catalogManager = new CatalogManager();

    /** The catalog resolver */
    protected CatalogResolver catalogResolver = new CatalogResolver(catalogManager);

    /** The component manager */
    protected ServiceManager manager;

    /** SourceResolver */
    protected SourceResolver resolver;
    
    /**
     * Set the configuration. Load the system catalog and apply any
     * parameters that may have been specified in cocoon.xconf
     * @param params The configuration information
     * @exception ParameterException
     */
    public void parameterize(Parameters params) 
    throws ParameterException 
    {

        // Over-ride debug level that is set by CatalogManager.properties 
        String verbosity = params.getParameter("verbosity", null);
        if ( null != verbosity ) 
        {
            if (this.getLogger().isDebugEnabled()) 
            {
                this.getLogger().debug("Setting Catalog resolver "
                    + "verbosity level to " + verbosity);
            }
            int verbosityLevel = 0;
            try 
            {
                verbosityLevel = Integer.parseInt(verbosity);
                catalogManager.setVerbosity(verbosityLevel);
            } 
            catch (NumberFormatException ce1) 
            {
                this.getLogger().warn("Trouble setting Catalog verbosity",
                                        ce1);
            }
        }

        // Load the built-in catalog 
        String catalogFile = params.getParameter("catalog", this.defaultCatalog());
        if ( null == catalogFile)
        {
            this.getLogger().warn("No default catalog defined.");
        }
        else
        {
            this.parseCatalog(catalogFile);
        }

        // Load a single additional local catalog 
        String localCatalogFile = params.getParameter("local-catalog", null);
        if ( null != localCatalogFile ) 
        {
            this.parseCatalog( localCatalogFile );
        }
    }

    /**
     * Parse a catalog
     */
    protected void parseCatalog(String file) 
    {
        if (this.getLogger().isDebugEnabled()) 
        {
            this.getLogger().debug("Additional Catalog is " + file);
        }
        
        Source source = null;
        try 
        {
            source = this.resolver.resolveURI(file);
            this.catalogResolver.getCatalog().parseCatalog(source.getURI());
        } 
        catch (Exception e) 
        {   
            this.getLogger().warn("Could not get Catalog file. Trying again: " + file, e);
                        
            // try it again
            if ( null != source )
            {   
                try 
                {
                    String mimeType = source.getMimeType();
                    if ( null == mimeType ) mimeType =" text/plain";
                    this.catalogResolver.getCatalog().parseCatalog(mimeType, source.getInputStream());
                } 
                catch (Exception ex)
                {
                    this.getLogger().warn("Could not get Catalog file: " + file, ex);
                }
            }
        }
        finally 
        {
            this.resolver.release( source );
        }
    }
    
    /**
     * Default catalog path
     */
    protected String defaultCatalog() 
    {
        return null;
    }
    
    /**
     * Set the global component manager.
     * @param manager The global component manager
     * @exception ServiceException
     */
    public void service(ServiceManager manager) 
    throws ServiceException 
    {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup( SourceResolver.ROLE );
    }

    /**
     * Allow the application to resolve external entities.
     *
     * <p>The Parser will call this method before opening any external
     * entity except the top-level document entity (including the
     * external DTD subset, external entities referenced within the
     * DTD, and external entities referenced within the document
     * element): the application may request that the parser resolve
     * the entity itself, that it use an alternative URI, or that it
     * use an entirely different input source.</p>
     *
     * <p>Application writers can use this method to redirect external
     * system identifiers to secure and/or local URIs, to look up
     * public identifiers in a catalogue, or to read an entity from a
     * database or other input source (including, for example, a dialog
     * box).</p>
     *
     * <p>If the system identifier is a URL, the SAX parser must
     * resolve it fully before reporting it to the application.</p>
     *
     * @param publicId The public identifier of the external entity
     *        being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *        being referenced.
     * @return An InputSource object describing the new input source,
     *         or null to request that the parser open a regular
     *         URI connection to the system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException A Java-specific IO exception,
     *            possibly the result of creating a new InputStream
     *            or Reader for the InputSource.
     * @see org.xml.sax.InputSource
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException 
    {
        InputSource altInputSource = catalogResolver.resolveEntity(publicId,
                                        systemId);
        if (altInputSource != null) 
        {
            if (this.getLogger().isDebugEnabled()) 
            {
                this.getLogger().debug("Resolved catalog entity: "
                    + publicId + " " + altInputSource.getSystemId());
            }
        }

        return altInputSource;
    }

    /**
     * Dispose
     */
    public void dispose() 
    {
        if ( null != this.resolver ) 
        {
            this.manager.release( this.resolver );
        }
    }
}
