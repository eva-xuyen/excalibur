/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.source.impl;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.factories.FileSource;

/**
 * A factory for filesystem-based sources (see {@link FileSource}).
 *
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=file-source
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FileSourceFactory.java 587633 2007-10-23 19:43:27Z cziegeler $
 */
public class FileSourceFactory extends org.apache.excalibur.source.factories.FileSourceFactory
    implements ThreadSafe
{

}
