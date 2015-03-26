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
package org.apache.excalibur.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.component.Component;

/**
 * A source factory creates new source objects.
 * <p>
 * Source factories are used to extend the source resolving mechanism
 * with new URI schemes. A new source factory is added in order to
 * handle a specific prototol. The {@link SourceResolver} delegates
 * the handling of a URI containing this new scheme to the factory,
 * and the factory can create a corresponding {@link Source} object.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SourceFactory.java,v 1.4 2004/02/28 11:47:26 cziegeler Exp $
 */
public interface SourceFactory
    extends Component
{
    String ROLE = SourceFactory.class.getName();

    /**
     * Get a {@link Source} object.
     * The factory creates a new {@link Source} object that can be used
     * by the application. However, when this source object is not needed
     * anymore it has to be released again using the {@link #release(Source)}
     * method. This is achieved by using {@link SourceResolver#release(Source)} which
     * finds the appropriate <code>SourceFactory</code>.
     * 
     * @param location   The URI to resolve - this URI includes the scheme.
     * @param parameters additionnal named parameters (optionnal and can be <code>null</code>)
     *        that drive the creation of the <code>Source</code> object. Each implementation
     *        must specify what parameters it accepts.
     * @return the created source object.
     *
     * @throws IOException if the source couldn't be created for some reason.
     */
    Source getSource( String location, Map parameters )
        throws IOException, MalformedURLException;
    
    /**
     * Release a {@link Source} object.
     * 
     * @param source the source to release.
     */
    void release( Source source );
}
