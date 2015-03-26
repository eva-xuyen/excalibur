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
package org.apache.avalon.excalibur.pool;

/**
 * <code>Poolable</code> is a marker interface for Components that can
 * be pooled.  Components that are not pooled are created anew via a
 * factory every time a request is made for the component.
 * <p>
 * Components implementing this interface can add the following
 * attributes to its definition:
 * <pre><code>
 *   &lt;component pool-min="1" pool-max="10" pool-grow="1"&gt;
 *     &lt;tag&gt;value&lt;/tag&gt;
 *   &lt;/component&gt;
 * </pre></code>
 * Where:
 * <table border="0" cellpadding="4" cellspacing="0">
 *   <tr>
 *     <td valign="top"><code>pool-min</code></td>
 *     <td valign="top">sets the minimum number of Components maintained by the
 *     pool</td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><code>pool-max</code></td>
 *     <td valign="top">sets the maximum number of Components maintained by the
 *     pool</td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><code>pool-grow</code></td>
 *     <td valign="top">sets the number of Components to grow or
 *     shrink the pool by whenever it becomes necessary to do so</td>
 *   </tr>
 * </table>
 * </p><p>
 * NB: It was a deliberate choice not to extend Component. This will have to
 * be reassed once we see it in action.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.7 $ $Date: 2004/03/29 16:50:37 $
 * @since 4.0
 */
public interface Poolable
{
}
