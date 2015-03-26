package org.apache.avalon.fortress.tools;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * Adapter to be used where a commons-logging Log is expected. 
 * Dispatch invocations to Avalon's Logger implementation.
 *
 * @author <a href="mailto:dev@excalibur.apache.org">The Excalibur Team</a>
 */
public class AvalonLogger implements Log
{
    private static Logger m_default;
    private Logger m_log;

    public static void setDefaultLogger(Logger logger)
    {
        m_default = logger;
    }

    public AvalonLogger(String category)
    {
        m_log = m_default.getChildLogger( category );
    }

    public boolean isDebugEnabled()
    {
        return m_log.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return m_log.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return m_log.isFatalErrorEnabled();
    }

    public boolean isInfoEnabled()
    {
        return m_log.isInfoEnabled();
    }

    public boolean isTraceEnabled()
    {
        return m_log.isDebugEnabled();
    }

    public boolean isWarnEnabled()
    {
        return m_log.isWarnEnabled();
    }

    public void trace( Object o )
    {
        m_log.debug( o.toString() );
    }

    public void trace( Object o, Throwable throwable )
    {
        m_log.debug( o.toString(), throwable );
    }

    public void debug( Object o )
    {
        m_log.debug( o.toString() );
    }

    public void debug( Object o, Throwable throwable )
    {
        m_log.debug( o.toString(), throwable);
    }

    public void info( Object o )
    {
        m_log.info( o.toString() );
    }

    public void info( Object o, Throwable throwable )
    {
        m_log.info( o.toString(), throwable );
    }

    public void warn( Object o )
    {
        m_log.warn( o.toString() );
    }

    public void warn( Object o, Throwable throwable )
    {
        m_log.warn( o.toString(), throwable );
    }

    public void error( Object o )
    {
        m_log.error( o.toString() );
    }

    public void error( Object o, Throwable throwable )
    {
        m_log.error( o.toString(), throwable );
    }

    public void fatal( Object o )
    {
        m_log.fatalError( o.toString() );
    }

    public void fatal( Object o, Throwable throwable )
    {
        m_log.fatalError( o.toString(), throwable );
    }
}
