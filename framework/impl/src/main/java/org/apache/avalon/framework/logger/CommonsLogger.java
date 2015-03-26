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
package org.apache.avalon.framework.logger;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An Avalon {@link Logger} implementation backed by a {@plaintext Log logger}
 * of commons lLogging.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CommonsLogger.java 506231 2007-02-12 02:36:54Z crossley $
 * @since 4.3
 */
public class CommonsLogger implements Logger {

    private final Log log;
    private final String name;

    /**
     * Construct a CommonsLogger. The constructor needs explicitly the name of the
     * commons-logger, since the commons-logging API misses the functionality to
     * retrieve it from the logger instance.
     *
     * @param log The logger of commons-logging.
     * @param name The name of the logger.
     * @since 2.0
     */
    public CommonsLogger(final Log log, final String name)
    {
        this.log = log;
        this.name = name;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
     */
    public void debug(final String message)
    {
        this.log.debug(message);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(final String message, final Throwable throwable)
    {
        this.log.debug(message, throwable);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return this.log.isDebugEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
     */
    public void info(final String message)
    {
        this.log.info(message);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
     */
    public void info(final String message, final Throwable throwable)
    {
        this.log.info(message, throwable);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return this.log.isInfoEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
     */
    public void warn(final String message)
    {
        this.log.warn(message);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn(final String message, final Throwable throwable)
    {
        this.log.warn(message, throwable);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return this.log.isWarnEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
     */
    public void error(final String message)
    {
        this.log.error(message);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error(final String message, final Throwable throwable)
    {
        this.log.error(message, throwable);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return this.log.isErrorEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
     */
    public void fatalError(final String message)
    {
        this.log.fatal(message);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
     */
    public void fatalError(final String message, final Throwable throwable)
    {
        this.log.fatal(message, throwable);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled()
    {
        return this.log.isFatalEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
     */
    public Logger getChildLogger(final String name)
    {
        final String newName = this.name + '.' + name;
        return new CommonsLogger(LogFactory.getLog(newName), newName);
    }
}
