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

package org.apache.avalon.excalibur.datasource.ids;

import java.math.BigDecimal;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 * @since 4.1
 */
public abstract class AbstractIdGenerator
    extends AbstractLogEnabled
    implements IdGenerator, ThreadSafe
{
    private static final BigDecimal BIG_DECIMAL_MAX_LONG = new BigDecimal( Long.MAX_VALUE );

    /**
     * Used to manage internal synchronization.
     */
    private Object m_semaphore = new Object();

    /**
     * Data type for the Id Pool.
     */
    private boolean m_useBigDecimals;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public AbstractIdGenerator()
    {
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Gets the next id as a Big Decimal.  This method will only be called
     *  when synchronized and when the data type is configured to be BigDecimal.
     *
     * @return the next id as a BigDecimal.
     *
     * @throws IdException if an Id could not be allocated for any reason.
     */
    protected abstract BigDecimal getNextBigDecimalIdInner()
        throws IdException;

    /**
     * Gets the next id as a long.  This method will only be called
     *  when synchronized and when the data type is configured to be long.
     *
     * @return the next id as a long.
     *
     * @throws IdException if an Id could not be allocated for any reason.
     */
    protected abstract long getNextLongIdInner()
        throws IdException;

    /**
     * By default, the IdGenerator will operate using a backend datatype of type long.  This
     *  is the most efficient, however it does not allow for Ids that are larger than
     *  Long.MAX_VALUE.  To allow very large Ids, it is necessary to make use of the BigDecimal
     *  data storage type.  This method should only be called durring initialization.
     *
     * @param useBigDecimals True to set BigDecimal as the internal data type.
     */
    protected final void setUseBigDecimals( boolean useBigDecimals )
    {
        m_useBigDecimals = useBigDecimals;
    }

    /**
     * Returns true if the internal data type is using BigDecimals, false if it is using longs.
     */
    protected final boolean isUsingBigDecimals()
    {
        return m_useBigDecimals;
    }

    /**
     * Gets the next Long Id constraining the value to be less than the specified maxId.
     *
     * @throws IdException if the next id is larger than the specified maxId.
     */
    protected final long getNextLongIdChecked( long maxId )
        throws IdException
    {
        long nextId;
        if( m_useBigDecimals )
        {
            // Use BigDecimal data type
            BigDecimal bd;
            synchronized( m_semaphore )
            {
                bd = getNextBigDecimalIdInner();
            }

            // Make sure that the Big Decimal value can be assigned to a long before continuing.
            if( bd.compareTo( BIG_DECIMAL_MAX_LONG ) > 0 )
            {
                String msg = "Unable to provide an id.  The next id would " +
                    "be greater than the id data type allows.";
                getLogger().error( msg );
                throw new IdException( msg );
            }
            nextId = bd.longValue();
        }
        else
        {
            // Use long data type
            synchronized( m_semaphore )
            {
                nextId = getNextLongIdInner();
            }
        }

        // Make sure that the id is valid for the requested data type.
        if( nextId > maxId )
        {
            String msg = "Unable to provide an id.  The next id would " +
                "be greater than the id data type allows.";
            getLogger().error( msg );
            throw new IdException( msg );
        }

        return nextId;
    }

    /*---------------------------------------------------------------
     * IdGenerator Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the next Id from the pool.
     *
     * @return the next Id.
     */
    public final BigDecimal getNextBigDecimalId()
        throws IdException
    {
        BigDecimal bd;
        if( m_useBigDecimals )
        {
            // Use BigDecimal data type
            synchronized( m_semaphore )
            {
                bd = getNextBigDecimalIdInner();
            }
        }
        else
        {
            // Use long data type
            synchronized( m_semaphore )
            {
                bd = new BigDecimal( getNextLongIdInner() );
            }
        }

        return bd;
    }

    /**
     * Returns the next Id from the pool.
     *
     * @return the next Id.
     *
     * @throws IdException if the next id is outside of the range of valid longs.
     */
    public final long getNextLongId()
        throws IdException
    {
        return getNextLongIdChecked( Long.MAX_VALUE );
    }

    /**
     * Returns the next Id from the pool.
     *
     * @return the next Id.
     *
     * @throws IdException if the next id is outside of the range of valid integers.
     */
    public final int getNextIntegerId()
        throws IdException
    {
        return (int)getNextLongIdChecked( Integer.MAX_VALUE );
    }

    /**
     * Returns the next Id from the pool.
     *
     * @return the next Id.
     *
     * @throws IdException if the next id is outside of the range of valid shorts.
     */
    public final short getNextShortId()
        throws IdException
    {
        return (short)getNextLongIdChecked( Short.MAX_VALUE );
    }

    /**
     * Returns the next Id from the pool.
     *
     * @return the next Id.
     *
     * @throws IdException if the next id is outside of the range of valid bytes.
     */
    public final byte getNextByteId()
        throws IdException
    {
        return (byte)getNextLongIdChecked( Byte.MAX_VALUE );
    }
}

