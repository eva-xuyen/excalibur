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
package org.apache.avalon.excalibur.monitor;

/**
 * This interface is used by the Monitor section so that we can test if a
 * resource is modified by an external source.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Modifiable.java,v 1.4 2004/02/28 11:47:32 cziegeler Exp $
 */
public interface Modifiable
{
    /**
     * Tests if a resource has been modified, and causes the resource to act on
     * that test.  The contract is that the method does its work <b>only</b>
     * when the time passed in is after the last time the resource was modified.
     */
    void testModifiedAfter( long time );

    /**
     * Simply provides the last time the resource has been modified.
     */
    long lastModified();
}
