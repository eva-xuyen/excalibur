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
package org.apache.avalon.excalibur.component.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.excalibur.component.DefaultComponentPool;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.excalibur.testcase.ComponentStateValidator;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.StreamTarget;

/**
 * This class is for testing the ExcaliburComponentManager to verify that
 * it is correctly handling component lifestyle management.
 *
 * @deprecated ECM is no longer supported
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
public class ExcaliburComponentManagerTestCase extends TestCase
{
    /**
     * Here we create a suite lots of tests to test the interactions of
     * various types of components. Basically there are three Roles
     * involved: Mom, Dad, and Kid. Each of the three Roles can be
     * implemented by a SingleThreaded, ThreadSafe, or Poolable component.
     * The Mom and Dad components both are Composable, and they use the
     * ComponentLocator that they are provided with to obtain references
     * to a Kid component. The Mom and Dad components may be "Good" (they
     * properly release their Kid) or "Bad" (they don't release their Kid).
     *
     * Each of the tests sets up a different combo of these component
     * implementations and checks to make sure that everything gets disposed,
     * and that Kids never get disposed before parents are done using them.
     *
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();

        String[] behaviors = {"Bad", "Good"};
        String[] kidTypes = {""}; // , "BadCircular", "GoodCircular" };
        String[] lifestyles = {"SingleThreaded", "ThreadSafe", "Poolable"};

        for( int mb = 0; mb < behaviors.length; mb++ )
        {
            for( int db = 0; db < behaviors.length; db++ )
            {
                for( int kt = 0; kt < kidTypes.length; kt++ )
                {
                    for( int ml = 0; ml < lifestyles.length; ml++ )
                    {
                        for( int dl = 0; dl < lifestyles.length; dl++ )
                        {
                            for( int kl = 0; kl < lifestyles.length; kl++ )
                            {
                                final String momClassName =
                                    lifestyles[ ml ] + behaviors[ mb ] + "Mom";
                                final String dadClassName =
                                    lifestyles[ dl ] + behaviors[ db ] + "Dad";
                                final String kidClassName =
                                    lifestyles[ kl ] + kidTypes[ kt ] + "Kid";

                                final String prefix =
                                    ExcaliburComponentManagerTestCase.class.getName() + "$";

                                suite.addTest
                                    ( new ExcaliburComponentManagerTestCase( momClassName +
                                                                             dadClassName +
                                                                             kidClassName )
                                    {
                                        public void runTest() throws Exception
                                        {
                                            managerLifecycle( Class.forName
                                                              ( prefix + momClassName ),
                                                              Class.forName
                                                              ( prefix + dadClassName ),
                                                              Class.forName
                                                              ( prefix + kidClassName ) );
                                        }
                                    }
                                    );
                            }
                        }
                    }
                }
            }
        }

        return suite;
    }

    protected void managerLifecycle( Class momClass, Class dadClass, Class kidClass )
        throws Exception
    {
        Configuration emptyConfig = new DefaultConfiguration( "", "" );

        m_manager.addComponent( Mom.ROLE, momClass, emptyConfig );
        m_manager.addComponent( Dad.ROLE, dadClass, emptyConfig );
        m_manager.addComponent( Kid.ROLE, kidClass, emptyConfig );

        m_manager.initialize();

        Component mom = m_manager.lookup( Mom.ROLE );
        Component dad = m_manager.lookup( Dad.ROLE );

        m_manager.release( mom );
        m_manager.release( dad );

        m_manager.dispose();

        // Auto disposal for badly behaved components aren't guaranteed.
        // This done to fix: http://marc.theaimsgroup.com/?t=106779872700001&r=1&w=2
        if( momClass.getName().indexOf( "Good" ) > -1 &&
            dadClass.getName().indexOf( "Good" ) > -1 ) 
        {
            checkNumberOfDisposals( momClass, dadClass, kidClass );
        }
    }

    private void checkNumberOfDisposals( Class momClass, Class dadClass, Class kidClass )
    {
        int momInstances = 1, dadInstances = 1;

        int kidInstances = determineNumberOfKidInstances( kidClass, momInstances, dadInstances );

        int expectedDisposals = momInstances + dadInstances + kidInstances;

        assertEquals( expectedDisposals, m_disposals );
    }

    private int determineNumberOfKidInstances( Class kidClass, int momInstances, int dadInstances )
    {
        int parentInstances = ( momInstances + dadInstances );

        if( ThreadSafe.class.isAssignableFrom( kidClass ) )
        {
            // parents share reference to same kid instance
            return 1;
        }
        else if( Poolable.class.isAssignableFrom( kidClass ) )
        {
            int poolGrowParameter = DefaultComponentPool.DEFAULT_POOL_SIZE / 4;

            int extraKidsNeeded = parentInstances % poolGrowParameter;

            if( extraKidsNeeded > 0 )
            {
                // kid pool will grow to feed parents
                return parentInstances + ( poolGrowParameter - extraKidsNeeded );
            }
        }

        // each parent has a single kid reference
        return parentInstances;
    }

    /* ======================================================================== *
     *                           Test Components.                               *
     * ======================================================================== */

    public static abstract class AbstractBadParent extends AbstractLogEnabled
        implements Component, Composable, Disposable
    {
        private final ComponentStateValidator m_validator = new ComponentStateValidator( this );

        protected ComponentManager m_innerManager;
        protected Kid m_kid;

        public void enableLogging( Logger logger )
        {
            m_validator.checkLogEnabled();

            super.enableLogging( logger );
        }

        public void compose( ComponentManager manager ) throws ComponentException
        {
            m_validator.checkComposed();

            m_innerManager = manager;

            m_kid = (Kid)m_innerManager.lookup( Kid.ROLE );
        }

        public void dispose()
        {
            m_validator.checkDisposed();

            try
            {
                m_kid.getName();
            }
            catch( IllegalStateException ise )
            {
                fail( ise.getMessage() );
            }

            m_disposals++;
        }
    }

    public static abstract class AbstractGoodParent extends AbstractBadParent
    {
        public void dispose()
        {
            super.dispose();
            m_innerManager.release( m_kid );
        }
    }

    public interface Mom extends Component
    {
        String ROLE = "Mom";
    }

    public static class SingleThreadedBadMom extends AbstractBadParent
        implements Mom, SingleThreaded
    {
    }
    public static class SingleThreadedGoodMom extends AbstractGoodParent
        implements Mom, SingleThreaded
    {
    }
    public static class ThreadSafeBadMom extends AbstractBadParent
        implements Mom, ThreadSafe
    {
    }
    public static class ThreadSafeGoodMom extends AbstractGoodParent
        implements Mom, ThreadSafe
    {
    }
    public static class PoolableBadMom extends AbstractBadParent
        implements Mom, Poolable
    {
    }
    public static class PoolableGoodMom extends AbstractGoodParent
        implements Mom, Poolable
    {
    }

    public interface Dad extends Component
    {
        String ROLE = "Dad";
    }

    public static class SingleThreadedBadDad extends AbstractBadParent
        implements Dad, SingleThreaded
    {
    }
    public static class SingleThreadedGoodDad extends AbstractGoodParent
        implements Dad, SingleThreaded
    {
    }
    public static class ThreadSafeBadDad extends AbstractBadParent
        implements Dad, ThreadSafe
    {
    }
    public static class ThreadSafeGoodDad extends AbstractGoodParent
        implements Dad, ThreadSafe
    {
    }
    public static class PoolableBadDad extends AbstractBadParent
        implements Dad, Poolable
    {
    }
    public static class PoolableGoodDad extends AbstractGoodParent
        implements Dad, Poolable
    {
    }

    public interface Kid extends Component
    {
        String ROLE = "Kid";

        String getName();
    }

    public static abstract class AbstractKid extends AbstractLogEnabled
        implements Kid, Disposable
    {
        public static final String ROLE = "Kid";

        protected final ComponentStateValidator m_validator = new ComponentStateValidator( this );

        public void enableLogging( Logger logger )
        {
            m_validator.checkLogEnabled();

            super.enableLogging( logger );
        }

        public void dispose()
        {
            m_validator.checkDisposed();

            m_disposals++;
        }

        public String getName()
        {
            m_validator.checkActive();

            return "Kid";
        }
    }

    public static class SingleThreadedKid extends AbstractKid
        implements SingleThreaded
    {
    }
    public static class ThreadSafeKid extends AbstractKid
        implements ThreadSafe
    {
    }
    public static class PoolableKid extends AbstractKid
        implements Poolable
    {
    }

    public static abstract class AbstractBadCircularKid extends AbstractKid
        implements Composable
    {
        protected ComponentManager m_innerManager;
        protected Mom m_mom;
        protected Dad m_dad;

        public void compose( ComponentManager manager ) throws ComponentException
        {
            m_validator.checkComposed();

            m_innerManager = manager;
        }

        public String getName()
        {
            String name = super.getName();

            try
            {
                m_mom = (Mom)m_innerManager.lookup( Mom.ROLE );
                m_dad = (Dad)m_innerManager.lookup( Dad.ROLE );
            }
            catch( ComponentException ce )
            {
                fail( ce.getMessage() );
            }

            return ( name + " belongs to " + m_mom + " and " + m_dad );
        }
    }

    public static abstract class AbstractGoodCircularKid extends AbstractBadCircularKid
    {
        public void dispose()
        {
            super.dispose();

            m_innerManager.release( m_mom );
            m_innerManager.release( m_dad );
        }
    }

    public static class SingleThreadedBadCircularKid extends AbstractBadCircularKid
        implements SingleThreaded
    {
    }
    public static class ThreadSafeBadCircularKid extends AbstractBadCircularKid
        implements ThreadSafe
    {
    }
    public static class PoolableBadCircularKid extends AbstractBadCircularKid
        implements Poolable
    {
    }
    public static class SingleThreadedGoodCircularKid extends AbstractGoodCircularKid
        implements SingleThreaded
    {
    }
    public static class ThreadSafeGoodCircularKid extends AbstractGoodCircularKid
        implements ThreadSafe
    {
    }
    public static class PoolableGoodCircularKid extends AbstractGoodCircularKid
        implements Poolable
    {
    }

    /* ======================================================================== *
     *                           Housekeeping.                                  *
     * ======================================================================== */

    private static int m_disposals;

    private ExcaliburComponentManager m_manager;

    private Logger m_logger;

    public ExcaliburComponentManagerTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        m_disposals = 0;

        m_manager = new ExcaliburComponentManager();

        final String pattern =
            ( "%5.5{priority} [%40.40{category}]: %{message}\n%{throwable}" );

        org.apache.log.Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor( getName() );
        logger.setLogTargets
            ( new LogTarget[]
            {new StreamTarget( System.out, new PatternFormatter( pattern ) )} );
        logger.setPriority( Priority.INFO );

        m_manager.enableLogging( new LogKitLogger( logger ) );
        m_manager.contextualize( new DefaultContext() );
        m_manager.configure( new DefaultConfiguration( "", "" ) );

        m_logger = new LogKitLogger( logger );
    }

    public void tearDown()
    {
        m_manager = null;
    }

}
