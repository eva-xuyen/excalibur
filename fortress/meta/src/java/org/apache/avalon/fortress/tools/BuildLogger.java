/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

/**
 * A simple interface to allow implementations that will wrap the logger
 * differences between Ant and Maven plugins.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">The Excalibur Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2005/12/25 08:29:44 $
 */
public interface BuildLogger
{

    void debug( String message );

    void error( String message );

    void info( String message );

    void warn( String message );

}
