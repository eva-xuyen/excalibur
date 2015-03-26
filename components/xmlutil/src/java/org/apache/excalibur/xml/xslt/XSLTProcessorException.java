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

import org.apache.avalon.framework.CascadingException;

/**
 * This exception is thrown by the XSLTProcessor. It will wrap any exceptions thrown throughout the processing process.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class XSLTProcessorException
    extends CascadingException
{
    public XSLTProcessorException( final String message )
    {
        super( message );
    }

    public XSLTProcessorException( final String message,
                                   final Throwable throwable )
    {
        super( message, throwable );
    }
}
