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

package org.apache.avalon.fortress;

/**
 * The ContainerManager is a single point of contact to manage your Container
 * resources.  It takes care of creating the other managers that a Container
 * needs to use, as well as initializing the Container.  It is designed to be
 * directly instantiated by whatever class needs to initialize your system.
 *
 * <p>
 *   The ContainerManager provides some constants used in the initial
 *   <code>Parameters</code> passed into the ContainerManager.  The
 *   ContainerManager uses these values to create all the pieces necessary
 *   for the Container. Below is a table that describes what those options
 *   are.
 * </p>
 *
 * <p>You can think of a ContainerManager is a pocket universe for a impl and its
 * components.</p>
 *
 * <p><b>Case 1: Use by a servlet or other "root" entity</b></p>
 *
 * <pre>
 * <code>
 *    FortressConfig config = new FortressConfig();
 *    config.setContainerClass( Thread.currentThread().getContextClassLoader().loadClass( "org.apache.avalon.fortress.test.TestContainer" ) );
 *    config.setContextDirectory( "./" );
 *    config.setWorkDirectory( "./" );
 *    config.setContainerConfiguration( "resource://org.apache.avalon.fortress/test/ContainerProfile.xconf" );
 *    config.setLoggerManagerConfiguration( "resource://org.apache.avalon.fortress/test/ContainerProfile.xlog" );
 *
 *    ContextManager contextManager = config.getContext();
 *    ContainerManager containerManager = new DefaultContainerManager( contextManager );
 *    ContainerUtil.initialize( containerManager );
 * </code>
 * </pre>
 *
 * Then, for example, wait for a request and pass it on to the impl:
 *
 * <pre>
 * <code>
 *    TestContainer impl = (TestContainer) containerManager.getContainer();
 *    impl.handleRequest( ... );
 * </code>
 * </pre>
 *
 * When done, dispose of the managers.
 *
 * <pre>
 * <code>
 *    ContainerUtil.dispose( containerManager );
 *    ContainerUtil.dispose( contextManager );
 * </code>
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">The Avalon Team</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/02/28 15:16:24 $
 * @see ContainerManagerConstants for the contract surrounding the ContainerManager context
 */
public interface ContainerManager
{
    /**
     * Get a reference to the managed Container.  This instance is typically cast to
     * the interface used to interact with the impl.
     *
     * @return the container implementation
     */
    Object getContainer();
}
