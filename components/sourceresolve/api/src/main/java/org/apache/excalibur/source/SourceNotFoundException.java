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
package org.apache.excalibur.source;

/**
 * This Exception should be thrown if the source could not be found.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SourceNotFoundException.java 587585 2007-10-23 18:19:40Z cziegeler $
 */
public class SourceNotFoundException
    extends SourceException
{
    /**
     * Construct a new <code>SourceNotFoundException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public SourceNotFoundException( final String message )
    {
        super( message, null );
    }

    /**
     * Construct a new <code>SourceNotFoundException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public SourceNotFoundException( final String message, final Throwable throwable )
    {
        super( message, throwable );
    }
}
