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
package org.apache.avalon.excalibur.logger.util;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;

/**
 * A proxy logger that switches between two underlying loggers
 * with recursive invocation detection.
 * <p>
 * This class is intended to be used by o.a.a.e.logger.AbstractLoggerManager.
 * all the logger switching is done in it during the "warm-up" phase
 * (constructor, enableLogging, contextualize, configure, start).
 * All these operations are held our on a single thread and the
 * object is not exposed to other threads untill (in strict synchronization
 * sense) it has been fully configured. That's why there is no synchronization
 * in this class. If the switching was to occur in a mulitythreaded
 * fasion we would have to synchronize access to m_fallback and m_preferred.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */

public class LoggerSwitch
{
    protected final static Logger SHARED_NULL = new NullLogger();

    private final static class BooleanThreadLocal extends ThreadLocal
    {
        public Object initialValue() { return Boolean.FALSE; }
        public boolean value() { return ((Boolean)this.get()).booleanValue(); }
    }

    private static class SwitchingLogger implements Logger
    {
        Logger m_fallback;
        Logger m_preferred;

        BooleanThreadLocal m_recursionOnPreferred = new BooleanThreadLocal();
        BooleanThreadLocal m_recursionOnFallback = new BooleanThreadLocal();

        SwitchingLogger( final Logger fallback, final Logger preferred )
        {
            m_fallback = fallback != null ? fallback : SHARED_NULL;
            m_preferred = preferred;
        }

        void setFallback( final Logger fallback )
        {
            m_fallback = fallback != null ? fallback : SHARED_NULL;
        }

        void setPreferred( final Logger preferred )
        {
            m_preferred = preferred;
        }

        /**
         * Retrieve m_preferred or if that is null m_fallback.
         * Safeguard against recursion. That is possible if
         * try to log something via a Logger that is failing
         * and trying to log its own error via itself.
         */
        private Logger getLogger()
        {
            if ( m_recursionOnFallback.value() )
            {
                throw new IllegalStateException( "infinite recursion" );
            }
            else if ( m_recursionOnPreferred.value() || m_preferred == null )
            {
                m_recursionOnFallback.set( Boolean.TRUE );
                return m_fallback;
            }
            else
            {
                m_recursionOnPreferred.set( Boolean.TRUE );
                return m_preferred;
            }
        }

        private Logger getLoggerLight()
        {
            return m_preferred != null ? m_preferred : m_fallback;
        }

        private void releaseLogger()
        {
            if ( m_recursionOnFallback.value() )
            {
                m_recursionOnFallback.set( Boolean.FALSE );
            }
            else if ( m_recursionOnPreferred.value() )
            {
                m_recursionOnPreferred.set( Boolean.FALSE );
            }
            else
            {
                throw new IllegalStateException( "no recursion" );
            }
        }

        public void debug( final String message )
        {
            final Logger logger = getLogger();
            try
            {
                logger.debug( message );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public void debug( final String message, final Throwable throwable )
        {
            final Logger logger = getLogger();
            try
            {
                logger.debug( message, throwable );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        /** 
         * This and similar method may probably be optimized in the
         * future by caching the boolean in our instance variables.
         * Each time setPreferred() or setFallback() is called they
         * will be cached. Maybe in the future. :-)
         */
        public boolean isDebugEnabled()
        {
            final Logger logger = getLoggerLight();
            return logger.isDebugEnabled();
        }

    
        public void info( final String message )
        {
            final Logger logger = getLogger();
            try
            {
                logger.info( message );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public void info( final String message, final Throwable throwable )
        {
            final Logger logger = getLogger();
            try
            {
                logger.info( message, throwable );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public boolean isInfoEnabled()
        {
            final Logger logger = getLoggerLight();
            return logger.isInfoEnabled();
        }
    
        public void warn( final String message )
        {
            final Logger logger = getLogger();
            try
            {
                logger.warn( message );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public void warn( final String message, final Throwable throwable )
        {
            final Logger logger = getLogger();
            try
            {
                logger.warn( message, throwable );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public boolean isWarnEnabled()
        {
            final Logger logger = getLoggerLight();
            return logger.isWarnEnabled();
        }
    
        public void error( final String message )
        {
            final Logger logger = getLogger();
            try
            {
                logger.error( message );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public void error( final String message, final Throwable throwable )
        {
            final Logger logger = getLogger();
            try
            {
                logger.error( message, throwable );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public boolean isErrorEnabled()
        {
            final Logger logger = getLoggerLight();
            return logger.isErrorEnabled();
        }
    
        public void fatalError( final String message )
        {
            final Logger logger = getLogger();
            try
            {
                logger.fatalError( message );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public void fatalError( final String message, final Throwable throwable )
        {
            final Logger logger = getLogger();
            try
            {
                logger.fatalError( message, throwable );
            }
            finally
            {
                releaseLogger();
            }
        }
    
        public boolean isFatalErrorEnabled()
        {
            final Logger logger = getLoggerLight();
            return logger.isFatalErrorEnabled();
        }
    
        public Logger getChildLogger( final String name ) { return this; }

    }

    private SwitchingLogger m_switch;

    /**
     * We create a logger with no methods for changing
     * m_fallback and m_preferred for security reasons.
     * All the control is done by the parent class
     * that does not implement Logger itself.
     */
    public Logger get()
    {
        return m_switch;
    }

    public LoggerSwitch( final Logger fallback )
    {
        this( fallback, null );
    }

    public LoggerSwitch( final Logger fallback, final Logger preferred )
    {
        m_switch = new SwitchingLogger( fallback, preferred );
    }

    public void setFallback( final Logger fallback )
    {
        m_switch.setFallback( fallback );
    }

    public void setPreferred( final Logger preferred )
    {
        m_switch.setPreferred( preferred );
    }
}
