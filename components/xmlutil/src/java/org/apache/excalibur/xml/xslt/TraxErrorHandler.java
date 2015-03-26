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
package org.apache.excalibur.xml.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.avalon.framework.logger.Logger;

/**
 * This ErrorListener simply logs the exception and in
 * case of an fatal-error the exception is rethrown.
 * Warnings and errors are ignored.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: TraxErrorHandler.java,v 1.4 2004/02/28 11:47:16 cziegeler Exp $
 */
class TraxErrorHandler
    implements ErrorListener
{
    private Logger m_logger;

    TraxErrorHandler( final Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        m_logger = logger;
    }

    public void warning( final TransformerException te )
        throws TransformerException
    {
        final String message = getMessage( te );
        if( null != m_logger )
        {
            m_logger.warn( message, te );
        }
        else
        {
            System.out.println( "WARNING: " + message );
        }
    }

    public void error( final TransformerException te )
        throws TransformerException
    {
        final String message = getMessage( te );
        if( null != m_logger )
        {
            m_logger.error( message, te );
        }
        else
        {
            System.out.println( "ERROR: " + message );
        }
    }

    public void fatalError( final TransformerException te )
        throws TransformerException
    {
        final String message = getMessage( te );
        if( null != m_logger )
        {
            m_logger.fatalError( message, te );
        }
        else
        {
            System.out.println( "FATAL-ERROR: " + message );
        }
        throw te;
    }

    private String getMessage( final TransformerException te )
    {
        final SourceLocator locator = te.getLocator();
        if( null != locator )
        {
            // System.out.println("Parser fatal error: "+exception.getMessage());
            final String id =
                ( locator.getPublicId() != locator.getPublicId() )
                ? locator.getPublicId()
                : ( null != locator.getSystemId() )
                ? locator.getSystemId() : "SystemId Unknown";
            return new StringBuffer( "Error in TraxTransformer: " )
                .append( id ).append( "; Line " ).append( locator.getLineNumber() )
                .append( "; Column " ).append( locator.getColumnNumber() )
                .append( "; " ).toString();
        }
        return "Error in TraxTransformer: " + te;
    }
}
