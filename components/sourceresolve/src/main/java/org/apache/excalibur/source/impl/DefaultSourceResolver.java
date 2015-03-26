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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * This is the default implemenation of a {@link SourceResolver}.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultSourceResolver.java 521242 2007-03-22 12:53:01Z cziegeler $
 */
public class DefaultSourceResolver
    extends AbstractSourceResolver
{

    /** Logger */
    protected final Log m_logger = LogFactory.getLog(getClass());

    /** The list of source factories. */
    protected Map m_sourceFactories = Collections.EMPTY_MAP;

    public void setSourceFactories(Map factories)
    {
        m_sourceFactories = factories;
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#getSourceFactory(java.lang.String)
     */
    protected SourceFactory getSourceFactory(String protocol)
    {
        return (SourceFactory)m_sourceFactories.get(protocol);
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#debug(java.lang.String)
     */
    protected void debug(String text)
    {
        m_logger.debug(text);
    }

    /**
     * @see org.apache.excalibur.source.impl.AbstractSourceResolver#isDebugEnabled()
     */
    protected boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }
}
