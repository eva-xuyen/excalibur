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
package org.apache.avalon.excalibur.pool;

/**
 * A ResourceLimitingPool which validates reused poolables before they are
 *  returned with a call get().
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/31 08:07:28 $
 * @since 4.1
 */
public class ValidatedResourceLimitingPool
    extends TraceableResourceLimitingPool
{
    /*---------------------------------------------------------------
     * Private Fields
     *-------------------------------------------------------------*/
    /**
     * Used for communication between the get() and newPoolable() methods, only valid
     *  within a single synchronized block.
     */
    private boolean m_needsValidation;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new ValidatedResourceLimitingPool
     *
     * @param factory The ObjectFactory which will be used to create new Poolables as needed by
     *  the pool.
     * @param max Maximum number of Poolables which can be stored in the pool, 0 implies no limit.
     * @param maxStrict true if the pool should never allow more than max Poolable to be created.
     *  Will cause an exception to be thrown if more than max Poolables are requested and blocking
     *  is false.
     * @param blocking true if the pool should cause a thread calling get() to block when Poolables
     *  are not currently available on the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused poolables will be removed
     *  from the pool.  A value of 0 will cause the pool to never trim poolables.
     * @param trace True if tracing of gets is enabled for the pool.
     */
    public ValidatedResourceLimitingPool( final ObjectFactory factory,
                                          int max,
                                          boolean maxStrict,
                                          boolean blocking,
                                          long blockTimeout,
                                          long trimInterval,
                                          boolean trace )
    {
        super( factory, max, maxStrict, blocking, blockTimeout, trimInterval, trace );
    }
    
    /**
     * Creates a new ValidatedResourceLimitingPool
     *
     * @param factory The ObjectFactory which will be used to create new Poolables as needed by
     *  the pool.
     * @param max Maximum number of Poolables which can be stored in the pool, 0 implies no limit.
     * @param maxStrict true if the pool should never allow more than max Poolable to be created.
     *  Will cause an exception to be thrown if more than max Poolables are requested and blocking
     *  is false.
     * @param blocking true if the pool should cause a thread calling get() to block when Poolables
     *  are not currently available on the pool.
     * @param blockTimeout The maximum amount of time, in milliseconds, that a call to get() will
     *  block before an exception is thrown.  A value of 0 implies an indefinate wait.
     * @param trimInterval The minimum interval with which old unused poolables will be removed
     *  from the pool.  A value of 0 will cause the pool to never trim poolables.
     */
    public ValidatedResourceLimitingPool( final ObjectFactory factory,
                                          int max,
                                          boolean maxStrict,
                                          boolean blocking,
                                          long blockTimeout,
                                          long trimInterval )
    {
        this( factory, max, maxStrict, blocking, blockTimeout, trimInterval, false );
    }

    /*---------------------------------------------------------------
     * Pool Methods
     *-------------------------------------------------------------*/
    /**
     * Gets a Poolable from the pool.  If there is room in the pool, a new Poolable will be
     *  created.  Depending on the parameters to the constructor, the method may block or throw
     *  an exception if a Poolable is not available on the pool.
     *
     * @return Always returns a Poolable.  Contract requires that put must always be called with
     *  the Poolable returned.
     * @throws Exception An exception may be thrown as described above or if there is an exception
     *  thrown by the ObjectFactory's newInstance() method.
     */
    public Poolable get() throws Exception
    {
        Poolable poolable;
        boolean needsValidation;

        // If an obtained Poolable is invalid, then we will want to obtain another one requiring
        //  that we loop.
        do
        {
            synchronized( m_semaphore )
            {
                // Set the needs validation flag to false.  The super.get() method will call the
                //  newPoolable() method causing the flag to be set to false if called.
                m_needsValidation = true;

                poolable = super.get();

                // Store the validation flag in a local variable so that we can continue to use it
                //  after we release the semaphore.
                needsValidation = m_needsValidation;
            }

            // If necessay, validate the poolable now that this thread owns it.
            if( needsValidation )
            {
                // Call the validation method for the obtained poolable.
                if( !validatePoolable( poolable ) )
                {
                    // The poolable is no longer valid.  We need to resynchronize to remove the bad
                    //  poolable and prepare to get another one.
                    synchronized( m_semaphore )
                    {
                        if( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Removing a " + poolable.getClass().getName()
                                               + " from the pool because it failed validation." );
                        }

                        permanentlyRemovePoolable( poolable );
                        poolable = null;
                    }
                }
            }
        } while( poolable == null );

        return poolable;
    }

    /*---------------------------------------------------------------
     * ResourceLimitingPool Methods
     *-------------------------------------------------------------*/
    /**
     * Create a new poolable instance by by calling the newInstance method
     *  on the pool's ObjectFactory.
     * This is the method to override when you need to enforce creational
     *  policies.
     * This method is only called by threads that have m_semaphore locked.
     */
    protected Poolable newPoolable() throws Exception
    {
        // Set the validation flag to false.  See the exclamation in the get() method.
        m_needsValidation = false;

        return super.newPoolable();
    }

    /*---------------------------------------------------------------
     * Public Methods
     *-------------------------------------------------------------*/
    /**
     * If the poolable implements Validatable, then its validate() method will be called to give
     *  the poolable a chance to validate itself.
     * Different functionality can be achieved by overriding this method.
     * This method is only called by threads that have m_semaphore locked.
     *
     * @param poolable The Poolable to be validated
     * @return true if the Poolable is valid, false if it should be removed from the pool.
     */
    protected boolean validatePoolable( Poolable poolable ) throws Exception
    {
        if( poolable instanceof Validatable )
        {
            return ( (Validatable)poolable ).validate();
        }
        else
        {
            return true;
        }
    }
}

