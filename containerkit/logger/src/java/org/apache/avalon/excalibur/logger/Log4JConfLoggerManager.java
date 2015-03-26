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
package org.apache.avalon.excalibur.logger;

import org.apache.avalon.excalibur.logger.log4j.Log4JConfigurator;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.ConfigurationUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A LoggerManager for Log4j that will configure the Log4j subsystem
 * using specified configuration.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.8 $ $Date: 2004/05/04 11:14:28 $
 */
public class Log4JConfLoggerManager
    extends Log4JLoggerManager
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
     * Work around a weird compilation problem. Can not call
     * the constructor from fortress/ContextManager, get a
     * file org\apache\log4j\spi\LoggerRepository.class not found
     *         new Log4JConfLoggerManager( lmDefaultLoggerName, lmLoggerName );
     */
    public static Log4JConfLoggerManager newInstance( final String prefix,
            final String switchToCategory )
    {
        return new Log4JConfLoggerManager( prefix, switchToCategory );
    }

    public Log4JConfLoggerManager( final String prefix, final String switchToCategory )
    {
        super( prefix, switchToCategory );
    }

    public Log4JConfLoggerManager()
    {
    }

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
        
        Log4JConfigurator configurator = new Log4JConfigurator(m_context);
        configurator.doConfigure( newElement, LogManager.getLoggerRepository());
    }
}
