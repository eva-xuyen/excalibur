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
package org.apache.avalon.excalibur.logger.log4j;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.ConfigurationUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.spi.RootCategory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A LoggerManager for Log4j that will configure the Log4j subsystem
 * using specified configuration.
 * <p>
 * Note that in case of logging errors Log4J will (via the
 * org.apache.log4j.helpers.LogLog class) write to System.err.
 * This can be switched off but we can not substitute our
 * own handler to log erros the way we prefer to do this. :-(
 *
 * <p>
 * So, unlike the LogKit case we have no Log4JLogger helper to
 * implement and hence a different architecture: this class
 * is not a helper but a regular subclass of Log4JAdapter.
 *
 * <p>
 * Attach PrefixDecorator and/or CachingDecorator if desired.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/03/10 13:54:51 $
 * @since 4.0
 */
public class Log4JConfAdapter extends Log4JAdapter 
implements Configurable, Contextualizable
{
    private Context m_context;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException 
    {
        m_context = context;
    }
    
    /**
     * This constructor creates a completely independent
     * Log4J hierarchy. If you want to log to an existing
     * Log4J hierarchy please use Log4JAdapter. This class
     * always creates a new private hierarchy and configures
     * it all by itself.
     */
    public Log4JConfAdapter()
    {
        /** 
         * Copied from org.apache.log4j.LogManager.
         */
        super( new Hierarchy( new RootCategory( Level.ALL ) ) );
    }

    /**
     * Feed our configuration to Log4J.
     */
    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Element element = ConfigurationUtil.toElement( configuration );
        final Document document = element.getOwnerDocument();
        final Element newElement = document.createElement( "log4j:configuration" );
        final NodeList childNodes = element.getChildNodes();
        final int length = childNodes.getLength();
        for( int i = 0; i < length; i++ )
        {
            final Node node = childNodes.item( i );
            final Node newNode = node.cloneNode( true );
            newElement.appendChild( newNode );
        }

        document.appendChild( newElement );

        /**
         * Copied from org.apache.log4j.xml.DomConfigurator configure().
         * We want our own hierarchy to be configured, so we shall
         * be a bit more elaborate then just calling configure().
         */
        final Log4JConfigurator domConfigurator = new Log4JConfigurator( m_context );
        domConfigurator.doConfigure( newElement, m_hierarchy );
    }
}
