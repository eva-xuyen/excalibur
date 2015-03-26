/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.fortress.migration;

import org.apache.avalon.fortress.Container;
import org.apache.avalon.fortress.impl.DefaultContainer;

/**
 * Fortress based component adapter. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/10 16:48:32 $
 * @avalon.component name="fortress" lifecycle="singleton"
 * @avalon.service type="org.apache.avalon.fortress.Container"
 */
public final class FortressAdapter extends DefaultContainer
{
}

